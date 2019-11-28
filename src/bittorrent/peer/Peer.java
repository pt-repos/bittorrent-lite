package bittorrent.peer;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Peer {

    private int peerId;
    private int fileOwnerPort;
    private int downloadPeerPort;
    private Set<Integer> chunkSet;

    public Peer(int peerId, int fileOwnerPort, int downloadPeerPort) {
        this.peerId = peerId;
        this.fileOwnerPort = fileOwnerPort;
        this.downloadPeerPort = downloadPeerPort;
        this.chunkSet = new HashSet<>();
    }

    public int getPeerId() {
        return peerId;
    }

    public int getFileOwnerPort() {
        return fileOwnerPort;
    }

    public int getDownloadPeerPort() {
        return downloadPeerPort;
    }

    public Set<Integer> getChunkSet() {
        return chunkSet;
    }

    public void start() throws IOException {
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(5);
            ServerSocket listener = new ServerSocket(peerId);
            executorService.submit(new ConnectionListener(listener, this));

            String host = "localhost";
            System.out.println("Trying to connect to FileOwner @ [" + fileOwnerPort + "]");

            try {
                Thread peerAsClientToFileOwner = new Thread(new ClientProcess(host, fileOwnerPort, this));
                executorService.submit(peerAsClientToFileOwner);
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("Trying to connect to Download Neighbor @ [" + downloadPeerPort + "]");
            try {
                Thread peerAsClientToPeer = new Thread(new ClientProcess(host, downloadPeerPort, this));
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
//        if (args.length < 3) {
//            throw new Exception("Invalid Number of Arguments");
//        }
//
//        try {
//            int fileOwnerPort = Integer.parseInt(args[0]);
//            int peerId = Integer.parseInt(args[1]);
//            int downloadPeerPort = Integer.parseInt(args[2]);
//            boolean isFileOwner = false;
//
//            if (args.length == 4) {
//                isFileOwner = Boolean.parseBoolean(args[3]);
//            }
//
//            Peer peer = new Peer(peerId, fileOwnerPort, downloadPeerPort, isFileOwner);
//            peer.start();
//        } catch(NumberFormatException e) {
//            System.out.println("Invalid arguments");
//        } catch(IOException e) {
//            System.out.println(e);
//        }
//    }
}
