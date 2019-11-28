package bittorrent.peer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionListener implements Runnable {
    private ServerSocket listener;
    private Peer self;

    public ConnectionListener(ServerSocket listener, Peer self) {
        this.listener = listener;
        this.self = self;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Socket connection = listener.accept();
                new Thread(new ServerProcess(connection, self)).start();
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
