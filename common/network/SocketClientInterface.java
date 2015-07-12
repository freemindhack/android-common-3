package nocom.common.network;


import android.os.Handler;


public interface SocketClientInterface {
	/*
	 * ============================
	 * main interface
	 */
	public boolean sendData (SendRecvData data, boolean sendNow);


	public boolean isUseBEOnlyNoAvailable ();


	/*
	 * ============================
	 * configurations
	 */
	/*
	 * sendRoutineTimeslice
	 * -- description:
	 * - msec (millisecond)
	 */
	public final long sendRoutineTimeslice = 200;


	/*
	 * ============================
	 * pure virtual
	 */
	public void backendOnReceived (byte[] data, int count, int whatData);


	public void backendOnSent (byte[] data, int count, int whatData);


	public byte[] getDataIdFromRcved (byte[] rcved, int count);


	/*
	 * getHeartbeat
	 * 
	 * -- description:
	 * - the min time interval of call getHeartbeat
	 *   in the send routine
	 *   is "sendRoutineTimeslice"
	 * - and call getHeartbeat only when current send
	 *   buffers null
	 */
	public int getHeartbeat (byte[] outHeartbeat, int maxOutSize);


	/*
	 * if return true and ... not null will call ...
	 */
	public boolean onReceivedMsgHandlerFilter (byte[] data, int count,
		int whatData);


	public boolean onReceivedFilter (byte[] data, int count, int whatData);


	public boolean onFindRcvHandlerFilter ();


	/*
	 * public what-data
	 */
	public static final int WHAT_MSG_RECVED = 0xfffff0 + 0x1;
	public static final int WHAT_MSG_HEARTBEAT = 0xfffff0 + 0x2;
	/* == */
	public static final String connectStateKeyStr = "connectStateKey";
	public static final int connectStateKey = 0xff * 0xb1;
	public static final String receivedKeyStr = "receivedFromPeerHexStr";
	public static final String receivedRawKeyStr = "receivedRaw";
	public static final String sentKeyStr = "sentKeyStr";
	public static final int SOCKET_STATE_IDLE = 0x0;
	public static final int SOCKET_STATE_CONNECT_TIMEOUT = 0x1;
	public static final int SOCKET_STATE_CONNECT_ONLINE = SOCKET_STATE_CONNECT_TIMEOUT + 1;
	public static final int SOCKET_STATE_CONNECT_ABORT = SOCKET_STATE_CONNECT_ONLINE + 2;
	public static final int SOCKET_STATE_SEND_OFFLINE = 0x10;
	public static final int SOCKET_STATE_SEND_BROKEN = SOCKET_STATE_SEND_OFFLINE + 1;/* disconnected */
	public static final int SOCKET_STATE_SEND_ABORT = SOCKET_STATE_SEND_BROKEN + 1;
	public static final int SOCKET_STATE_RECV_OFFLINE = 0x20;
	public static final int SOCKET_STATE_RECV_BROKEN = SOCKET_STATE_RECV_OFFLINE + 1;/* disconnected */
	public static final int SOCKET_STATE_RECV_ABORT = SOCKET_STATE_RECV_BROKEN + 1;
	public static final int SOCKET_STATE_FI_DESTROYED = 0x100;


	public int getSocketClientConnectState ();


	public void setSocketClientConnectState (int socketClientConnectState);


	public boolean isConnected ();


	public boolean setStateChangedHandler (Handler stateChangedHandler);


	public void clearStateChangedHandler ();


	public void finish ();


	/* ERRNO */
	public static final int ERRNO_NO_ERROR = 0;
	public static final int ERRNO_INVALID_CONNECT_ARGUMENTS = 1;
	public static final int ERRNO_ALREADY = 2;
	public static final int ERRNO_INVALID_PROT_ARG = 3;
	public static final int ERRNO_ABORT = 4;
	public static final int ERRNO_CONNECT_TIMEOUT = 5;
	public static final int ERRNO_CONNECT_ERROR = 6;
}
