package pi3_server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
	
	public static final int 
			PORT_CONN_SYS=6660,
			PORT_CONN_MOB=7660,
			PORT_MESSAGE_SYS=6676,
			PORT_MESSAGE_MOB=7676,
			PORT_NOTIF_SYS=6667,
			PORT_NOTIF_MOB=7667,
			PORT_NOTIF_FRAME_SYS=6669,
			PORT_NOTIF_FRAME_MOB=7669,
			PORT_LIVEFEED_TCP_SYS=6666,
			PORT_LIVEFEED_TCP_MOB=7666,
			PORT_LIVEFEED_UDP_SYS=6663,
			PORT_LIVEFEED_UDP_MOB=7663,
			PORT_AUDIO_TCP_SYS=6670,
			PORT_AUDIO_TCP_MOB=7670,
			PORT_AUDIO_UDP_SYS=6671,
			PORT_AUDIO_UDP_MOB=7671;

	public static final byte 
			BYTE_FACEFOUND_VDOGENERATING = 1, 
			BYTE_FACEFOUND_VDOGENERATED = 2, 
			BYTE_ALERT1 = 3, 
			BYTE_ALERT2 = 4, 
			BYTE_ABRUPT_END = 5, 
			BYTE_LIGHT_CHANGE = 6;

	public static ExchangeFrame exchangeFrame;
	public static ConcurrentHashMap<String, MergeThread> mergeThreadsMap = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<String, ConnectSysThread> connSysThreadsMap = new ConcurrentHashMap<>();
	public static UserDatabaseHandler db = new UserDatabaseHandler();
	private static ServerSocket connSysSS = null;
	private static ServerSocket connMobSS = null;
	
	public static void main(String[] args) {
		
		try {
			connSysSS = new ServerSocket(PORT_CONN_SYS);
			connMobSS = new ServerSocket(PORT_CONN_MOB);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		new Thread(new Runnable(){
			public void run(){
				try {
					Socket connMobSock = connMobSS.accept();
					ConnectMobThread connMobThread = new ConnectMobThread(connMobSock);
					connMobThread.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}).start();
		
		while(true){
			
			try {
				Socket connSysSock = connSysSS.accept();
				ConnectSysThread connSysThread = new ConnectSysThread(connSysSock);
				connSysThread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}	
	}
}