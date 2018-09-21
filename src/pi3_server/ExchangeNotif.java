package pi3_server;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

import javax.imageio.ImageIO;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class ExchangeNotif extends Thread{
	
	static ServerSocket ssNotif_sys, ssNotif_mob, ssFrame_sys, ssFrame_mob;
	static Socket socketNotif_sys, socketNotif_mob, socketFrame_sys, socketFrame_mob;
	public BufferedImage notifFrame;
	public String storeActivityName, registrationToken= "fdzX3k4Y8hY:APA91bHmeMXSz0bBGM2yvIw5MlWS6CVZcUNEUNyT5iOW4BiDx_EqWbzg2L3LqdscSTCmKpn4OOzl8jfWPDZLs41Ak6aSts_qg4QpTuTaURrnDjgQ0xD4RAv5vlxSvWLfGuWStb4xnsVTRi6ZjEhuocXSAAE9byCBVA";
	
	public Boolean sendNotif = true;	// Turns true when FCM reg token of app is received!
	static String serverKey = "AIzaSyDIAmdjp7GnZ-LZX9G3NFWnzsFKPvnFpUY";
	final static private String FCM_URL = "https://fcm.googleapis.com/fcm/send";
	BufferedImage image;
	
	public ExchangeNotif(){
		FileInputStream serviceAccount;
		try {
			ssNotif_sys = new ServerSocket(Main.PORT_NOTIF_SYS);
			ssNotif_sys.setSoTimeout(0);
			ssNotif_mob = new ServerSocket(Main.PORT_NOTIF_MOB);
			ssNotif_mob.setSoTimeout(0);
			
			ssFrame_sys = new ServerSocket(Main.PORT_NOTIF_FRAME_SYS);
			ssFrame_sys.setSoTimeout(3000);
			ssFrame_mob = new ServerSocket(Main.PORT_NOTIF_FRAME_MOB);
			ssFrame_mob.setSoTimeout(3000);		
			
			//FCM app initialization
			serviceAccount = new FileInputStream("/home/ubuntu/magiceye-abfb5-firebase-adminsdk-ml2gu-8b64f71342.json");
			FirebaseOptions options = new FirebaseOptions.Builder()
				    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
				    .setDatabaseUrl("https://magiceye-abfb5.firebaseio.com/")
				    .build();
			FirebaseApp.initializeApp(options);
			System.out.println("...Firebase initialized...");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("....exchgNotif constructor problem...");
		}
	}
	
	public void run(){
		
		// Receive the FCM token from app
		System.out.println("...TCP for FCM token from app initialized...");
		/*try {
			socketNotif_mob = ssNotif_mob.accept();
			System.out.println("#######..........Client Sapadla!!!!!!");
			
	        BufferedReader br = new BufferedReader(new InputStreamReader(socketNotif_mob.getInputStream()));
	        registrationToken = br.readLine();
		
	        System.out.println("#######..........Kahi tari bhetla!!!!!!");
			if(registrationToken != null){
				sendNotif = true;
				System.out.println("...Valid registration token received..." + registrationToken +"	.toString yields :	"+registrationToken.toString());
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
				
		while(true){
			
			try {
				if(sendNotif){
					System.out.println("Ready to send notifications");
					socketNotif_sys = ssNotif_sys.accept();
					int p = socketNotif_sys.getInputStream().read();
					socketNotif_sys.getOutputStream().write(1);
					socketNotif_sys.getOutputStream().flush();
					
					
					if (p == Main.BYTE_FACEFOUND_VDOGENERATING || p == Main.BYTE_ALERT1) {
						ExchangeVideo.startVdoServer_sys = true;
						System.out.println("1st notif received from sys..........................");
						socketFrame_sys = ssFrame_sys.accept();
						image = ImageIO.read(socketFrame_sys.getInputStream());
						socketFrame_sys.close();
					}
					if (p == Main.BYTE_FACEFOUND_VDOGENERATED || p == Main.BYTE_ALERT2 || p == Main.BYTE_ABRUPT_END || p == Main.BYTE_LIGHT_CHANGE){
						DataInputStream din_activity = new DataInputStream(socketNotif_sys.getInputStream());
						storeActivityName = din_activity.readUTF();
						ExchangeVideo.startVdoServer_sys = false;
						System.out.println("2nd vdo generated notif received.......................");
					}
					DataInputStream din_note = new DataInputStream(socketNotif_sys.getInputStream());
					int myNotifId = din_note.readInt();
					System.out.println("Notifthread value of notifId is " + myNotifId);
					
					//while(!ExchangeVideo.startVdoServer_sys){}
					socketNotif_sys.getOutputStream().write(9);
					socketNotif_sys.getOutputStream().flush();
					
					socketNotif_sys.close();
					
					/////////////////// Forwarding to android app
					send_FCM_Notification(registrationToken,serverKey,p,storeActivityName,myNotifId,image);
				}
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	static void send_FCM_Notification(String tokenId, String server_key, int p, String activityName, int NotifId, BufferedImage image){
		try{
			// Create URL instance.
			URL url = new URL(FCM_URL);
			
			// create connection.
			HttpURLConnection conn;
			conn = (HttpURLConnection) url.openConnection();
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			//set method as POST or GET
			conn.setRequestMethod("POST");
			//pass FCM server key
			conn.setRequestProperty("Authorization","key="+server_key);
			//Specify Message Format
			conn.setRequestProperty("Content-Type","application/json");
			
			//Create JSON Object & pass value
			/*JSONObject infoJson = new JSONObject();
			infoJson.put("title","Notif title");
			infoJson.put("body", "Notif body");*/
			
			JSONObject dataJson = new JSONObject();
			dataJson.put("NotifByte", p);
			dataJson.put("NotifId", NotifId);
			System.out.println("..........Prepared 1st notif json object for app");
			if (p == Main.BYTE_FACEFOUND_VDOGENERATED || p == Main.BYTE_ALERT2 || p == Main.BYTE_ABRUPT_END || p == Main.BYTE_LIGHT_CHANGE){	
				dataJson.put("date",activityName);
				System.out.println("..........Prepared 2nd notif json object for app");
			}
			JSONObject json = new JSONObject();
			json.put("to",tokenId.trim());
			//json.put("notification", infoJson);
			json.put("data",dataJson);
			System.out.println("json length = "+json.length());

			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(json.toString());
			wr.flush();
			int status = 0;
			if( null != conn ){
				status = conn.getResponseCode();
			}
			if( status != 0){
				if( status == 200 ){
					//SUCCESS message
					BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					System.out.println("Android Notification Response : " + reader.readLine());
				}else if(status == 401){
					//client side error
					System.out.println("Notification Response : TokenId : " + tokenId + " Error occurred :");
				}else if(status == 501){
					//server side error
					System.out.println("Notification Response : [ errorCode=ServerError ] TokenId : " + tokenId);
				}else if( status == 503){
					//server side error
					System.out.println("Notification Response : FCM Service is Unavailable  TokenId : " + tokenId);
				}
			}
		}catch(MalformedURLException mlfexception){
			// Protocol Error
			System.out.println("Error occurred while sending push Notification!.." + mlfexception.getMessage());
		}catch(IOException mlfexception){
			//URL problem
			System.out.println("Reading URL, Error occurred while sending push Notification!.." + mlfexception.getMessage());
		}catch(JSONException jsonexception){
			//Message format error
			System.out.println("Message Format, Error occurred while sending push Notification!.." + jsonexception.getMessage());
		}catch (Exception exception) {
			//General Error or exception.
			System.out.println("Error occurred while sending push Notification!.." + exception.getMessage());
		}
		
		//write the frame
		if (p == Main.BYTE_FACEFOUND_VDOGENERATING || p == Main.BYTE_ALERT1){
			try {
				System.out.println("...Sending key image for notification to app...");
				socketFrame_mob = ssFrame_mob.accept();
				System.out.println("#######..........Client Sapadla!!!!!!");
				
				OutputStream out = socketFrame_mob.getOutputStream();
				ImageIO.write(image, "jpg", out);
				
				socketFrame_mob.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

}
