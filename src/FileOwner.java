import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class FileOwner {

    public FileOwner() {}

    public static void main(String[] args) throws IOException {
        System.out.println("Server is running");
        System.out.println("Listening for connections");

        // TODO: 11/23/2019 Division of file into chunks

        ServerSocket listener = null;
        try {
//            int port = Integer.parseInt(args[0]);
            int port = 8000;
            listener = new ServerSocket(port);

            while(true) {
                new Thread(new ConnectionHandler(listener.accept())).start();
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            e.printStackTrace();
        } finally {
            if (null != listener) {
                listener.close();
            }
        }
    }

    private static class ConnectionHandler implements Runnable {
        private Socket connection;
        private int peerId;
        ObjectInputStream inputStream;
        ObjectOutputStream outputStream;

        private ConnectionHandler(Socket connection) {
            this.connection = connection;
            this.peerId = connection.getPort();
        }

        @Override
        public void run() {
            System.out.println("received CONNECTION from peer [" + peerId + "]");

            try {
                outputStream = new ObjectOutputStream(connection.getOutputStream());
                outputStream.flush();
                inputStream = new ObjectInputStream(connection.getInputStream());

                while (true) {
                    outputStream.writeObject("Hello");
                    outputStream.flush();

                    String message = (String) inputStream.readObject();
                    System.out.println(message + " received from peer [" + peerId + "]");
                    Thread.sleep(5000);
                }
            } catch (IOException e) {
                System.out.println("Disconnected with peer [" + peerId + "]");
            } catch (InterruptedException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
