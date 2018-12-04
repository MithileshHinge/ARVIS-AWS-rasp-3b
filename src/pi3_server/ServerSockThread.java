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
	
	private ServerSocket ss;
	private int port;
	
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
								InetAddress sysIP = Main.mobIP2sysIP.get(sock.getInetAddress());
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
									System.out.println("UDP port: " + udpPort);
									
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
									
									ExchangeFrame.sysIP2MobUdpPortMap.put(sysIP2, udpPort);
									try{
										sockIn.read();
									} catch (IOException e1) {
										System.out.println("Livefeed stopped!!!");
										ExchangeFrame.sysIP2MobUdpPortMap.remove(sysIP2);
										Socket sysLivefeedSock = sysIP2LivefeedSockMap.get(sysIP2);
										sysLivefeedSock.close();
										e1.printStackTrace();
										return;
									}
									
									System.out.println("Livefeed stopped!!!");
									ExchangeFrame.sysIP2MobUdpPortMap.remove(sysIP2);
									Socket sysLivefeedSock = sysIP2LivefeedSockMap.get(sysIP2);
									sysLivefeedSock.close();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								break;
							case Main.PORT_AUDIO_TCP_SYS:
								sysIP2AudioSockMap.put(sock.getInetAddress(), sock);
								break;
							case Main.PORT_AUDIO_TCP_MOB:
								try {
									InputStream sockIn = sock.getInputStream();
									OutputStream sockOut = sock.getOutputStream();
									InetAddress sysIP1 = Main.mobIP2sysIP.get(sock.getInetAddress());
									if (sysIP1 == null){
										System.out.println("System not connected!!");
										sock.close();
										return;
									}
									sockIn.read();
									sockOut.write(2);
									try{
										sockIn.read();
									}catch(IOException e){
										System.out.println("Sending Audio stopped!!");
										Socket sysAudioSock = sysIP2AudioSockMap.get(sysIP1);
										sysAudioSock.close();
										e.printStackTrace();
									}
									System.out.println("Sending Audio stopped!!");
									Socket sysAudioSock = sysIP2AudioSockMap.get(sysIP1);
									sysAudioSock.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
								break;
							case Main.PORT_NOTIF_VIDEO_SYS:
								sysIP2VideoSockMap.put(sock.getInetAddress(), sock);
								break;
							case Main.PORT_NOTIF_VIDEO_MOB:
								try {
									InputStream mobIn = sock.getInputStream();
									OutputStream mobOut = sock.getOutputStream();
									DataInputStream mobDin = new DataInputStream(mobIn);
									DataOutputStream mobDout = new DataOutputStream(mobOut); 
									String hashID = mobDin.readUTF();
									InetAddress sysIP1 = Main.connSysThreadsMap.get(hashID).connSysSock.getInetAddress();
									if (sysIP1 == null){
										System.out.println("System not connected!!");
										sock.close();
										return;
									}
									Socket sysVideoSock = sysIP2AudioSockMap.get(sysIP1);
									InputStream sysIn = sysVideoSock.getInputStream();
									OutputStream sysOut = sysVideoSock.getOutputStream();
									DataInputStream sysDin = new DataInputStream(sysIn);
									DataOutputStream sysDout = new DataOutputStream(sysOut);
									sysDout.writeInt(mobDin.readInt());
									mobIn.read();
									sysOut.write(1);
									sysOut.flush();
									String filename = sysDin.readUTF();
									mobDout.writeUTF(filename);
									mobIn.read();
									sysOut.write(1);
									sysOut.flush();
									byte[] videoBuffer = new byte[16*1024];
									int count;
									while((count = sysIn.read(videoBuffer)) > 0){
										mobOut.write(videoBuffer, 0, count);
									}
									mobOut.flush();
									sysVideoSock.close();
									sock.close();
									
								} catch (IOException e) {
									e.printStackTrace();
								}
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
