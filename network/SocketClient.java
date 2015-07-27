
package common.network;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.regex.Pattern;


import common.datastructure.CompareMethod;
import common.datastructure.MyQueue;
import common.utils.StringUtils;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


public abstract class SocketClient implements SocketClientInterface {
	private Socket socketClient = null;

	private Thread onSocketThread = null;

	private Thread onReceiveThread = null;

	private Thread onSendThread = null;

	private InputStream thisInputStream = null;

	private OutputStream thisOutputStream = null;

	private int socketClientConnectState = SocketClient.SOCKET_STATE_IDLE;

	private ConnectArguments connectArguments = new ConnectArguments("");

	private MyQueue <SendRecvData> newSendBuffers = new MyQueue <SendRecvData>();

	private MyQueue <SendRecvData> waitAckBuffers = new MyQueue <SendRecvData>();

	private boolean thisIsSendRunning = false;

	private boolean thisIsRecvRunning = false;

	private boolean _SOCKET_MUTEX = true;

	public String lastErrorString = "";

	public int lastErrno = SocketClient.ERRNO_NO_ERROR;

	/* saved and for ... and restartMyself */
	private Handler savedStateChangedHandler;

	private OnSocketStateChanged savedOnSocketStateChanged;


	public SocketClient (Handler stateChangedHandler,
		OnSocketStateChanged onSocketStateChanged) {
		this.savedStateChangedHandler = stateChangedHandler;
		this.savedOnSocketStateChanged = onSocketStateChanged;
	}


	@Override
	public void finish () {
		try {
			this.Disconnect();
		} catch (Exception e) {
			;
		}
	} /* end of finish */


	public int restartMyself () {
		try {
			Log.v(TAG, "restartMyself");

			try {
				this.Disconnect();
			} catch (Exception e) {
				Log.e(TAG + ":restartMyself:Disconnect",
					"ERROR: " + e.getMessage());
			}

			int ret;

			try {
				ret = this.Connect(this.savedConnectArguments);
			} catch (Exception e) {
				Log.e(TAG + ":restartMyself:Connect",
					"ERROR: " + e.getMessage());
				ret = -999;
			}

			return ret;
		} catch (Exception e) {
			Log.e(TAG + ":restartMyself", "ERROR: " + e.getMessage());
			return -999;
		}
	} /* restartMyself */


	@Override
	public boolean isConnected () {
		try {
			/* Log.v(TAG, "isConnected"); */
			boolean ret;
			LOCK();
			if (null == this.socketClient) {
				ret = false;
			} else {
				ret = true;
			}
			if (ret) {
				ret = this.socketClient.isConnected();
				if ((ret && (SocketClient.SOCKET_STATE_CONNECT_ONLINE != socketClientConnectState))
					|| ((!ret) && (SocketClient.SOCKET_STATE_CONNECT_ONLINE == socketClientConnectState))) {
					UNLOCK();
					this.Disconnect();
					LOCK();
					ret = false;
				}
			}
			UNLOCK();
			return ret;
		} catch (Exception e) {
			UNLOCK();
			return false;
		}
	} /* isConnected */


	@Override
	public boolean sendData (SendRecvData data, boolean sendNow) {
		try {
			Log.v(TAG, "sendData");
			if (sendNow) {
				if (null == thisOutputStream) {
					return false;
				}
				try {
					thisOutputStream.write(data.data, 0, data.data.length);
					thisOutputStream.flush();
					LOCK();
					this.waitAckBuffers.enqueue(data);
					UNLOCK();
					return true;
				} catch (IOException e) {
					Log.println(Log.ERROR, "SocketClient/sendData", "write: "
						+ e.getMessage());
					return false;
				}
			} else {
				LOCK();
				this.newSendBuffers.enqueue(data);
				this.waitAckBuffers.enqueue(data);
				UNLOCK();
				return true;
			}
		} catch (Exception e) {
			Log.println(Log.ERROR, "SocketClient/sendData",
				"all" + e.getMessage());
			return false;
		}
	}


	private synchronized void LOCK () {
		while (!this._SOCKET_MUTEX) {
			try {
				wait(10);
			} catch (InterruptedException e) {
				Log.println(Log.ERROR, "LOCK/wait", e.getMessage());
			}
		}
		this._SOCKET_MUTEX = false;
	}


	private synchronized void UNLOCK () {
		this._SOCKET_MUTEX = true;
		notifyAll();
	}


	private boolean mutexSocketClientConnectState = true;


	@Override
	public synchronized int getSocketClientConnectState () {
		int ret;
		while (!this.mutexSocketClientConnectState) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.mutexSocketClientConnectState = false;
		ret = this.socketClientConnectState;
		this.mutexSocketClientConnectState = true;
		notifyAll();
		return ret;
	}


	@Override
	public synchronized void setSocketClientConnectState (
		int socketClientConnectState) {
		while (!this.mutexSocketClientConnectState) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.mutexSocketClientConnectState = false;
		this.socketClientConnectState = socketClientConnectState;
		this.mutexSocketClientConnectState = true;
		notifyAll();
	}


	/* == */
	private Runnable socketRuntime = new Runnable() {
		public void run () {
			try {
				synchronized (this) {
					while (true) {
						LOCK();
						socketClient = new Socket();
						Log.v("SocketClient/socketRuntime", "NEW!!!!!!");
						try {
							socketClient.connect(new InetSocketAddress(
								connectArguments.getPeerIp(),
								connectArguments.getPeerPort()),
								connectArguments.connectTimeout);
						} catch (SocketTimeoutException to) {
							lastErrorString = "connect: " + to.getMessage();
							lastErrno = SocketClient.ERRNO_CONNECT_TIMEOUT;
							Log.println(Log.ERROR,
								"SocketClient/socketRuntime", to.getMessage());
							socketClientConnectState = SocketClient.SOCKET_STATE_CONNECT_TIMEOUT;
							break;
						} catch (IOException e) {
							lastErrorString = "connect: " + e.getMessage();
							lastErrno = SocketClient.ERRNO_CONNECT_ERROR;
							Log.println(Log.ERROR,
								"SocketClient/socketRuntime", e.getMessage());
							socketClientConnectState = SocketClient.SOCKET_STATE_CONNECT_ABORT;
							break;
						}
						boolean ret = socketClient.isConnected();
						if (ret) {
							/** get instream and outstream */
							try {
								thisInputStream = socketClient
									.getInputStream();
							} catch (IOException e) {
								Log.println(Log.ERROR,
									"SocketClient/socketRuntime",
									e.getMessage());
								socketClientConnectState = SocketClient.SOCKET_STATE_CONNECT_ABORT;
								break;
							}
							try {
								thisOutputStream = socketClient
									.getOutputStream();
							} catch (IOException e) {
								Log.println(Log.ERROR,
									"SocketClient/socketRuntime",
									e.getMessage());
								socketClientConnectState = SocketClient.SOCKET_STATE_CONNECT_ABORT;
								break;
							}
							Log.println(Log.INFO,
								"SocketClient/socketRuntime", "Connected");
							socketClientConnectState = SocketClient.SOCKET_STATE_CONNECT_ONLINE;
							onReceiveThread = new Thread(receiveRuntime);
							onReceiveThread.start();
							onSendThread = new Thread(sendRuntime);
							onSendThread.start();
						} else {
							socketClientConnectState = SocketClient.SOCKET_STATE_CONNECT_TIMEOUT;
						}
						UNLOCK();
						break;/* once */
					} /* while true */
					if (null != savedOnSocketStateChanged) {
						savedOnSocketStateChanged
							.onSocketStateChanged(socketClientConnectState);
					}
					if (null != savedStateChangedHandler) {
						Log.println(Log.VERBOSE,
							"SocketClient/socketRuntime", "FEEDBACK　STATE");
						Bundle bundle = new Bundle();
						Message msg = new Message();
						msg.what = SocketClient.connectStateKey;
						bundle.clear();
						bundle.putInt(SocketClient.connectStateKeyStr,
							socketClientConnectState);
						msg.setData(bundle);
						try {
							savedStateChangedHandler.sendMessage(msg);
						} catch (Exception e) {
							;
						}
					}
				} /* synchronized */
				UNLOCK();
				Log.println(Log.VERBOSE, "SocketClient/socketRuntime",
					"Exited");
			} catch (Exception e) {
				try {
					UNLOCK();
					if (null != savedOnSocketStateChanged) {
						savedOnSocketStateChanged
							.onSocketStateChanged(SocketClient.SOCKET_STATE_CONNECT_ABORT);
					}
					if (null != savedStateChangedHandler) {
						Bundle bundle = new Bundle();
						Message msg = new Message();
						msg.what = SocketClient.connectStateKey;
						bundle.clear();
						bundle.putInt(SocketClient.connectStateKeyStr,
							SocketClient.SOCKET_STATE_CONNECT_ABORT);
						msg.setData(bundle);
						savedStateChangedHandler.sendMessage(msg);
					}
				} catch (Exception ee) {
					;
				}
			}
		}
	}; /* end of socketRuntime */

	Runnable receiveRuntime = new Runnable() {
		public void run () {
			try { /* all of this func */
				int prompt = 0;
				boolean unlock;
				synchronized (this) {
					while (true) {
						LOCK();
						unlock = true;
						thisIsRecvRunning = true;
						if ((null == socketClient)
							|| (!socketClient.isConnected())) {
							socketClientConnectState = SocketClient.SOCKET_STATE_RECV_BROKEN;
							break;
						}
						/** prompt alive */
						if (0 == (prompt % 1000)) {
							Log.println(Log.VERBOSE,
								"SocketClient/receiveRuntime", "alive");
							prompt = 0;
						}
						++prompt;
						String recvedData = "";
						int dataReady = 0;
						try {
							dataReady = thisInputStream.available();
						} catch (IOException e) {
							e.printStackTrace();
						}
						if ((null == socketClient)
							|| (!socketClient.isConnected())) {
							socketClientConnectState = SocketClient.SOCKET_STATE_RECV_BROKEN;
							break;
						}
						if (dataReady > 0) {
							boolean onlyWhenNoAvailable = isUseBEOnlyNoAvailable();
							boolean hasAvailable = false;
							byte[] buffer = new byte[1024];
							int count = 0;
							try {
								count = thisInputStream.read(buffer, 0, 1024);
								// count = inputStream.read(buffer);
							} catch (Exception e) {
								Log.e("SockectClient/receiveRuntime",
									e.getMessage());
								socketClientConnectState = SocketClient.SOCKET_STATE_RECV_ABORT;
								break;
							}
							if (count > 0) {
								byte[] recvedDataId = getDataIdFromRcved(
									buffer, count);
								SendRecvData sendRecvData = getSendRecvDataRM(recvedDataId);
								if ((null == sendRecvData)
									&& (onFindRcvHandlerFilter())) {
									sendRecvData = waitAckBuffers.dequeue();
								}
								recvedData = StringUtils.toHexString(buffer,
									count);
								Log.println(Log.VERBOSE,
									"SocketClient/receiveRuntime", "Recved: "
										+ count + " byte(s): hex: "
										+ recvedData);
								UNLOCK();
								unlock = false;
								boolean calledMsgHandler;
								try {
									if (null != sendRecvData
										&& null != sendRecvData.sentRcvedMsgHandler) {
										Bundle bundle = new Bundle();
										Message msg = new Message();
										msg.what = SocketClient.WHAT_MSG_RECVED;
										bundle.clear();
										bundle.putString(
											SocketClient.receivedKeyStr,
											recvedData);
										byte[] toput = new byte[count];
										for (int i = 0; i < count; ++i) {
											toput[i] = buffer[i];
										}
										bundle.putByteArray(
											SocketClient.receivedRawKeyStr,
											toput);
										msg.setData(bundle);
										sendRecvData.sentRcvedMsgHandler
											.sendMessage(msg);
										calledMsgHandler = true;
										hasAvailable = true;
									} else {
										Log.println(Log.WARN,
											"SocketClient/receiveRuntime",
											"NO sentRcvedMsgHandler");
										calledMsgHandler = false;
									}
								} catch (Exception e) {
									Log.println(
										Log.ERROR,
										"SocketClient/receiveRuntime",
										"sentRcvedMsgHandler:"
											+ e.getMessage());
									calledMsgHandler = false;
								}
								try {
									if (null != sendRecvData
										&& null != sendRecvData.rcvedHandler
										&& onReceivedFilter(buffer, count,
											SocketClient.WHAT_MSG_RECVED)) {
										sendRecvData.rcvedHandler
											.handleReceived(buffer, count,
												SocketClient.WHAT_MSG_RECVED,
												calledMsgHandler);
										hasAvailable = true;
									}
								} catch (Exception e) {
									Log.println(Log.ERROR,
										"SocketClient/receiveRuntime",
										"rcvedHandler:" + e.getMessage());
									calledMsgHandler = false;
								}
								if ((!onlyWhenNoAvailable)
									|| (onlyWhenNoAvailable && (!hasAvailable))) {
									backendOnReceived(buffer, count,
										SocketClient.WHAT_MSG_RECVED);
								}
								LOCK();
								unlock = true;
							} /* if (count > 0) */
						} /* if (dataReady > 0) */
						if ((null == socketClient)
							|| (!socketClient.isConnected())) {
							socketClientConnectState = SocketClient.SOCKET_STATE_RECV_BROKEN;
							break;
						}
						UNLOCK();
						unlock = false;
						try {
							Thread.sleep(20);
						} catch (InterruptedException e) {
							e.printStackTrace();
							socketClientConnectState = SocketClient.SOCKET_STATE_RECV_ABORT;
						}
					} /* while */
					if (null != savedOnSocketStateChanged) {
						savedOnSocketStateChanged
							.onSocketStateChanged(socketClientConnectState);
					}
					if (null != savedStateChangedHandler) {
						Bundle bundle = new Bundle();
						Message msg = new Message();
						msg.what = SocketClient.connectStateKey;
						bundle.clear();
						bundle.putInt(SocketClient.connectStateKeyStr,
							socketClientConnectState);
						msg.setData(bundle);
						savedStateChangedHandler.sendMessage(msg);
					}
				} /* synchronized */
				if (unlock) {
					UNLOCK();
				}
				thisIsRecvRunning = false;
				Disconnect();
				Log.println(Log.VERBOSE, "SocketClient/receiveRuntime",
					"Exited");
			} catch (Exception e) {
				/* end of all of this func */
				try {
					UNLOCK();
					if (null != savedOnSocketStateChanged) {
						savedOnSocketStateChanged
							.onSocketStateChanged(SOCKET_STATE_RECV_ABORT);
					}
					if (null != savedStateChangedHandler) {
						Bundle bundle = new Bundle();
						Message msg = new Message();
						msg.what = SocketClient.connectStateKey;
						bundle.clear();
						bundle.putInt(SocketClient.connectStateKeyStr,
							SocketClient.SOCKET_STATE_RECV_ABORT);
						msg.setData(bundle);
						savedStateChangedHandler.sendMessage(msg);
					}
				} catch (Exception ee) {
					;
				}
			}
		}
	}; /* receiveRuntime */

	Runnable sendRuntime = new Runnable() {
		public void run () {
			try { /* all of this func */
				int prompt = 0;
				boolean unlock;
				MyQueue <SendRecvData> dump2Send = new MyQueue <SendRecvData>();
				synchronized (this) {
					while (true) {
						LOCK();
						unlock = true;
						thisIsSendRunning = true;
						if ((null == socketClient)
							|| (!socketClient.isConnected())) {
							socketClientConnectState = SocketClient.SOCKET_STATE_SEND_BROKEN;
							break;
						}
						if (newSendBuffers.count() > 0) {
							dump2Send.appendEmpty(newSendBuffers);
						}
						/* prompt alive */
						if (0 == (prompt % 1000)) {
							Log.println(Log.VERBOSE,
								"SocketClient/sendRuntime", "alive");
							prompt = 0;
						}
						++prompt;
						if ((null == socketClient)
							|| (!socketClient.isConnected())) {
							socketClientConnectState = SocketClient.SOCKET_STATE_SEND_BROKEN;
							break;/* died */
						}
						if (dump2Send.count() > 0) {
							SendRecvData toSend = dump2Send.dequeue();
							byte[] toSendData = toSend.data;
							boolean sentOK;
							/* send */
							try {
								thisOutputStream.write(toSendData, 0,
									toSendData.length);
								thisOutputStream.flush();
								try {
									Thread.sleep(500);
								} catch (Exception e) {
									;
								}
								sentOK = true;
							} catch (Exception e) {
								Log.e("SockectClient/sendRuntime", "write: "
									+ e.getMessage());
								socketClientConnectState = SocketClient.SOCKET_STATE_SEND_ABORT;
								sentOK = false;
								break;
							}
							if (sentOK) {
								/* sentOK */
								String sentFeedback = StringUtils
									.toHexString(toSendData,
										toSendData.length);
								Log.println(Log.VERBOSE,
									"SocketClient/sendRuntime", "Sent: "
										+ toSendData.length
										+ " byte(s): hex: " + sentFeedback);
								UNLOCK();
								unlock = false;
								try {
									if (null != toSend.sentRcvedMsgHandler) {
										Bundle bundle = new Bundle();
										Message msg = new Message();
										msg.what = toSend.whatData;
										bundle.clear();
										bundle.putString(
											SocketClient.sentKeyStr,
											sentFeedback);
										msg.setData(bundle);
										toSend.sentRcvedMsgHandler
											.sendMessage(msg);
									}
								} catch (Exception e) {
									Log.println(Log.ERROR,
										"SocketClient/sendRuntime",
										e.getMessage());
								}
								LOCK();
								unlock = true;
							} else {
								socketClientConnectState = SocketClient.SOCKET_STATE_SEND_BROKEN;
								break;/* died */
							} /* if (sentOK) */
						} else {
							/* send heart beat */
							byte[] buffer = new byte[255];
							int n = getHeartbeat(buffer, 255);
							if (n > 0) {
								boolean sentOK;
								Log.println(
									Log.VERBOSE,
									"sendRuntime",
									"HB(HEX):"
										+ StringUtils.toHexString(buffer, n)
										+ "; n: " + n);
								/* send */
								try {
									thisOutputStream.write(buffer, 0, n);
									thisOutputStream.flush();
									try {
										Thread.sleep(500);
									} catch (Exception e) {
										;
									}
									sentOK = true;
								} catch (Exception e) {
									Log.e("SockectClient/sendRuntime",
										"write: " + e.getMessage());
									socketClientConnectState = SocketClient.SOCKET_STATE_SEND_ABORT;
									sentOK = false;
									break;
								}
								if (sentOK) {
									/* sentOK */
									UNLOCK();
									unlock = false;
									try {
										backendOnSent(buffer, n,
											SocketClient.WHAT_MSG_HEARTBEAT);
									} catch (Exception e) {
										Log.println(Log.ERROR,
											"SocketClient/sendRuntime",
											e.getMessage());
									}
									LOCK();
									unlock = true;
								} else {
									socketClientConnectState = SocketClient.SOCKET_STATE_SEND_BROKEN;
									break;/* died */
								} /* if (sentOK) */
							} /* if (n > 0) */
						} /* if (dump2Send.count() > 0) */
						if ((null == socketClient)
							|| (!socketClient.isConnected())) {
							socketClientConnectState = SocketClient.SOCKET_STATE_SEND_BROKEN;
							break;
						}
						UNLOCK();
						unlock = false;
						try {
							Thread.sleep(sendRoutineTimeslice);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} /* while */
					if (null != savedOnSocketStateChanged) {
						savedOnSocketStateChanged
							.onSocketStateChanged(socketClientConnectState);
					}
					if (null != savedStateChangedHandler) {
						Log.w(TAG + ":sendRuntime:run", "stateChangedHandler");
						Bundle bundle = new Bundle();
						Message msg = new Message();
						msg.what = SocketClient.connectStateKey;
						bundle.clear();
						bundle.putInt(SocketClient.connectStateKeyStr,
							socketClientConnectState);
						msg.setData(bundle);
						savedStateChangedHandler.sendMessage(msg);
					}
				} /* synchronized */
				if (unlock) {
					UNLOCK();
				}
				Disconnect();
				Log.println(Log.VERBOSE, "SocketClient/sendRuntime", "Exited");
			} catch (Exception e) {
				/* end of all of this func */
				try {
					UNLOCK();
					Log.e(TAG + ":sendRuntime", "ERROR: " + e.getMessage());
					if (null != savedOnSocketStateChanged) {
						savedOnSocketStateChanged
							.onSocketStateChanged(SOCKET_STATE_SEND_ABORT);
					}
					if (null != savedStateChangedHandler) {
						Bundle bundle = new Bundle();
						Message msg = new Message();
						msg.what = SocketClient.connectStateKey;
						bundle.clear();
						bundle.putInt(SocketClient.connectStateKeyStr,
							SocketClient.SOCKET_STATE_SEND_ABORT);
						msg.setData(bundle);
						savedStateChangedHandler.sendMessage(msg);
					}
				} catch (Exception ee) {
					;
				}
			}
		}
	}; /* sendRuntime */

	private ConnectArguments savedConnectArguments;


	public int Connect (ConnectArguments connectArguments) {
		try {
			Log.v(TAG, "Connect");
			if (null == connectArguments) {
				return SocketClient.ERRNO_INVALID_CONNECT_ARGUMENTS * -1;
			}

			this.savedConnectArguments = connectArguments;

			int ret = SocketClient.ERRNO_NO_ERROR;
			LOCK();
			if (null != this.socketClient) {
				if (thisIsSendRunning || thisIsRecvRunning
					|| this.socketClient.isConnected()) {
					if (!connectArguments.reconnect) {
						/* one has being running */
						ret = SocketClient.ERRNO_ALREADY * -1;
					} else {
						try {
							UNLOCK();
							this.Disconnect();
							LOCK();
						} catch (Exception e) {
							;
						}
					}
				}
			}
			if (SocketClient.ERRNO_NO_ERROR == ret) {
				ret = this.parseDumpValidProtocol(connectArguments.protocol);
				if (SocketClient.ERRNO_NO_ERROR != ret) {
					Log.println(Log.ERROR, "SocketClient/ClientConnect",
						"INvalid IP:PORT:" + connectArguments.protocol);
				}
			}
			if (SocketClient.ERRNO_NO_ERROR == ret) {
				onSocketThread = new Thread(socketRuntime);
				onSocketThread.start();
			} else {
				/* when not no error: DE-INIT old in case exists */
				UNLOCK();
				this.Disconnect();
				return ret;
			}
			UNLOCK();
			return ret;
		} catch (Exception e) {
			lastErrorString = "Exception: " + e.getMessage();
			UNLOCK();
			this.Disconnect();
			return SocketClient.ERRNO_ABORT;
		}
	} /* Connect */


	protected SendRecvData getSendRecvDataRM (byte[] recvedDataId) {
		Log.println(Log.VERBOSE, "getSendRecvDataRM", "getSendRecvDataRM");
		int sz = this.waitAckBuffers.count();
		if (sz <= 0) {
			return null;
		} else {
			SendRecvData srd = this.waitAckBuffers.removeFound(recvedDataId,
				new CompareMethod <byte[], SendRecvData>() {
					@Override
					public int compare (byte[] l, SendRecvData r) {
						byte[] rcmp = r.data;
						if ((rcmp.length < 27) || (l.length < 17)) {
							return -2;
						}
						for (int i = 0; i < 17; ++i) {
							if (l[i] != rcmp[10 + i]) {
								return -1;
							}
						}
						return 0;
					}
				});
			if (null == srd) {
				Log.println(Log.INFO, "getSendRecvDataRM/find", "NOT FOUND");
			} else {
				Log.println(Log.INFO, "getSendRecvDataRM/find", "FOUND");
			}
			return srd;
		}
	}


	@SuppressWarnings ("deprecation")
	public void Disconnect () {
		try {
			LOCK();
			if (null != socketClient) {
				Socket tmp = socketClient;
				socketClient = null;
				thisOutputStream = null;
				thisInputStream = null;
				try {
					tmp.shutdownInput();
				} catch (IOException e) {
					Log.println(Log.ERROR,
						"DisconnectFromServer/shutdownInput", e.getMessage());
				}
				try {
					tmp.shutdownOutput();
				} catch (IOException e) {
					Log.println(Log.ERROR,
						"DisconnectFromServer/shutdownInput", e.getMessage());
				}
				try {
					tmp.close();
				} catch (IOException e) {
					Log.println(Log.ERROR, "DisconnectFromServer/close",
						e.getMessage());
				}
			}
			this.socketClientConnectState = SocketClient.SOCKET_STATE_FI_DESTROYED;
			UNLOCK();
			this.notifyAll();
			int to = 3;
			while (thisIsSendRunning && (to-- > 0)) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			to = 3;
			while (thisIsRecvRunning && (to-- > 0)) {
				try {
					Thread.sleep(50);
					onReceiveThread.stop();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			UNLOCK();
		}
	} /* Disconnect */


	private int parseDumpValidProtocol (String prot) {
		if (prot.length() <= 0) {
			return SocketClient.ERRNO_INVALID_PROT_ARG * -1;
		}
		int start = prot.indexOf(":");
		if ((start == -1) || (start + 1 >= prot.length())) {
			return SocketClient.ERRNO_INVALID_PROT_ARG * -1;
		}
		String ip = prot.substring(0, start);
		String port = prot.substring(start + 1);
		if (checkIP(ip) && isInteger(port)) {
			this.connectArguments.protocol = prot;
			this.connectArguments.setPeerIp(ip);
			this.connectArguments.setPeerPort(Integer.parseInt(port));
			return SocketClient.ERRNO_NO_ERROR;
		} else {
			this.connectArguments.protocol = "";
			return SocketClient.ERRNO_INVALID_PROT_ARG * -1;
		}
	} /* parseDumpValidProtocol */


	private boolean checkIP (String str) {
		Pattern pattern = Pattern
			.compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]"
				+ "|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");
		return pattern.matcher(str).matches();
	}


	public static boolean isInteger (String value) {
		try {
			Integer.parseInt(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}


	@Override
	public boolean onReceivedFilter (byte[] data, int count, int whatData) {
		return true;
	}


	@Override
	public boolean onReceivedMsgHandlerFilter (byte[] data, int count,
		int whatData) {
		return true;
	}


	@Override
	public boolean onFindRcvHandlerFilter () {
		return false;
	}


	@Override
	public boolean setStateChangedHandler (Handler stateChangedHandler) {
		try {
			LOCK();
			this.savedStateChangedHandler = stateChangedHandler;
			UNLOCK();
			return false;
		} catch (Exception e) {
			UNLOCK();
			return false;
		}
	}


	@Override
	public void clearStateChangedHandler () {
		try {
			LOCK();
			this.savedStateChangedHandler = null;
			UNLOCK();
		} catch (Exception e) {
			;
		}
	}


	/*** XXX private static final ***/
	private static final String TAG = SocketClient.class.getSimpleName();
}
