package bittorrent.peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class ServerProcess implements Runnable {

    private Socket connection;
    private int neighborId;

    ServerProcess(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        // TODO: 11/24/2019
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(connection.getOutputStream());
            outputStream.flush();
            ObjectInputStream inputStream = new ObjectInputStream(connection.getInputStream());

            this.neighborId = inputStream.readInt();

            System.out.println("received CONNECTION from peer [" + neighborId + "]");

            while (true) {
                outputStream.writeObject("ServerProcess: Hello");
                outputStream.flush();

                String message = (String) inputStream.readObject();
                System.out.println(message + " received from peer [" + neighborId + "]");
                Thread.sleep(5000);

                // TODO: 11/27/2019 message to break connection and exit loop
            }
        } catch (IOException e) {
            System.out.println("Disconnected with peer [" + neighborId + "]");
        } catch (InterruptedException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
