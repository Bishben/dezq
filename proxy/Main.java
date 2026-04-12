import java.io.*;
import java.net.*;

public class Main {
    public static void main(String[] args) {
        int port = 8888;

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                handleClient(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void handleClient(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}