package org.example.blocking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.logging.Logger;

// 블로킹 소켓: ServerSocket, Socket Class
// accept 메서드 -> 단기간에 요청이 많아지면 대기 시간이 길어짐 + 자바 힙 메모리 부족으로 인한 OOM 오류 발생할수도 있음.
public class BlockingServer {
    private final static Logger LOG = Logger.getGlobal();

    public static void main(String[] args) throws Exception {
        BlockingServer server = new BlockingServer();
        server.run();
    }

    private void run() throws IOException {
        ServerSocket server = new ServerSocket(8888);
        LOG.info("접속 대기중");

        while (true) {
            Socket sock = server.accept();
            LOG.info("클라이언트 연결됨");

            OutputStream out = sock.getOutputStream();
            InputStream is = sock.getInputStream();

            try {
                int request = is.read();
                out.write(request);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }

        server.close();
    }
}
