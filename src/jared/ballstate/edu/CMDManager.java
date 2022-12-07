package jared.ballstate.edu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CMDManager {
	
	public void renameFile(String path, String rename, NetworkManager nm) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(("R" + path + "|" + rename).getBytes());
        SocketChannel channel = nm.getSocketChannel();
        
        channel.write(buffer);
        channel.shutdownOutput();
        
        //receive server reply code
        if (getServerCode(channel) != 'Y') {
            System.out.println("Server failed to serve the request.");
        } else {
            System.out.println("The request was accepted");
        }
	}
	
	public void uploadFile(String path, NetworkManager nm) throws IOException {
		SocketChannel channel = nm.getSocketChannel();
		ByteBuffer buffer = ByteBuffer.wrap(("U"+path).getBytes());
		channel.write(buffer);
		File file = new File(path);
		BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            //write contents of file to client
            line = line+"\n";
            channel.write(ByteBuffer.wrap(line.getBytes()));
        }
        channel.shutdownOutput();
        br.close();
	}
	
	public void downloadFile(String path, NetworkManager nm) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(("G" + path).getBytes());
        SocketChannel channel = nm.getSocketChannel();
        channel.write(buffer);
        //It's critical to shut down output on client side
        //when client is done sending to server
        channel.shutdownOutput();
        //receive server reply code
        if (getServerCode(channel) != 'Y') {
            System.out.println("Server failed to serve the request.");
        } else {
            System.out.println("The request was accepted");
            Files.createDirectories(Paths.get("./downloaded"));
            //make sure to set the "append" flag to true
            nm.download(path, "./downloaded/");
        }
	}
	
	public void deleteFile(String path, NetworkManager nm) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(("D" + path).getBytes());
		SocketChannel channel = nm.getSocketChannel();
		channel.write(buffer);
		channel.shutdownOutput();
        if (getServerCode(channel) != 'Y') {
            System.out.println("Server failed to serve the request.");
        } else {
            System.out.println("The request was accepted");
        }
	}
	
	public void listFiles(NetworkManager nm) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(("L").getBytes());
		SocketChannel channel = nm.getSocketChannel();
		channel.write(buffer);
		channel.shutdownOutput();
	}
	
	public void stopService(NetworkManager nm) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(("Q").getBytes());
		SocketChannel channel = nm.getSocketChannel();
		channel.write(buffer);
		channel.shutdownOutput();
	}
	
    private static char getServerCode(SocketChannel channel) throws IOException{
        ByteBuffer buffer = ByteBuffer.allocate(1);
        int bytesToRead = 1;
        //make sure we read the entire server reply
        while((bytesToRead -= channel.read(buffer)) > 0);
        //before reading from buffer, flip buffer
        buffer.flip();
        byte[] a = new byte[1];
        //copy bytes from buffer to array
        buffer.get(a);
        char serverReplyCode = new String(a).charAt(0);
        //System.out.println(serverReplyCode);
        return serverReplyCode;
    }
}
