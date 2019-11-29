package bittorrent.peer;

import bittorrent.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;
import java.util.Set;

class ClientProcess implements Runnable {

    private Socket requestSocket;
    private String host;
    private int neighborPort;
    private Peer self;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    ClientProcess(String host, int neighborPort, Peer self) {
        this.host = host;
        this.neighborPort = neighborPort;
        this.self = self;
    }

    private Set<Integer> getChunksAvailableAtNeighbor() throws ClassNotFoundException, IOException {

        outputStream.writeObject(MessageType.GET_AVAILABLE_CHUNKS);
        outputStream.flush();

        return (Set<Integer>) inputStream.readObject();
    }

    private void requestNewChunk() throws IOException, ClassNotFoundException {
        Set<Integer> availableChunks = getChunksAvailableAtNeighbor();
        availableChunks.removeAll(self.getChunkSet());

        int size = availableChunks.size();
        if (size == 0) {
            System.out.println("No New chunk available at this moment @ peer: " + neighborPort);
            sendStandbyMessage();
            return;
        }

        int random = new Random().nextInt(size);
        int i = 0;
        for (int chunk: availableChunks) {
            if (i < random) {
                i++;
                continue;
            }

            System.out.println("Requesting chunk:" + chunk + " from peer: " + neighborPort);
            outputStream.writeObject(MessageType.GET_CHUNK);
            outputStream.writeInt(chunk);
            outputStream.flush();
            break;
        }
    }

    private void sendStandbyMessage() throws IOException {
        outputStream.writeObject(MessageType.STANDBY);
        outputStream.flush();
    }

    private void receiveChunk(int chunkId) {
        System.out.println("Received chunk: " + chunkId + "from peer: " + neighborPort);
        self.getChunkSet().add(chunkId);
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
                    connectionEstablished = true;
                } catch (Exception e) {
//                    e.printStackTrace();
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
            while (true) {

                requestNewChunk();

                MessageType messageType = (MessageType) inputStream.readObject();

                switch (messageType) {
                    case GET_CHUNK:
                        int chunkId = inputStream.readInt();
                        receiveChunk(chunkId);
                        break;

                    case STANDBY:
                        Thread.sleep(2000);
                        break;
                }

                // TODO: 11/27/2019 message to break connection and exit loop 
            }
        } catch (SocketException e) {
            System.out.println("Connection with self [" + neighborPort + "] lost");
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
