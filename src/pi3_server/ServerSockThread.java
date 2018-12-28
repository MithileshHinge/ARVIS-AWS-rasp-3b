package pi3_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ServerSockThread extends Thread {

	public static ConcurrentHashMap<InetAddress, Socket> sysIP2MessageSockMap = new ConcurrentHashMap<InetAddress, Socket>();
	//public static ConcurrentHashMap<InetAddress, Socket> mobIP2MessageSockMap = new ConcurrentHashMap<InetAddress, Socket>();
	public static ConcurrentHashMap<InetAddress, Socket> sysIP2LivefeedSockMap = new ConcurrentHashMap<InetAddress, Socket>();
	//public static ConcurrentHashMap<InetAddress, Socket> mobIP2LivefeedSockMap = new ConcurrentHashMap<InetAddress, Socket>();
	public static ConcurrentHashMap<InetAddress, Socket> sysIP2AudioSockMap = new ConcurrentHashMap<InetAddress, Socket>();
	public static ConcurrentHashMap<InetAddress, Socket> sysIP2VideoSockMap = new ConcurrentHashMap<InetAddress, Socket>();
	public static ConcurrentHashMap<InetAddress, Socket> sysIP2ListenSockMap = new ConcurrentHashMap<InetAddress, Socket>();
	
	
	private ServerSocket ss;
	private int port;
	public static int udpAudioSysLocalPort;
	
	ServerSockThread(int port) throws IOException{
		this.port = port;
		ss = new ServerSocket(port);
	}
	
	public void run(){
		while(true){
			try {
				Socket sock = ss.accept();
				System.out.println("Socket accepted port: " + port);
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						
							switch (port){
							case Main.PORT_MESSAGE_SYS:
								sysIP2MessageSockMap.put(sock.getInetAddress(), sock);
								break;
							case Main.PORT_MESSAGE_MOB:
								InetAddress androidIP = sock.getInetAddress();
								System.out.println("Message MobIP : " + androidIP);
								InetAddress sysIP = Main.mobIP2sysIP.get(androidIP);
								System.out.println("Message SysIP : " + sysIP);
								if (sysIP == null){
									System.out.println("No device with this IP has initiated ConnectMob method OR Corresponding System is offline");
									try {
										sock.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
									return;
								}
								Socket sysMessageSock = sysIP2MessageSockMap.get(sysIP);
								MessageThread messageThread = new MessageThread(sysMessageSock, sock);
								messageThread.start();
								break;
							case Main.PORT_LIVEFEED_TCP_SYS:
								sysIP2LivefeedSockMap.put(sock.getInetAddress(), sock);
								break;
							case Main.PORT_LIVEFEED_TCP_MOB:
								//mobIP2LivefeedSockMap.put(sock.getInetAddress(), sock);
									
								try {
									InputStream sockIn = sock.getInputStream();
									InetAddress mobIP = sock.getInetAddress();
									DataInputStream din = new DataInputStream(sockIn);
									int udpPort = din.readInt();
									System.out.println(" Livefeed UDP port: " + udpPort);
									
									InetAddress sysIP2 = Main.mobIP2sysIP.get(mobIP);
									if (sysIP2 == null){
										System.out.println("Mobile with this IP has never initiated ConnectMob method OR Corresponding System is offline");
										
										sock.getOutputStream().write(0);
										sock.getOutputStream().flush();
										try {
											sock.close();
										} catch (IOException e) {
											e.printStackTrace();
										}
										return;
									}else {
										sock.getOutputStream().write(1);
										sock.getOutputStream().flush();
									}
									
									ExchangeFrame.sysIP2MobUdpPortLFMap.put(sysIP2, udpPort);
									try{
										sockIn.read();
									} catch (IOException e1) {
										System.out.println("Livefeed stopped!!!");
										ExchangeFrame.sysIP2MobUdpPortLFMap.remove(sysIP2);
										Socket sysLivefeedSock = sysIP2LivefeedSockMap.get(sysIP2);
										sysLivefeedSock.close();
										e1.printStackTrace();
										return;
									}
									
									System.out.println("Livefeed stopped!!!");
									ExchangeFrame.sysIP2MobUdpPortLFMap.remove(sysIP2);
									Socket sysLivefeedSock = sysIP2LivefeedSockMap.get(sysIP2);
									sysLivefeedSock.close();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								break;
							case Main.PORT_AUDIO_TCP_SYS:
								sysIP2AudioSockMap.put(sock.getInetAddress(), sock);
								try {
									InetAddress mobIP2 = Main.sysIP2mobIP.get(sock.getInetAddress()); 
									int i = sock.getInputStream().read();
									if(i == 2){
										// initial exchange of stuff
										System.out.println("....recvd = 2...for audio");
										udpAudioSysLocalPort = new DataInputStream(sock.getInputStream()).readInt();
										
										sock.getOutputStream().write(1);
										sock.getOutputStream().flush();
										sock.getInputStream().read();
									}/*else if(i == 1){
										//normal exchange while exchange audio packets
										System.out.println("....recvd = 1...for audio");
									}*/
									System.out.println(".....Audio port exchg complete................................!!...............");
									
									ExchangeAudio.mobIP2SysAudioUdpPortMap.put(mobIP2,udpAudioSysLocalPort);
									System.out.println("In serverSock mobIP : " + mobIP2);
									System.out.println("sysIP : " + sock.getInetAddress());
									System.out.println("Udp Port recieved : " + udpAudioSysLocalPort);
								
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								break;
							case Main.PORT_AUDIO_TCP_MOB:
								try {
									System.out.println("In serverSock Audio TCP Mob");
									InputStream mobIn = sock.getInputStream();
									OutputStream mobOut = sock.getOutputStream();
									
									InetAddress sysIP1 = Main.mobIP2sysIP.get(sock.getInetAddress());
									if (sysIP1 == null){
										System.out.println("System not connected!!");
										sock.close();
										return;
									}
									mobIn.read();
									mobOut.write(2);
									try{
										mobIn.read();
									}catch(IOException e){
										System.out.println("Sending Audio stopped!!");
										ExchangeAudio.mobIP2SysAudioUdpPortMap.remove(sock.getInetAddress());
										Socket sysAudioSock = sysIP2AudioSockMap.get(sysIP1);
										sysAudioSock.close();
										e.printStackTrace();
									}
									System.out.println("Sending Audio stopped!!");
									ExchangeAudio.mobIP2SysAudioUdpPortMap.remove(sock.getInetAddress());
									Socket sysAudioSock = sysIP2AudioSockMap.get(sysIP1);
									sysAudioSock.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
								
								
								break;
							case Main.PORT_NOTIF_VIDEO_SYS:
								sysIP2VideoSockMap.put(sock.getInetAddress(), sock);
								System.out.println("Concurrent hash map VideoSockMap");
								break;
							case Main.PORT_NOTIF_VIDEO_MOB:
								try {
									InputStream mobIn = sock.getInputStream();
									OutputStream mobOut = sock.getOutputStream();
									DataInputStream mobDin = new DataInputStream(mobIn);
									DataOutputStream mobDout = new DataOutputStream(mobOut); 
									String hashID = mobDin.readUTF();		//Reading hashID from mob
									System.out.println("Hash ID from mob = " + hashID);
									InetAddress sysIP1 = Main.connSysThreadsMap.get(hashID).connSysSock.getInetAddress();
									if (sysIP1 == null){
										System.out.println("System not connected!!");
										sock.close();
										return;
									}
									System.out.println("Before Socket sysVideoSock");
									Socket sysVideoSock = sysIP2VideoSockMap.get(sysIP1);
									if(sysVideoSock == null){
										System.out.println("sysVideoSock is null");
										return;
									}
										
									InputStream sysIn = sysVideoSock.getInputStream();
									OutputStream sysOut = sysVideoSock.getOutputStream();
									DataInputStream sysDin = new DataInputStream(sysIn);
									DataOutputStream sysDout = new DataOutputStream(sysOut);
									
									//Reading videoNotifID from mob and sending to sys
									sysDout.writeInt(mobDin.readInt());	
									sysDout.flush();
									System.out.println("Video Notif ID : ");
									
									// Read filename length from sys and send to mob
									int fileNameSize = sysDin.readInt();
									mobDout.writeInt(fileNameSize);
									mobDout.flush();
									mobIn.read();
									sysOut.write(1);
									sysOut.flush();
									System.out.println("Filename length : " + fileNameSize );
									
									// Read filename from sys and send to mob
									byte[] filenameInBytes = new byte[fileNameSize];
									sysIn.read(filenameInBytes);
									mobOut.write(filenameInBytes);
									mobOut.flush();
									mobIn.read();
									sysOut.write(1);
									sysOut.flush();
									System.out.println("Filename sent " );
									
									/*String filename = sysDin.readUTF();
									mobDout.writeUTF(filename);
									mobIn.read();
									sysOut.write(1);
									sysOut.flush();*/
									
									byte[] videoBuffer = new byte[16*1024];
									int count;
									while((count = sysIn.read(videoBuffer)) > 0){
										mobOut.write(videoBuffer, 0, count);
									}
									mobOut.flush();
									System.out.println("Video Sent Successfully");
									sysVideoSock.close();
									sock.close();
									
								} catch (IOException e) {
									e.printStackTrace();
								}
								break;
							case Main.PORT_LISTEN_TCP_MOB:
								try {
									InputStream sockIn = sock.getInputStream();
									InetAddress mobIP = sock.getInetAddress();
									DataInputStream din = new DataInputStream(sockIn);
									int udpPort = din.readInt();
									System.out.println("Listen UDP port: " + udpPort);
									
									InetAddress sysIP2 = Main.mobIP2sysIP.get(mobIP);
									if (sysIP2 == null){
										System.out.println("Mobile with this IP has never initiated ConnectMob method OR Corresponding System is offline");
										
										sock.getOutputStream().write(0);
										sock.getOutputStream().flush();
										try {
											sock.close();
										} catch (IOException e) {
											e.printStackTrace();
										}
										return;
									}else {
										sock.getOutputStream().write(1);
										sock.getOutputStream().flush();
									}
									
									ExchangeListen.sysIP2MobUdpPortListenMap.put(sysIP2, udpPort);
									try{
										sockIn.read();
									} catch (IOException e1) {
										System.out.println("Listen stopped!!!");
										ExchangeListen.sysIP2MobUdpPortListenMap.remove(sysIP2);
										Socket sysListenSock = sysIP2ListenSockMap.get(sysIP2);
										sysListenSock.close();
										e1.printStackTrace();
										return;
									}
									
									System.out.println("Listen stopped!!!");
									ExchangeListen.sysIP2MobUdpPortListenMap.remove(sysIP2);
									Socket sysListenSock = sysIP2ListenSockMap.get(sysIP2);
									sysListenSock.close();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								break;
							case Main.PORT_LISTEN_TCP_SYS:
								sysIP2ListenSockMap.put(sock.getInetAddress(), sock);
								break;
							}
					}
				}).start();
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}
