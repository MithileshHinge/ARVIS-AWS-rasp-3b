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
            //System.out.println("...SYS UDP HOLE PUNCHING... ");
            try {
				dataSocket_system.receive(receivedPacket);
				InetSocketAddress sysUDP = (InetSocketAddress) receivedPacket.getSocketAddress();
				String hashID = new String(receivedPacket.getData()).trim();
				//System.out.println("...........SysUDPPacketRx IP = .............." + sysUDP);
				if(!Main.hashID2SysUDPPortMap.containsKey(hashID)){
					Main.hashID2SysUDPPortMap.put(hashID,sysUDP);
					System.out.println("...hashID 2 SysUDP FILLED... ");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
            
		}
	}
}
