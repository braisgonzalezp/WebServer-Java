package es.udc.redes.tutorial.tcp.server;
import java.net.*;
import java.io.*;

/** Thread that processes an echo server connection. */

public class ServerThread extends Thread {

  private final Socket socket;

  public ServerThread(Socket s) {
    this.socket=s;
  }


  public void run() {
    try {
      // Set the input channel
      BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      // Set the output channel
      PrintWriter salida =  new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
      // Receive the message from the client
      String mensaje = entrada.readLine();
      System.out.println("SERVER: Received " +  mensaje + " from " + socket.getLocalAddress()+ ":" + socket.getPort());
      // Sent the echo message to the client
      salida.println(mensaje);
      System.out.println("SERVER: Sending " +  mensaje + " from " + socket.getLocalAddress() + ":" + socket.getPort());
      // Close the streams
      salida.close();
      entrada.close();
     //Uncomment next catch clause after implementing the logic
     } catch (SocketTimeoutException e) {
      System.err.println("Nothing received in 300 secs");
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      } finally {
	// Close the socket
      try {
        socket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
