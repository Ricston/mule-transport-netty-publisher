package com.ricston.nettypublisher.handlers;

import org.mule.api.callback.SourceCallback;

import com.ricston.nettypublisher.NettyUtils;

import io.netty.channel.ChannelHandlerContext;

import java.util.List;

public class NettySourceHandler extends AbstractNettyInboundHandlerAdapter
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
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        String data = NettyUtils.channelRead(ctx, msg);
        
        Object response = this.source.process(data);
        NettyUtils.writeToByteBuf(ctx, response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
    
    @Override
    public void close()
    {
        ctx.close();
    }

}
