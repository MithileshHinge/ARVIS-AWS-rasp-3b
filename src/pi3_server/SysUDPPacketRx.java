package pi3_server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class SysUDPPacketRx extends Thread{
public static DatagramSocket dataSocket_system;
	
	
	public SysUDPPacketRx() throws SocketException{
		dataSocket_system = new DatagramSocket(Main.PORT_AUDIO_UDP_SYS);
	}
	
	public void run(){
		System.out.println("SysUDPPacketRx STARTED...................");
		while(true){
			byte[] buf = new byte[64];
            DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);
            //udpSocket_sys.setSoTimeout(2000);
            System.out.println("...SYS UDP HOLE PUNCHING... ");
            try {
				dataSocket_system.receive(receivedPacket);
				InetSocketAddress sysUDP = (InetSocketAddress) receivedPacket.getSocketAddress();
				System.out.println("...........SysUDPPacketRx IP = .............." + sysUDP);
				// receive system udp port
				// extract sys UDP IP
				InetSocketAddress mobUDP = Main.sysUDPIP2mobUDPPortMap.get(sysUDP.getAddress());
				// get mob UDP address and then its IP from sysUDPIP2MobUDPmap
				if(!Main.mobUDPIP2sysUDPPortMap.containsKey(mobUDP)){
					Main.mobUDPIP2sysUDPPortMap.put(mobUDP.getAddress(), sysUDP);
					System.out.println("...........mobIP2AudioUDPMap entry done..............");
				}
				// Fill mobUDPIP2sysUDPPortMap
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
		}
	}
}
