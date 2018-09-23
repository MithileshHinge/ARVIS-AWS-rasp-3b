package pi3_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSockThread extends Thread {

	ServerSocket ss;
	int port;
	
	ServerSockThread(int port) throws IOException{
		this.port = port;
		ss = new ServerSocket(port);
	}
	
	public void run(){
		while(true){
			try {
				Socket sock = ss.accept();
				
				switch (port){
				case Main.PORT_LIVEFEED_TCP_MOB:
					
					break;
				case Main.PORT_LIVEFEED_TCP_SYS:
					
					break;
				case Main.PORT_MESSAGE_MOB:
					
					break;
				case Main.PORT_MESSAGE_SYS:
					
					break;
				case Main.PORT_NOTIF_FRAME_MOB:
					
					break;
				case Main.PORT_NOTIF_FRAME_SYS:
					
					break;
				case Main.PORT_NOTIF_MOB:
					
					break;
				case Main.PORT_NOTIF_SYS:
					
					break;
				case Main.PORT_TCP_AUDIO_MOB:
					
					break;
				case Main.PORT_TCP_AUDIO_SYS:
					
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}
