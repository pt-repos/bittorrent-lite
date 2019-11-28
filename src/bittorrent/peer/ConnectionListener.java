package bittorrent.peer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionListener implements Runnable {
    private ServerSocket listener;

    public ConnectionListener(ServerSocket listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Socket connection = listener.accept();
                new Thread(new ServerProcess(connection)).start();
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
