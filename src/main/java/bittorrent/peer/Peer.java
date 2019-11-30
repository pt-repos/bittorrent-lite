package bittorrent.peer;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Peer {

    private int peerId;
    private int fileOwnerPort;
    private int downloadPeerPort;
    private BitSet bitField;
    private Map<Integer, Integer> downloadTracker;

    Peer(int peerId) {
        this(peerId, 0, 0);
    }

    public Peer(int peerId, int fileOwnerPort, int downloadPeerPort) {
        this.peerId = peerId;
        this.fileOwnerPort = fileOwnerPort;
        this.downloadPeerPort = downloadPeerPort;
        this.downloadTracker = new ConcurrentHashMap<>();
    }

    int getPeerId() {
        return peerId;
    }

    BitSet getBitField() {
        if (null != this.bitField) {
            return (BitSet) this.bitField.clone();
        }
        return null;
    }

    synchronized void setBitField(BitSet bitField) {
        this.bitField = bitField;
    }

    synchronized void setBitFieldIndex(int bitIndex) {
        this.bitField.set(bitIndex);
    }

    synchronized void clearBitFieldIndex(int bitIndex) {
        this.bitField.clear(bitIndex);
    }

    Map<Integer, Integer> getDownloadTracker() {
        return this.downloadTracker;
    }

    synchronized boolean checkAndUpdateDownloadTracker(int val) {
        if (this.downloadTracker.containsValue(val)) {
            return true;
        } else {
            this.downloadTracker.put(this.peerId, val);
            return false;
        }
    }

//    void mergeChunksIntoFile() throws IOException {
//        // TODO: 11/29/2019 cleaner implementation. this is only temporary
//        String into = "./src/main/files/8000/cn-book.pdf";
//        List<File> files = new ArrayList<>();
//
//        for (int i = 1; i <= 101; i++) {
//            String filePartName = String.format("%s.%03d", into, i);
//            files.add(new File(filePartName));
//        }
//        System.out.println("size: " + files.size());
//
//        FileUtil.mergeFiles(files, new File(into));
//    }

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
