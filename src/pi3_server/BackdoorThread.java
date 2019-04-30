package pi3_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class BackdoorThread extends Thread{
	ServerSocket ss;
	
	private static final int 
			GETconnSysThreadsMap = 1, //done
			SETconnSysThreadsMap = 2,
			GETmobIP2SysIPMap = 3,
			SETmobIP2SysIPMap = 4,
			GETsysIP2MobIPMap = 5,
			SETsysIP2MobIPMap = 6,
			GETsysIP2MessageSockMap = 7,
			REMOVEsysIP2MessageSockMap = 8,
			GETsysIP2LivefeedSockMap = 9,
			REMOVEsysIP2LivefeedSockMap = 10,
			GETsysIP2AudioSockMap = 11,
			REMOVEsysIP2AudioSockMap = 12,
			GETsysIP2VideoSockMap = 13,
			REMOVEsysIP2VideoSockMap = 14,
			GETsysIP2MobUdpPortMap = 15,
			REMOVEsysIP2MobUdpPortMap = 16,
			GETmobIP2SysAudioUdpPortMap = 17,
			REMOVEmobIP2SysAudioUdpPortMap = 18,
			CountdownSysLatch = 19,
			GETUserDatabaseEntryFromHashID = 20,
			SETUserDatabaseEntryFromHashID = 21,
			GETUserDatabaseEntryFromUsername = 22,
			SETUserDatabaseEntryFromUsername = 23,
			UserDatabaseReset = 24, //done
			REMOVEconnSysThreadsMap = 25, //done
			REMOVEmobIP2SysIPMap = 26,
			REMOVEsysIP2MobIPMap = 27;
	
	BackdoorThread() throws IOException{
		ss = new ServerSocket(Main.PORT_BACKDOOR);
	}
	
	@Override
	public void run() {
		while(true){
			try {
				Socket sock = ss.accept();
				System.out.println("Socket accepted");
				if (!sock.getInetAddress().getHostName().contains("localhost")){
					System.out.println("Connection attempted not from localhost");
					sock.close();
					continue;
				}
				InputStream in = sock.getInputStream();
				OutputStream out = sock.getOutputStream();
				DataInputStream din = new DataInputStream(in);
				DataOutputStream dout = new DataOutputStream(out);
				
				switch(in.read()){
				case GETconnSysThreadsMap:{
					if (in.read() == 1){
						String hashID = din.readUTF();
						ConnectSysThread connSysThread = Main.connSysThreadsMap.get(hashID);
						if (connSysThread == null){
							dout.writeUTF("No thread associated with hashID");
							dout.flush();
							sock.close();
							break;
						}
						sendConnSysThread(connSysThread, dout);
						sock.close();
					}else {
						for (ConnectSysThread connSysThread : Main.connSysThreadsMap.values()){
							sendConnSysThread(connSysThread, dout);
						}
						sock.close();
					}
					break;
				}
				case SETconnSysThreadsMap:{
					String hashID = din.readUTF();
					ConnectSysThread connectSysThread = Main.connSysThreadsMap.get(hashID);
					if (connectSysThread != null)
						out.write(0);
					else
						out.write(1);
					out.flush();
					
					String sysLocalIP = din.readUTF().trim();
					if (!sysLocalIP.equals("")) connectSysThread.sysLocalIP = sysLocalIP;
					String username = din.readUTF().trim();
					if (!username.equals("")) connectSysThread.username = username;
					String password = din.readUTF().trim();
					if (!password.equals("")) connectSysThread.password = password;
					String email = din.readUTF();
					if (!email.equals("")) connectSysThread.emailId = email;
					String fcmToken = din.readUTF().trim();
					if (!fcmToken.equals("")) connectSysThread.fcm_token = fcmToken;
					String latchCount = din.readUTF().trim();
					if (!latchCount.equals("")){
						try{
							int count = Integer.parseInt(latchCount);
							connectSysThread.latch = new CountDownLatch(count);
						}catch (NumberFormatException e){
							e.printStackTrace();
						}
					}
					sock.close();
					break;
				}
				case REMOVEconnSysThreadsMap:{
					if (in.read() == 1){
						String hashID = din.readUTF();
						ConnectSysThread connSysThread = Main.connSysThreadsMap.remove(hashID);
						if (connSysThread == null){
							dout.writeUTF("No thread associated with hashID");
							dout.flush();
						}
					}else {
						Main.connSysThreadsMap.clear();
					}
					sock.close();
					break;
				}
				case GETmobIP2SysIPMap:{
					if (in.read() == 1){
						String mobIPString = din.readUTF();
						InetAddress mobIP = InetAddress.getByName(mobIPString);
						InetAddress sysIP = Main.mobIP2sysIP.get(mobIP);
						if (sysIP == null){
							dout.writeUTF("No sysIP associated with mobIP");
							dout.flush();
							sock.close();
							break;
						}
						dout.writeUTF(sysIP.getHostAddress());
						dout.flush();
						sock.close();
					}else {
						for (InetAddress mobIP : Main.mobIP2sysIP.keySet()){
							dout.writeUTF("mobIP: " + mobIP.getHostAddress() + "   sysIP: " + Main.mobIP2sysIP.get(mobIP).getHostAddress());
							dout.flush();
						}
						sock.close();
					}
					break;
				}
				case SETmobIP2SysIPMap:{
					String mobIP = din.readUTF();
					String sysIP = din.readUTF();
					
					Main.mobIP2sysIP.put(InetAddress.getByName(mobIP), InetAddress.getByName(sysIP));
					sock.close();
					break;
				}
				case REMOVEmobIP2SysIPMap: {
					if (in.read() == 1){
						String mobIPString = din.readUTF();
						InetAddress mobIP = InetAddress.getByName(mobIPString);
						InetAddress sysIP = Main.mobIP2sysIP.remove(mobIP);
						if (sysIP == null){
							dout.writeUTF("No sysIP associated with mobIP");
							dout.flush();
						}
					}else {
						Main.mobIP2sysIP.clear();
					}
					sock.close();
					break;
				}
				case GETsysIP2MobIPMap:{
					if (in.read() == 1){
						String sysIPString = din.readUTF();
						InetAddress sysIP = InetAddress.getByName(sysIPString);
						InetAddress mobIP = Main.sysIP2mobIP.get(sysIP);
						if (mobIP == null){
							dout.writeUTF("No mobIP associated with sysIP");
							dout.flush();
							sock.close();
							break;
						}
						dout.writeUTF(mobIP.getHostAddress());
						dout.flush();
						sock.close();
					}else {
						for (InetAddress sysIP : Main.sysIP2mobIP.keySet()){
							dout.writeUTF("sysIP: " + sysIP.getHostAddress() + "   mobIP: " + Main.sysIP2mobIP.get(sysIP).getHostAddress());
							dout.flush();
						}
						sock.close();
					}
					break;
				}
				case SETsysIP2MobIPMap:{
					String sysIP = din.readUTF();
					String mobIP = din.readUTF();
					
					Main.sysIP2mobIP.put(InetAddress.getByName(sysIP), InetAddress.getByName(mobIP));
					sock.close();
					break;
				}
				case REMOVEsysIP2MobIPMap: {
					if (in.read() == 1){
						String sysIPString = din.readUTF();
						InetAddress sysIP = InetAddress.getByName(sysIPString);
						InetAddress mobIP = Main.sysIP2mobIP.remove(sysIP);
						if (mobIP == null){
							dout.writeUTF("No mobIP associated with sysIP");
							dout.flush();
						}
					}else {
						Main.sysIP2mobIP.clear();
					}
					sock.close();
				}
				case GETsysIP2MessageSockMap:{
					if (in.read() == 1){
						String sysIPString = din.readUTF();
						InetAddress sysIP = InetAddress.getByName(sysIPString);
						Socket msgSock = ServerSockThread.sysIP2MessageSockMap.get(sysIP);
						
						if (msgSock == null){
							dout.writeUTF("No messageSock associated with sysIP");
							dout.flush();
							sock.close();
							break;
						}
						dout.writeUTF(msgSock.getInetAddress().getHostAddress());
						dout.flush();
						sock.close();
					}else {
						for (InetAddress sysIP : ServerSockThread.sysIP2MessageSockMap.keySet()){
							dout.writeUTF("sysIP: " + sysIP.getHostAddress() + "   messageSock: " + ServerSockThread.sysIP2MessageSockMap.get(sysIP).getInetAddress().getHostAddress());
							dout.flush();
						}
						sock.close();
					}
					break;
				}
				case REMOVEsysIP2MessageSockMap:{
					if (in.read() == 1){
						String sysIPString = din.readUTF();
						InetAddress sysIP = InetAddress.getByName(sysIPString);
						Socket msgSock = ServerSockThread.sysIP2MessageSockMap.remove(sysIP);
						if (msgSock == null){
							dout.writeUTF("No messageSock associated with sysIP");
							dout.flush();
						}else{
							try{
								msgSock.close();
							}catch (IOException e){
								e.printStackTrace();
							}
						}
					}else {
						for (Socket msgSock : ServerSockThread.sysIP2MessageSockMap.values()){
							try{
								msgSock.close();
							}catch (IOException e){
								e.printStackTrace();
							}
						}
						ServerSockThread.sysIP2MessageSockMap.clear();
					}
					sock.close();
					break;
				}
				case GETsysIP2LivefeedSockMap:{
					if (in.read() == 1){
						String sysIPString = din.readUTF();
						InetAddress sysIP = InetAddress.getByName(sysIPString);
						Socket livefeedSock = ServerSockThread.sysIP2LivefeedSockMap.get(sysIP);
						
						if (livefeedSock == null){
							dout.writeUTF("No livefeedSock associated with sysIP");
							dout.flush();
							sock.close();
							break;
						}
						dout.writeUTF(livefeedSock.getInetAddress().getHostAddress());
						dout.flush();
						sock.close();
					}else {
						for (InetAddress sysIP : ServerSockThread.sysIP2LivefeedSockMap.keySet()){
							dout.writeUTF("sysIP: " + sysIP.getHostAddress() + "   livefeedSock: " + ServerSockThread.sysIP2LivefeedSockMap.get(sysIP).getInetAddress().getHostAddress());
							dout.flush();
						}
						sock.close();
					}
					break;
				}
				case REMOVEsysIP2LivefeedSockMap:{
					if (in.read() == 1){
						String sysIPString = din.readUTF();
						InetAddress sysIP = InetAddress.getByName(sysIPString);
						Socket livefeedSock = ServerSockThread.sysIP2LivefeedSockMap.remove(sysIP);
						if (livefeedSock == null){
							dout.writeUTF("No livefeedSock associated with sysIP");
							dout.flush();
						}else{
							try{
								livefeedSock.close();
							}catch (IOException e){
								e.printStackTrace();
							}
						}
					}else {
						for (Socket livefeedSock : ServerSockThread.sysIP2LivefeedSockMap.values()){
							try{
								livefeedSock.close();
							}catch (IOException e){
								e.printStackTrace();
							}
						}
						ServerSockThread.sysIP2LivefeedSockMap.clear();
					}
					sock.close();
					break;
				}
				case GETsysIP2AudioSockMap:{
					if (in.read() == 1){
						String sysIPString = din.readUTF();
						InetAddress sysIP = InetAddress.getByName(sysIPString);
						Socket audioSock = ServerSockThread.sysIP2AudioSockMap.get(sysIP);
						
						if (audioSock == null){
							dout.writeUTF("No audioSock associated with sysIP");
							dout.flush();
							sock.close();
							break;
						}
						dout.writeUTF(audioSock.getInetAddress().getHostAddress());
						dout.flush();
						sock.close();
					}else {
						for (InetAddress sysIP : ServerSockThread.sysIP2AudioSockMap.keySet()){
							dout.writeUTF("sysIP: " + sysIP.getHostAddress() + "   audioSock: " + ServerSockThread.sysIP2AudioSockMap.get(sysIP).getInetAddress().getHostAddress());
							dout.flush();
						}
						sock.close();
					}
					break;
				}
				case REMOVEsysIP2AudioSockMap:{
					if (in.read() == 1){
						String sysIPString = din.readUTF();
						InetAddress sysIP = InetAddress.getByName(sysIPString);
						Socket audioSock = ServerSockThread.sysIP2AudioSockMap.remove(sysIP);
						if (audioSock == null){
							dout.writeUTF("No audioSock associated with sysIP");
							dout.flush();
						}else{
							try{
								audioSock.close();
							}catch (IOException e){
								e.printStackTrace();
							}
						}
					}else {
						for (Socket audioSock : ServerSockThread.sysIP2AudioSockMap.values()){
							try{
								audioSock.close();
							}catch (IOException e){
								e.printStackTrace();
							}
						}
						ServerSockThread.sysIP2AudioSockMap.clear();
					}
					sock.close();
					break;
				}
				case GETsysIP2VideoSockMap:{
					if (in.read() == 1){
						String sysIPString = din.readUTF();
						InetAddress sysIP = InetAddress.getByName(sysIPString);
						Socket videoSock = ServerSockThread.sysIP2VideoSockMap.get(sysIP);
						
						if (videoSock == null){
							dout.writeUTF("No videoSock associated with sysIP");
							dout.flush();
							sock.close();
							break;
						}
						dout.writeUTF(videoSock.getInetAddress().getHostAddress());
						dout.flush();
						sock.close();
					}else {
						for (InetAddress sysIP : ServerSockThread.sysIP2VideoSockMap.keySet()){
							dout.writeUTF("sysIP: " + sysIP.getHostAddress() + "   videoSock: " + ServerSockThread.sysIP2VideoSockMap.get(sysIP).getInetAddress().getHostAddress());
							dout.flush();
						}
						sock.close();
					}
					break;
				}
				case REMOVEsysIP2VideoSockMap:{
					if (in.read() == 1){
						String sysIPString = din.readUTF();
						InetAddress sysIP = InetAddress.getByName(sysIPString);
						Socket videoSock = ServerSockThread.sysIP2VideoSockMap.remove(sysIP);
						if (videoSock == null){
							dout.writeUTF("No audioSock associated with sysIP");
							dout.flush();
						}else{
							try{
								videoSock.close();
							}catch (IOException e){
								e.printStackTrace();
							}
						}
					}else {
						for (Socket videoSock : ServerSockThread.sysIP2VideoSockMap.values()){
							try{
								videoSock.close();
							}catch (IOException e){
								e.printStackTrace();
							}
						}
						ServerSockThread.sysIP2VideoSockMap.clear();
					}
					sock.close();
					break;
				}
/*//				/*case GETsysIP2MobUdpPortMap:{
//					if (in.read() == 1){
//						String sysIPString = din.readUTF();
//						InetAddress sysIP = InetAddress.getByName(sysIPString);
//						//Integer udpPort = ExchangeFrame.sysIP2MobUdpPortMap.get(sysIP);
//						
//						if (udpPort == null){
//							dout.writeUTF("No udpPort associated with sysIP");
//							dout.flush();
//							sock.close();
//							break;
//						}
//						dout.writeUTF("udpPort: " + udpPort);
//						dout.flush();
//						sock.close();
//					}else {
//						for (InetAddress sysIP : ExchangeFrame.sysIP2MobUdpPortMap.keySet()){
//							dout.writeUTF("sysIP: " + sysIP.getHostAddress() + "   udpPort: " + ExchangeFrame.sysIP2MobUdpPortMap.get(sysIP));
//							dout.flush();
//						}
//						sock.close();
//					}
//					break;
//				}*/
				/*case REMOVEsysIP2MobUdpPortMap:{
					if (in.read() == 1){
						String sysIPString = din.readUTF();
						InetAddress sysIP = InetAddress.getByName(sysIPString);
						Integer udpPort = ExchangeFrame.sysIP2MobUdpPortMap.remove(sysIP);
						if (udpPort == null){
							dout.writeUTF("No udpPort associated with sysIP");
							dout.flush();
						}
					}else {
						ExchangeFrame.sysIP2MobUdpPortMap.clear();
					}
					sock.close();
					break;
				}*/
				case GETmobIP2SysAudioUdpPortMap:{
					if (in.read() == 1){
						String mobIPString = din.readUTF();
						InetAddress mobIP = InetAddress.getByName(mobIPString);
						Integer udpPort = ExchangeAudio.mobIP2SysAudioUdpPortMap.get(mobIP);
						
						if (udpPort == null){
							dout.writeUTF("No udpPort associated with mobIP");
							dout.flush();
							sock.close();
							break;
						}
						dout.writeUTF("udpPort: " + udpPort);
						dout.flush();
						sock.close();
					}else {
						for (InetAddress mobIP : ExchangeAudio.mobIP2SysAudioUdpPortMap.keySet()){
							dout.writeUTF("mobIP: " + mobIP.getHostAddress() + "   udpPort: " + ExchangeAudio.mobIP2SysAudioUdpPortMap.get(mobIP));
							dout.flush();
						}
						sock.close();
					}
					break;
				}
				case REMOVEmobIP2SysAudioUdpPortMap:{
					if (in.read() == 1){
						String mobIPString = din.readUTF();
						InetAddress mobIP = InetAddress.getByName(mobIPString);
						Integer udpPort = ExchangeAudio.mobIP2SysAudioUdpPortMap.remove(mobIP);
						if (udpPort == null){
							dout.writeUTF("No udpPort associated with mobIP");
							dout.flush();
						}
					}else {
						ExchangeAudio.mobIP2SysAudioUdpPortMap.clear();
					}
					sock.close();
					break;
				}
				case CountdownSysLatch:{
					if (in.read() == 1){
						String hashID = din.readUTF();
						ConnectSysThread connSysThread = Main.connSysThreadsMap.get(hashID);
						
						if (connSysThread == null){
							dout.writeUTF("No connSysThread associated with mobIP");
							dout.flush();
						}else
							connSysThread.latch.countDown();
					}else {
						for (ConnectSysThread connSysThread : Main.connSysThreadsMap.values()){
							connSysThread.latch.countDown();
						}
					}
					sock.close();
					break;
				}
				case UserDatabaseReset:{
					File databaseFile = new File("UserDatabase.db");
					databaseFile.delete();
					Thread.sleep(1000);
					Main.db = new UserDatabaseHandler();
					sock.close();
					break;
				}
				default:
					System.out.println("Invalid argument");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	void sendConnSysThread(ConnectSysThread connSysThread, DataOutputStream dout) throws IOException {
		dout.writeUTF(connSysThread.hashID);
		dout.writeUTF("sysIP: " + connSysThread.connSysSock.getInetAddress().getHostAddress());
		dout.writeUTF("port: " + connSysThread.connSysSock.getPort());
		dout.writeUTF("sysLocalIP: " + connSysThread.sysLocalIP);
		dout.writeUTF("username: " + connSysThread.username);
		dout.writeUTF("password: " + connSysThread.password);
		dout.writeUTF("emailID: " + connSysThread.emailId);
		dout.writeUTF("fcm_token: " + connSysThread.fcm_token);
		dout.writeUTF("latchCount: " + connSysThread.latch.getCount());
		dout.flush();
		
	}
}
