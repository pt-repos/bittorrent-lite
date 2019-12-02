package bittorrent.peer;

import bittorrent.MessageType;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

class ServerProcess implements Runnable {

    private Socket connection;
    private int neighborId;
    private Peer self;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private Properties properties;

    ServerProcess(Socket connection, Peer self) {
        this.connection = connection;
        this.self = self;
        this.properties = self.getConfigProperties();
    }

    private void sendChunk(int chunkId) throws IOException {
        System.out.println("Sending chunk: " + chunkId + "to peer: " + neighborId);


        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        DataInputStream dataInputStream = null;
        try {
            String fileName = String.format(
                    properties.getProperty("dir.path.format") + properties.getProperty("chunk.name.format"),
                    self.getPeerId(),
                    properties.getProperty("file.name"),
                    chunkId);
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

    private void sendBitField() throws IOException {
        System.out.println("Sending bitField to peer [" + neighborId + "]");
        outputStream.writeObject(self.getBitField());
        outputStream.flush();
    }

    @Override
    public void run() {
        try {
            outputStream = new ObjectOutputStream(connection.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(connection.getInputStream());

            this.neighborId = inputStream.readInt();
            System.out.println("received CONNECTION from peer [" + neighborId + "]");

            while (true) {
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
                }
            }
        } catch (IOException e) {
            System.out.println("Disconnected with peer [" + neighborId + "]");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
