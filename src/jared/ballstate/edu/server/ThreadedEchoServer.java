package Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

// Server class
public class ThreadedEchoServer {
    public static void main(String[] args)
    {
        ServerSocket server = null;

        try {

            // server is listening on port 1234
            server = new ServerSocket(1978);
            server.setReuseAddress(true);

            // running infinite loop for getting
            // client request
            while (true) {

                // socket object to receive incoming client
                // requests
                Socket client = server.accept();

                // Displaying that new client is connected
                // to server
                System.out.println("New client connected"
                        + client.getInetAddress()
                        .getHostAddress());

                // create a new thread object
                ClientHandler clientSock
                        = new ClientHandler(client);

                // This thread will handle the client
                // separately
                new Thread(clientSock).start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (server != null) {
                try {
                    server.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // ClientHandler class
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        // Constructor
        public ClientHandler(Socket socket)
        {
            this.clientSocket = socket;
        }

        public void run()
        {
            PrintWriter out = null;
            BufferedReader in = null;

            try {

                // get the outputstream of client
                out = new PrintWriter(
                        clientSocket.getOutputStream(), true);

                // get the inputstream of client
                in = new BufferedReader(
                        new InputStreamReader(
                                clientSocket.getInputStream()));
                final int MAX_Client_MESSAGE_LENGTH = 1024;
                ByteBuffer buffer = ByteBuffer.allocate(MAX_Client_MESSAGE_LENGTH);
                String line;
                while ((line = in.readLine()) != null) {

                    // writing the received message from
                    // client
                    System.out.printf(
                            " Sent from the client: %s\n",
                            line);
                    out.println(line);
                    buffer.flip();
                    //get the first character from the client message
                    char command = (char)buffer.get();
                    System.out.println("Command from client: "+ command);
                    switch (command){
                        case 'G':
                            //"Get": client wants to get the file
                            byte[] a = new byte[buffer.remaining()];
                            // copy the rest of the client message (i.e., the file name)
                            // to the byte array
                            buffer.get(a);
                            String fileName = new String(a);
                            File file = new File(fileName);
                            System.out.println("The requested File name is: " + file);
                            if (!file.exists() || file.isDirectory()) {
                                try {
                                    sendReplyCode(clientSocket, 'N');
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                System.out.println("The reply code is: N");
                            }else{
                                System.out.println("The reply code is Y");
                                try {
                                    sendReplyCode(clientSocket, 'Y');
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                //read contents of file
                                //here
                                BufferedReader br = null;
                                try {
                                    br = new BufferedReader(new
                                            FileReader(file));
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                                String cont = null;
                                while (true) {
                                    try {
                                        if (!((cont = br.readLine()) != null)) break;
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    //write contents of file to client
                                    cont = cont+"\n";
                                    try {
                                        clientSocket.write(ByteBuffer.wrap(line.getBytes()));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    //to here
                                }
                            }
                            try {
                                clientSocket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                        clientSocket.close();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private static void sendReplyCode (Socket clientSocket, char code) throws IOException{
        byte[] a = new byte[1];
        a[0] = (byte)code;
        ByteBuffer data = ByteBuffer.wrap(a);
        ((WritableByteChannel) clientSocket).write(data);
    }
}