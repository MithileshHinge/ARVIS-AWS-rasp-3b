package pi3_server;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MessageThread extends Thread {

	private ServerSocket ssSystem, ssMob;
	private Socket sockSystem, sockMob;
	
	private static final byte 
			BYTE_SURV_MODE_ON = 1, 
			BYTE_SURV_MODE_OFF = 3, 
			BYTE_EMAIL_NOTIF_ON = 9, 
			BYTE_EMAIL_NOTIF_OFF = 10,
			BYTE_PLAY_ALARM=7, 
			BYTE_STOP_ALARM=8, 
			BYTE_START_LIVEFEED=2,
			BYTE_STOP_LIVEFEED=4;
	
	MessageThread(){
		try {
			ssSystem = new ServerSocket(Main.PORT_MESSAGE_SYS);
			ssMob = new ServerSocket(Main.PORT_MESSAGE_MOB);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run(){
		
		System.out.println("Message thread started...");
		while(true){
			try {
				sockMob = ssMob.accept();
				sockSystem = ssSystem.accept();
				
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
					try {
						Main.exchangeFrame = new ExchangeFrame();
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(1);
					}
					Main.exchangeFrame.start();
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
				
				}
				
				sockSystem.getOutputStream().write(msg);
				sockSystem.getOutputStream().flush();
				
				sockSystem.getInputStream().read(); //TODO: Error handling can be implemented here (different bytes received for different errors/response, e.g. 1=SUCCESS)
				
				sockMob.getOutputStream().write(1);
				sockMob.getOutputStream().flush();
				
				sockSystem.close();
				sockMob.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}