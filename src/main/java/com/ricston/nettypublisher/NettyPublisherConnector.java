/**
 * This file was automatically generated by the Mule Development Kit
 */

package com.ricston.nettypublisher;

import org.mule.api.ConnectionException;
import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.Connect;
import org.mule.api.annotations.ConnectionIdentifier;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Disconnect;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.Source;
import org.mule.api.annotations.ValidateConnection;
import org.mule.api.annotations.lifecycle.Start;
import org.mule.api.annotations.lifecycle.Stop;
import org.mule.api.annotations.param.ConnectionKey;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.Optional;
import org.mule.api.callback.SourceCallback;

import com.ricston.nettypublisher.exception.UnsupportedDataTypeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
@Connector(name = "nettypublisher", schemaVersion = "1.0-SNAPSHOT")
public class NettyPublisherConnector
{

    protected Log logger = LogFactory.getLog(getClass());

    /**
     * Map of publisher names and their port numbers. Configurable from the XML.
     */
    @Configurable
    private Map<String, Integer> publishers;

    /**
     * A list of handlers for each publisher. Publishers are keyed by name.
     */
    protected Map<String, List<NettyPublisherHandler>> publisherHandlers = new HashMap<String, List<NettyPublisherHandler>>();
    
    /**
     * A list of source handles.
     */
    protected List<NettySourceHandler> sourceHandlers = new ArrayList<NettySourceHandler>();
    
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
     * @param nettyPublisherName Name of the connector
     * @throws ConnectionException
     */
    @Connect
    public void connect(@ConnectionKey String nettyPublisherName) throws ConnectionException
    {
    }

    /**
     * 
     */
    @Disconnect
    public void disconnect()
    {
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
     */
    @Stop
    public void destroy()
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

                // create a new list of handlers for each publisher
                List<NettyPublisherHandler> publisherHandlerList = new ArrayList<NettyPublisherHandler>();
                publisherHandlers.put(publisherName, publisherHandlerList);

                // start the server as publisher
                NettyUtils.startServer(port, ServerType.PUBLISHER, null, null, publisherHandlerList);

                logger.info("Netty server started publisher on port " + port);
            }
        }
    }
    
    /**
     * Close all servers (inbound endpoints) and pub/sub servers
     */
    protected void stopAllServers()
    {
        //close all servers (inbound endpoints) and associated connections
        for(NettySourceHandler sourceHandler : sourceHandlers)
        {
            sourceHandler.close();
            logger.info("Netty server closing listener");
        }
        
        //close all publishers and the connections associated with each publisher
        for (Map.Entry<String, List<NettyPublisherHandler>> publisherHandler : publisherHandlers.entrySet())
        {
            for (NettyPublisherHandler publisher : publisherHandler.getValue())
            {
                publisher.close();
            }
            logger.info("Netty server closing publisher");
        }
    }

    /**
     * Are we connected
     */
    @ValidateConnection
    public boolean isConnected()
    {
        return true;
    }

    /**
     * Are we connected
     */
    @ConnectionIdentifier
    public String connectionId()
    {
        return "001";
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
    public void publish(String publisher, @Optional @Default(value="#[payload]") String data) throws UnsupportedDataTypeException
    {
        List<NettyPublisherHandler> publishers = publisherHandlers.get(publisher);
        
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
        NettyUtils.startServer(port, ServerType.SOURCE, callback, sourceHandlers, null);
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
    public void write(String host, Integer port, @Optional @Default(value="#[message.payload]") String data) throws Exception
    {
        NettyClientHandler clientHandler = new NettyClientHandler();
        NettyChannelInfo channelInfo = NettyUtils.startClient(host, port, clientHandler);
        clientHandler.writeToServer(data);
        clientHandler.close();
        channelInfo.getWorkerGroup().shutdownGracefully();
    }
}
