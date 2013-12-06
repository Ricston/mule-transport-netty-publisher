/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.ricston.nettypublisher;

import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;

public class NettyChannelInfo
{
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture serverChannel;

    public NettyChannelInfo()
    {
        super();
    }

    public NettyChannelInfo(EventLoopGroup bossGroup, EventLoopGroup workerGroup, ChannelFuture serverChannel)
    {
        super();
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
        this.serverChannel = serverChannel;
    }
    
    public void closeServer() throws InterruptedException
    {
        serverChannel.channel().closeFuture().sync();
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
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

    public ChannelFuture getServerChannel()
    {
        return serverChannel;
    }

    public void setServerChannel(ChannelFuture serverChannel)
    {
        this.serverChannel = serverChannel;
    }

}
