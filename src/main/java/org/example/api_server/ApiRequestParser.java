package org.example.api_server;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.json.JSONObject;

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
            헤더  100 Continue 체크 메서드인데.. 5 버전에선 해당 메서드가 빠지고 자동으로 체크해준다는 말이 있어서 확인 필요함.
            if (HttpHeaders.is100ContinueExpected(request)) {}
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

        }

    }
}
