package com.ricston.nettypublisher.handlers;

import org.mule.util.StringUtils;

import com.ricston.nettypublisher.NettyUtils;
import com.ricston.nettypublisher.exception.UnsupportedDataTypeException;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

public class NettyPublisherHandler extends AbstractNettyInboundHandlerAdapter
{
    protected ChannelHandlerContext ctx;
    protected List<NettyPublisherHandler> publisherHandlers;
    
    public NettyPublisherHandler(List<NettyPublisherHandler> publisherHandlers)
    {
        super();
        this.publisherHandlers = publisherHandlers;
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        super.channelActive(ctx);
        this.ctx = ctx;
        publisherHandlers.add(this);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        super.channelInactive(ctx);
        publisherHandlers.remove(this);
    }
    
    public void publish(String data) throws UnsupportedDataTypeException
    {
        if (ctx != null)
        {
            NettyUtils.writeToByteBuf(ctx, data);
        }
    }
    
    @Override
    public void close()
    {
        ctx.close();
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
    {
        ByteBuf in = (ByteBuf) msg;
        try
        {
            String data = in.toString(io.netty.util.CharsetUtil.UTF_8);
            
            if (StringUtils.equals(NettyUtils.TERMINATING_STRING, data)){
                ctx.close();
                return;
            }
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

}


