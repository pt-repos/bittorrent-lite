package bittorrent.peer;

import bittorrent.MessageType;

import java.io.*;
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


        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        DataInputStream dataInputStream = null;
        try {
            String fileName = String.format("./src/main/files/%d/%s.%03d", self.getPeerId(), "cn-book.pdf", chunkId);
            File file = new File(fileName);
            long bufferSize = file.length();
            byte[] buffer = new byte[(int) bufferSize];
            fileInputStream = new FileInputStream(file);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            dataInputStream = new DataInputStream(bufferedInputStream);
            dataInputStream.readFully(buffer, 0, buffer.length);

            outputStream.writeObject(MessageType.GET_CHUNK);
            outputStream.writeInt(chunkId);
            outputStream.writeLong(bufferSize);
            outputStream.write(buffer, 0, buffer.length);
            outputStream.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (null != dataInputStream) dataInputStream.close();
            if (null != bufferedInputStream) bufferedInputStream.close();
            if (null != fileInputStream) fileInputStream.close();
        }
    }

    void sendBitField() throws IOException {
        System.out.println("Sending bitField to peer [" + neighborId + "]");
        outputStream.writeObject(self.getBitField());
        outputStream.flush();
    }

    void sendBitFieldLength() throws IOException {
        outputStream.writeObject(MessageType.BIT_FIELD_LENGTH);
        try {
            outputStream.writeInt(self.getBitField().length());
        } catch (NullPointerException e) {
            outputStream.writeInt(-1);
        }
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
                System.out.println("Waiting for instructions from peer [" + neighborId + "]");
                MessageType messageType = (MessageType) inputStream.readObject();

                switch (messageType) {
                    case BIT_FIELD:
                        sendBitField();
                        break;

                    case GET_CHUNK:
                        int requestedChunkId = inputStream.readInt();
                        sendChunk(requestedChunkId);
                        break;

                    case STANDBY:
                        outputStream.writeObject(MessageType.STANDBY);
                        outputStream.flush();
                        break;

                    case SHUTDOWN:
                        System.out.println("!!Shutting down connection with peer [" + neighborId + "]");
                        return;

                    case BIT_FIELD_LENGTH:
                        sendBitFieldLength();
                        break;
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
