package bittorrent;

import bittorrent.peer.FileOwner;
import bittorrent.peer.Peer;

public class PeerInitializer {
    public static void main(String[] args) {
        try {
            int peerId = Integer.parseInt(args[0]);

            if (args.length < 2) {
                FileOwner fileOwner = new FileOwner(peerId);
                fileOwner.start();
            }
            else {
                int fileOwnerPort = Integer.parseInt(args[1]);
                int downloadPeerPort = Integer.parseInt(args[2]);

                Peer peer = new Peer(peerId, fileOwnerPort, downloadPeerPort);
                peer.start();
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            System.out.println("Invalid arguments");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
