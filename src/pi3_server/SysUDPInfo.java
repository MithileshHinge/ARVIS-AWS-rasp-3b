package pi3_server;
import java.net.InetAddress;

public class SysUDPInfo {
	InetAddress publicIP, localIP;
	int publicPort, localPort;
	
	public SysUDPInfo(InetAddress publicIP, int publicPort, InetAddress localIP, int localPort) {
		this.publicIP = publicIP;
		this.publicPort = publicPort;
		this.localIP = localIP;
		this.localPort = localPort;
	}
}
