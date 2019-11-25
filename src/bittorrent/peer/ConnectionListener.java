package bittorrent.peer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class ConnectionListener implements Runnable {
    ServerSocket listener;

    ConnectionListener(ServerSocket listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Socket socket = listener.accept();
                new Thread(new ServerProcess(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                listener.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
