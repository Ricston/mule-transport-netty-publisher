package com.ricston.nettypublisher;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPromise;

public class NettyClientHandler extends ChannelOutboundHandlerAdapter
{
    private ChannelHandlerContext ctx;
    
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception
    {
        super.handlerAdded(ctx);
        this.ctx = ctx;
    }
    
    public void writeToServer(Object msg) throws Exception
    {
        NettyUtils.writeToByteBuf(ctx, msg);
    }
    
    public void close() throws Exception
    {
        ChannelPromise promise =  new DefaultChannelPromise(ctx.channel());
        this.close(ctx, promise);
    }
    
}
