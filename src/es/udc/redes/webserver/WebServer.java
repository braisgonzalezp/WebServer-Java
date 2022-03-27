package es.udc.redes.webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;


public class WebServer {

    static int PORT;
    static String DIRECTORY;

    public static void main(String[] argv) {

        ServerSocket socketServer = null;

        try {
            PORT = Integer.parseInt(argv[0]);
            DIRECTORY = "p1-files/";

            socketServer = new ServerSocket(PORT);
            socketServer.setSoTimeout(300000);
            while (true) {
                Socket socketClient = socketServer.accept();
                ServerThread serverThread = new ServerThread(socketClient, DIRECTORY);
                serverThread.start();
            }
        } catch (SocketTimeoutException e) {
            System.err.println("Nothing received in 300 secs ");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            try {
                if (socketServer != null) {
                    socketServer.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}