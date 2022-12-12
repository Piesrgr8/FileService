package Server;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

 */

public class EchoThread extends Thread {
    protected static Socket socket;
    private SocketChannel sc;

    public EchoThread(Socket clientSocket) {
        this.socket = clientSocket;
    }
    

    static class executableThread implements Runnable
    {
        private SocketChannel sc; 
        public void run()
        {
            InputStream inp = null;
            BufferedReader brinp = null;
            int port = 1978;
            ServerSocketChannel listenChannel = null;
            try {
                listenChannel = ServerSocketChannel.open();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                listenChannel.bind(new InetSocketAddress(port));
            } catch (IOException e) {
                e.printStackTrace();
            }
            final int MAX_Client_MESSAGE_LENGTH = 1024;

            try {
                inp = socket.getInputStream();
                brinp = new BufferedReader(new InputStreamReader(inp));
            } catch (IOException e) {
                return;
            }
            while(true){
                //accept() is a blocking call
                //it will return only when it receives a new
                //connection request from a client
                //accept() performs the three-way handshake
                //with the client before it returns
                SocketChannel serveChannel = null;
                try {
                    serveChannel = listenChannel.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteBuffer buffer = ByteBuffer.allocate(MAX_Client_MESSAGE_LENGTH);

                //ensures that we read the whole message
                while(true) {
                    try {
                        if (!(serveChannel.read((buffer)) >= 0)) break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ;
                }
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
                                sendReplyCode(serveChannel, 'N');
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            System.out.println("The reply code is: N");
                        }else{
                            System.out.println("The reply code is Y");
                            try {
                                sendReplyCode(serveChannel, 'Y');
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
                            String line = null;
                            while (true) {
                                try {
                                    if (!((line = br.readLine()) != null)) break;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                //write contents of file to client
                                line = line+"\n";
                                try {
                                    serveChannel.write(ByteBuffer.wrap(line.getBytes()));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                //to here
                            }
                        }
                        try {
                            serveChannel.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;

                    case 'D':
                        byte[] b = new byte[buffer.remaining()];
                        buffer.get(b);
                        String delFileName = new String(b);
                        File fileDel = new File(delFileName);
                        if (!fileDel.exists() || fileDel.isDirectory()) {
                            try {
                                sendReplyCode(serveChannel, 'N');
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else{
                            try {
                                sendReplyCode(serveChannel, 'Y');
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (fileDel.delete()){
                                System.out.println("Deleted the File: " + delFileName);
                            }
                            else{
                                System.out.println("Failed to Delete the File ");
                            }
                        }
                        try {
                            serveChannel.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;

                    case 'R':
                        byte[] rn = new byte[buffer.remaining()];
                        buffer.get(rn);
                        String seperateString = new String(rn);
                        String[] newString = seperateString.split("|");
                        String old = newString[0];
                        String newS = newString[1];

                        File rnmd = new File(old);

                    /*
                    Path source = Paths.get("C:\\Users\seand\\316 part 4\\src\\" + old);
                    Path target = Paths.get("C:\\Users\\seand\\316 part 4\\src\\" + newS);
                    try{

                        Files.move(source, target);

                        } catch (IOException e) {
                        e.printStackTrace();
                        }
                    */
                        File newlyNamedFile = new File(newS);

                        if (!rnmd.exists() || rnmd.isDirectory()) {
                            try {
                                sendReplyCode(serveChannel, 'N');
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            try {
                                sendReplyCode(serveChannel, 'Y');
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Path source = Paths.get("C:\\Users\\seand\\316 part 4\\"+rnmd);
                            try{
                                Files.move(source,source.resolveSibling(String.valueOf(newlyNamedFile)));

                            }catch(IOException e)
                            {
                                e.printStackTrace();
                            }
                            System.out.println("File was renamed to " + newlyNamedFile);
                        }
                        try {
                            serveChannel.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        break;


                    case 'Q':
                        byte[] e = new byte[buffer.remaining()];
                        buffer.get(e);
                        System.out.println("Thank you for using our client/server program!");
                        System.exit(0);
                        try {
                            serveChannel.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        break;


                    case 'L':
                        byte[] l = new byte[buffer.remaining()];
                        buffer.get(l);
                        File[] filesList = new File(":\\Users\\seand\\316 part 4").listFiles();
                        for (File f : filesList){
                            if (!f.isDirectory()){
                                System.out.println(f.getName());
                            }
                        }

                        try {
                            serveChannel.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        break;

                }
            }

        }
        public static void main(String[] args){
            // newSingleThreadExecutor creates a thread pool with a single thread.
            // If multiple threads are needed, use:
            //   newFixedThreadPool(numThreads)
            ExecutorService p = Executors.newFixedThreadPool(8);

            p.submit(new executableThread());
            System.out.println("Main thread submitted the task.");
            System.out.println("Main thread sleeps for 1 second.");
            try{
                Thread.sleep(1_000_000);
            }catch(Exception e){}
            p.shutdown();
            System.out.println("Main thread terminates.");
        }
    }
        private static void sendReplyCode (SocketChannel channel, char code) throws
                IOException{
            byte[] a = new byte[1];
            a[0] = (byte)code;
            ByteBuffer data = ByteBuffer.wrap(a);
            channel.write(data);
        }
        public void executableThread (SocketChannel socketChannel)
        {
            this.sc = socketChannel;
        }
    }
/*
    public void call() throws IOException {
        InputStream inp = null;
        BufferedReader brinp = null;
        int port = ThreadedEchoServer.PORT;
        ServerSocketChannel listenChannel = null;
        try {
            listenChannel = ServerSocketChannel.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            listenChannel.bind(new InetSocketAddress(port));
        } catch (IOException e) {
            e.printStackTrace();
        }
        final int MAX_Client_MESSAGE_LENGTH = 1024;

        try {
            inp = socket.getInputStream();
            brinp = new BufferedReader(new InputStreamReader(inp));
        } catch (IOException e) {
            return;
        }
        while(true){
            //accept() is a blocking call
            //it will return only when it receives a new
            //connection request from a client
            //accept() performs the three-way handshake
            //with the client before it returns
            SocketChannel serveChannel = listenChannel.accept();

            ByteBuffer buffer = ByteBuffer.allocate(MAX_Client_MESSAGE_LENGTH);

            //ensures that we read the whole message
            while(serveChannel.read((buffer)) >= 0);
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
                        sendReplyCode(serveChannel, 'N');
                        System.out.println("The reply code is: N");
                    }else{
                        System.out.println("The reply code is Y");
                        sendReplyCode(serveChannel, 'Y');
                        //read contents of file
                        //here
                        BufferedReader br = new BufferedReader(new
                                FileReader(file));
                        String line;
                        while ((line = br.readLine()) != null) {
                            //write contents of file to client
                            line = line+"\n";
                            serveChannel.write(ByteBuffer.wrap(line.getBytes()));
                            //to here
                        }
                    }
                    serveChannel.close();
                    break;


                case 'D':
                    byte[] b = new byte[buffer.remaining()];
                    buffer.get(b);
                    String delFileName = new String(b);
                    File fileDel = new File(delFileName);
                    if (!fileDel.exists() || fileDel.isDirectory()) {
                        sendReplyCode(serveChannel, 'N');
                    }
                    else{
                        sendReplyCode(serveChannel, 'Y');
                        if (fileDel.delete()){
                            System.out.println("Deleted the File: " + delFileName);
                        }
                        else{
                            System.out.println("Failed to Delete the File ");
                        }
                    }
                    serveChannel.close();
                    break;

                case 'R':
                    byte[] rn = new byte[buffer.remaining()];
                    buffer.get(rn);
                    String seperateString = new String(rn);
                    String[] newString = seperateString.split("|");
                    String old = newString[0];
                    String newS = newString[1];

                    File rnmd = new File(old);

                    
                    Path source = Paths.get("C:\\Users\seand\\316 part 4\\src\\" + old);
                    Path target = Paths.get("C:\\Users\\seand\\316 part 4\\src\\" + newS);
                    try{

                        Files.move(source, target);

                        } catch (IOException e) {
                        e.printStackTrace();
                        }



                 
                    File newlyNamedFile = new File(newS);

                    if (!rnmd.exists() || rnmd.isDirectory()) {
                        sendReplyCode(serveChannel, 'N');
                    }
                    else {
                        sendReplyCode(serveChannel, 'Y');
                        Path source = Paths.get("C:\\Users\\seand\\316 part 4\\"+rnmd);
                        try{
                            Files.move(source,source.resolveSibling(String.valueOf(newlyNamedFile)));

                        }catch(IOException e)
                        {

                        }
                        System.out.println("File was renamed to " + newlyNamedFile);
                    }
                    serveChannel.close();

                    break;


                case 'Q':
                    byte[] e = new byte[buffer.remaining()];
                    buffer.get(e);
                    System.out.println("Thank you for using our client/server program!");
                    System.exit(0);
                    serveChannel.close();
                    break;


                case 'L':
                    byte[] l = new byte[buffer.remaining()];
                    buffer.get(l);
                    File[] filesList = new File(":\\Users\\seand\\316 part 4").listFiles();
                    for (File f : filesList){
                        if (!f.isDirectory()){
                            System.out.println(f.getName());
                        }
                    }

                    serveChannel.close();
                    break;

            }
        }

    }
*/
    /*
    private static void sendReplyCode (SocketChannel channel, char code) throws
            IOException{
        byte[] a = new byte[1];
        a[0] = (byte)code;
        ByteBuffer data = ByteBuffer.wrap(a);
        channel.write(data);
    }
    */







