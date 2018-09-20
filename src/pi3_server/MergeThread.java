package pi3_server;

import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;

public class MergeThread extends Thread {
	
	public InetAddress sysIP;
	public InetAddress mobIP;
	public CountDownLatch latch = new CountDownLatch(1);
	public MergeThread(){
		
	}
	
	public void run(){
		try {
			latch.await();
			if (sysIP == null || mobIP == null){
				System.out.println("Error: Null IP\n" + "sysIP = " + sysIP.getHostAddress() + " mobIP = " + mobIP.getHostAddress());
				return;
			}
			
			MessageThread messageThread = new MessageThread(sysIP, mobIP);
		} catch (InterruptedException e) {
			//TODO: Disconnect client
			e.printStackTrace();
		}
	}
}
