
package com.ricston.nettypublisher;

import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;

public class NettyChannelInfo
{
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture channel;

    public NettyChannelInfo()
    {
        super();
    }

    public NettyChannelInfo(EventLoopGroup bossGroup, EventLoopGroup workerGroup, ChannelFuture serverChannel)
    {
        super();
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
        this.channel = serverChannel;
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

}
