package bittorrent.peer;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Peer {

    private int peerId;
    private int fileOwnerPort;
    private int downloadPeerPort;

    public Peer(int peerId, int fileOwnerPort, int downloadPeerPort) {
        this.peerId = peerId;
        this.fileOwnerPort = fileOwnerPort;
        this.downloadPeerPort = downloadPeerPort;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public void setFileOwnerPort(int fileOwnerPort) {
        this.fileOwnerPort = fileOwnerPort;
    }

    public void setDownloadPeerPort(int downloadPeerPort) {
        this.downloadPeerPort = downloadPeerPort;
    }

    public void start() throws IOException {
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(5);
            ServerSocket listener = new ServerSocket(peerId);
            executorService.submit(new ConnectionListener(listener));

            String host = "localhost";
            System.out.println("Trying to connect to FileOwner @ [" + fileOwnerPort + "]");
            try {
                Thread peerAsClientToFileOwner = new Thread(new ClientProcess(host, fileOwnerPort, peerId));
                executorService.submit(peerAsClientToFileOwner);
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("Trying to connect to Download Neighbor @ [" + downloadPeerPort + "]");
            try {
                Thread peerAsClientToPeer = new Thread(new ClientProcess(host, downloadPeerPort, peerId));
                executorService.submit(peerAsClientToPeer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public static void main(String[] args) throws Exception {
//        // TODO: 11/24/2019 Implement threaded listener for accepting connections as a server
//        ExecutorService executorService = Executors.newFixedThreadPool(5);
//
//        try {
//            ServerSocket listener = new ServerSocket(peerId);
//            executorService.submit(new ConnectionListener(listener));
//
//            String host = "localhost";
//            System.out.println("Trying to connect to FileOwner @ [" + fileOwnerPort + "]");
//            try {
//                Thread peerAsClientToFileOwner = new Thread(new ClientProcess(host, fileOwnerPort, peerId));
//                executorService.submit(peerAsClientToFileOwner);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            System.out.println("Trying to connect to Download Neighbor @ [" + downloadPeerPort + "]");
//            try {
//                Thread peerAsClientToPeer = new Thread(new ClientProcess(host, downloadPeerPort, peerId));
//                executorService.submit(peerAsClientToPeer);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
//            e.printStackTrace();
//        }
//    }
}
