import bittorrent.peer.ConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;

public class FileOwner {

    public FileOwner() {}

    public static void main(String[] args) throws IOException {
        System.out.println("Server is running");
        System.out.println("Listening for connections");

        // TODO: 11/23/2019 Division of file into chunks

        try {
            int port = Integer.parseInt(args[0]);
            ServerSocket listener = new ServerSocket(port);
            Thread connectionListener = new Thread(new ConnectionListener(listener));
            connectionListener.start();
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
