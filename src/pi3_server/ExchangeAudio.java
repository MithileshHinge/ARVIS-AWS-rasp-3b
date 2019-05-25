package pi3_server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentHashMap;

public class ExchangeAudio extends Thread{
	
	DatagramSocket dataSocket_mob;
	public ExchangeAudio() throws IOException{
		dataSocket_mob = new DatagramSocket(Main.PORT_AUDIO_UDP_MOB);
	}
	
	public void run(){
		
		while (true) {
			byte[] receiveData = new byte[4096];   ///1280
            // ( 1280 for 16 000Hz and 3584 for 44 100Hz (use AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) to get the correct size)
            DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);
            try{
				dataSocket_mob.receive(receivePacket);
	            System.out.println("...Audio received from mob... "+receivePacket.getLength());
	            new Thread(new Runnable(){
	            	@Override
	            	public void run() {
	            		InetAddress mobUDPIP = (InetAddress) receivePacket.getAddress();
	            		if(receivePacket.getLength() < 100 && !Main.mobUDPIP2hashIDMap.containsKey(mobUDPIP)){
	            			//hash ID and mobUDPIP
	            			String hashID = new String(receivePacket.getData()).trim();
	            			Main.mobUDPIP2hashIDMap.put(mobUDPIP,hashID);
	            			System.out.println("EA : MOB 2 HASHID ENTRY DONE ");
	            		} else if(Main.mobUDPIP2hashIDMap.containsKey(mobUDPIP) && !Main.mobUDPIP2sysUDPPortMap.containsKey(mobUDPIP)){
	            			String hashID = Main.mobUDPIP2hashIDMap.get(mobUDPIP);
	            			if(Main.hashID2SysUDPPortMap.containsKey(hashID)){
	            				InetSocketAddress sysUDP = Main.hashID2SysUDPPortMap.get(hashID);
	            				Main.mobUDPIP2sysUDPPortMap.put(mobUDPIP, sysUDP);
	            				Main.sysUDPIP2mobUDPIPMap.put(sysUDP.getAddress(), mobUDPIP);
	            				System.out.println("EA : MOB 2 SYS ENTRY DONE ");
	            			}	
	            		} else if(receivePacket.getLength() > 100 && Main.mobUDPIP2sysUDPPortMap.containsKey(mobUDPIP)){
	            			InetAddress destination = Main.mobUDPIP2sysUDPPortMap.get(mobUDPIP).getAddress();
	            			int remoteUDPPort = Main.mobUDPIP2sysUDPPortMap.get(mobUDPIP).getPort();
	            			receivePacket.setAddress(destination);
	            			receivePacket.setPort(remoteUDPPort);
	            			System.out.println("SYS UDP IP: " + destination + " SYS UDP PORT: " + remoteUDPPort);
	            			try {
				            	// check if mobUDPIP2sysUDPPortMap has the respective entry
								SysUDPPacketRx.dataSocket_system.send(receivePacket);
							} catch (IOException e) {
								e.printStackTrace();
							}
	            		} else{
	            			System.out.println(".....................NA........................");
	            		}
	            	}
	            }).start();
            }catch(IOException e){
            	e.printStackTrace();
            }
	    }
	}	
}
