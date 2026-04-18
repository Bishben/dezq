package com.dezq;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static org.fusesource.jansi.Ansi.Color.CYAN;
import static org.fusesource.jansi.Ansi.Color.YELLOW;
import static org.fusesource.jansi.Ansi.ansi;
import org.fusesource.jansi.AnsiConsole;

public class Main {
    public static void main(String[] args) {
        AnsiConsole.systemInstall();
        int port = 8888;

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            // Use Jansi to make the startup look professional
            System.out.println(ansi().fg(CYAN).a(">> dezq Engine Listening on port: ").a(port).reset());

            while (true) {
                Socket clientSocket = serverSocket.accept();
                // SURGERY: Spawning a new thread so requests don't block each other
                new Thread(() -> handleClient(clientSocket)).start();
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
                    host = line.split(" ")[1].trim(); // Trim removes invisible spaces that crash connections
                    // Professional Yellow highlight for the target domain
                    System.out.println(ansi().fg(YELLOW).a("[TARGET] ").reset().a(host));
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