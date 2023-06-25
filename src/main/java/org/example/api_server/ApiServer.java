package org.example.api_server;

import com.sun.jmx.mbeanserver.NamedObject;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.serviceloader.ServiceFactoryBean;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import java.awt.*;
import java.net.InetSocketAddress;
import java.security.cert.CertificateException;

@Component
public class ApiServer {

    @Autowired
    @Qualifier("tcpSocketAddress")
    private InetSocketAddress address;

    @Autowired
    @Qualifier("workerThreadCount")
    private int workerThreadCount;

    @Autowired
    @Qualifier("bossThreadCount")
    private int bossThreadCount;

    public void start(){
        EventLoopGroup bossGroup = new NioEventLoopGroup(bossThreadCount);
        EventLoopGroup workerGroup = new NioEventLoopGroup(workerThreadCount);
        ChannelFuture channelFuture = null;

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ApiServerInitializer(null));

            Channel channel = b.bind().sync().channel();

            channelFuture = channel.closeFuture();
            //channelFuture.sync(); // 채널 닫힘 이벤트가 발생할 때 까지 대기

            final SslContext sslCtx;
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());

            ServerBootstrap b2 = new ServerBootstrap();
            b2.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ApiServerInitializer((sslCtx)));

            Channel ch2 = b2.bind(8443).sync().channel();

            channelFuture = ch2.closeFuture();
            channelFuture.sync();
        } catch (CertificateException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (SSLException ex) {
            ex.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
