package com.ricston.nettypublisher;

import org.mule.api.callback.SourceCallback;
import org.mule.util.StringUtils;

import com.ricston.nettypublisher.exception.UnknownServerTypeException;
import com.ricston.nettypublisher.exception.UnsupportedDataTypeException;

import java.util.List;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;

public class NettyUtils
{
    public static final String TERMINATING_STRING = "^]\r\n";

    public static NettyChannelInfo startServer(Integer port,
                                               final ServerType serverType,
                                               final SourceCallback callback,
                                               final List<NettySourceHandler> sourceHandlers,
                                               final List<NettyPublisherHandler> publisherHandlers) throws InterruptedException
    {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>()
            {
                @Override
                public void initChannel(SocketChannel ch) throws Exception
                {
                    ChannelInboundHandlerAdapter handler = null;

                    switch (serverType)
                    {
                        case SOURCE :
                            handler = new NettySourceHandler(callback, sourceHandlers);
                            break;
                        case PUBLISHER :
                            handler = new NettyPublisherHandler(publisherHandlers);
                            break;
                        default:
                            throw new UnknownServerTypeException(serverType.name());
                    }

                    ch.pipeline().addLast(handler);
                }
                
            })
            .option(ChannelOption.SO_BACKLOG, 128)
            .childOption(ChannelOption.SO_KEEPALIVE, true);

        // Bind and start to accept incoming connections.
        ChannelFuture serverChannel = bootstrap.bind(port).sync();

        NettyChannelInfo nettyChannelInfo = new NettyChannelInfo(bossGroup, workerGroup, serverChannel);
        return nettyChannelInfo;
    }
    
    public static NettyChannelInfo startClient(String host, Integer port, final NettyClientHandler handler) throws InterruptedException
    {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .handler(new ChannelInitializer<SocketChannel>()
            {
                @Override
                public void initChannel(SocketChannel ch) throws Exception
                {
                    ch.pipeline().addLast(handler);
                }
                
            });

        // Connect to server.
        ChannelFuture clientChannel = bootstrap.connect(host, port).sync();

        NettyChannelInfo nettyChannelInfo = new NettyChannelInfo(null, workerGroup, clientChannel);
        return nettyChannelInfo;
    }

    public static ByteBuf writeToByteBuf(ChannelHandlerContext ctx, Object msg) throws UnsupportedDataTypeException
    {
        if (msg instanceof org.mule.transport.NullPayload)
        {
            return writeToByteBuf(ctx, "");
        }
        
        if (msg instanceof String)
        {
            String responseString = (String) msg;
            byte[] dataBytes = responseString.getBytes();
            ByteBuf out = ctx.alloc().buffer(dataBytes.length);
            out.writeBytes(dataBytes);

            ctx.write(out);
            ctx.flush();

            return out;
        }

        throw new UnsupportedDataTypeException(msg);
    }
    
    public static String channelRead(ChannelHandlerContext ctx, Object msg)
    {
        ByteBuf in = (ByteBuf) msg;
        String data = null;
        try
        {
            data = in.toString(io.netty.util.CharsetUtil.UTF_8);
            
            if (StringUtils.equals(NettyUtils.TERMINATING_STRING, data)){
                ctx.close();
            }
        }
        finally
        {
            ReferenceCountUtil.release(msg);
        }
        
        return data;
    }

}
