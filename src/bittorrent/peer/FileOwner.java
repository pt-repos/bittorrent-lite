package bittorrent.peer;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Set;

public class FileOwner extends Peer{

    public FileOwner(int peerId) {
        super(peerId);
    }

    private void divideFileIntoChunks() {
        // TODO: 11/28/2019 temporary implementation for the purpose of
        // establishing messaging and requesting chunks.

        Set<Integer> chunkSet = this.getChunkSet();
        for(int i = 0; i < 100; i++) {
            chunkSet.add(i);
        }
    }

    @Override
    public void start() throws IOException{
        System.out.println("Initializing...");

        divideFileIntoChunks();

        System.out.println("FileOwner is running");
        System.out.println("Listening for connections");

        try {
            ServerSocket listener = new ServerSocket(this.getPeerId());
            Thread connectionListener = new Thread(new ConnectionListener(listener, this));
            connectionListener.start();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
