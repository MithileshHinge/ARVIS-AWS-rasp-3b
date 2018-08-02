package pi3_server;

import java.io.IOException;

public class Main {
	
	public static final int 
			PORT_MESSAGE_SYS=6676,
			PORT_MESSAGE_MOB=7676,
			PORT_NOTIF_SYS=6667,
			PORT_NOTIF_FRAME_SYS=6669,
			PORT_NOTIF_MOB=7667,
			PORT_NOTIF_FRAME_MOB=7669,
			PORT_LIVEFEED_TCP_SYS=6666,
			PORT_LIVEFEED_UDP_SYS=6663,
			PORT_LIVEFEED_TCP_MOB=7666,
			PORT_LIVEFEED_UDP_MOB=7663,
			PORT_TCP_AUDIO_SYS=6670,
			PORT_UDP_AUDIO_SYS=6671,
			PORT_TCP_AUDIO_MOB=7670,
			PORT_UDP_AUDIO_MOB=7671;

	public static final byte 
			BYTE_FACEFOUND_VDOGENERATING = 1, 
			BYTE_FACEFOUND_VDOGENERATED = 2, 
			BYTE_ALERT1 = 3, 
			BYTE_ALERT2 = 4, 
			BYTE_ABRUPT_END = 5, 
			BYTE_LIGHT_CHANGE = 6;

	public static ExchangeFrame exchangeFrame;
	
	public static void main(String[] args) {
		MessageThread msgThread = new MessageThread();
		msgThread.start();
		
		try {
			exchangeFrame = new ExchangeFrame();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
