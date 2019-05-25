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
import java.util.Date;
import java.util.concurrent.CountDownLatch;

class ConnectMobThread extends Thread{
	private Socket connMobSock;
	
	ConnectSysThread connSysThread;
	public Socket sysMessageSock, mobMessageSock;
	
	ConnectMobThread(Socket connMobSock){
		this.connMobSock = connMobSock;
	}
	
	public void run(){
		InputStream inMob;
		String hashID = null;
		try {
			inMob = connMobSock.getInputStream();
			DataInputStream dInMob = new DataInputStream(inMob);
			OutputStream outMob = connMobSock.getOutputStream();
			DataOutputStream dOutMob = new DataOutputStream(outMob);
			
			hashID = dInMob.readUTF();
			Date date = new Date();
			System.out.println(Main.ft.format(date)+"	hash id from mob = "+hashID);
			if (Main.db.checkRegistered(hashID)){
				outMob.write(1);  //Registered
				outMob.flush();
				for (int i=0; i<5; i++){    // No of login attempts
					String username = dInMob.readUTF();
					String password = dInMob.readUTF();
					
					if(Main.db.verifyUser(username, password, hashID)){
						outMob.write(2);  //Verified
						outMob.flush();
						break;
					}else{
						System.out.println("VERIFICATION FAILED ON MOBILE SIDE");
						if (i==4){
							outMob.write(3); //End of max attempts
							outMob.flush();
							connMobSock.close();
							return;
						}else{
							outMob.write(4); //Incorrect
							outMob.flush();
						}
					}
				}
				
				connSysThread = Main.connSysThreadsMap.get(hashID);
				if (connSysThread == null){
					outMob.write(8); // System is offline
					outMob.flush();
					connMobSock.close();
					return;
				}
			}else {
				outMob.write(5);  // Not registered
				outMob.flush();
				
				String username = dInMob.readUTF();
				String password = dInMob.readUTF();
				String fcm_token = dInMob.readUTF();
				String emailId = dInMob.readUTF();
				if (Main.db.registerUser(username, password, hashID, emailId)){
					outMob.write(6); // Registration successful
					outMob.flush();
				}else {
					System.out.println("USER REGISTRATION FAILED");
					outMob.write(7); // Registration failed
					outMob.flush();
					connMobSock.close();
					return;
				}
				
				connSysThread = Main.connSysThreadsMap.get(hashID);
				if (connSysThread == null){
					outMob.write(8); // System is offline
					outMob.flush();
					connMobSock.close();
					return;
				}
				
				connSysThread.username = username;
				connSysThread.password = password;
				connSysThread.fcm_token = fcm_token;
				connSysThread.emailId = emailId;
				
				connSysThread.latch.countDown();
			}
			
			/*MergeThread mergeThread = mergeThreadsMap.get(hashID);
			if (mergeThread == null){
				outMob.write(7); // System is offline
				outMob.flush();
				return;
			}
			if (mergeThread.mobIP != null){
				outMob.write(8); // Another device already logged in
				outMob.flush();
				return;
			}
			mergeThread.mobIP = connMobSock.getInetAddress();
			mergeThread.latch.countDown();*/
			
			InetAddress mobIP = connMobSock.getInetAddress();
			InetAddress sysIP = connSysThread.connSysSock.getInetAddress();
			
			while (Main.mobIP2sysIP.containsKey(mobIP)){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Main.mobIP2sysIP.put(mobIP, sysIP);
			System.out.println("ConnectMobThread MobIP : " + mobIP + " SysIP : " + sysIP);
			Main.sysIP2mobIP.put(sysIP, mobIP);
			
			//MessageThread messageThread = new MessageThread(sysMessageSock, mobMessageSock);
			//messageThread.start();
			
			outMob.write(9);  // Connection successful
			
			while (connSysThread.sysLocalIP == null){
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			dOutMob.writeUTF(connSysThread.sysLocalIP);
			dOutMob.flush();
			
			connMobSock.setSoTimeout(12000);
			int i = 0;
			while(true){
				try {
					outMob.write(1);
					outMob.flush();
					int p = inMob.read();
					if (p == -1) break;
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (SocketTimeoutException s){
					s.printStackTrace();
				} catch (IOException f){
					f.printStackTrace();
					i++;
					System.out.println("Connect Mob thread disconnect " + i);
					if(i == 3)	break;
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Date date = new Date();
		System.out.println(Main.ft.format(date)+"Mobile disconnected ");
		InetAddress mobIP = connMobSock.getInetAddress();
		System.out.println("IN ConnectMobThread mobIP : " + mobIP);
		if (mobIP != null){
			
			InetAddress sysIP = Main.mobIP2sysIP.get(mobIP);
			System.out.print("sysIP : " + sysIP);
			if (sysIP != null){
				Main.sysIP2mobIP.remove(sysIP);
				//ExchangeFrame.sysIP2MobUdpPortMap.remove(sysIP);
				//ExchangeAudio.mobIP2SysAudioUdpPortMap.remove(mobIP);
			}	
			if(hashID != null){
				InetSocketAddress sysUDP = Main.hashID2SysUDPPortMap.get(hashID);
				Main.hashID2SysUDPPortMap.remove(hashID);
				
				if(sysUDP != null){
					InetAddress mobUDPIP = Main.sysUDPIP2mobUDPIPMap.get(sysUDP.getAddress());
					Main.mobUDPIP2hashIDMap.remove(mobUDPIP);
					Main.mobUDPIP2sysUDPPortMap.remove(mobUDPIP);
					Main.sysUDPIP2mobUDPIPMap.remove(sysUDP.getAddress());
				}
				/*InetSocketAddress mobUDP = Main.hashID2MobUDPMap.get(hashID);
				Main.hashID2MobUDPMap.remove(hashID);
				if(mobUDP != null){
					InetAddress sysUDPIP = Main.mobUDPIP2sysUDPIPPortMap.get(mobUDP.getAddress());
					Main.mobUDPIP2sysUDPIPPortMap.remove(mobUDP.getAddress());
					Main.mobUDPIP2sysUDPPortMap.remove(mobUDP.getAddress());
					if(sysUDPIP != null){
						Main.sysUDPIP2mobUDPPortMap.remove(sysUDPIP);
						Main.sysUDPIP2hashIDMap.remove(sysUDPIP);
						Main.sysUDPIP2mobUDPListenPortMap.remove(sysUDPIP);
					}
				}*/
			}
			Main.mobIP2sysIP.remove(mobIP);
		}
		try {
			connMobSock.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}
}
