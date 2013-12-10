package com.ricston.nettypublisher;

import org.mule.util.StringUtils;

import com.ricston.nettypublisher.exception.UnsupportedDataTypeException;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class NettyPublisherHandler extends ChannelInboundHandlerAdapter
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


