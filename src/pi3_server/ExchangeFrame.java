package pi3_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ExchangeFrame extends Thread{
	
	private Socket ssSys, ssMob;
	private OutputStream outMob;
	private InputStream inSys;
	private DataInputStream dInSys;
	private DataOutputStream dOutMob;
	private String hashID;
	
	public ExchangeFrame(Socket ssSys, Socket ssMob, String hashID) throws IOException{
		this.ssSys = ssSys;
		this.ssMob = ssMob;	
		this.hashID = hashID;
	}
	
	public void run(){		
		System.out.println("ExchangeFrame Thread started....");
		InetAddress sysIP = ssSys.getInetAddress();
		
		 try {
			ExchangeListen exchgListen = new ExchangeListen();
			exchgListen.start();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			outMob = ssMob.getOutputStream();
			inSys = ssSys.getInputStream();
			ssSys.setSoTimeout(3000);
			dInSys = new DataInputStream(inSys);
			dOutMob = new DataOutputStream(outMob);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int i = 0;	 
		while(true){
	        try{	
	        	System.out.println("...Frame receiving from system... ");
	        	//BufferedImage image = ImageIO.read(inSys);
	        	int size = dInSys.readInt();
	        	byte[] b = new byte[size];
	        	dInSys.readFully(b, 0, size);
	        	System.out.println("...Frame received from system... ");
	        	if(b != null){
	        		dOutMob.writeInt(size);
	        		dOutMob.flush();
	        		dOutMob.write(b);
	        		dOutMob.flush();
	        		System.out.println("...Frame forwarded to mob... "+size);
	        	}
	        } catch (SocketTimeoutException t){
				t.printStackTrace();
				i++;
				if(i == 3)	break;
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
		try {
			ssSys.close();
			ssMob.close();
			ServerSockThread.sysIP2LivefeedSockMap.remove(sysIP);
			
			InetSocketAddress sysUDP = Main.hashID2SysUDPPortMap.get(hashID);
			Main.hashID2SysUDPPortMap.remove(hashID);
			
			if(sysUDP != null){
				InetAddress mobUDPIP = Main.sysUDPIP2mobUDPIPMap.get(sysUDP.getAddress());
				Main.mobUDPIP2hashIDMap.remove(mobUDPIP);
				Main.mobUDPIP2sysUDPPortMap.remove(mobUDPIP);
				Main.sysUDPIP2mobUDPIPMap.remove(sysUDP.getAddress());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
