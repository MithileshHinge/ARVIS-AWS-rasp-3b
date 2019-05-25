package pi3_server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;


public class MobUDPPacketRx extends Thread{
	public static DatagramSocket dsMob;
	
	
	public MobUDPPacketRx() throws SocketException{
		dsMob = new DatagramSocket(Main.PORT_AUDIO_UDP_MOB);
	}
	
	public void run(){
		System.out.println("MOBUDPPacketRX STARTED...................");
		String hashID = null;
		while(true){
			byte[] buf = new byte[64];
            DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);
            //udpSocket_sys.setSoTimeout(2000);
            //System.out.println("...MOB UDP HOLE PUNCHING... " + hashID);
            try {
				dsMob.receive(receivedPacket);
				hashID = new String(receivedPacket.getData()).trim();
				InetSocketAddress mobUDP = (InetSocketAddress) receivedPacket.getSocketAddress();
				/*if(!Main.hashID2MobUDPMap.containsKey(hashID)){
					Main.hashID2MobUDPMap.put(hashID, mobUDP);
					System.out.println("...........hashID2MobUDPMap entry done..............");
				}*/	
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
		}
	}
	
	
}
