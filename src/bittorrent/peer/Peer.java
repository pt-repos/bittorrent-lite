package bittorrent.peer;

public class Peer {
    // TODO: 11/23/201

    public static void main(String[] args) {
        // TODO: 11/23/2019
        String host = "localhost";
        int port = 8000;

        Thread peerAsClient = new Thread(new ClientProcess(host, port));
        peerAsClient.start();
    }
}
