package bittorrent.peer;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Peer {

    private int peerId;
    private int fileOwnerPort;
    private int downloadPeerPort;
    private Set<Integer> chunkSet;

    Peer(int peerId) {
        this(peerId, 0, 0);
    }

    public Peer(int peerId, int fileOwnerPort, int downloadPeerPort) {
        this.peerId = peerId;
        this.fileOwnerPort = fileOwnerPort;
        this.downloadPeerPort = downloadPeerPort;
        this.chunkSet = ConcurrentHashMap.newKeySet();
    }

    int getPeerId() {
        return peerId;
    }

    Set<Integer> getChunkSet() {
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
}
