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
	private DatagramSocket dsSys, dsMob;
	public static ConcurrentHashMap<InetAddress, Integer> sysIP2MobUdpPortMap = new ConcurrentHashMap<>();
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
		dsMob = new DatagramSocket(Main.PORT_LIVEFEED_UDP_MOB);
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
                System.out.println("...Frame receiving from system... ");
                dsSys.receive(receivedPacket);
                System.out.println("...Frame received from system... ");
                new Thread(new Runnable() {
					
					@Override
					public void run() {
						//InetAddress mobAddress = ((InetSocketAddress) sockMob.getRemoteSocketAddress()).getAddress();
						InetAddress mobAddress = Main.sysIP2mobIP.get(receivedPacket.getAddress());
						System.out.println("Mob address......."+mobAddress);
						int remoteUDPPort = sysIP2MobUdpPortMap.get(receivedPacket.getAddress());
	                    
	                    receivedPacket.setAddress(mobAddress);
	                    receivedPacket.setPort(remoteUDPPort);
	                    try {
							dsMob.send(receivedPacket);
						} catch (IOException e) {
							e.printStackTrace();
					
						}
	                    
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
