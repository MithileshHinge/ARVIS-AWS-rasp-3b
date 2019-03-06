package pi3_server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
	
	public static final int 
			PORT_CONN_SYS=6660,
			PORT_CONN_MOB=7660,
			PORT_MESSAGE_SYS=6676,
			PORT_MESSAGE_MOB=7676,
			PORT_NOTIF_SYS=6667,
			PORT_NOTIF_MOB=7667,
			PORT_NOTIF_VIDEO_SYS=6668,
			PORT_NOTIF_VIDEO_MOB=7668,
			PORT_NOTIF_FRAME_SYS=6669,
			PORT_NOTIF_FRAME_MOB=7669,
			PORT_LIVEFEED_TCP_SYS=6666,
			PORT_LIVEFEED_TCP_MOB=7666,
			PORT_LIVEFEED_UDP_SYS=6663,
			PORT_LIVEFEED_UDP_MOB=7663,
			PORT_AUDIO_TCP_SYS=6670,
			PORT_AUDIO_TCP_MOB=7670,
			PORT_AUDIO_UDP_SYS=6671,
			PORT_AUDIO_UDP_MOB=7671,
			PORT_PERSON_DETECT_SYS=6672,
			PORT_PERSON_DETECT_GPU=5672,
			PORT_PERSON_DETECT_GPU2=5673;

	public static final byte 
			BYTE_FACEFOUND_VDOGENERATING = 1, 
			BYTE_FACEFOUND_VDOGENERATED = 2, 
			BYTE_ALERT1 = 3, 
			BYTE_ALERT2 = 4, 
			BYTE_ABRUPT_END = 5, 
			BYTE_LIGHT_CHANGE = 6;

	public static ExchangeFrame exchangeFrame;
	//public static ConcurrentHashMap<String, MergeThread> mergeThreadsMap = new ConcurrentHashMap<>();
	//public static ConcurrentHashMap<InetAddress, Socket> mobIP2connMobSock = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<InetAddress, InetAddress> mobIP2sysIP = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<InetAddress, InetAddress> sysIP2mobIP = new ConcurrentHashMap<InetAddress, InetAddress>();
	public static ConcurrentHashMap<String, ConnectSysThread> connSysThreadsMap = new ConcurrentHashMap<>();
	
	//public static ConcurrentHashMap<String, String> hashID2emailID = new ConcurrentHashMap<>();
	//public static ConcurrentHashMap<String, InetAddress> hashID2sysIPMap= new ConcurrentHashMap<>();
	//public static ConcurrentHashMap<String, InetAddress> hashID2mobIPMap= new ConcurrentHashMap<>();
	public static UserDatabaseHandler db = new UserDatabaseHandler();
	private static ServerSocket connSysSS = null;
	private static ServerSocket connMobSS = null;
	public static volatile Socket sockGPU = null;
	public static ServerSocket ssGPU2;
	public static SimpleDateFormat ft = new SimpleDateFormat("yyyy_MM_dd'at'hh_mm_ss_a");
	
	public static void main(String[] args) {
		
		try {
			connSysSS = new ServerSocket(PORT_CONN_SYS);
			connMobSS = new ServerSocket(PORT_CONN_MOB);
			
			ssGPU2 = new ServerSocket(PORT_PERSON_DETECT_GPU2);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		new Thread(new Runnable(){
			public void run(){
				while(true){
					try {
						Socket connMobSock = connMobSS.accept();
						ConnectMobThread connMobThread = new ConnectMobThread(connMobSock);
						connMobThread.start();
						System.out.println("conn mob new thread started");
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
				
			}
		}).start();
		
		try {
			ServerSockThread servSockMsgSysThread = new ServerSockThread(PORT_MESSAGE_SYS);
			ServerSockThread servSockMsgMobThread = new ServerSockThread(PORT_MESSAGE_MOB);
			ServerSockThread servSockLivefeedSysThread = new ServerSockThread(PORT_LIVEFEED_TCP_SYS);
			ServerSockThread servSockLivefeedMobThread = new ServerSockThread(PORT_LIVEFEED_TCP_MOB);
			ServerSockThread servSockAudioSysThread = new ServerSockThread(PORT_AUDIO_TCP_SYS);
			ServerSockThread servSockAudioMobThread = new ServerSockThread(PORT_AUDIO_TCP_MOB);
			ServerSockThread servSockDetectPersonSysThread = new ServerSockThread(PORT_PERSON_DETECT_SYS);
			ServerSockThread servSockDetectPersonGPUThread = new ServerSockThread(PORT_PERSON_DETECT_GPU);
			ServerSockThread servSockVideoSysThread = new ServerSockThread(PORT_NOTIF_VIDEO_SYS);
			ServerSockThread servSockVideoMobThread = new ServerSockThread(PORT_NOTIF_VIDEO_MOB);
			

			servSockMsgSysThread.start();
			servSockMsgMobThread.start();
			servSockLivefeedSysThread.start();
			servSockLivefeedMobThread.start();
			servSockAudioSysThread.start();
			servSockAudioMobThread.start();
			servSockDetectPersonSysThread.start();
			servSockDetectPersonGPUThread.start();
			servSockVideoSysThread.start();
			servSockVideoMobThread.start();
			
		} catch (IOException e2) {
			e2.printStackTrace();
			return;
		}
		
		
		
		
		try {
			exchangeFrame = new ExchangeFrame();
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		exchangeFrame.start();
		
		while(true){
			
			try {
				Socket connSysSock = connSysSS.accept();
				ConnectSysThread connSysThread = new ConnectSysThread(connSysSock);
				connSysThread.start();
				System.out.println("conn sys new thread started");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}