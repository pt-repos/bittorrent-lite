package bittorrent.peer;

import java.io.IOException;

public class PeerInit {
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            throw new Exception("Invalid Number of Arguments");
        }

        try {
            int fileOwnerPort = Integer.parseInt(args[0]);
            int peerId = Integer.parseInt(args[1]);
            int downloadPeerPort = Integer.parseInt(args[2]);

            Peer peer = new Peer(peerId, fileOwnerPort, downloadPeerPort);
            peer.start();
        } catch(NumberFormatException e) {
            System.out.println("Invalid arguments");
        } catch(IOException e) {
            System.out.println(e);
        }
    }
}
