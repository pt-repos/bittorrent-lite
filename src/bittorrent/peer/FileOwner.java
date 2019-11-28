package bittorrent.peer;

import java.io.IOException;
import java.net.ServerSocket;

public class FileOwner extends Peer{

    public FileOwner(int peerId) {
        super(peerId, 0, 0);
    }

    @Override
    public void start() throws IOException{
        System.out.println("FileOwner is running");
        System.out.println("Listening for connections");

        try {
            ServerSocket listener = new ServerSocket(this.getPeerId());
            Thread connectionListener = new Thread(new ConnectionListener(listener, this));
            connectionListener.start();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public static void main(String[] args) throws IOException {
//        // TODO: 11/23/2019 Division of file into chunks
//        try {
//            int port = Integer.parseInt(args[0]);
//            FileOwner fileOwner = new FileOwner(port);
//            fileOwner.start();
//        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
//            e.printStackTrace();
//        }
//    }
}
