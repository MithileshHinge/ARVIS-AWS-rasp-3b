package pi3_server;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class MessageThread extends Thread {

	//private ServerSocket ssSys, ssMob;
	private Socket sockSys, sockMob;
	
	private static final byte 
			BYTE_SURV_MODE_ON = 1, 
			BYTE_SURV_MODE_OFF = 3, 
			BYTE_EMAIL_NOTIF_ON = 9, 
			BYTE_EMAIL_NOTIF_OFF = 10,
			BYTE_PLAY_ALARM=7, 
			BYTE_STOP_ALARM=8, 
			BYTE_START_LIVEFEED=2,
			BYTE_START_LISTEN=5,
			//BYTE_STOP_LIVEFEED=4,
			BYTE_START_VIDEO_DOWNLOAD = 14;
			
	
	MessageThread(Socket sockSys, Socket sockMob){
		/*try {
			ssSys = new ServerSocket();
			ssSys.bind(new InetSocketAddress(addrSys, Main.PORT_MESSAGE_SYS));
			ssMob = new ServerSocket();
			ssMob.bind(new InetSocketAddress(addrMob, Main.PORT_MESSAGE_MOB));
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		this.sockSys = sockSys;
		this.sockMob = sockMob;
	}
	
	public void run(){
		
		System.out.println("Message thread started...");
			try {
				//sockSys = ssSys.accept();
				//sockMob = ssMob.accept();
				
				InputStream mobIn = sockMob.getInputStream();
				int msg = mobIn.read();
				switch(msg){

				case BYTE_SURV_MODE_ON:
					System.out.println("surveillance mode ON");
					break;
				case BYTE_SURV_MODE_OFF:
					System.out.println("Normal CCTV recording mode");
					break;
				case BYTE_EMAIL_NOTIF_ON:
					System.out.println("......email notif turned ON.....");
					break;

				case BYTE_EMAIL_NOTIF_OFF:
					System.out.println("......email notif turned OFF.....");
					break;
					
				case BYTE_START_LIVEFEED:
					System.out.println("@@@@@@@@@@@@@@@@@Live Feed on kela..........................");
					
					/*try {
						ExchangeFrame exchangeFrame = new ExchangeFrame(sockSys.getInetAddress(), sockMob.getInetAddress());
						exchangeFrame.start();
					} catch (IOException e) {
						e.printStackTrace();
						break;
					}
					*/
					break;
					
				case BYTE_START_LISTEN:
					System.out.println("@@@@@@@@@@@@@@@@@Listen on kela..........................");
					break;
				/*case BYTE_START_LISTEN:
					System.out.println("@@@@@@@@@@@@@@@@@Listen on kela.............................");
					SendingFrame.listen = true;
					break;
				case BYTE_STOP_LISTEN:
					System.out.println("@@@@@@@@@@@@@@@@@Listen off kela.............................");
					SendingFrame.listen = false;
					break;*/
				case BYTE_PLAY_ALARM:
					System.out.println("#########################   Alarm on kela ");
					break;
				case BYTE_STOP_ALARM:
					System.out.println("#########################   Alarm off kela ");
					break;
				case BYTE_START_VIDEO_DOWNLOAD:
					System.out.println("#######################   Video download request ");
					break;
				
				}
				
				sockSys.getOutputStream().write(msg);
				sockSys.getOutputStream().flush();
				System.out.println("............................msg thread - byte sent to sys................."+msg);
				
				int ptemp = sockSys.getInputStream().read(); //TODO: Error handling can be implemented here (different bytes received for different errors/response, e.g. 1=SUCCESS)
				System.out.println("............................msg thread - ack received from sys................." + ptemp);
				
				sockMob.getOutputStream().write(1);
				sockMob.getOutputStream().flush();
				System.out.println("............................msg thread - ack sent to mob.................");
				
				sockSys.close();
				sockMob.close();
				
			} catch (IOException e) {
				e.printStackTrace();
				try {
					sockMob.getOutputStream().write(0);
					sockMob.getOutputStream().flush();
					System.out.println("............................msg thread - ack sent to mob....................");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
	}
}