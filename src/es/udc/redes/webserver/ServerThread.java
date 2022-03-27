package es.udc.redes.webserver;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

public class ServerThread extends Thread {

    private Codigos code;
    private Date date;
    private File file = null;
    private String dir_codes;
    private String dir_archives;

    private final SimpleDateFormat formatoFecha = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");

    private final Socket socket;
    private final String DIRECTORY;

    public ServerThread(Socket s, String d) {
        this.socket = s;
        this.DIRECTORY = d;
    }


    @Override
    public void run() {

        dir_codes = DIRECTORY + "codes";//ruta carpeta de errores
        dir_archives = DIRECTORY + "archivos";//ruta carpeta de archivos

        BufferedReader reader = null;
        PrintWriter writer = null;
        BufferedOutputStream dataOut = null;
        String fileRequest = null;

        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));//recibido
            writer = new PrintWriter(socket.getOutputStream());//enviado(text cabecera)
            dataOut = new BufferedOutputStream(socket.getOutputStream());//enviado(archivos)

            String msg = reader.readLine();//mensaje de solicitud cliente
            String header = null;
            String aux = reader.readLine();


            if (aux != null) {//CADA VEZ QUE SE PROCESA UNA PETICION DEL CLIENTE SE COMPRUEBA SI ES if-modified-since
                while (!aux.equals("")) {
                    if (aux.contains("If-Modified-Since")) {
                        header = aux;
                        break;
                    } else {
                        aux = reader.readLine();
                    }
                }
            }


            if (msg != null) {
                StringTokenizer parse = new StringTokenizer(msg);
                String method = parse.nextToken().toUpperCase();//si es get o head
                fileRequest = parse.nextToken().toLowerCase();//archivo que pide el cliente

                if (!method.equals("GET") && !method.equals("HEAD")) {
                    code = Codigos.BAD_REQUEST;
                    printHeader(writer, dataOut, "", fileRequest);
                }
                if (method.equals("GET")) {
                    if (header != null && header.contains("If-Modified-Since")) {//Comprobamos si es modifiedsince
                        File reload = new File(dir_archives + fileRequest);
                        String stringIfMod = header.substring(19);
                        Date ifModDate = parseDate(stringIfMod);//Fecha de solicitud del archivo
                        Date lastDate = parseDate(formatDate((int) reload.lastModified()));//ultima fecha de modificaciom
                        if (ifModDate.after(lastDate) || ifModDate.equals(lastDate)) {//si la fecha de solicitud es igual o o currio despues de la ultima fecha de modificacion no esta modificada.
                            code = Codigos.NOT_MODIFIED;
                            printHeader(writer, dataOut, "", fileRequest);
                        } else {
                            printHeader(writer, dataOut, method, fileRequest);
                        }
                    } else {
                    printHeader(writer, dataOut, method, fileRequest);
                }
                }
                if (method.equals("HEAD")) {
                    printHeader(writer, dataOut, method, fileRequest);
                }
            }
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
        } catch (SocketTimeoutException e) {
            System.err.println("Nothing received in 300 secs");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (writer != null) {
                    writer.close();
                }
                if (dataOut != null) {
                    dataOut.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void printHeader(PrintWriter printer, OutputStream dataOut, String metodo, String content) throws IOException {
        File requested = new File(dir_archives + "/" + content);//archivo solicitado por el server

        if (code == Codigos.NOT_IMPLEMENTED || code == Codigos.NOT_MODIFIED || code == Codigos.BAD_REQUEST) {
            switch (code) {
                case NOT_IMPLEMENTED:
                    file = new File(dir_codes + "/error501.html");
                    break;
                case NOT_MODIFIED:
                    file = new File(dir_codes + "/error304.html");
                    break;
                case BAD_REQUEST:
                    file = new File(dir_codes + "/error400.html");
                    break;

            }
        } else {
            if (requested.exists()) {//si el archivo que me pidio el cliente existe lo creo
                code = Codigos.OK;
                file = new File(dir_archives + content);
            } else {//si no creo un archivo html del error correspondiente
                code = Codigos.NOT_FOUND;
                file = new File(dir_codes + "/error404.html");
            }
        }

        date = new Date();//fecha
        int length = (int) file.length();//tamaño archivo
        byte[] data = readerData(file, length);//archivo solicitado por el cliente
        //CABECERA DE RESPUESTA
        printer.println("HTTP/1.0 " + code.getEstado());
        printer.println("Date: " + date);
        printer.println("Server: WebServer_75A");
        printer.println("Last-Modified: " + formatDate((int) file.lastModified()));
        printer.println("Content-Length: " + file.length());
        printer.println("Content-Type: " + getType(file));
        printer.println("");
        printer.flush();


        //Envio respuesta
        if(code==Codigos.NOT_FOUND && metodo.equals("HEAD")) {
            dataOut.write(data, 0, 0);

        }else {
            if (code == Codigos.OK) {

                switch (metodo) {
                    case "GET":
                        dataOut.write(data, 0, length);
                        break;
                    case "HEAD":
                        break;
                    default:
                        break;
                }
            } else {
                dataOut.write(data, 0, length);
            }
        }
        dataOut.flush();
    }




    private String formatDate(int seconds) { //Da formato a la fecha
        return formatoFecha.format(seconds);
    }

    private Date parseDate(String fecha) {//Cambia de tipo String a fecha
        Date parsedDate = null;
        try {
            parsedDate = formatoFecha.parse(fecha);
        } catch (ParseException ex) {
            System.out.println(ex);
        }
        return parsedDate;
    }


    private byte[] readerData(File file, int lentgh) throws IOException {//le le los archivos para despues mandarlos
        FileInputStream input = null;
        byte[] data = new byte[lentgh];

        try {
            input = new FileInputStream(file);
            input.read(data);
        } finally {
            if (input != null) {
                input.close();
            }
        }
        return data;
    }

    private String getType(File fileRequest) {//Identifica el tipo de archivo
        String requested = fileRequest.getName();
        String type = "application/octet-stream";

        if (requested.endsWith(".htm") || requested.endsWith(".html")) {
            type = "text/html";
        }
        if (requested.endsWith(".log") || requested.endsWith(".txt")) {
            type = "text/plain";
        }
        if (requested.endsWith(".gif")) {
            type = "image/gif";
        }
        if (requested.endsWith(".png")) {
            type = "image/png";
        }
        return type;
    }

}
