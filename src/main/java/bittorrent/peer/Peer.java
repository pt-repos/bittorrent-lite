package bittorrent.peer;

import bittorrent.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Peer {

    private int peerId;
    private int fileOwnerPort;
    private int downloadPeerPort;
    private BitSet bitField;
    private Map<Integer, Integer> downloadTracker;
    Properties configProperties;

    Peer(int peerId, Properties configProperties) {
        this(peerId, 0, 0, configProperties);
    }

    public Peer(int peerId, int fileOwnerPort, int downloadPeerPort, Properties configProperties) {
        this.peerId = peerId;
        this.fileOwnerPort = fileOwnerPort;
        this.downloadPeerPort = downloadPeerPort;
        this.configProperties = configProperties;
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

    Properties getConfigProperties() {
        return this.configProperties;
    }

    void mergeChunksIntoFile(int nChunks) throws IOException {
        // TODO: 11/29/2019 cleaner implementation. this is only temporary
//        String into = String.format("./src/main/files/%d/%s", this.peerId, "cn-book.pdf");

        String into = String.format(
                configProperties.getProperty("file.path.format"),
                this.peerId,
                configProperties.getProperty("file.name")
        );

        List<File> files = new ArrayList<>();
        for (int i = 0; i < nChunks; i++) {
            String filePartName = String.format(
                    configProperties.getProperty("chunk.name.format"), into, i);
            files.add(new File(filePartName));
        }
        FileUtil.mergeFiles(files, new File(into));
    }

    public void start() throws IOException {
        try {
            File directory = new File(String.format(
                    configProperties.getProperty("dir.path.format"), this.peerId));

            if (!directory.exists()) {
                directory.mkdir();
            }

            int nChunks = Integer.parseInt(configProperties.getProperty("chunk.count"));
            this.setBitField(new BitSet(nChunks));

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
