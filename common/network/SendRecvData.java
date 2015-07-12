package nocom.common.network;


import android.os.Handler;


public class SendRecvData {
	public byte[] data = null;
	public int whatData = -1;
	public OnReceived rcvedHandler = null;
	Handler sentRcvedMsgHandler = null;


	protected SendRecvData (String data, int whatData, OnReceived rcvedHandler) {
		if ((null != data) && (data.length() > 0)) {
			this.data = data.getBytes();
		} else {
			this.data = new byte[0];
		}
		this.whatData = whatData;
		this.rcvedHandler = rcvedHandler;
	}


	public SendRecvData () {
	}


	public SendRecvData (SendRecvData cp) {
		if ((null != cp) && (cp.data.length > 0)) {
			this.data = cp.data;
		} else {
			this.data = new byte[0];
		}
		if (null != cp) {
			this.whatData = cp.whatData;
		}
		if (null != rcvedHandler) {
			this.rcvedHandler = cp.rcvedHandler;
		}
	}


	protected SendRecvData (byte[] data, int whatData, OnReceived rcvedHandler) {
		if ((null != data) && (data.length > 0)) {
			this.data = data;
		} else {
			this.data = new byte[0];
		}
		this.whatData = whatData;
		this.rcvedHandler = rcvedHandler;
	}


	protected SendRecvData (byte[] data, int whatData,
		Handler sentRcvedMsgHandler) {
		if ((null != data) && (data.length > 0)) {
			this.data = data;
		} else {
			this.data = new byte[0];
		}
		this.whatData = whatData;
		this.sentRcvedMsgHandler = sentRcvedMsgHandler;
	}


	protected SendRecvData (String data, int whatData,
		Handler sentRcvedMsgHandler) {
		if ((null != data) && (data.length() > 0)) {
			this.data = data.getBytes();
		} else {
			this.data = new byte[0];
		}
		this.whatData = whatData;
		this.sentRcvedMsgHandler = sentRcvedMsgHandler;
	}


	protected SendRecvData (byte[] data, int whatData,
		Handler sentRcvedMsgHandler, OnReceived rcvedCallback) {
		if ((null != data) && (data.length > 0)) {
			this.data = data;
		} else {
			this.data = new byte[0];
		}
		this.whatData = whatData;
		this.sentRcvedMsgHandler = sentRcvedMsgHandler;
		this.rcvedHandler = rcvedCallback;
	}
}
