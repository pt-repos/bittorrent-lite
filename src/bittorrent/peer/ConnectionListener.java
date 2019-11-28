package bittorrent.peer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionListener implements Runnable {
    private ServerSocket listener;
    private Peer peer;

    public ConnectionListener(ServerSocket listener, Peer peer) {
        this.listener = listener;
        this.peer = peer;
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
