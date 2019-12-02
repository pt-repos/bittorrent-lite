package bittorrent.peer;

import bittorrent.MessageType;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.*;

class ClientProcess implements Runnable {

    private Socket requestSocket;
    private String host;
    private int neighborPort;
    private Peer self;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private Properties properties;
    private boolean receivedAll;
    private int nChunks;

    ClientProcess(String host, int neighborPort, Peer self) {
        this.host = host;
        this.neighborPort = neighborPort;
        this.self = self;
        this.properties = self.getConfigProperties();
        this.nChunks = Integer.parseInt(properties.getProperty("chunk.count"));
        this.receivedAll = false;
    }

    private BitSet getNeighborBitField() throws IOException, ClassNotFoundException {
        outputStream.writeObject(MessageType.BIT_FIELD);
        outputStream.flush();

        return (BitSet) inputStream.readObject();
    }

    private void requestNewChunk() throws IOException, ClassNotFoundException {
        /*
        get available bits in neighbor needed by self.
        required_bits = (myBitSet ^ neighborBitSet) & neighborBitSet
        */
        BitSet neighborBitField = getNeighborBitField();
        BitSet selfBitField = self.getBitField();
        selfBitField.xor(neighborBitField);
        neighborBitField.and(selfBitField);

        if (neighborBitField.length() == 0) {
            System.out.println("No New chunk available at this moment @ peer: " + neighborPort);

            BitSet bitField = self.getBitField();

            if (bitField.cardinality() == nChunks) {
                receivedAll = true;
                self.mergeChunksIntoFile(nChunks);
                sendShutDownMessage();
            } else {
                sendStandbyMessage();
            }
            return;
        }

        List<Integer> availableChunks = new ArrayList<>();
        for (int i = neighborBitField.nextSetBit(0); i >= 0;
             i = neighborBitField.nextSetBit(i+1)) {
            availableChunks.add(i);
        }
        while (!availableChunks.isEmpty()) {
            int randomIndex = new Random().nextInt(availableChunks.size());
            int chunkId = availableChunks.get(randomIndex);
            if (!self.checkAndUpdateDownloadTracker(chunkId)) {
                System.out.println("Requesting chunk:" + chunkId + " from peer: " + neighborPort);
                outputStream.writeObject(MessageType.GET_CHUNK);
                outputStream.writeInt(chunkId);
                outputStream.flush();
                return;
            }
        }
    }

    private void sendStandbyMessage() throws IOException {
        outputStream.writeObject(MessageType.STANDBY);
        outputStream.flush();
    }

    private void sendShutDownMessage() throws IOException {
        System.out.println("$Shutting down connection with peer [" + neighborPort + "]");
        outputStream.writeObject(MessageType.SHUTDOWN);
        outputStream.flush();
    }

    private void receiveChunk(int chunkId) throws IOException {
        System.out.println("Received chunk: " + chunkId + " from peer: " + neighborPort);

        int bufferSize = (int) inputStream.readLong();
        String fileName = String.format(
                properties.getProperty("dir.path.format") + properties.getProperty("chunk.name.format"),
                self.getPeerId(),
                properties.getProperty("file.name"),
                chunkId);
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        byte[] buffer = new byte[bufferSize];
        int bytesRead;
        while (bufferSize > 0
                && (bytesRead = inputStream.read(buffer, 0, Math.min(buffer.length, bufferSize))) != -1) {

            fileOutputStream.write(buffer, 0, bytesRead);
            bufferSize -= bytesRead;
        }
        fileOutputStream.close();
        self.setBitFieldIndex(chunkId);
        self.getDownloadTracker().remove(self.getPeerId(), chunkId);

        BitSet bitField = self.getBitField();
        if (bitField.cardinality() == bitField.length()) {

        }
    }

    @Override
    public void run() {
        // TODO: 11/24/2019
        try {
            boolean connectionEstablished = false;
            do {
                try {
                    System.out.println("Trying connection to Peer @ [" + neighborPort + "]");
                    requestSocket = new Socket(host, neighborPort);
                    requestSocket.setSoTimeout(3000);
                    connectionEstablished = true;
                } catch (ConnectException e) {
                    System.out.println("Connection refused @ [" + neighborPort + "]");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (!connectionEstablished);

            System.out.println("Connected to Peer @ [" + neighborPort + "]");

            outputStream = new ObjectOutputStream(requestSocket.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(requestSocket.getInputStream());

            outputStream.writeInt(self.getPeerId());
            outputStream.flush();

            while (!receivedAll) {
                Thread.sleep(300);
                try {
                    requestNewChunk();
                    MessageType messageType = (MessageType) inputStream.readObject();
                    switch (messageType) {
                        case GET_CHUNK:
                            int chunkId = inputStream.readInt();
                            receiveChunk(chunkId);
                            break;

                        case STANDBY:
                            Thread.sleep(600);
                            break;
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Socket timeout: Peer [" + neighborPort + "]");
                    self.getDownloadTracker().remove(self.getPeerId());
                }
            }
        } catch (SocketTimeoutException e) {
            System.out.println("###Socket timeout: Peer [" + neighborPort + "]");
        } catch (SocketException e) {
            System.out.println("Connection with self [" + neighborPort + "] lost");
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            self.getDownloadTracker().remove(self.getPeerId());
        }
    }
}
