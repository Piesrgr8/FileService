package jared.ballstate.edu.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

public class ServerTCP {
    private static final int MAX_Client_MESSAGE_LENGTH = 1024;
    public static void main(String[] args) throws IOException {
        if(args.length != 1){
            System.out.println("Usage: ServerTCP <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        ServerSocketChannel listenChannel =
                ServerSocketChannel.open();
        listenChannel.bind(new InetSocketAddress(port));
        while(true){
            //accept() is a blocking call
            //it will return only when it receives a new
            //connection request from a client
            //accept() performs the three-way handshake
            //with the client before it returns
            SocketChannel serveChannel =
                    listenChannel.accept();
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
                    String toBeRenamed = new String(rn);
                    File rnmd = new File(toBeRenamed);


                    byte[] nn = new byte[buffer.remaining()];
                    buffer.get(nn);
                    String newFile = new String(nn);
                    File newlyNamedFile = new File(newFile);


                    if (!rnmd.exists() || rnmd.isDirectory()) {
                        sendReplyCode(serveChannel, 'N');
                    }
                    else {
                        sendReplyCode(serveChannel, 'Y');
                        Path source = Paths.get("./files/"+rnmd);
                        try{
                            Files.move(source,source.resolveSibling(String.valueOf(newlyNamedFile)));

                        }catch(IOException e)
                        {

                        }
                        System.out.println("File was renamed to " + newlyNamedFile);
                    }
                    //a little unconventional, i can send a stack overflow article i based this on if you need
                    serveChannel.close();
                    break;

                case 'Q':
                    byte[] e = new byte[buffer.remaining()];
                    buffer.get(e);
                    System.out.println("Thank you for using our client/server program!");
                    System.exit(0);
                    serveChannel.close();
                    break;


                case 'U':
                    byte[] u = new byte[buffer.remaining()];
                    buffer.get(u);
                    sendReplyCode(serveChannel, 'Y');
                    String uploadedFileName = new String(u);
                    File uploadedFile = new File(uploadedFileName);
                    System.out.println("The requested File name is: " + uploadedFile);
                    Files.createDirectories(Paths.get("./uploaded"));
                    
            		BufferedWriter bw = new BufferedWriter(new FileWriter("./uploaded/"+uploadedFileName, true));
                    ByteBuffer data = ByteBuffer.allocate(1024);
            		int bytesRead;
            		while ((bytesRead = serveChannel.read(data)) != -1) {
            			data.flip();
            			byte[] c = new byte[bytesRead];
            			data.get(c);
            			String serverMsg = new String(c);
            			bw.write(serverMsg);
            			data.clear();
            		}
            		bw.close();
            		serveChannel.close();
            		break;
                    //you do the highlited portion of writing a new file
                    //check y or n if file exists

                case 'L':
                    byte[] l = new byte[buffer.remaining()];
                    buffer.get(l);
                    String[] pathnames;
                    File check = new File("./files");
                    pathnames = check.list();

                    for (String pathname : pathnames){
                        System.out.println(pathname);
                    }

                    serveChannel.close();
                    break;

            }
        }
    }
    private static void sendReplyCode (SocketChannel channel, char code) throws
            IOException{
        byte[] a = new byte[1];
        a[0] = (byte)code;
        ByteBuffer data = ByteBuffer.wrap(a);
        channel.write(data);
    }
}