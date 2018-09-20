package pi3_server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

class ConnectMobThread extends Thread{
	private Socket connMobSock;
	
	ConnectSysThread connSysThread;
	
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
			if (Main.db.checkRegistered(hashID)){
				outMob.write(1);  //Registered
				outMob.flush();
				for (int i=0; i<5; i++){ // No of login attempts
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
			
			
			MessageThread messageThread = new MessageThread(connSysThread.connSysSock.getInetAddress(), connMobSock.getInetAddress());
			messageThread.start();
			
			outMob.write(9);  // Connection successful, message thread started
			
			while(true){
				inMob.read();
			}
			
		} catch (IOException e) {
			try {
				connMobSock.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}
}
