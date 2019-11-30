package bittorrent.peer;

import bittorrent.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.BitSet;
import java.util.Properties;

public class FileOwner extends Peer{

    public FileOwner(int peerId, Properties configProperties) {
        super(peerId, configProperties);
    }

    private void divideFileIntoChunks() throws IOException {
//        URL filePath = getClass().getClassLoader().getResource("files/8000/cn-book.pdf");
//        int nChunks = FileUtil.splitFile(filePath.getFile());
//        String filePath = String.format("./src/main/files/%d/%s", this.getPeerId(), "cn-book.pdf");

        String filePath = String.format(
                configProperties.getProperty("file.path.format"),
                this.getPeerId(),
                configProperties.getProperty("file.name")
        );

        int chunkSize = Integer.parseInt(configProperties.getProperty("chunk.size"));
        FileUtil.splitFile(new File(filePath), chunkSize);

        int nChunks = Integer.parseInt(configProperties.getProperty("chunk.count"));
        System.out.println("nChunks: " + nChunks);
        BitSet bitField = new BitSet(nChunks);
        bitField.set(0, nChunks);
        this.setBitField(bitField);
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
