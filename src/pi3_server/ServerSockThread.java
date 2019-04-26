package pi3_server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

public class ServerSockThread extends Thread {

	public static ConcurrentHashMap<InetAddress, Socket> sysIP2MessageSockMap = new ConcurrentHashMap<InetAddress, Socket>();
	//public static ConcurrentHashMap<InetAddress, Socket> mobIP2MessageSockMap = new ConcurrentHashMap<InetAddress, Socket>();
	public static ConcurrentHashMap<String, Socket> hashID2LivefeedSockMap = new ConcurrentHashMap<String, Socket>();
	public static ConcurrentHashMap<InetAddress, SysUDPInfo> sysIP2SysLivefeedUDPInfoMap = new ConcurrentHashMap<InetAddress, SysUDPInfo>();
	//public static ConcurrentHashMap<InetAddress, Socket> mobIP2LivefeedSockMap = new ConcurrentHashMap<InetAddress, Socket>();
	public static ConcurrentHashMap<InetAddress, Socket> sysIP2AudioSockMap = new ConcurrentHashMap<InetAddress, Socket>();
	public static ConcurrentHashMap<InetAddress, SysUDPInfo> sysIP2SysAudioUDPInfoMap = new ConcurrentHashMap<InetAddress, SysUDPInfo>();
	public static ConcurrentHashMap<InetAddress, Socket> sysIP2VideoSockMap = new ConcurrentHashMap<InetAddress, Socket>();
	
	private ServerSocket ss;
	private int port;
	//public static int udpAudioSysLocalPort;
	
	ServerSockThread(int port) throws IOException{
		this.port = port;
		if(port == Main.PORT_LIVEFEED_TCP_SYS || port == Main.PORT_LIVEFEED_TCP_MOB){
			ss = new ServerSocket(port,10,InetAddress.getByName(Main.ipv6));
			
		}else{
		ss = new ServerSocket(port);
		}
	}
	
	public void run(){
		while(true){
			try {
				Socket sock = ss.accept();
				Date date = new Date();
				System.out.println(Main.ft.format(date) + "	Socket accepted port: " + port);
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						
							switch (port){
							case Main.PORT_MESSAGE_SYS:{
								sysIP2MessageSockMap.put(sock.getInetAddress(), sock);
								break;
							}
							case Main.PORT_MESSAGE_MOB:{
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
							}
							case Main.PORT_LIVEFEED_TCP_SYS:{
								try {
									DataInputStream din = new DataInputStream(sock.getInputStream());
									DataOutputStream dout = new DataOutputStream(sock.getOutputStream());
									String hashID = din.readUTF();
									
									//InetSocketAddress addr = new InetSocketAddress(packet.getAddress(), packet.getPort());
									hashID2LivefeedSockMap.put(hashID, sock);
								} catch (IOException e) {
									e.printStackTrace();
									try {
										sock.close();
									} catch (IOException e1) {
										e1.printStackTrace();
									}
								}
								break;
							}
							case Main.PORT_LIVEFEED_TCP_MOB:{
								//mobIP2LivefeedSockMap.put(sock.getInetAddress(), sock);
								try {
									InputStream sockIn = sock.getInputStream();
									String mobIPv6 = sock.getInetAddress().getHostAddress();
									DataInputStream din = new DataInputStream(sockIn);
									DataOutputStream dout = new DataOutputStream(sock.getOutputStream());
									String hashID = din.readUTF();
									int mobUdpPort = din.readInt();
									
									
									Socket sysLivefeedSock = hashID2LivefeedSockMap.remove(hashID);
									long time1 = System.currentTimeMillis();
									while (System.currentTimeMillis() - time1 < 2000 && sysLivefeedSock == null){
										sysLivefeedSock = hashID2LivefeedSockMap.remove(hashID);
									}
									if (sysLivefeedSock == null){
										System.out.println("System is offline");
										
										sock.getOutputStream().write(0);
										sock.getOutputStream().flush();
										try {
											sock.close();
										} catch (IOException e) {
											e.printStackTrace();
										}
										return;
									}
									
									sock.getOutputStream().write(1);
									sock.getOutputStream().flush();
									
									DataOutputStream doutSys = new DataOutputStream(sysLivefeedSock.getOutputStream());
									doutSys.writeInt(mobUdpPort);
									doutSys.writeUTF(mobIPv6);
									dout.flush();
									
									//ExchangeFrame.sysIP2MobUdpPortMap.put(sysIP2, udpPort);

									while(true){
										try{
											sock.getOutputStream().write(1);
											sock.getOutputStream().flush();
											int p = sock.getInputStream().read();
											if (p == -1) break;
											
											try {
												Thread.sleep(2000);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
											
										} catch (IOException e1) {
											e1.printStackTrace();
											break;
										}
									}
									
									System.out.println("Livefeed stopped!!!");
									//ExchangeFrame.sysIP2MobUdpPortMap.remove(sysIP);
									sysLivefeedSock.close();
									/*
									System.out.println("Livefeed stopped weirdly!!!@@@@@@@@@@@@@@@@@@");
									ExchangeFrame.sysIP2MobUdpPortMap.remove(sysIP2);
									Socket sysLivefeedSock = sysIP2LivefeedSockMap.get(sysIP2);
									sysLivefeedSock.close();
									*/
								} catch (IOException e) {
									e.printStackTrace();
									try {
										sock.close();
									} catch (IOException e1) {
										e1.printStackTrace();
									}
								}
								break;
							}
							case Main.PORT_AUDIO_TCP_SYS:{
								sysIP2AudioSockMap.put(sock.getInetAddress(), sock);
								DatagramSocket ds = null;
								try {
									InetAddress mobIP = Main.sysIP2mobIP.get(sock.getInetAddress());
									int p = sock.getInputStream().read();
									if(p == 2 && mobIP != null){
										// initial exchange of stuff
										System.out.println("....recvd = 2...for audio");
										//udpAudioSysLocalPort = new DataInputStream(sock.getInputStream()).readInt();
										
										sock.getOutputStream().write(1);
										sock.getOutputStream().flush();
										sock.getInputStream().read();
									}else {
										sock.close();
										return;
									}
										/*else if(i == 1){
									
										//normal exchange while exchange audio packets
										System.out.println("....recvd = 1...for audio");
									}*/
									ds = new DatagramSocket();
									DataInputStream din = new DataInputStream(sock.getInputStream());
									DataOutputStream dout = new DataOutputStream(sock.getOutputStream());
									dout.writeInt(ds.getLocalPort());
									dout.flush();
									
									byte[] buf = new byte[2];
									DatagramPacket packet = new DatagramPacket(buf, buf.length);
									ds.setSoTimeout(5000);
									ds.receive(packet);
									for (int i=0; i<10; i++){
										ds.send(new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort()));
									}
									ds.close();
									String sysLocaIP = din.readUTF();
									int sysLocalUDPPort = din.readInt();
									
									System.out.println("SYS Audio Datagram public IP: " + packet.getAddress() + " public port: " + packet.getPort());
									System.out.println("SYS Audio Datagram private IP: " + sysLocaIP + " private port: " + sysLocalUDPPort);
									
									sysIP2SysAudioUDPInfoMap.put(sock.getInetAddress(), new SysUDPInfo(packet.getAddress(), packet.getPort(), InetAddress.getByName(sysLocaIP), sysLocalUDPPort));
									
									System.out.println(".....Audio port exchg complete................................!!...............");
									//ExchangeAudio.mobIP2SysAudioUdpPortMap.put(mobIP2,udpAudioSysLocalPort);
								} catch (IOException e) {
									e.printStackTrace();
									try {
										ds.close();
										sock.close();
									} catch (IOException e1) {
										e1.printStackTrace();
									}
								}
								break;
							}
							case Main.PORT_AUDIO_TCP_MOB:{
								DatagramSocket ds = null;
								try {
									System.out.println("In serverSock Audio TCP Mob");
									InputStream mobIn = sock.getInputStream();
									OutputStream mobOut = sock.getOutputStream();
									
									mobIn.read();
									mobOut.write(2);
									mobOut.flush();
									
									DataInputStream din = new DataInputStream(mobIn);
									DataOutputStream dout = new DataOutputStream(mobOut);
									
									InetAddress sysIP = Main.mobIP2sysIP.get(sock.getInetAddress());
									if (sysIP == null){
										System.out.println("System not connected!!");
										sock.close();
										return;
									}
									
									long time1 = System.currentTimeMillis();
									SysUDPInfo sysAudioUdpInfo = null;
									while(System.currentTimeMillis() - time1 < 2000 && sysAudioUdpInfo == null){
										sysAudioUdpInfo = sysIP2SysAudioUDPInfoMap.remove(sysIP);
									}
									if (sysAudioUdpInfo == null){
										System.out.println("POSSIBLE ATTACK! No corresponding system audio UDP socket found.");
										sock.close();
										return;
									}
									
									ds = new DatagramSocket();
									dout.writeInt(ds.getLocalPort());
									dout.flush();
									
									byte[] buf = new byte[2];
									DatagramPacket packet = new DatagramPacket(buf, buf.length);
									ds.setSoTimeout(5000);
									ds.receive(packet);
									for (int i=0; i<10; i++){
										ds.send(new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort()));
									}
									System.out.println("MOB Audio Datagram IP: " + packet.getAddress() + " port: " + packet.getPort());
									ds.close();
									
									String mobLocalIP = din.readUTF();
									int mobLocalPort = din.readInt();
									
									Socket sysAudioTCPSock = sysIP2AudioSockMap.get(sysIP);
									DataOutputStream doutSys = new DataOutputStream(sysAudioTCPSock.getOutputStream());
									
									if (sysIP.getHostAddress().equals(sock.getInetAddress().getHostAddress())){
										//System and mobile on same network, send localIP
										dout.writeUTF(sysAudioUdpInfo.localIP.getHostAddress());
										dout.writeInt(sysAudioUdpInfo.localPort);
										dout.flush();
										doutSys.writeUTF(mobLocalIP);
										doutSys.writeInt(mobLocalPort);
										doutSys.flush();
									}else {
										dout.writeUTF(sysAudioUdpInfo.publicIP.getHostAddress());
										dout.writeInt(sysAudioUdpInfo.publicPort);
										dout.flush();
										doutSys.writeUTF(packet.getAddress().getHostAddress());
										doutSys.writeInt(packet.getPort());
										doutSys.flush();
									}
									
									while(true){
										try{
											mobOut.write(1);
											mobOut.flush();
											int p = mobIn.read();
											if (p == -1) break;
											
											try {
												Thread.sleep(2000);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
											
										} catch (IOException e1) {
											e1.printStackTrace();
											break;
										}
									}
									
									System.out.println("Sending Audio stopped!!!");
									//ExchangeAudio.mobIP2SysAudioUdpPortMap.remove(sock.getInetAddress());
									sysIP2SysAudioUDPInfoMap.remove(sysIP);
									Socket sysAudioSock = sysIP2AudioSockMap.get(sysIP);
									sysAudioSock.close();
									
								} catch (IOException e) {
									e.printStackTrace();
									try {
										if (ds != null)
											ds.close();
										sock.close();
									} catch (IOException e1) {
										e1.printStackTrace();
									}
								}
								break;
							}
							case Main.PORT_NOTIF_VIDEO_SYS:{
								sysIP2VideoSockMap.put(sock.getInetAddress(), sock);
								System.out.println("Concurrent hash map VideoSockMap");
								break;
							}
							case Main.PORT_NOTIF_VIDEO_MOB:{
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
							}
							case Main.PORT_PERSON_DETECT_GPU:{
								Main.sockGPU = sock;
								break;
							}
							case Main.PORT_PERSON_DETECT_SYS:{
								try {
									InputStream in = sock.getInputStream();
									OutputStream out = sock.getOutputStream();
									DataInputStream din = new DataInputStream(in);
									DataOutputStream dout = new DataOutputStream(out);
									String hashID = din.readUTF();
									ConnectSysThread connSysThread = Main.connSysThreadsMap.get(hashID);
									if (sock.getInetAddress().equals(connSysThread.connSysSock.getInetAddress())){
										out.write(1); //system verified
										out.flush();
										int width = din.readInt();
										int height = din.readInt();
										int dataSize = width * height * 3;
										byte[] bWidth = ByteBuffer.allocate(4).putInt(width).array();
										byte[] bHeight = ByteBuffer.allocate(4).putInt(height).array();
										byte[] bSize = ByteBuffer.allocate(4).putInt(dataSize).array();
										while(Main.sockGPU == null){
											try {
												Thread.sleep(500);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
										}
										OutputStream outGPU = Main.sockGPU.getOutputStream();
										outGPU.write(bWidth);
										outGPU.write(bHeight);
										outGPU.write(bSize);
										outGPU.flush();

										for (int i=0; i<dataSize; i++){
											int oneByte = in.read();
											outGPU.write(oneByte);
										}
										outGPU.flush();
										Main.sockGPU.close();

										Socket sockGPU2 = Main.ssGPU2.accept();
										byte[] bCount = new byte[4];
										InputStream inGPU2 = sockGPU2.getInputStream();

										for (int i=0; i<4; i++){
											bCount[i] = (byte) inGPU2.read();
										}

										int count = ByteBuffer.wrap(bCount).getInt();

										sockGPU2.close();
										
										System.out.println("Person count: " + count);

										dout.writeInt(count);
										dout.flush();
									}else{
										sock.close();
										return;
									}
									
								} catch (IOException e) {
									e.printStackTrace();
								}
								break;
							}
						}
					}
				}).start();
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}
