package com.ricston.nettypublisher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.Source;
import org.mule.api.annotations.lifecycle.Start;
import org.mule.api.annotations.lifecycle.Stop;
import org.mule.api.annotations.param.Default;
import org.mule.api.callback.SourceCallback;

import com.ricston.nettypublisher.exception.UnsupportedDataTypeException;
import com.ricston.nettypublisher.handlers.NettyPublisherHandler;
import com.ricston.nettypublisher.handlers.NettySourceHandler;

/**
 * Netty connector. 
 * 
 * This connector is designed to host highly concurrent asynchronous TCP servers
 * using the Netty library. 
 * 
 * A new feature to perform pub/sub connections was also added.
 * 
 * @author Ricston Ltd.
 */
@Connector(name = "nettypublisher", schemaVersion = "1.0-SNAPSHOT", friendlyName="Netty Publisher")
public class NettyPublisherConnector
{

    protected Log logger = LogFactory.getLog(getClass());

    /**
     * Map of publisher names and their port numbers. Configurable from the XML.
     */
    @Configurable
    private Map<String, Integer> publishers;

    /**
     * A NettyChannelInfo with a list of handlers for each publisher. Publishers are keyed by name.
     */
    protected Map<String, NettyChannelInfo<NettyPublisherHandler>> publisherHandlers = new HashMap<String, NettyChannelInfo<NettyPublisherHandler>>();
    
    /**
     * NettyChannelInfo that contains a list of source handles.
     */
    protected NettyChannelInfo<NettySourceHandler> sourceHandlers = new NettyChannelInfo<NettySourceHandler>();
    
    protected boolean initialised = false;

    /**
     * 
     * @return The map of publishers and their port numbers
     */
    public Map<String, Integer> getPublishers()
    {
        return publishers;
    }

    /**
     * 
     * @param publishers The map of publishers and their port numbers
     */
    public void setPublishers(Map<String, Integer> publishers)
    {
        this.publishers = publishers;
    }

    /**
     * Start all pub/sub servers.
     * 
     * @throws InterruptedException 
     */
    @Start
    public synchronized void init() throws InterruptedException
    {
        logger.info("Initialising Netty Publisher");
        if (!initialised)
        {
            startPublisherServers();
            initialised = true;
            logger.info("Netty Publisher initialised");
        }
        else
        {
            logger.info("Netty Publisher was already initialised");
        }
    }
    
    /**
     * Close all servers (inbound endpoints) and pub/sub servers
     * 
     * @throws InterruptedException Exception while stopping servers
     */
    @Stop
    public void destroy() throws InterruptedException
    {
        logger.info("Stopping Netty Publisher");
        stopAllServers();
    }
    
    /**
     * Start all pub/sub servers.
     * 
     * @throws InterruptedException
     */
    protected void startPublisherServers() throws InterruptedException
    {
        if (publishers != null)
        {
            for (Map.Entry<String, Integer> publisher : publishers.entrySet())
            {
                // get the publisher name and port which would have been configured in the XML
                String publisherName = publisher.getKey();
                Integer port = publisher.getValue();
                
                // start the server as publisher
                NettyChannelInfo<NettyPublisherHandler> channelInfo = NettyUtils.startServer(port, ServerType.PUBLISHER, null, new ArrayList<NettyPublisherHandler>());
                publisherHandlers.put(publisherName, channelInfo);

                logger.info("Netty server started publisher on port " + port);
            }
        }
    }
    
    /**
     * Close all servers (inbound endpoints) and pub/sub servers
     * @throws InterruptedException Exception while stopping
     */
    protected void stopAllServers() throws InterruptedException
    {
        //close all servers (inbound endpoints) and associated connections
        sourceHandlers.closeAll();
        logger.info("Netty server closing listener");
        
        //close all publishers and the connections associated with each publisher
        for (Map.Entry<String, NettyChannelInfo<NettyPublisherHandler>> channelInfo : publisherHandlers.entrySet())
        {
            channelInfo.getValue().closeAll();
            logger.info("Netty server closing publisher");
        }
    }

    /**
     * Publishes the data on all clients connected on the publisher.
     *  
     * {@sample.xml ../../../doc/NettyPublisher-connector.xml.sample nettypublisher:publish}
     * 
     * @param data Content to be published
     * @param publisher The publisher to publish the data on
     * @throws UnsupportedDataTypeException thrown when data type to be written is not supported
     */
    @Processor
    public void publish(String publisher, @Default(value="#[payload]") String data) throws UnsupportedDataTypeException
    {
        List<NettyPublisherHandler> publishers = publisherHandlers.get(publisher).getChannelInboundHandlers();
        
        for(NettyPublisherHandler publisherHandler : publishers)
        {
            publisherHandler.publish(data);
        }
    }

    /**
     * Hosts a netty server and starts listening on the configured port.
     * 
     * {@sample.xml../../../doc/NettyPublisher-connector.xml.sample nettypublisher:server}
     * 
     * @param callback the flow's message processors' callback
     * @param port The port number
     * @throws InterruptedException If interrupted, an exception is thrown
     */
    @Source
    public void server(Integer port, SourceCallback callback) throws InterruptedException
    {
        NettyUtils.startServer(port, ServerType.SOURCE, callback, new ArrayList<NettySourceHandler>());
        logger.info("Netty server started listening on port " + port);
    }
    
    /**
     * To be used as a TCP client. Writes data to a TCP server
     * 
     * {@sample.xml../../../doc/NettyPublisher-connector.xml.sample nettypublisher:write}
     * @param host The host of the server
     * @param port The port of the server
     * @param data The data to  be written
     * @throws Exception Anything that goes wrong
     */
    @Processor
    public void write(String host, Integer port, @Default(value="#[message.payload]") String data) throws Exception
    {
        NettyClientHandler clientHandler = new NettyClientHandler();
        NettyChannelInfo channelInfo = NettyUtils.startClient(host, port, clientHandler);
        clientHandler.writeToServer(data);
        clientHandler.close();
        channelInfo.getWorkerGroup().shutdownGracefully();
    }
}
