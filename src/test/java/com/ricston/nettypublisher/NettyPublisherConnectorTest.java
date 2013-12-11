package com.ricston.nettypublisher;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.client.MuleClient;
import org.mule.construct.Flow;
import org.mule.tck.functional.FunctionalTestComponent;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.transport.NullPayload;

import org.junit.Assert;
import org.junit.Test;

public class NettyPublisherConnectorTest extends FunctionalTestCase
{
    protected static final String PAYLOAD = "some data\r\n";
    protected static final String CONNECTOR_NAME = "tcpConnector";
    
    protected static final int PUBLISHER1_PORT = 8091;
    protected static final int PUBLISHER2_PORT = 8092;
    
    public NettyPublisherConnectorTest()
    {
        super();
        this.setDisposeContextPerClass(true);
    }
    
    @Override
    protected String getConfigResources()
    {
        return "mule-config.xml, test-mule-config.xml";
    }

    protected class RunTcpTest extends Thread
    {
        private int port;
        private String connectorName;
        private String result;
        
        public RunTcpTest(int port, String connectorName)
        {
            this.port = port;
            this.connectorName = connectorName;
        }
        
        public void run()
        {
            try
            {
                MuleClient client = muleContext.getClient();
                MuleMessage response = client.send(String.format("tcp://localhost:%d?connector=%s", port, connectorName), new DefaultMuleMessage("", muleContext), 10000);
                result = response.getPayloadAsString();
                logger.info("*******************" + result);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        public String getResult()
        {
            return result;
        }
    }

    @Test
    public void testFlowPublisher1() throws Exception
    {
        //Allow servers to start
        Thread.sleep(1000);
        
        RunTcpTest t1 = new RunTcpTest(PUBLISHER1_PORT, CONNECTOR_NAME);
        t1.start();
        
        runFlowWithPayloadAndExpect("testFlowPublisher1", NullPayload.getInstance(), PAYLOAD);
        
        t1.join();
        Assert.assertEquals(PAYLOAD, t1.getResult());
    }
    
    @Test
    public void testFlowPublisher2() throws Exception
    {
        //Allow servers to start
        Thread.sleep(1000);
        
        RunTcpTest t1 = new RunTcpTest(PUBLISHER1_PORT, CONNECTOR_NAME);
        t1.start();
        
        RunTcpTest t2 = new RunTcpTest(PUBLISHER2_PORT, CONNECTOR_NAME);
        t2.start();
        
        runFlowWithPayloadAndExpect("testFlowPublisher2", NullPayload.getInstance(), PAYLOAD);
        
        t1.join();
        Assert.assertEquals(PAYLOAD, t1.getResult());
        t2.join();
        Assert.assertEquals(PAYLOAD, t2.getResult());
    }
    
    @Test
    public void testFlowWriter() throws Exception
    {
        int repeat = 10;
        
        for (int i=0; i<repeat; i++)
        {
            runFlowWithPayloadAndExpect("testFlowClient", PAYLOAD, PAYLOAD);
        }
        
        //Allow client to call server (async)
        Thread.sleep(1000);
        
        FunctionalTestComponent testComponent = this.getFunctionalTestComponent("tcpServerFlow");
        Assert.assertEquals(repeat, testComponent.getReceivedMessagesCount());
    }

    /**
     * Run the flow specified by name and assert equality on the expected output
     * 
     * @param flowName The name of the flow to run
     * @param expect The expected output
     */
    protected <T> void runFlowAndExpect(String flowName, T expect) throws Exception
    {
        Flow flow = lookupFlowConstruct(flowName);
        MuleEvent event = getTestEvent(NullPayload.getInstance());
        MuleEvent responseEvent = flow.process(event);

        Assert.assertEquals(expect, responseEvent.getMessage().getPayload());
    }

    /**
     * Run the flow specified by name using the specified payload and assert equality
     * on the expected output
     * 
     * @param flowName The name of the flow to run
     * @param expect The expected output
     * @param payload The payload of the input event
     */
    protected <T, U> void runFlowWithPayloadAndExpect(String flowName, T expect, U payload) throws Exception
    {
        Flow flow = lookupFlowConstruct(flowName);
        MuleEvent event = getTestEvent(payload);
        MuleEvent responseEvent = flow.process(event);

        Assert.assertEquals(expect, responseEvent.getMessage().getPayload());
    }

    /**
     * Retrieve a flow by name from the registry
     * 
     * @param name Name of the flow to retrieve
     */
    protected Flow lookupFlowConstruct(String name)
    {
        return (Flow) muleContext.getRegistry().lookupFlowConstruct(name);
    }
}
