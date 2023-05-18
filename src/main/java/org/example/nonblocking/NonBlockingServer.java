package org.example.nonblocking;

import com.sun.org.apache.bcel.internal.generic.Select;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NonBlockingServer {
    private Map<SocketChannel, List<byte[]>> keepDataTrack = new HashMap<>();
    private ByteBuffer buffer = ByteBuffer.allocate(2 * 1024);
    private final static Logger LOG = Logger.getGlobal();

    private void startEchoServer() {
        try {
            Selector selector = Selector.open(); // 채널 변경사항 모니터링 및 접근
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open(); // 논블로킹 소켓 서버 생성

            if (serverSocketChannel.isOpen() && selector.isOpen()) {
                serverSocketChannel.configureBlocking(false); // 논블로킹 모드로 선언(default: true)
                serverSocketChannel.bind(new InetSocketAddress(8888));

                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT); //accept 이벤트 감지
                LOG.info("접속 대기중");

                while (true) {
                    selector.select(); //변경사항이 감지되었는지 확인
                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                    while (true) {
                        SelectionKey key = (SelectionKey) keys.next();
                        keys.remove(); //동일한 이벤트 감지 방지를 위해 제거함

                        if (!key.isValid()) {
                            continue;
                        }

                        if (key.isAcceptable()) {
                            this.acceptOP(key, selector);
                        } else if (key.isReadable()) {
                            this.readOP(key);
                        } else if (key.isWritable()) {
                            this.writeOP(key);
                        } else {
                            LOG.info("서버 소켓 생성 실패");
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void acceptOP(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);

        LOG.info("클라이언트 연결됨 : " + socketChannel.getRemoteAddress());

        keepDataTrack.put(socketChannel, new ArrayList<>());
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    private void readOP(SelectionKey key) {
        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            buffer.clear();
            int numRead = -1;
            try {
                numRead = socketChannel.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
                LOG.info("데이터 읽기 에러 : " + e);
            }

            if (numRead == -1) {
                this.keepDataTrack.remove(socketChannel);
                LOG.info("클라이언트 연결 종료 : " + socketChannel.getRemoteAddress());
                socketChannel.close();
                key.cancel();
                return;
            }

            byte[] data = new byte[numRead];
            System.arraycopy(buffer.array(), 0, data, 0, numRead);
            LOG.info(new String(data, StandardCharsets.UTF_8) + "from " + socketChannel.getRemoteAddress());
            doEchoJob(key, data);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void writeOP(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        List<byte[]> channelData = keepDataTrack.get(socketChannel);
        Iterator<byte[]> its = channelData.iterator();

        while (its.hasNext()) {
            byte[] it = its.next();
            its.remove();
            socketChannel.write(ByteBuffer.wrap(it));
        }

        key.interestOps(SelectionKey.OP_READ);
    }

    private void doEchoJob(SelectionKey key, byte[] data) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        List<byte[]> channelData = keepDataTrack.get(socketChannel);
        channelData.add(data);

        key.interestOps(SelectionKey.OP_WRITE);
    }

    public static void main(String[] args) {
        NonBlockingServer main = new NonBlockingServer();
        main.startEchoServer();
    }

}
