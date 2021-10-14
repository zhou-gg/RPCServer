package rpcserver.server;

import java.io.IOException;
import java.net.ServerSocket;

public class TestDemo {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        serverSocket.accept();
    }

}
