package org.example.discard;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class DiscardServerHandler extends SimpleChannelInboundHandler<Object>{
    /*
        4.x version까지만 해도 channelRead0이란 이름을 가지고 있었음.
        channelRead0() :: 새로운 데이터가 수신되었을때 발생하는 이벤트
        - ChannelHandlerContext : 이벤트 처리를 위한 컨텍스트 객체
        - ByteBuf : 수신된 데이터를 저장한 객체
     */
    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object obj) throws Exception {
        // 일단 비워둠
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
