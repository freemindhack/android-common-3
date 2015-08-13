
package common.network;


import android.os.Handler;


public interface SendRecvDataInteface {
	public SendRecvData getSendRecvData (String data, int whatData,
		Handler sentRcvedMsgHandler);


	public SendRecvData getSendRecvData (byte[] data, int whatData,
		Handler sentRcvedMsgHandler);


	public SendRecvData getSendRecvData (String data, int whatData,
		OnReceived rcvedCallback);


	public SendRecvData getSendRecvData (byte[] data, int whatData,
		OnReceived rcvedCallback);


	public SendRecvData getSendRecvData (byte[] data, int whatData,
		Handler sentRcvedMsgHandler, OnReceived rcvedCallback);
}
