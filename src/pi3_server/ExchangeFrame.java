package pi3_server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ExchangeFrame extends Thread{
	
	//private ServerSocket ssSys, ssMob;
	private DatagramSocket dsSys;
	//public static ConcurrentHashMap<InetAddress, Integer> sysIP2MobUdpPortMap = new ConcurrentHashMap<>();
	//private Socket sockSys, sockMob;
	
	public ExchangeFrame() throws IOException{
		/*ssSys = new ServerSocket();
		ssSys.bind(new InetSocketAddress(addrSys, Main.PORT_LIVEFEED_TCP_SYS));
		ssMob = new ServerSocket();
		ssMob.bind(new InetSocketAddress(addrMob, Main.PORT_LIVEFEED_TCP_MOB));
		
		dsSys = new DatagramSocket();
		dsSys.bind(new InetSocketAddress(addrSys, Main.PORT_LIVEFEED_UDP_SYS));
		dsMob = new DatagramSocket();
		dsMob.bind(new InetSocketAddress(addrMob, Main.PORT_LIVEFEED_UDP_MOB));
		*/
		
		dsSys = new DatagramSocket(Main.PORT_LIVEFEED_UDP_SYS);
	}
	
	public void run(){
	
		/*sockSys = ssSys.accept();
		sockMob = ssMob.accept();
		
		InputStream inMob = sockMob.getInputStream();
		OutputStream outMob = sockMob.getOutputStream();
		
		InputStream inSys = sockSys.getInputStream();
		OutputStream outSys = sockSys.getOutputStream();
		*/
		
		System.out.println("ExchangeFrame Thread started....");
		
		 try {
			ExchangeAudio exchgAudio = new ExchangeAudio();
			exchgAudio.start();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		 
		 
		/*
		byte[] handshakeBuf = new byte[256];
		DatagramPacket handshakePacket = new DatagramPacket(handshakeBuf, handshakeBuf.length);
		dsMob.receive(handshakePacket);
		int remoteUDPPort = handshakePacket.getPort();
		//outMob.write(1);
		//outMob.flush();
		*/
		
		while(true){
            long time1 = System.currentTimeMillis();
           
            //outMob.write(1);
            //outMob.flush();
            
            try{	
            	byte[] buf = new byte[64000];
                DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);
                //udpSocket_sys.setSoTimeout(2000);
                //System.out.println("...Frame receiving from system... ");
                dsSys.receive(receivedPacket);
                System.out.println("...Frame received from system... ");
                
                new Thread(new Runnable() {
					
					@Override
					public void run() {
						//InetAddress mobAddress = ((InetSocketAddress) sockMob.getRemoteSocketAddress()).getAddress();
						/*InetAddress mobAddress = Main.sysIP2mobIP.get(receivedPacket.getAddress());
						System.out.println("Mob address......."+mobAddress);
						int remoteUDPPort = sysIP2MobUdpPortMap.get(receivedPacket.getAddress());
						receivedPacket.setAddress(mobAddress);
	                    receivedPacket.setPort(remoteUDPPort);*/
						InetAddress sysUDP = (InetAddress) receivedPacket.getAddress();
						System.out.println("EXCHANGE FRAME LENGTH: " + receivedPacket.getLength() + "	" + sysUDP);
						if(receivedPacket.getLength()<100 && !Main.sysUDPIP2hashIDMap.containsKey(sysUDP)){
							String hashId = new String(receivedPacket.getData()).trim();
							Main.sysUDPIP2hashIDMap.put(sysUDP,hashId);
							System.out.println("EF THREAD SYS2HASHID ENTRY DONE............ next bool = "+Main.sysUDP2mobUDPPortMap.containsKey(sysUDP));
		                }else if(Main.sysUDPIP2hashIDMap.containsKey(sysUDP) && !Main.sysUDP2mobUDPPortMap.containsKey(sysUDP)){
		                	String hashId = Main.sysUDPIP2hashIDMap.get(sysUDP);
		                	System.out.println("EF THREAD hashID2MobUDPMap = "+Main.hashID2MobUDPMap.containsKey(hashId));
		                	if(Main.hashID2MobUDPMap.containsKey(hashId)){
			                	InetSocketAddress mobUDP = Main.hashID2MobUDPMap.get(hashId);
			                	System.out.println("Exchange Frame Hash ID: " + hashId + " MobUdpIp: " + mobUDP + "SysUdpIp: " + sysUDP);
			                	Main.sysUDP2mobUDPPortMap.put(sysUDP,mobUDP);
			                	Main.mobUDP2sysUDPPortMap.put(mobUDP, sysUDP);
			                	System.out.println("EXCHANGE FRAME SYS2MOB ENTRY DONE.......... ");
		                	}
		                }else if(receivedPacket.getLength()>100 && Main.sysUDP2mobUDPPortMap.containsKey(sysUDP)){
		                	sysUDP = (InetAddress) receivedPacket.getAddress();
							InetSocketAddress mobAddress = Main.sysUDP2mobUDPPortMap.get(sysUDP);
							System.out.println("MOB UDP ADDRESS SET: " + mobAddress);
		                    receivedPacket.setAddress(mobAddress.getAddress());
		                    receivedPacket.setPort(mobAddress.getPort());
		                    
		                    try {
		                    	//System.out.println("dsMob: " + MobUDPPacketRx.dsMob + " dsSys: " + dsSys);
		                    	MobUDPPacketRx.dsMob.send(receivedPacket);
							} catch (IOException e) {
								e.printStackTrace();
							}
							
						}else
							System.out.println("EF THREAD : fakta System.out.println");
	                    
	                    //System.out.println("...Frame forwarded to android..." + "port = " + remoteUDPPort);
					}
				}).start();
                

            } catch (IOException e) {
    			e.printStackTrace();
    			System.out.println("Exchange Frame Sys catch");
    		}
            
            //long time2 = System.currentTimeMillis();
            //System.out.println("time = " + (time2 - time1));
		}
	}
}
