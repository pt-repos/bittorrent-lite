package bittorrent.peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class ServerProcess implements Runnable {

    private Socket connection;
    private int port;
    private int peerId;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    ServerProcess(Socket connection) {
        this.connection = connection;
        this.port = connection.getPort();
    }

    @Override
    public void run() {
        // TODO: 11/24/2019
        System.out.println("received CONNECTION from peer [" + port + "]");

        try {
            outputStream = new ObjectOutputStream(connection.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(connection.getInputStream());

            while (true) {
                outputStream.writeObject("Hello");
                outputStream.flush();

                String message = (String) inputStream.readObject();
                System.out.println(message + " received from peer [" + port + "]");
                Thread.sleep(5000);
            }
        } catch (IOException e) {
            System.out.println("Disconnected with peer [" + port + "]");
        } catch (InterruptedException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
