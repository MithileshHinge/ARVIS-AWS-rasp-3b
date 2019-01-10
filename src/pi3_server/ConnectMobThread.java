package pi3_server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

class ConnectMobThread extends Thread{
	private Socket connMobSock;
	
	ConnectSysThread connSysThread;
	public CountDownLatch latch = new CountDownLatch(1);
	public Socket sysMessageSock, mobMessageSock;
	
	ConnectMobThread(Socket connMobSock){
		this.connMobSock = connMobSock;
	}
	
	public void run(){
		InputStream inMob;
		try {
			inMob = connMobSock.getInputStream();
			DataInputStream dInMob = new DataInputStream(inMob);
			OutputStream outMob = connMobSock.getOutputStream();
			
			String hashID = dInMob.readUTF();
			System.out.println("hash id from mob = "+hashID);
			if (Main.db.checkRegistered(hashID)){
				outMob.write(1);  //Registered
				outMob.flush();
				for (int i=0; i<5; i++){    // No of login attempts
					String username = dInMob.readUTF();
					String password = dInMob.readUTF();
					//TODO - receive fcm token from mob !
					
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
				ConnectSysThread.fcm_token = dInMob.readUTF();
				ConnectSysThread.emailId = dInMob.readUTF();
						
				if (Main.db.registerUser(username, password, hashID)){
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
			
			
			Main.mobIP2sysIP.put(mobIP, sysIP);
			System.out.println("ConnectMobThread MobIP : " + mobIP + " SysIP : " + sysIP);
			Main.sysIP2mobIP.put(sysIP, mobIP);
			Main.hashID2emailID.put(hashID, ConnectSysThread.emailId);
			
			//MessageThread messageThread = new MessageThread(sysMessageSock, mobMessageSock);
			//messageThread.start();
			
			outMob.write(9);  // Connection successful
			
			while(true){
				outMob.write(1);
				outMob.flush();
				inMob.read();
				Thread.sleep(10000);
			}
			
		} catch (IOException | InterruptedException e) {
			System.out.println("Mobile disconnected ");
			InetAddress mobIP = connMobSock.getInetAddress();
			System.out.println("IN ConnectMobThread mobIP : " + mobIP);
			if (mobIP != null){
				
				InetAddress sysIP = Main.mobIP2sysIP.get(mobIP);
				System.out.print("sysIP : " + sysIP);
				if (sysIP != null){
					Main.sysIP2mobIP.remove(sysIP);
					ExchangeFrame.sysIP2MobUdpPortMap.remove(sysIP);
					ExchangeAudio.mobIP2SysAudioUdpPortMap.remove(mobIP);
					//TODO - do for exchange audio
				}	
				Main.mobIP2sysIP.remove(mobIP);
			}
			try {
				connMobSock.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}
}
