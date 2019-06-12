package pi3_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UTFDataFormatException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

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
	        
	        	//int size = dInSys.readInt();
	        	//System.out.println("...Frame size... ");
	        	//byte[] b = new byte[size];
	        	/*byte[] b = new byte[15000];
	        	dInSys.readFully(b);*/
	        	/*while(true){
        		try{
        			dInSys.readFully(b, 0, size);
        			break;
        		}catch(SocketTimeoutException s){
        			System.out.println(".");
        			s.printStackTrace();
        		}catch(EOFException f){
        			System.out.println("..");
        			f.printStackTrace();
        		}catch(IOException e){
        			System.out.println("...");
        			e.printStackTrace();
        			break;
        		}
	        	}*/
	        	/*System.out.println("...Frame received from system... ");
	        	dOutMob.write(b);
	    		dOutMob.flush();
	    		System.out.println("...Frame forwarded to mob... ");*/
	        	/*if(b != null){
	        		dOutMob.writeInt(size);
	        		dOutMob.flush();
	        		dOutMob.write(b);
	        		dOutMob.flush();
	        		System.out.println("...Frame forwarded to mob... "+size);
	        	}*/
			String frame = null;
			try{	
	        	System.out.println("...Frame receiving from system... ");
	        	frame = dInSys.readUTF();
	        	byte[] frameInBuf = frame.getBytes(StandardCharsets.UTF_8);
	        	//System.out.println("...Frame ... " + " " + frameInBuf[0] + " " + frameInBuf[1] + " " + frameInBuf[2] + " " + frameInBuf[3] + " " + frameInBuf[4] + " " + frameInBuf[5] + " " + frameInBuf[6] + " " + frameInBuf[7] + " " + frameInBuf[8] + " " + frameInBuf[9] + " " + frameInBuf[10]);
			} catch (IOException e){
	        	System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>BREAK FROM SYSTEM");
	        	e.printStackTrace();
	        	i++;
	        	if(i == 3) break;
	        }
	        try{	
	        	if(frame != null){
	        		dOutMob.writeUTF(frame);
	        		dOutMob.flush();
	        		System.out.println("...Frame forwarded to mob... ");
	        	}
	        } catch (IOException e) {
				System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<BREAK FROM ANDROID");
				e.printStackTrace();

				InetSocketAddress sysUDP = Main.hashID2SysUDPPortMap.get(hashID);
				Main.hashID2SysUDPPortMap.remove(hashID);
				if(sysUDP != null){
					InetAddress mobUDPIP = Main.sysUDPIP2mobUDPIPMap.get(sysUDP.getAddress());
					Main.mobUDPIP2hashIDMap.remove(mobUDPIP);
					Main.mobUDPIP2sysUDPPortMap.remove(mobUDPIP);
					Main.sysUDPIP2mobUDPIPMap.remove(sysUDP.getAddress());
				}
				System.out.println("UDP IPs removed from SENDING AUDIO");
				InetSocketAddress mobUDP = Main.hashID2MobUDPPortMap.get(hashID);
				if(mobUDP!=null){
					InetAddress mobUDPIP = mobUDP.getAddress();
					Main.hashID2MobUDPPortMap.remove(hashID);			
					InetAddress sysUDPIP = Main.mobUDPIP2sysUDPIPMap.get(mobUDPIP);
					Main.sysUDPIP2hashIDMap.remove(sysUDPIP);
					Main.sysUDPIP2mobUDPPortMap.remove(sysUDPIP);
					Main.mobUDPIP2sysUDPIPMap.remove(mobUDPIP);	
				}
				System.out.println("UDP IPs removed from LISTEN");
				
				break;
			}
		}
		try {
			ssSys.close();
			ssMob.close();
			ServerSockThread.sysIP2LivefeedSockMap.remove(sysIP);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
