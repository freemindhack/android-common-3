package nocom.common.network;


public class ConnectArguments {
	public String protocol = new String("");
	private String peerIp = new String("");
	private int peerPort = 0x0;
	public int connectTimeout = 5000;/* 5 s */
	protected long recvTimeout = 1000;/* 10 s */
	public boolean reconnect = false;


	public ConnectArguments (String protocol) {
		if (null != protocol) {
			this.protocol = protocol;
		}
	}


	public ConnectArguments (String protocol, boolean reconnect) {
		if (null != protocol) {
			this.protocol = protocol;
		}
		this.reconnect = reconnect;
	}


	public String getPeerIp () {
		return peerIp;
	}


	public void setPeerIp (String peerIp) {
		this.peerIp = peerIp;
	}


	public int getPeerPort () {
		return peerPort;
	}


	public void setPeerPort (int peerPort) {
		this.peerPort = peerPort;
	}
}
