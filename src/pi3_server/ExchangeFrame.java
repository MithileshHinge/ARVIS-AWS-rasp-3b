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

public class ExchangeFrame extends Thread{
	
	private ServerSocket ssSys, ssMob;
	private DatagramSocket dsSys, dsMob;
	private Socket sockSys, sockMob;
	
	public ExchangeFrame() throws IOException{
		ssSys = new ServerSocket(Main.PORT_LIVEFEED_TCP_SYS);
		ssMob = new ServerSocket(Main.PORT_LIVEFEED_TCP_MOB);
		
		dsSys = new DatagramSocket(Main.PORT_LIVEFEED_UDP_SYS);
		dsMob = new DatagramSocket(Main.PORT_LIVEFEED_UDP_MOB);
	}
	
	public void run(){
		try {
			sockSys = ssSys.accept();
			sockMob = ssMob.accept();
			
			InputStream inMob = sockMob.getInputStream();
			OutputStream outMob = sockMob.getOutputStream();
			
			InputStream inSys = sockSys.getInputStream();
			OutputStream outSys = sockSys.getOutputStream();
			
			System.out.println("ExchangeThread started....");
			
			byte[] handshakeBuf = new byte[256];
			DatagramPacket handshakePacket = new DatagramPacket(handshakeBuf, handshakeBuf.length);
			dsMob.receive(handshakePacket);
			int remoteUDPPort = handshakePacket.getPort();
			outMob.write(1);
			outMob.flush();
			
			while(true){
                long time1 = System.currentTimeMillis();
                
                outSys.write(1);
                outSys.flush();
                
                try{	
                	byte[] buf = new byte[64000];
                    DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);
                    //udpSocket_sys.setSoTimeout(2000);
                    System.out.println("...Frame receiving from system... ");
                    dsSys.receive(receivedPacket);
                    System.out.println("...Frame received from system... ");
                	
                    InetAddress mobAddress = ((InetSocketAddress) sockMob.getRemoteSocketAddress()).getAddress();
                    System.out.println(".getRemoteSocketAddress()).getAddress() gives = "+mobAddress);
                    receivedPacket.setAddress(mobAddress);
                    receivedPacket.setPort(remoteUDPPort);
                    dsMob.send(receivedPacket);
                    
                    System.out.println("...Frame forwarded to android..." + "port = " + remoteUDPPort);

                } catch (IOException e) {
        			e.printStackTrace();
        		}
                
                long time2 = System.currentTimeMillis();
                System.out.println("time = " + (time2 - time1));
			}
		} catch (IOException e) {
			
			try {
				if (sockMob !=null)
					sockMob.close();
				if (sockSys != null)
					sockSys.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}
}
