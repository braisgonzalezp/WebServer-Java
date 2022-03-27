package es.udc.redes.tutorial.udp.server;

import java.net.*;

/**
 * Implements a UDP echo server.
 */
public class UdpServer {

    public static void main(String argv[]) {
        byte[] buffer = new byte[1024];

        if (argv.length != 1) {
            System.err.println("Format: es.udc.redes.tutorial.udp.server.UdpServer <port_number>");
            System.exit(-1);
        }
        DatagramSocket socketUDP = null;
        try {
            // Create a server socket
            socketUDP = new DatagramSocket(Integer.parseInt(argv[0]));
            // Set maximum timeout to 300 secs
            socketUDP.setSoTimeout(300000);
            while (true) {
                // Prepare datagram for reception
                DatagramPacket peticion = new DatagramPacket(buffer, buffer.length);
                // Receive the message
                socketUDP.receive(peticion);
                String mensaje = new String(peticion.getData());
                int puertoCliente = peticion.getPort();
                InetAddress direccion = peticion.getAddress();
                System.out.println("SERVER: Received :" +  mensaje + " from /" + peticion.getAddress() + ":" + puertoCliente);
                // Prepare datagram to send response
                buffer = mensaje.getBytes();
                DatagramPacket respuesta = new DatagramPacket(buffer, buffer.length,direccion,puertoCliente);
                
                // Send response
                socketUDP.send(respuesta);
                System.out.println("SERVER: Sending :" +  mensaje + " from /" + peticion.getAddress() + ":" + puertoCliente);
            }
        // Uncomment next catch clause after implementing the logic
        } catch (SocketTimeoutException e) {
           System.err.println("No requests received in 300 secs ");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close the socket
            socketUDP.close();

        }

    }
}
