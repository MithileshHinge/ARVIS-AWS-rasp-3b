/*package pi3_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;

public class ExchangeVideo extends Thread{
	
	static ServerSocket ssVideo_sys, ssVideo_mob;
	static Socket socketVideo_sys, socketVideo_mob;
	
	public static String filename;
	private ServerSocketChannel listener = null;
	String OutputFileName = "/home/ubuntu/videos/";
	private int beginIndex = OutputFileName.length();
	
	public static boolean startVdoServer_sys = false;
	
	public ExchangeVideo(){
		new File("/home/ubuntu/videos").mkdir();
		try {
			ssVideo_mob = new ServerSocket(Main.PORT_NOTIF_VIDEO_MOB);
			ssVideo_mob.setSoTimeout(0);
			ssVideo_sys = new ServerSocket(Main.PORT_NOTIF_VIDEO_SYS);
			ssVideo_sys.setSoTimeout(0);
			
			InetSocketAddress listenAddr = new InetSocketAddress(Main.PORT_NOTIF_VIDEO_MOB);
			//listener = ssVideo_sys.getChannel();
			//listener = ServerSocketChannel.open();
			//ServerSocket ssVdo = listener.socket();
			//ssVideo_sys.setReuseAddress(true);
			//ssVideo_sys.bind(listenAddr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public void run(){
		//while(true){
			try {
				// Forwarding Video Notification ID from mob to sys
				ServerSocketChannel sc_sys = ServerSocketChannel.open();
				//sc_sys.bind(new InetSocketAddress(Main.PORT_NOTIF_VIDEO_SYS));
				SocketChannel clientChannel_sys = sc_sys.accept();
				socketVideo_sys = clientChannel_sys.socket();
				socketVideo_sys.setReuseAddress(true);
				socketVideo_sys.bind(new InetSocketAddress(Main.PORT_NOTIF_VIDEO_SYS));
				
				
				//socketVideo_sys = ssVideo_sys.accept();
				System.out.println("status of listener = "+listener);
				startVdoServer_sys = true;
				SocketChannel sc = listener.accept();
				socketVideo_sys = ssVideo_sys.accept();
				SocketChannel sc = socketVideo_sys.getChannel();
				//sc.configureBlocking(true);
				//Socket socketVideo_sys = sc.socket();
				
				InetSocketAddress listenAddr = new InetSocketAddress(Main.PORT_NOTIF_VIDEO_MOB);
				listener = ssVideo_sys.getChannel();
				ssVideo_sys.setReuseAddress(true);
				ssVideo_sys.bind(listenAddr);
				
				System.out.println("...tcp for video xchange from sys side accepted");
				
				ServerSocketChannel sc_mob = ServerSocketChannel.open();
				sc_mob.bind(new InetSocketAddress(Main.PORT_NOTIF_VIDEO_SYS));
				SocketChannel clientChannel_mob = sc_mob.accept();
				//socketVideo_mob = ssVideo_mob.accept();
				System.out.println("...tcp for video xchange from mob side accepted");		
				
				socketVideo_mob = clientChannel_mob.socket();
				
				
				InputStream inVdo_mob = socketVideo_mob.getInputStream();
                DataInputStream dInVdo_mob = new DataInputStream(inVdo_mob);
                OutputStream outVdo_mob = socketVideo_mob.getOutputStream();
                DataOutputStream dOutVdo_mob = new DataOutputStream(outVdo_mob);
                
				int vdoNotifId = dInVdo_mob.readInt();
				
				InputStream inVdo_sys = socketVideo_sys.getInputStream();
                DataInputStream dInVdo_sys = new DataInputStream(inVdo_sys);
                OutputStream outVdo_sys = socketVideo_sys.getOutputStream();
                DataOutputStream dOutVdo_sys = new DataOutputStream(outVdo_sys);
                
				dOutVdo_sys.writeInt(vdoNotifId);
				dOutVdo_sys.flush();
				
				
				
				// Receiving video from sys
				//SocketChannel sc_in = socketVideo_sys.getChannel();
				int filenameSize = dInVdo_sys.readInt();
                byte[] filenameInBytes = new byte[filenameSize];
                outVdo_sys.write(1);
                outVdo_sys.flush();
                inVdo_sys.read(filenameInBytes);
                filename = new String(filenameInBytes);
                System.out.println("FILENAME RECIEVED :" + filename);
                outVdo_sys.write(1);
                outVdo_sys.flush();
                
                final String filepath = Paths.get("/home/ubuntu/videos") + "/" + filename;
                FileOutputStream fileOut = new FileOutputStream(filepath);
                byte[] buffer = new byte[16 * 1024];
                int count;
                System.out.println("Receiving video");
                while ((count = inVdo_sys.read(buffer)) > 0) {
                	System.out.println("receiving video");
                    fileOut.write(buffer, 0, count);
                }
                if(filename != null)
                	receiveVideo(clientChannel_sys,filepath);
                fileOut.close();
                System.out.println("...video received...");
                socketVideo_sys.close();
                
				// Sending video to mob
                try {
    				InputStream sIn = s.getInputStream();
    				DataInputStream dIn = new DataInputStream(sIn);
    				OutputStream sOut = s.getOutputStream();
                	SocketChannel sc_out = socketVideo_mob.getChannel();
    				//int notifId = dInVdo_mob.readInt();
    				//System.out.println("Sending Video value of notifId is " + notifId);
    				String filename = filepath.substring(beginIndex);
    				//DataOutputStream dOut = new DataOutputStream(sOut);
    				dOutVdo_mob.writeInt(filename.length());
    				dOutVdo_mob.flush();
    				System.out.println("...video filename flushed to app...");
    				inVdo_mob.read();
    				outVdo_mob.write(filename.getBytes());
    				outVdo_mob.flush();
    				inVdo_mob.read();
    				if (filepath != null){
    					sendVideo(sc_out, filepath);
    				}
    				System.out.println("...video flushed to app...");
    				socketVideo_mob.close();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		//}		
	}

	private void sendVideo(SocketChannel sc, String filepath) {
		try {
			FileInputStream fis = new FileInputStream(filepath);
			FileChannel fc = fis.getChannel();
			fc.transferTo(0, fc.size(), sc);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void receiveVideo(SocketChannel sc, String filepath) {
		try {
			FileOutputStream fos = new FileOutputStream(filepath);
			FileChannel fc = fos.getChannel();
			fc.transferFrom(sc, 0, fc.size());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
*/

package pi3_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ExchangeVideo extends Thread{
	
	static ServerSocket ssVdo_sys, ssVdo_mob;
	static Socket socketVideo_sys, socketVideo_mob;
	
	public static String filename;
	private ServerSocketChannel listener_sys = null, listener_mob = null;
	String OutputFileName = "/home/ubuntu/videos/";
	//private int beginIndex = OutputFileName.length();
	
	public static boolean startVdoServer_sys = false;
	private FileInputStream fis;
	
	public ExchangeVideo(){
		try {
			//System side
			InetSocketAddress listenAddr_sys = new InetSocketAddress(Main.PORT_NOTIF_VIDEO_SYS);
			listener_sys = ServerSocketChannel.open();
			ssVdo_sys = listener_sys.socket();
			ssVdo_sys.setReuseAddress(true);
			ssVdo_sys.bind(listenAddr_sys);
			
			//Mob side
			InetSocketAddress listenAddr_mob = new InetSocketAddress(Main.PORT_NOTIF_VIDEO_MOB);
			listener_mob = ServerSocketChannel.open();
			ssVdo_mob = listener_mob.socket();
			ssVdo_mob.setSoTimeout(0);
			ssVdo_mob.setReuseAddress(true);
			ssVdo_mob.bind(listenAddr_mob);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run(){
		while(true){
			try {
				// Connection Building
				SocketChannel sc_sys = listener_sys.accept();
				sc_sys.configureBlocking(true);
				Socket s_sys = sc_sys.socket();
				InputStream sIn_sys = s_sys.getInputStream();
				DataInputStream dIn_sys = new DataInputStream(sIn_sys);
				OutputStream sOut_sys = s_sys.getOutputStream();
				DataOutputStream dOut_sys = new DataOutputStream(sOut_sys);
				System.out.println("......sys connection established for vdo.....");
				
				SocketChannel sc_mob = listener_mob.accept();
				sc_mob.configureBlocking(true);
				Socket s_mob = sc_mob.socket();
				InputStream sIn_mob = s_mob.getInputStream();
				DataInputStream dIn_mob = new DataInputStream(sIn_mob);
				OutputStream sOut_mob = s_mob.getOutputStream();
				DataOutputStream dOut_mob = new DataOutputStream(sOut_mob);
				System.out.println("......mob connection established for vdo.....");
				
				// Forwarding Notif ID
				int notifId = dIn_mob.readInt();
				System.out.println("......notif ID received.....");
				dOut_sys.writeInt(notifId);
				System.out.println("......notif ID exchanged.....");
				
				// Receive video from system
				int filenameSize = dIn_sys.readInt();
                byte[] filenameInBytes = new byte[filenameSize];
                sOut_sys.write(1);
                sOut_sys.flush();
                System.out.println("......read filename size from sys.....");
                
                sIn_sys.read(filenameInBytes);
                filename = new String(filenameInBytes);
                System.out.println("FILENAME RECIEVED :" + filename);
                sOut_sys.write(1);
                sOut_sys.flush();
                System.out.println("......read filename from system.....");
                
                new File("/home/ubuntu/videos").mkdir();
                String filepath = "/home/ubuntu/videos/" + filename;
                FileOutputStream fileOut = new FileOutputStream(filepath);
                //FileOutputStream fileOut = openFileOutput(filename, MODE_PRIVATE);
                byte[] buffer = new byte[16 * 1024];
                int count;
                System.out.println("......starting to receive vdo.....");
                
                while ((count = sIn_sys.read(buffer)) > 0) {
                    fileOut.write(buffer, 0, count);
                    System.out.println("......receiving video.....");
                }
                fileOut.close();
                s_sys.close();
                System.out.println("......sys connectn closed.....");
                
                // Send video to app
                dOut_mob.writeInt(filename.length());
                dOut_mob.flush();
                sIn_mob.read();
                System.out.println(".....filename length flushed......");
                
                sOut_mob.write(filename.getBytes());
                sOut_mob.flush();
                sIn_mob.read();
                System.out.println("......filename flushed.....");
                
                if(filename != null){
                	int count1;
                	fis = new FileInputStream(filepath);
                	byte[] bytes = new byte[16*1024];
                	System.out.println("......starting to send vdo.....");
                	
                	while((count1 = fis.read(bytes)) > 0){
                		sOut_mob.write(bytes, 0, count1);
                		System.out.println("......sending video.....");
                	}
                }
                s_mob.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}