package pi3_server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ServerSockThread extends Thread {

	public static ConcurrentHashMap<InetAddress, Socket> sysIP2MessageSockMap = new ConcurrentHashMap<InetAddress, Socket>();
	//public static ConcurrentHashMap<InetAddress, Socket> mobIP2MessageSockMap = new ConcurrentHashMap<InetAddress, Socket>();
	public static ConcurrentHashMap<InetAddress, Socket> sysIP2LivefeedSockMap = new ConcurrentHashMap<InetAddress, Socket>();
	//public static ConcurrentHashMap<InetAddress, Socket> mobIP2LivefeedSockMap = new ConcurrentHashMap<InetAddress, Socket>();

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
							//case Main.PORT_TCP_AUDIO_MOB:
								
								//break;
							//case Main.PORT_TCP_AUDIO_SYS:
								
								//break;
							}
					}
				}).start();
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}
