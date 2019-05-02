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
	
	//ServerSocket ss_system,ss_mob;
	//Socket socket_system,socket_mob;
	//OutputStream out_sys,out_mob;
	//InputStream in_sys,in_mob;
	DatagramSocket dataSocket_mob;
	//public static ConcurrentHashMap<InetAddress, Integer> mobIP2SysAudioUdpPortMap = new ConcurrentHashMap<>();
	
	//public ExchangeAudio(InetAddress sysIP, InetAddress mobIP) throws IOException{
	public ExchangeAudio() throws IOException{
		/*ss_mob = new ServerSocket();
		ss_mob.bind(new InetSocketAddress(sysIP, Main.PORT_AUDIO_TCP_MOB));
		ss_mob.setSoTimeout(0);
		
		ss_system = new ServerSocket();
		ss_system.bind(new InetSocketAddress(mobIP, Main.PORT_AUDIO_TCP_SYS));
		ss_system.setSoTimeout(0);
		*/
		
        dataSocket_mob = new DatagramSocket(Main.PORT_AUDIO_UDP_MOB);
        //dataSocket_mob.setSoTimeout(500);
        
        
	}
	
	public void run(){
		//System.out.println(String.format("Receiving Audio started"));
		
		while (true) {
			System.out.println("...Sending audio initiated...");
			
			//socket_mob=ss_mob.accept();
			//System.out.println("...Sending audio requested...");
			
			/*socket_system = ss_system.accept();
			
			InetAddress destination = socket_system.getInetAddress();
			System.out.println(String.format("....................................................system connected for sending audio"));
		
			out_mob = socket_mob.getOutputStream();
			in_mob = socket_mob.getInputStream();
			
			out_sys = socket_system.getOutputStream();
			in_sys = socket_system.getInputStream();
			
			int p=in_mob.read();
			out_sys.write(p);
			
			if(p==1)
			{
				p = 0;
				System.out.println(String.format(".................p=1 received"));
				in_sys.read();
				out_mob.write(2);
			}else{
				continue;
			}*/
            
            while (true) {
            	
            	byte[] receiveData = new byte[4096];   ///1280
	            // ( 1280 for 16 000Hz and 3584 for 44 100Hz (use AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) to get the correct size)
	            
	            //System.out.println(String.format("here"));
	            DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);
	            //System.out.println(String.format("...........................................................here"));
	            try{
					//out_mob.write(1);
            		//out_mob.flush();
	            	dataSocket_mob.receive(receivePacket);
		            System.out.println("...Audio received from mob... ");
		            new Thread(new Runnable(){
		            	@Override
		            	public void run() {
		            		/*int remoteUDPPort =  mobIP2SysAudioUdpPortMap.get(receivePacket.getAddress());
		            		InetAddress destination = Main.mobIP2sysIP.get(receivePacket.getAddress());*/
		            		 
		            		InetAddress mobIP = receivePacket.getAddress();
		            		System.out.println("In ExchangeAudio MobIP : " + mobIP + " " + Main.mobUDPIP2sysUDPPortMap.containsKey(mobIP));
		            		if(Main.mobUDPIP2sysUDPPortMap.containsKey(mobIP)){
		            			InetAddress destination = Main.mobUDPIP2sysUDPPortMap.get(mobIP).getAddress();
		            			int remoteUDPPort = Main.mobUDPIP2sysUDPPortMap.get(mobIP).getPort();
		            			receivePacket.setAddress(destination);
		            			receivePacket.setPort(remoteUDPPort);
		            			System.out.println("SYS UDP IP: " + destination + " SYS UDP PORT: " + remoteUDPPort);
		            			try {
					            	// check if mobUDPIP2sysUDPPortMap has the respective entry
									SysUDPPacketRx.dataSocket_system.send(receivePacket);
								} catch (IOException e) {
									e.printStackTrace();
								}
		            		}
		            		//System.out.println("In ExchangeAudio SysIP : " + destination);
				            //receivePacket.setPort(Main.PORT_AUDIO_UDP_SYS);
				            //System.out.println("Remote Port : " + remoteUDPPort);
				            
				            
				            //System.out.println(".....audio packet sent...................................port = "+remoteUDPPort);
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

	
}
