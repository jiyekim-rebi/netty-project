package org.example.eventloop;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

import java.util.logging.Logger;

public class EchoServerHandlerWithFuture extends SimpleChannelInboundHandler<Object> {

    private final static Logger LOG = Logger.getGlobal();


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ChannelFuture channelFuture = ctx.writeAndFlush(msg);

        final int writeMessageSize = ((ByteBuf)msg).readableBytes();

        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                LOG.info("전송한 bytes: " + writeMessageSize);
                future.channel().close();
            }
        });
    }

}
