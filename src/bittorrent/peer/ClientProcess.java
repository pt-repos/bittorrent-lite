package bittorrent.peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

class ClientProcess implements Runnable {

    private Socket requestSocket;
    private String host;
    private int port;
    private int peerId;

    ClientProcess(String host, int port, int peerId) {
        this.host = host;
        this.port = port;
        this.peerId = peerId;
    }

    @Override
    public void run() {
        // TODO: 11/24/2019
        try {

            boolean connectionEstablished = false;
            do {
                try {
                    System.out.println("Trying connection to Peer @ [" + port + "]");
                    requestSocket = new Socket(host, port);
                    connectionEstablished = true;
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            } while (!connectionEstablished);

            ObjectOutputStream outputStream = new ObjectOutputStream(requestSocket.getOutputStream());
            outputStream.flush();
            ObjectInputStream inputStream = new ObjectInputStream(requestSocket.getInputStream());

            outputStream.writeInt(peerId);
            outputStream.flush();

            // TODO: 11/24/2019 infinite loop to handle connection of peer as a client.
            // current implementation is for test purposes
            while (true) {
                String message = (String) inputStream.readObject();
                System.out.println(message + " received from [" + port + "]");

                outputStream.writeObject("ClientProcess: Hi");
                outputStream.flush();

                // TODO: 11/27/2019 message to break connection and exit loop 
            }
        } catch (SocketException e) {
            System.out.println("Connection with peer [" + port + "] lost");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
