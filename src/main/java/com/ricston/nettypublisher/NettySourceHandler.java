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

import org.mule.api.callback.SourceCallback;
import org.mule.util.StringUtils;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class NettySourceHandler extends ChannelInboundHandlerAdapter
{
    protected SourceCallback source;
    protected ChannelHandlerContext ctx;
    protected List<NettySourceHandler> sourceHandlers;

    public NettySourceHandler(SourceCallback source, List<NettySourceHandler> sourceHandlers)
    {
        super();
        this.source = source;
        this.sourceHandlers = sourceHandlers;
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        super.channelActive(ctx);
        this.ctx = ctx;
        sourceHandlers.add(this);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        super.channelInactive(ctx);
        sourceHandlers.remove(this);
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
    {
        ByteBuf in = (ByteBuf) msg;
        try
        {
            String data = in.toString(io.netty.util.CharsetUtil.US_ASCII);
            
            if (StringUtils.equals(NettyUtils.TERMINATING_STRING, data)){
                ctx.close();
                return;
            }

            Object response = this.source.process(data);
            NettyUtils.writeToByteBuf(ctx, response);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
    
    public void close()
    {
        ctx.close();
    }

}
