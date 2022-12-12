package Server;

import java.io.IOException;
import java.util.Scanner;

public class FileClient {
	
	private static String serverIP;
	private static int serverPort;
	
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println("Usage: ClientTCP <server_ip> <server_port>");
			return;
		}
		
		serverIP = args[1];
		serverPort = Integer.parseInt(args[1]);
		
		NetworkManager nm = new NetworkManager(serverIP, serverPort);
		CMDManager mg = new CMDManager();
		Scanner sc = new Scanner(System.in);
		
		boolean life = true;
		
		while(life == true) {
			System.out.println("Whats your move?");
			char cmd = sc.nextLine().toUpperCase().charAt(0);
			switch (cmd) {
				case 'G':
					System.out.println("Enter the name of the file to download: ");
	                String fileName1 = sc.nextLine();
	                mg.downloadFile(fileName1, nm);
	                break;
				case 'D':
					System.out.println("Enter the name of the file to delete: ");
	                String fileName2 = sc.nextLine();
	                mg.deleteFile(fileName2, nm);
					break;
//				case 'U':
//					System.out.println("Enter the name of the file to upload: ");
//					String fileName3 = sc.nextLine();
//					mg.uploadFile(fileName3, nm);
//					break;
				case 'R':
					System.out.println("Enter the name of the file to rename: ");
					String fileName4 = sc.nextLine();
					String rename = sc.nextLine();
					mg.renameFile(fileName4, rename, nm);
					break;
				case 'L':
					System.out.println("Listing files...");
					mg.listFiles(nm);
					break;
				case 'Q':
					System.out.println("Goodnight!");
					mg.stopService(nm);
					life = false;
					break;
			}
		}
		
		nm.getSocketChannel().close();
		sc.close();
	}
}
