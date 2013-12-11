
package com.ricston.nettypublisher;

import com.ricston.nettypublisher.handlers.AbstractNettyInboundHandlerAdapter;

import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;

import java.util.List;

public class NettyChannelInfo <T extends AbstractNettyInboundHandlerAdapter>
{
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture channel;
    private List<T> channelInboundHandlers;

    public NettyChannelInfo()
    {
        super();
    }

    public NettyChannelInfo(EventLoopGroup bossGroup,
                            EventLoopGroup workerGroup,
                            ChannelFuture channel,
                            List<T> channelInboundHandlers)
    {
        super();
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
        this.channel = channel;
        this.channelInboundHandlers = channelInboundHandlers;
    }

    public void closeAll() throws InterruptedException
    {
        if (channel != null)
        {
            channel.channel().closeFuture().sync();
        }

        if (workerGroup != null)
        {
            workerGroup.shutdownGracefully();
        }

        if (bossGroup != null)
        {
            bossGroup.shutdownGracefully();
        }
        
        if (channelInboundHandlers != null){
            for (AbstractNettyInboundHandlerAdapter channel : channelInboundHandlers)
            {
                channel.close();
            }
        }
    }

    public EventLoopGroup getBossGroup()
    {
        return bossGroup;
    }

    public void setBossGroup(EventLoopGroup bossGroup)
    {
        this.bossGroup = bossGroup;
    }

    public EventLoopGroup getWorkerGroup()
    {
        return workerGroup;
    }

    public void setWorkerGroup(EventLoopGroup workerGroup)
    {
        this.workerGroup = workerGroup;
    }

    public ChannelFuture getChannel()
    {
        return channel;
    }

    public void setChannel(ChannelFuture serverChannel)
    {
        this.channel = serverChannel;
    }

    public List<T> getChannelInboundHandlers()
    {
        return channelInboundHandlers;
    }

    public void setChannelInboundHandlers(List<T> channelInboundHandlers)
    {
        this.channelInboundHandlers = channelInboundHandlers;
    }

}
