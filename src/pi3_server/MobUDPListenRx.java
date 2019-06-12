package pi3_server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;


public class MobUDPListenRx extends Thread{
	public static DatagramSocket dsMob;
	
	
	public MobUDPListenRx() throws SocketException{
		dsMob = new DatagramSocket(Main.PORT_LISTEN_UDP_MOB);
	}
	
	public void run(){
		System.out.println("MOBUDPListenRX STARTED...................");
		while(true){
			byte[] buf = new byte[256];
            DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);
            //udpSocket_sys.setSoTimeout(2000);
            //System.out.println("...MOB UDP HOLE PUNCHING... ");
            try {
				dsMob.receive(receivedPacket);
				InetSocketAddress mobUDP = (InetSocketAddress) receivedPacket.getSocketAddress();
				String hashID = new String(receivedPacket.getData()).trim();
				//System.out.println("...........SysUDPPacketRx IP = .............." + sysUDP);
				if(!Main.hashID2MobUDPPortMap.containsKey(hashID)){
					Main.hashID2MobUDPPortMap.put(hashID,mobUDP);
					System.out.println("...hashID 2 MobUDP FILLED... ");
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
		}
	}
	
	
}
