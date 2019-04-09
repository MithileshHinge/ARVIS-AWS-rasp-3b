package pi3_server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentHashMap;

public class ExchangeListen extends Thread {
	private DatagramSocket dsSys, dsMob;
	public static ConcurrentHashMap<InetAddress, Integer> sysIP2MobUdpPortListenMap = new ConcurrentHashMap<>();
	private long time1;
	public ExchangeListen()throws IOException{
		dsSys = new DatagramSocket(Main.PORT_LISTEN_UDP_SYS);
		dsMob = new DatagramSocket(Main.PORT_LISTEN_UDP_MOB);
	}
	
	public void run(){
		while (true) {
			System.out.println("...Sending listen initiated...");
           
            	byte[] receiveData = new byte[4096];   ///1280
	            // ( 1280 for 16 000Hz and 3584 for 44 100Hz (use AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) to get the correct size)
	            DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);
	            
	            try{
					//out_mob.write(1);
            		//out_mob.flush();
	            	System.out.println("...Listen receiving from sys... ");
		            dsSys.receive(receivePacket);
		            System.out.println("...Listen received from sys... time: " + (System.currentTimeMillis() - time1));
		            time1 = System.currentTimeMillis();
		            long time2 = System.currentTimeMillis();
		            new Thread(new Runnable(){
		            	@Override
		            	public void run() {
		            		//System.out.println("In ExchangeAudio MobIP : " + receivePacket.getAddress());
		            		//int remoteUDPPort =  mobIP2SysAudioUdpPortMap.get(receivePacket.getAddress());
		            		int remoteUDPPort = sysIP2MobUdpPortListenMap.get(receivePacket.getAddress());
		            		//System.out.println("In ExchangeAudio SysIP : " + destination);
		            		InetAddress destination = Main.sysIP2mobIP.get(receivePacket.getAddress());
				            receivePacket.setAddress(destination);
				            receivePacket.setPort(remoteUDPPort);
				            
				            //System.out.println("Remote Port : " + remoteUDPPort);
				            try {
								dsMob.send(receivePacket);
							} catch (IOException e) {
								e.printStackTrace();
							}
				            System.out.println(".....audio packet sent...................................port = "+remoteUDPPort + " time: " + (System.currentTimeMillis() - time2));
		            	}
		            }).start();
	            }catch (SocketTimeoutException s) {
	                //System.out.println("Socket timed out!");
	            }catch (IOException e){
	            	System.out.println("............Audio sending closed");
	            	break;
	            }
	    }

	}

}

