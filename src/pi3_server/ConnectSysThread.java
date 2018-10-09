package pi3_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class ConnectSysThread extends Thread{
	public Socket connSysSock;
	public String hashID;
	volatile public String username, password;
	volatile public CountDownLatch latch = new CountDownLatch(1);
	
	ConnectSysThread(Socket connSysSock){
		this.connSysSock = connSysSock;
	}
	public void run(){
		try {
			InputStream inSys = connSysSock.getInputStream();
			DataInputStream dinSys = new DataInputStream(inSys);
			OutputStream outSys = connSysSock.getOutputStream();
			DataOutputStream doutSys = new DataOutputStream(outSys);
			
			hashID = dinSys.readUTF();
			
			if (Main.db.checkRegistered(hashID)){
				String username = dinSys.readUTF();
				String password = dinSys.readUTF();
				if (Main.db.verifyUser(username, password, hashID)){
//					MergeThread mergeThread = new MergeThread();
//					mergeThread.sysIP = connSysSock.getInetAddress();
//					mergeThread.run();
//					mergeThreadsMap.put(hashID, mergeThread);	
					
					Main.connSysThreadsMap.put(hashID, this);
					
					// Check if connection is alive every 10 seconds
					while(true){
						outSys.write(1);
						outSys.flush();
						inSys.read();
						Thread.sleep(10000);
					}
					
				}else {
					System.out.println("VERIFICATION FAILED ON SYSTEM SIDE: MALICIOUS ATTACK"); //TODO: Better way to notify developer
					connSysSock.close();
				}
			}else {
				Main.db.addSystem(hashID);
//				MergeThread mergeThread = new MergeThread();
//				mergeThread.sysIP = connSysSock.getInetAddress();
//				mergeThread.run();
//				mergeThreadsMap.put(hashID, mergeThread);
				
				Main.connSysThreadsMap.put(hashID, this);
				
				latch.await();
				
				doutSys.writeUTF(username);
				doutSys.writeUTF(password);
				
				// Check if connection is alive every 10 seconds
				
				while(true){
					outSys.write(1);
					outSys.flush();
					inSys.read();
					Thread.sleep(10000);
				}
			}
		} catch (IOException | InterruptedException e) {
			InetAddress sysIp = connSysSock.getInetAddress();
			if (sysIp != null){
				InetAddress mobIP = Main.sysIP2mobIP.get(sysIp);
				if (mobIP != null){
					Main.mobIP2sysIP.remove(mobIP);
				}
				Main.sysIP2mobIP.remove(sysIp);
				ServerSockThread.sysIP2MessageSockMap.remove(sysIp);
				ExchangeFrame.sysIP2MobUdpPortMap.remove(sysIp);
			}
			
			
			
			try {
				connSysSock.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if (hashID != null)
				Main.connSysThreadsMap.remove(hashID);
			e.printStackTrace();
		}
	}
}