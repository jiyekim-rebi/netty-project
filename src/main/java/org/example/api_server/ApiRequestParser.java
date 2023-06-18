package org.example.api_server;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import org.example.ApiRequest;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class ApiRequestParser extends SimpleChannelInboundHandler<FullHttpMessage> {

    private static final Logger logger = LoggerFactory.getLogger(ApiRequestParser.class);

    private HttpRequest request;
    private JSONObject apiResult;
    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
    private HttpPostRequestDecoder decoder;
    private Map<String, String> reqData = new HashMap<>();
    private static final Set<String> usingHeader = new HashSet<>();

    static {
        usingHeader.add("token");
        usingHeader.add("email");
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, FullHttpMessage msg) throws Exception {
        // request header
        if (msg instanceof HttpRequest) {
            this.request = (HttpRequest) msg;


            /*
            HttpHeaders.is100ContinueExpected(request) ==> 2023.06.07 deprecated 확인
            Ref. https://netty.io/5.0/api/io.netty5.codec.http/io/netty5/handler/codec/http/HttpHeaders.html#is100ContinueExpected(io.netty5.handler.codec.http.HttpMessage)
             */

            HttpHeaders headers = request.headers();
            if (!headers.isEmpty()) {
                for(Map.Entry<CharSequence, CharSequence> h : headers) {
                    String key = (String) h.getKey();
                    if (usingHeader.contains(key)) {
                        reqData.put(key, (String) h.getValue());
                    }
                }
            }

            reqData.put("REQUEST_URI", request.uri());
            reqData.put("REQUEST_METHOD", request.method().toString());

        }

        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;

            ByteBuf content = httpContent.content();

            if (msg instanceof LastHttpContent) {
                logger.debug("LastHttpContent message received : " + request.uri());

                LastHttpContent trailer = (LastHttpContent) msg;

                readPostData();

                ApiRequest service = ServiceDispatcher.dispatch(reqData);

                try {
                    service.executeService();

                    apiResult = service.getApiResult();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    reqData.clear();
                }

                if (!writeResponse(trailer, ctx)) {
                    ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                }

                reset();
            }
        }

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        logger.debug("요청 처리 완료");
        ctx.flush();
    }

    private void reset() {
        request = null;
    }

    private void readPostData() {

        try {
            decoder = new HttpPostRequestDecoder(factory, request);
            for(InterfaceHttpData data : decoder.getBodyHttpDatas()) {
                if (InterfaceHttpData.HttpDataType.Attribute == data.getHttpDataType()) {
                    try {
                        Attribute attribute = (Attribute) data;
                        reqData.put(attribute.getName(), attribute.getValue());
                    } catch (IOException e) {
                        logger.error("BODY Attribute: " + data.getHttpDataType().name(), e);
                        return;
                    }
                } else {
                    logger.debug("BODY data : " + data.getHttpDataType().name() + ": " + data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        } finally {
            if(decoder != null) {
                decoder.destroy();
            }
        }

    }
}
