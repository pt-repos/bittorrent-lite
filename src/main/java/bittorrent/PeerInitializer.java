package bittorrent;

import bittorrent.peer.FileOwner;
import bittorrent.peer.Peer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PeerInitializer {
    public static void main(String[] args) {
        try (InputStream inputStream
                     = new FileInputStream("./src/main/resources/config.properties")) {

            int peerId = Integer.parseInt(args[0]);
            Properties configProperties = new Properties();
            configProperties.load(inputStream);

            if (args.length < 2) {
                FileOwner fileOwner = new FileOwner(peerId, configProperties);
                fileOwner.start();
            }
            else {
                int fileOwnerPort = Integer.parseInt(args[1]);
                int downloadPeerPort = Integer.parseInt(args[2]);

                Peer peer = new Peer(peerId, fileOwnerPort, downloadPeerPort, configProperties);
                peer.start();
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            System.out.println("Invalid arguments");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
