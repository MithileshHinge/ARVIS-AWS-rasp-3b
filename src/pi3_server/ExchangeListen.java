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

public class ExchangeListen extends Thread{
	
	private DatagramSocket dsSys;
	
	public ExchangeListen() throws IOException{		
		dsSys = new DatagramSocket(Main.PORT_LISTEN_UDP_SYS);
	}
	
	public void run(){
	
		System.out.println("ExchangeListen Thread started....");
		 
		 while(true){
            long time1 = System.currentTimeMillis();
           
            try{	
            	byte[] buf = new byte[4096];
                DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);
                dsSys.receive(receivedPacket);
                System.out.println("...Listen Packet received from system... "+receivedPacket.getLength());
                
                new Thread(new Runnable() {
					
					@Override
					public void run() {
						
						InetAddress sysUDPIP = (InetAddress) receivedPacket.getAddress();
						//System.out.println("ExchangeListen...............map key set : "+Main.sysUDPIP2mobUDPListenPortMap.keySet() + "  " + sysUDPIP);
						if(receivedPacket.getLength() < 100 && !Main.sysUDPIP2hashIDMap.containsKey(sysUDPIP)){
	            			String hashID = new String(receivedPacket.getData()).trim();
	            			Main.sysUDPIP2hashIDMap.put(sysUDPIP,hashID);
	            			System.out.println("EL : MOB 2 HASHID ENTRY DONE ");
	            		} else if(Main.sysUDPIP2hashIDMap.containsKey(sysUDPIP) && !Main.sysUDPIP2mobUDPPortMap.containsKey(sysUDPIP)){
	            			String hashID = Main.sysUDPIP2hashIDMap.get(sysUDPIP);
	            			if(Main.hashID2MobUDPPortMap.containsKey(hashID)){
	            				InetSocketAddress mobUDP = Main.hashID2MobUDPPortMap.get(hashID);
	            				Main.sysUDPIP2mobUDPPortMap.put(sysUDPIP, mobUDP);
	            				Main.mobUDPIP2sysUDPIPMap.put(mobUDP.getAddress(),sysUDPIP);
	            				System.out.println("EL : MOB 2 SYS ENTRY DONE ");
	            			}
	            		} else if(receivedPacket.getLength() > 100 && Main.sysUDPIP2mobUDPPortMap.containsKey(sysUDPIP)){
	            			InetAddress destination = Main.sysUDPIP2mobUDPPortMap.get(sysUDPIP).getAddress();
	            			int remoteUDPPort = Main.sysUDPIP2mobUDPPortMap.get(sysUDPIP).getPort();
	            			receivedPacket.setAddress(destination);
	            			receivedPacket.setPort(remoteUDPPort);
	            			System.out.println("SYS UDP IP: " + destination + " SYS UDP PORT: " + remoteUDPPort);
	            			try {
				            	// check if mobUDPIP2sysUDPPortMap has the respective entry
								MobUDPListenRx.dsMob.send(receivedPacket);
							} catch (IOException e) {
								e.printStackTrace();
							}
	            		} else{
	            			System.out.println(".....................NA........................");
	            		}
					}
				}).start();
                

            } catch (IOException e) {
    			e.printStackTrace();
    			System.out.println("Exchange Listen Sys catch");
    		}
            
            //long time2 = System.currentTimeMillis();
            //System.out.println("time = " + (time2 - time1));
		}
	}
}
