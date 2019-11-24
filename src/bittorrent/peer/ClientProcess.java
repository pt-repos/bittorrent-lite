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
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    protected ClientProcess(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        // TODO: 11/24/2019
        try {
            requestSocket = new Socket(host, port);
            outputStream = new ObjectOutputStream(requestSocket.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(requestSocket.getInputStream());

            // TODO: 11/24/2019 infinite loop to handle connection of peer as a client.
            // current implementation is for test purposes
            while (true) {
                String message = (String) inputStream.readObject();
                System.out.println(message + " received from [" + port + "]");

                outputStream.writeObject("Hi");
                outputStream.flush();
            }
        } catch (SocketException e) {
            System.out.println("Connection with peer [" + port + "] lost");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
