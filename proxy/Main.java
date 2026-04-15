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
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream clientOut = clientSocket.getOutputStream();

            String line;
            StringBuilder request = new StringBuilder();

            String host = null;
            int port = 80;

            // Build Request
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                if (line.toLowerCase().startsWith("connection:")) {
                    line = "Connection: close";
                }
                if (line.toLowerCase().startsWith("proxy-connection:")) {
                    continue; // remove it
                }

                System.out.println(line);
                request.append(line).append("\r\n");

                if (line.toLowerCase().startsWith("host:")) {
                    host = line.split(" ")[1];
                    System.out.println("[+]Host found: [" + host + "]");
                }
            }
            request.append("\r\n");
            
            if (host == null) {
                System.out.println("[-]No host found");
                clientSocket.close();
                return;
            }

            // Send Request to Target Server
            System.out.println("[!]Attempting to connect to target server: " + host + ":" + port);
            Socket serverSocket = new Socket(host, port);
            System.out.println("[+]Connected to target server: " + host + ":" + port);

            OutputStream serverOut = serverSocket.getOutputStream();
            InputStream serverIn = serverSocket.getInputStream();

            serverOut.write(request.toString().getBytes());
            serverOut.flush();

            // Read Response from Target Server and Send Back to Client
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = serverIn.read(buffer)) != -1) {
                clientOut.write(buffer, 0, bytesRead);
            }
            clientOut.flush();

            serverSocket.close();
            clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}