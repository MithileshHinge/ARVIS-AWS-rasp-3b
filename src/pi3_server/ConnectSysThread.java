package pi3_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

public class ConnectSysThread extends Thread{
	public Socket connSysSock;
	public String hashID, sysLocalIP;
	volatile public String username, password, fcm_token , emailId;
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
			Date date = new Date();
			System.out.println(Main.ft.format(date)+"	hash id from sys = "+ hashID);
			while(Main.connSysThreadsMap.containsKey(hashID)){
				if (Main.connSysThreadsMap.get(hashID).latch.getCount() > 0)
					Main.connSysThreadsMap.get(hashID).latch.countDown();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			outSys.write(0);
			outSys.flush();
			if (Main.db.checkRegistered(hashID)){
				String username = dinSys.readUTF();
				String password = dinSys.readUTF();
				
				if (Main.db.verifyUser(username, password, hashID)){
//					MergeThread mergeThread = new MergeThread();
//					mergeThread.sysIP = connSysSock.getInetAddress();
//					mergeThread.run();
//					mergeThreadsMap.put(hashID, mergeThread);	
					outSys.write(0);
					outSys.flush();
					Main.connSysThreadsMap.put(hashID, this);
					
					sysLocalIP = dinSys.readUTF();
					System.out.println("....System local IP = "+sysLocalIP);
					// Check if connection is alive every 10 seconds
					connSysSock.setSoTimeout(12000);
					while(true){
						outSys.write(1);
						outSys.flush();
						int p = inSys.read();
						if (p == -1) break;
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
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
				
				
				try {
					latch.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				doutSys.writeUTF(username);
				doutSys.writeUTF(password);
				doutSys.writeUTF(fcm_token);
				doutSys.writeUTF(emailId);
				System.out.println("FCM Token : " + fcm_token);
				System.out.println("..................emailId............. = " + emailId);
				
				sysLocalIP = dinSys.readUTF();
				
				// Check if connection is alive every 10 seconds
				connSysSock.setSoTimeout(12000);
				while(true){
					outSys.write(1);
					outSys.flush();
					int p = inSys.read();
					if (p == -1) break;
					try{
						Thread.sleep(10000);
					}catch (InterruptedException e){
						e.printStackTrace();
					}
				}
			}
		} catch (IOException | NullPointerException e) {
			e.printStackTrace();
		}
		
		Date date = new Date();
		System.out.println(Main.ft.format(date) + "System disconnected");
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
		
		
		// Notify mob that system is disconnected
		SendMail sendmail = new SendMail();
		sendmail.sendMailTo = Main.db.getEmail(hashID);
		sendmail.sendmail = true;
		sendmail.start();
		
		System.out.println("sendMailTo = "+sendmail.sendMailTo);
	}
}