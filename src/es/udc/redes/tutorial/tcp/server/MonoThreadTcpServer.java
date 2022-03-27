package es.udc.redes.tutorial.tcp.server;

import java.net.*;
import java.io.*;

/**
 * MonoThread TCP echo server.
 */
public class MonoThreadTcpServer {

    public static void main(String argv[]) throws IOException {
        if (argv.length != 1) {
            System.err.println("Format: es.udc.redes.tutorial.tcp.server.MonoThreadTcpServer <port>");
            System.exit(-1);
        }
        ServerSocket TcpSocket = null;
        try {
            // Create a server socket
             TcpSocket = new ServerSocket(Integer.parseInt(argv[0]));
            // Set a timeout of 300 secs
            TcpSocket.setSoTimeout(300000);
            while (true) {
                // Wait for connections
                Socket socket = TcpSocket.accept();
                // Set the input channel
                BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // Set the output channel
                PrintWriter salida =  new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);

                // Receive the client message
                String mensaje = entrada.readLine();
                System.out.println("SERVER: Received " +  mensaje + " from " + socket.getLocalAddress()+ ":" + socket.getPort());

                // Send response to the client
                salida.println(mensaje);
                System.out.println("SERVER: Sending " +  mensaje + " from " + socket.getLocalAddress() + ":" + socket.getPort());

                // Close the streams
                socket.close();
                entrada.close();
                salida.close();
            }
        // Uncomment next catch clause after implementing the logic            
        } catch (SocketTimeoutException e) {
            System.err.println("Nothing received in 300 secs ");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
	        //Close the socket
            TcpSocket.close();
        }
    }
}
