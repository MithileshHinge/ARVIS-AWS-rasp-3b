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
                System.out.println("...Listen Packet received from system... ");
                
                new Thread(new Runnable() {
					
					@Override
					public void run() {
						InetAddress sysUDPIP = (InetAddress) receivedPacket.getAddress();
						System.out.println("ExchangeListen...............map key set : "+Main.sysUDPIP2mobUDPListenPortMap.keySet() + "  " + sysUDPIP);
						if(Main.sysUDPIP2mobUDPListenPortMap.containsKey(sysUDPIP)){
							InetSocketAddress mobAddress = Main.sysUDPIP2mobUDPListenPortMap.get(sysUDPIP);
							System.out.println("MOB UDP ADDRESS SET: " + mobAddress);
		                    receivedPacket.setAddress(mobAddress.getAddress());
		                    receivedPacket.setPort(mobAddress.getPort());
	                    
		                    try {
		                    	System.out.println("dsMob: " + MobUDPListenRx.dsMob + " dsSys: " + dsSys);
		                    	MobUDPListenRx.dsMob.send(receivedPacket);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
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
