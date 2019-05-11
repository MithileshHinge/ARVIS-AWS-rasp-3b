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
				InetAddress mobUDPIP = mobUDP.getAddress();
				InetAddress sysUDPIP = Main.mobUDPIP2sysUDPIPPortMap.get(mobUDPIP);
				if(!Main.sysUDPIP2mobUDPListenPortMap.contains(sysUDPIP)){
					Main.sysUDPIP2mobUDPListenPortMap.put(sysUDPIP, mobUDP);
					System.out.println("MOBUDPListenRX ...................map entry done" + Main.sysUDPIP2mobUDPListenPortMap.keySet());
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
		}
	}
	
	
}
