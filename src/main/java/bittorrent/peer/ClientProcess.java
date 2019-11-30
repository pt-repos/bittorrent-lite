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
    private boolean receivedAll;

    ClientProcess(String host, int neighborPort, Peer self) {
        this.host = host;
        this.neighborPort = neighborPort;
        this.self = self;
        this.receivedAll = false;
    }

    private BitSet getNeighborBitField() throws IOException, ClassNotFoundException {
        outputStream.writeObject(MessageType.BIT_FIELD);
        outputStream.flush();

        return (BitSet) inputStream.readObject();
    }

    private void requestNewChunk() throws IOException, ClassNotFoundException {
        while (true) {
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

                if (bitField.cardinality() == bitField.length()) {
                    receivedAll = true;
                    sendShutDownMessage();
                } else {
                    sendStandbyMessage();
                }
                return;
            }

            for (int i = neighborBitField.nextSetBit(0); i >= 0;
                    i = neighborBitField.nextSetBit(i+1)) {

                int random = new Random().nextInt(100);
                if (random < 10 && !self.checkAndUpdateDownloadTracker(i)) {
                    System.out.println("Requesting chunk:" + i + " from peer: " + neighborPort);
                    outputStream.writeObject(MessageType.GET_CHUNK);
                    outputStream.writeInt(i);
                    outputStream.flush();
                    return;
                }
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
        String fileName = String.format("./src/main/files/%d/%s.%03d", self.getPeerId(), "cn-book.pdf", chunkId);
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

    private void requestBitFieldLength() throws IOException {
        outputStream.writeObject(MessageType.BIT_FIELD_LENGTH);
        outputStream.flush();
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

            // TODO: 11/24/2019 infinite loop to handle connection of self as a client.
            // current implementation is for test purposes
            while (!receivedAll) {
                Thread.sleep(300);
                try {
                    if (null == self.getBitField()) {
                        requestBitFieldLength();
                    } else {
                        requestNewChunk();
                    }

                    System.out.println("Waiting for message from server @ [" + neighborPort + "]");
                    MessageType messageType = (MessageType) inputStream.readObject();

                    switch (messageType) {
                        case GET_CHUNK:
                            int chunkId = inputStream.readInt();
                            receiveChunk(chunkId);
                            break;

                        case STANDBY:
                            Thread.sleep(2000);
                            break;

                        case BIT_FIELD_LENGTH:
                            int bitFieldSize = inputStream.readInt();
                            if (bitFieldSize > 0 && null == self.getBitField()) {
                                self.setBitField(new BitSet(bitFieldSize));
                            }
                            break;
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Socket timeout: Peer [" + neighborPort + "]");
                    self.getDownloadTracker().remove(self.getPeerId());
                }
                // TODO: 11/27/2019 message to break connection and exit loop 
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
