package bittorrent.peer;

import bittorrent.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class ServerProcess implements Runnable {

    private Socket connection;
    private int neighborId;
    private Peer self;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    ServerProcess(Socket connection, Peer self) {
        this.connection = connection;
        this.self = self;
    }

    void sendChunk(int chunkId) throws IOException {
        System.out.println("Sending chunk: " + chunkId + "to peer: " + neighborId);
        outputStream.writeObject(MessageType.GET_CHUNK);
        outputStream.writeInt(chunkId);
        outputStream.flush();
    }

    @Override
    public void run() {
        // TODO: 11/24/2019
        try {
            outputStream = new ObjectOutputStream(connection.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(connection.getInputStream());

            this.neighborId = inputStream.readInt();

            System.out.println("received CONNECTION from peer [" + neighborId + "]");

            while (true) {
                System.out.println("Waiting for instructions");
                MessageType messageType = (MessageType) inputStream.readObject();

                switch (messageType) {
                    case GET_AVAILABLE_CHUNKS:
                        System.out.println("Sending chunkSet to peer [" + neighborId + "]");
                        outputStream.writeObject(self.getChunkSet());
                        break;

                    case GET_CHUNK:
                        int requestedChunkId = inputStream.readInt();
                        sendChunk(requestedChunkId);
                        break;

                    case STANDBY:
                        outputStream.writeObject(MessageType.STANDBY);
                        outputStream.flush();
                }

                // TODO: 11/27/2019 message to break connection and exit loop
            }
        } catch (IOException e) {
            System.out.println("Disconnected with peer [" + neighborId + "]");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
