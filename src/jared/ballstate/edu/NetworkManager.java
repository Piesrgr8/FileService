package jared.ballstate.edu;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NetworkManager {
	
	private SocketChannel sa;
	
	public NetworkManager(String ip, int port) {
		try {
			init(ip, port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void init(String ip, int port) throws IOException {
		sa = SocketChannel.open();
		
		try {
			sa.connect(new InetSocketAddress(ip, port));
			System.out.println("Connected!");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void download(String fileName, String path) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(path+fileName, true));
        ByteBuffer data = ByteBuffer.allocate(1024);
		int bytesRead;
		while ((bytesRead = sa.read(data)) != -1) {
			data.flip();
			byte[] a = new byte[bytesRead];
			data.get(a);
			String serverMsg = new String(a);
			bw.write(serverMsg);
			data.clear();
		}
		bw.close();
	}
	
	public SocketChannel getSocketChannel() {
		return sa;
	}
}
