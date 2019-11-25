package bittorrent.peer;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Peer {

    public static void main(String[] args) throws Exception {

        if (args.length != 3) {
            throw new Exception("Invalid Number of Arguments");
        }
        // TODO: 11/24/2019 Implement threaded listener for accepting connections as a server
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        try {
            int peerId = Integer.parseInt(args[1]);

            ServerSocket listener = new ServerSocket(peerId);
            executorService.submit(new ConnectionListener(listener));
            
            String host = "localhost";
            int fileOwnerPort = Integer.parseInt(args[0]);
            System.out.println("Trying to connect to FileOwner @ [" + fileOwnerPort + "]");
            try {
                Thread peerAsClientToFileOwner = new Thread(new ClientProcess(host, fileOwnerPort, peerId));
                executorService.submit(peerAsClientToFileOwner);
            } catch (Exception e) {
                e.printStackTrace();
            }

            int downloadPeerPort = Integer.parseInt(args[2]);
            System.out.println("Trying to connect to Download Neighbor @ [" + downloadPeerPort + "]");
            try {
                Thread peerAsClientToPeer = new Thread(new ClientProcess(host, downloadPeerPort, peerId));
                executorService.submit(peerAsClientToPeer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
