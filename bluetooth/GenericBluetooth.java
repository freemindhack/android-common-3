/*
 * version 1.0.0.1 20150717 AM
 */

package common.bluetooth;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


import org.apache.http.util.EncodingUtils;


import posix.generic.errno.errno;


import common.bluetooth.le.BluetoothLe;
import common.bluetooth.le.BluetoothLe.OnConnectListener;
import common.bluetooth.le.BluetoothLe.OnDataAvailableListener;
import common.bluetooth.le.BluetoothLe.OnDisconnectListener;
import common.bluetooth.le.BluetoothLe.OnServiceDiscoverListener;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;


@SuppressLint ({ "NewApi", "DefaultLocale" })
public class GenericBluetooth {
	/* serial port profile */

	public GenericBluetooth (Context context) {
		Log.v(TAG, "onCreate");

		this.context = context;

		if (-1 == this.le) {
			if (GenericBluetooth.leCheck(this.context)) {
				this.le = 1;
			} else {
				this.le = 0;
			}
		}

		/* get BluetoothAdapter */
		if (1 == this.le) {
			Log.v(TAG + ":GenericBluetooth", "support ble");
			final BluetoothManager bluetoothManager = (BluetoothManager) this.context
				.getSystemService(Context.BLUETOOTH_SERVICE);
			this.bluetoothAdapter = bluetoothManager.getAdapter();
		} else {
			Log.w(TAG + ":GenericBluetooth", "NOT support ble");
			this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		}

		if (null == this.bluetoothAdapter) {
			Log.e(TAG + ":onCreate", "null bluetoothAdapter");
		} else {
			this.savedEnable = this.bluetoothAdapter.isEnabled();
			Log.v(TAG + ":onCreate", "bluetoothAdapter ok");
		}

	} /* MyBluetooth */


	public BluetoothLe getLE () {
		return this.LE;
	}


	public static boolean leCheck (Context c) {
		try {
			Log.v(TAG, "leCheck");

			/*
			 * Use this check to determine whether BLE is supported on the
			 * device. Then you can selectively disable BLE-related features.
			 */
			if (c.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			Log.w(TAG + ":leCheck", "ERROR: " + e.getMessage());
			return false;
		}
	} /* leCheck */


	public int enableBluetooth () {
		try {
			if (null == this.bluetoothAdapter) {
				return bluetooth_errno.ENOADAPTER * -1;
			}

			/* if BT is not on, request that it be enabled */
			if (!this.bluetoothAdapter.isEnabled()) {
				Intent intent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
				this.context.startActivity(intent);
			}

			return 0;

		} catch (Exception e) {
			return bluetooth_errno.EENABLEEXCEPTION * -1;
		}
	} /* enableBluetooth */


	public int disableBluetooth () {
		try {
			if (null == this.bluetoothAdapter) {
				return bluetooth_errno.ENOADAPTER * -1;
			}

			/* if BT is on, disable it */
			if (this.bluetoothAdapter.isEnabled()) {
				this.bluetoothAdapter.disable();
			}

			return 0;

		} catch (Exception e) {
			return bluetooth_errno.EDISABLEEXCEPTION * -1;
		}
	} /* disableBluetooth */


	public boolean isBluetoothAvailable () {
		try {
			Log.v(TAG, "isBluetoothAvailable");

			if (this.bluetoothAdapter == null
				|| null == this.bluetoothAdapter.getAddress()
				|| this.bluetoothAdapter.getAddress().length() <= 0) {
				return false;
			} else {
				return true;
			}
			/* NullPointerException */
		} catch (Exception e) {
			return false;
		}
	} /* isBluetoothAvailable */


	public boolean isBluetoothEnabled () {
		try {
			Log.v(TAG, "isBluetoothEnabled");

			return this.bluetoothAdapter.isEnabled();
		} catch (Exception e) {
			return false;
		}
	} /* isBluetoothEnabled */


	public Set <BluetoothDevice> getPairedDevices () {
		try {
			/* Get a set of currently paired devices */
			return this.bluetoothAdapter.getBondedDevices();
		} catch (Exception e) {
			return null;
		}
	} /* getPairedDevices */


	public String getPairedMacAddr (int index) {
		if (index < 0) {
			return "";
		}

		Set <BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();

		if (devices.size() <= 0 || devices.size() <= index) {
			return "";
		}

		int c = 0;
		for (BluetoothDevice device : devices) {
			if (c == index) {
				return device.getAddress();
			}

			++c;
		}

		return "";
	} /* getPairedMacAddr */


	/* XXX NOTE: without bundled */
	public int startGetVisibleDevices (BroadcastReceiver receiver) {
		try {
			/* If we're already discovering, stop it */
			if (this.isDiscovering()) {
				this.cancelDiscovery();
			}

			try {
				if (null != receiver) {
					/* Register for broadcasts when a device is discovered */
					IntentFilter filter = new IntentFilter(
						BluetoothDevice.ACTION_FOUND);
					this.context.registerReceiver(receiver, filter);

					/* Register for broadcasts when discovery has finished */
					filter = new IntentFilter(
						BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
					this.context.registerReceiver(receiver, filter);
				}
			} catch (Exception e) {
				Log.e(TAG + ":startGetVisibleDevices",
					"ERROR: " + e.getMessage());
			}

			/* Request discover from BluetoothAdapter */
			if (this.startDiscovery()) {
				return 0;
			} else {
				return bluetooth_errno.ESTARTLESCANFAIL * -1;
			}
		} catch (Exception e) {
			return bluetooth_errno.ESTARTLESCANEXCEPTION * -1;
		}
	} /* startGetVisibleDevices */


	/**
	 * LE part
	 */
	public int startGetLEDevices (LeScanCallback cb, long timeout,
		LeScanStopListener leScanStopListener) {
		try {
			Log.v(TAG, "startGetLEDevices");

			try {
				if (null != this.leScanThread) {
					this.leScanThread.finish();
					this.leScanThread = null;
				}

				this.leScanThread = new LeScanThread(cb, timeout);
				this.leScanThread.setLeScanStopListener(leScanStopListener);
				this.leScanThread.start();

				return 0;
			} catch (Exception e) {
				Log.e(TAG + ":startGetLEDevices", "E: " + e.getMessage());

				return errno.EXTRA_EEUNRESOLVED * -1;
			}
		} catch (Exception e) {
			Log.e(TAG + ":startGetLEDevices", "E: " + e.getMessage());
			return errno.EXTRA_EEUNRESOLVED * -1;
		}
	} /* startGetLEDevices */


	public void finishGetLEDevices () {
		try {
			if (null != this.leScanThread) {
				this.leScanThread.finish();
				this.leScanThread = null;
			}
		} catch (Exception e) {
			;
		}
	} /* finishGetLEDevices */


	/* use this in onCreate */
	public int leOnCreate () {
		if (1 != this.le) {
			return -1;
		}

		if (null == this.LE) {
			this.LE = new BluetoothLe(this.context);
			if (!this.LE.initialize()) {
				Log.e(TAG, "Unable to initialize LE");
				this.LE = null;

				return -2;
			}
		}

		return 0;
	} /* leOnCreate */


	public boolean leSetOnConnectListener (OnConnectListener onConnectListener) {
		if (null == this.LE) {
			return false;
		} else {
			this.LE.setOnConnectListener(onConnectListener);
			return true;
		}
	} /* leSetOnConnectListener */


	public boolean leSetOnDataAvailableListener (
		OnDataAvailableListener onDataAvailableListener) {
		if (null == this.LE) {
			return false;
		} else {
			this.LE.setOnDataAvailableListener(onDataAvailableListener);
			return true;
		}
	} /* leSetOnDataAvailableListener */


	public boolean leSetOnDisconnectListener (
		OnDisconnectListener onDisconnectListener) {
		if (null == this.LE) {
			return false;
		} else {
			this.LE.setOnDisconnectListener(onDisconnectListener);
			return true;
		}
	} /* leSetOnDisconnectListener */


	public boolean leSetOnServiceDiscoverListener (
		OnServiceDiscoverListener onServiceDiscoverListener) {
		if (null == this.LE) {
			return false;
		} else {
			this.LE.setOnServiceDiscoverListener(onServiceDiscoverListener);
			return true;
		}
	} /* leSetOnServiceDiscoverListener */


	public int leStartConnect (String addr, boolean autoConnect,
		boolean noExists) {
		if (null == this.LE) {
			return -1;
		}

		this.finishGetLEDevices();

		Log.v(TAG + ":leStartConnect", "autoConnect: " + autoConnect
			+ " noExists: " + noExists);

		if (this.LE.connect(addr, autoConnect, noExists)) {
			try {
				if (null != this.leReadyThread) {
					this.leReadyThread.finish();
					this.leReadyThread = null;
				}

				this.leReadyThread = new LeReadyThread(this.leReadUUID);
				this.leReadyThread.start();
			} catch (Exception e) {
				Log.e(TAG + ":leStartConnect", "ERROR: " + e.getMessage());
			}
			return 0;
		} else {
			return -2;
		}
	} /* leStartConnect */


	public void leDisconnect (boolean noLEDiconnect) {
		if (null != this.LE) {
			if (null != this.leReadyThread) {
				this.leReadyThread.finish();
				this.leReadyThread = null;
			}

			if (!noLEDiconnect) {
				this.LE.disconnect();
			}
		}
	} /* leDisconnect */


	public void leFinish () {
		try {
			try {
				if (null != this.bluetoothAdapter) {
					if (this.savedEnable
						&& !this.bluetoothAdapter.isEnabled()) {
						this.enableBluetooth();
					} else if (!this.savedEnable
						&& this.bluetoothAdapter.isEnabled()) {
						this.disableBluetooth();
					}
				}
			} catch (Exception e) {
				Log.w(TAG + "leFinish", "E: " + e.getMessage());
			}

			// new Thread() {
			// @Override
			// public void run () {
			try {
				try {
					GenericBluetooth.this.serverSocket = null;

					GenericBluetooth.this.clientSocket = null;

					GenericBluetooth.this.context = null;

					GenericBluetooth.this.bluetoothAdapter = null;

				} catch (Exception e) {
					Log.w(TAG + "leFinish:thread:run", "E: " + e.getMessage());
				}

				/* LE */

				try {
					GenericBluetooth.this.le = -1;

					if (null != GenericBluetooth.this.LE) {
						if (null != GenericBluetooth.this.leReadyThread) {
							GenericBluetooth.this.leReadyThread.finish();
							GenericBluetooth.this.leReadyThread = null;
						}

						GenericBluetooth.this.LE.disconnect();
						GenericBluetooth.this.LE = null;
					}

				} catch (Exception e) {
					Log.w(TAG + "leFinish:thread:run", "E: " + e.getMessage());
				}

				try {
					if (null != GenericBluetooth.this.leReadyThread) {
						GenericBluetooth.this.leReadyThread.finish();
						GenericBluetooth.this.leReadyThread = null;
					}

					GenericBluetooth.this.leConnectedGatt = null;

					GenericBluetooth.this.leWriteUUID = "";

					GenericBluetooth.this.leReadUUID = "";

					GenericBluetooth.this.leGattRead = null;

					GenericBluetooth.this.leGattWrite = null;
				} catch (Exception e) {
					Log.w(TAG + "leFinish:thread:run", "E: " + e.getMessage());
				}

				try {
					if (null != GenericBluetooth.this.leScanThread) {
						GenericBluetooth.this.leScanThread.finish();
						GenericBluetooth.this.leScanThread = null;
					}
				} catch (Exception e) {
					Log.w(TAG + "leFinish:thread:run", "E: " + e.getMessage());
				}
			} catch (Exception e) {
				Log.w(TAG + "leFinish:thread:run", "E: " + e.getMessage());
			}

			// }
			// }.start();
		} catch (Exception e) {
			Log.w(TAG + "leFinish", "E: " + e.getMessage());
		}
	} /* leFinish */


	private class LeReadyThread extends Thread {
		public LeReadyThread (String knownReadUUID) {
			this.knownReadUUID = knownReadUUID;
			this.running = false;
			this.terminate = false;
			leGattRead = null;
			leGattWrite = null;
		}


		public void finish () {
			this.terminate = true;

			Log.w(TAG + ":LeReadyThread", "finish");

			try {
				Thread.sleep(200);
			} catch (Exception e) {
				;
			}

			if (this.running) {
				for (int i = 0; i < 4; ++i) {
					try {
						Thread.sleep(200);
					} catch (Exception e) {
						;
					}
				}
			}

			leGattRead = null;
			leGattWrite = null;
		}


		@Override
		public void run () {
			try {
				this.running = true;

				Log.w(TAG + ":LeReadyThread", "run");

				List <BluetoothGattCharacteristic> all = new ArrayList <BluetoothGattCharacteristic>();

				boolean hasReadUUID;
				if (this.knownReadUUID != null
					&& this.knownReadUUID.length() > 0) {
					hasReadUUID = true;
				} else {
					hasReadUUID = false;
				}

				while (!this.terminate) {

					if (null == leGattRead) {
						try {
							if (null != GenericBluetooth.this.leConnectedGatt) {
								List <BluetoothGattService> okss = GenericBluetooth.this.leConnectedGatt
									.getServices();

								for (BluetoothGattService s : okss) {
									List <BluetoothGattCharacteristic> gattCharacteristics = s
										.getCharacteristics();

									/*
									 * Log.v(TAG, "size: " +
									 * gattCharacteristics.size());
									 */

									for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

										if (!all.contains(gattCharacteristic)) {
											if (!hasReadUUID) {
												GenericBluetooth.this.LE
													.setCharacteristicNotification(
														gattCharacteristic,
														true);
											}
											all.add(gattCharacteristic);
										}

										String uuid = gattCharacteristic
											.getUuid().toString()
											.toLowerCase();

										Log.i(
											TAG + ":LeRearThread:run",
											"uuid: "
												+ uuid
												+ " w type: "
												+ gattCharacteristic
													.getWriteType());
										if (!hasReadUUID) {
											String ruuid = GenericBluetooth.this.LE
												.getReadUUID();
											if (ruuid != null
												&& ruuid.length() > 0
												&& uuid.equals(ruuid)
												&& null == leGattRead) {
												leGattRead = gattCharacteristic;
											}
										} else {
											if (this.knownReadUUID
												.equals(uuid)) {
												GenericBluetooth.this.LE
													.setCharacteristicNotification(
														gattCharacteristic,
														true);
												leGattRead = gattCharacteristic;
											}
										}

										if (uuid.equals(leWriteUUID)) {
											leGattWrite = gattCharacteristic;
										}
									}
								}
							}
						} catch (Exception e) {
							Log.e(TAG, "ERROR: " + e.getMessage());
							// this.running = false;
							// return;
						}

						try {
							Thread.sleep(200);
						} catch (Exception e) {
							;
						}
					} else {
						/* Log.v(TAG, "triggerReadCharacteristic"); */
						try {
							GenericBluetooth.this.LE
								.triggerReadCharacteristic(leGattRead);
						} catch (Exception e) {
							Log.e(TAG, "ERROR: " + e.getMessage());
						}
					}

					try {
						Thread.sleep(150);
					} catch (Exception e) {
						;
					}
				}

				Log.w(TAG + ":LeReadyThread", "exit");
				this.running = false;
			} catch (Exception e) {
				Log.w(TAG + ":LeReadyThread", "exit: E");
				this.running = false;
			}
		}


		private String knownReadUUID = null;

		private boolean terminate = true;

		private boolean running = false;
	}


	public void leSetConnectedGatt (BluetoothGatt gatt) {
		this.leConnectedGatt = gatt;
	}


	public void leSetWriteUUID (String writeUUID) {
		this.leWriteUUID = writeUUID;
	}


	public void leSetReadUUID (String readUUID) {
		this.leReadUUID = readUUID;
	}


	@SuppressLint ("DefaultLocale")
	public int leSend (byte[] utf8Data) {
		try {
			if (null == this.LE) {
				return -2;
			}

			if (null == this.leGattWrite) {
				return -3;
			}

			this.LE.setCharacteristicNotification(leGattWrite, true);

			leGattWrite.setValue(utf8Data);

			this.LE.writeCharacteristic(leGattWrite);

			return utf8Data.length;
		} catch (Exception e) {
			return -1;
		}
	}


	public interface LeScanStopListener {
		public void onLeScanStopped (int retval);
	};


	private class LeScanThread extends Thread {
		public LeScanThread (LeScanCallback cb, long timeout) {
			this.cb = cb;
			this.timeout = timeout;
			this.scaning = true;
		}


		private boolean scaning = false;

		private LeScanStopListener leScanStopListener;


		public void setLeScanStopListener (
			LeScanStopListener leScanStopListener) {
			this.leScanStopListener = leScanStopListener;
		}


		public void finish () {
			Log.v(TAG + ":LeScanThread", "finish");

			this.scaning = false;

			try {
				Thread.sleep(200);
			} catch (Exception e) {
				;
			}
		}


		@Override
		public void run () {
			try {
				Log.v(TAG + ":LeScanThread", "run");

				long time2stop = System.currentTimeMillis() + timeout;

				while (time2stop > System.currentTimeMillis()) {
					if (!this.scaning) {
						Log.v(TAG + ":LeScanThread:run", "get stop signal");
						break;
					}

					if (GenericBluetooth.this.bluetoothAdapter.isEnabled()) {
						break;
					}

					try {
						Thread.sleep(100);
					} catch (Exception e) {
						;
					}
				}

				if (this.scaning) {
					try {
						if (true) {
							/* if (GenericBluetooth.this.isInLeScan) { */
							GenericBluetooth.this.bluetoothAdapter
								.stopLeScan(this.cb);

							for (int i = 0; i < 5; ++i) {
								try {
									Thread.sleep(200);
								} catch (Exception e) {
									;
								}
							}
						}
					} catch (Exception e) {
						Log.e(TAG + ":LeScanThread:run:stop",
							"E: " + e.getMessage());
					}

					/* FIXME: check retVal */
					boolean ret = GenericBluetooth.this.bluetoothAdapter
						.startLeScan(this.cb);
					Log.v(TAG + ":LeScanThread:run", "startLeScan:result: "
						+ ret);

					if (!ret) {
						this.scaning = false;
						Log.v(TAG + ":LeScanThread:run", "stop");
						if (null != this.leScanStopListener) {
							new Thread() {
								@Override
								public void run () {
									try {
										LeScanThread.this.leScanStopListener
											.onLeScanStopped(errno.EPERM * -1);
									} catch (Exception e) {
										;
									}
								}
							}.start();
						}

						try {
							if (true) {
								GenericBluetooth.this.bluetoothAdapter
									.stopLeScan(this.cb);

								for (int i = 0; i < 5; ++i) {
									try {
										Thread.sleep(200);
									} catch (Exception e) {
										;
									}
								}
							}
						} catch (Exception e) {
							Log.e(TAG + ":LeScanThread:run:stop",
								"E: " + e.getMessage());
						}
						return;
					}

					while (time2stop > System.currentTimeMillis()) {
						if (!this.scaning) {
							Log.v(TAG + ":LeScanThread:run",
								"get stop signal");
							break;
						}

						try {
							Thread.sleep(100);
						} catch (Exception e) {
							;
						}
					}

					if (time2stop > System.currentTimeMillis()) {
						Log.v(TAG + ":LeScanThread:run", "timeout");
					}

					GenericBluetooth.this.bluetoothAdapter
						.stopLeScan(this.cb);

				}

				Log.v(TAG + ":LeScanThread:run", "stop");

				this.scaning = false;
				if (null != this.leScanStopListener) {
					new Thread() {
						@Override
						public void run () {
							try {
								LeScanThread.this.leScanStopListener
									.onLeScanStopped(0);
							} catch (Exception e) {
								;
							}
						}
					}.start();
				}
			} catch (Exception e) {
				Log.e(TAG + ":LeScanThread:run:final", "E: " + e.getMessage());
				this.scaning = false;
				if (null != this.leScanStopListener) {
					new Thread() {
						@Override
						public void run () {
							try {
								LeScanThread.this.leScanStopListener
									.onLeScanStopped(errno.EXTRA_EEUNRESOLVED
										* -1);
							} catch (Exception e) {
								;
							}
						}
					}.start();
				}
			}
		}


		private LeScanCallback cb;

		private long timeout;
	}


	public void finishGetVisibleDevices (BroadcastReceiver receiver) {
		try {
			if (this.isDiscovering()) {
				this.cancelDiscovery();
			}

			this.context.unregisterReceiver(receiver);
		} catch (Exception e) {
			Log.e(TAG + ":finishGetVisibleDevices",
				"ERROR: " + e.getMessage());
		}
	} /* finishGetVisibleDevices */


	// private IntentFilter makeGattUpdateIntentFilter () {
	// /* final */
	// IntentFilter intentFilter = new IntentFilter();
	//
	// intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
	// intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
	// intentFilter
	// .addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
	// intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
	//
	// return intentFilter;
	// } /* makeGattUpdateIntentFilter */

	public boolean startDiscovery () {
		return this.bluetoothAdapter.startDiscovery();
	}


	public boolean isDiscovering () {
		return this.bluetoothAdapter.isDiscovering();
	}


	public boolean cancelDiscovery () {
		return this.bluetoothAdapter.cancelDiscovery();
	}


	public String myName () {
		try {
			return this.bluetoothAdapter.getName();
		} catch (Exception e) {
			Log.e(TAG + ":myName", "ERROR: " + e.getMessage());

			return null;
		}
	} /* myName */


	public String myAddr () {
		try {
			return this.bluetoothAdapter.getAddress();
		} catch (Exception e) {
			Log.e(TAG + ":myAddr", "ERROR: " + e.getMessage());

			return null;
		}
	} /* myAddr */


	public interface OnClientListener {
		public void onClientConnected (BluetoothSocket clientSocket);


		public void onReceived (BluetoothSocket clientSocket,
			byte[] utf8Data, int sz);
	} /* OnClientListener */


	@SuppressLint ("DefaultLocale")
	public String makeUUID (String macAddr) {
		byte[] data = EncodingUtils.getBytes(macAddr.toLowerCase(), "UTF-8");

		byte[] retdata = new byte[32];

		try {
			byte[] hash = null;

			try {
				hash = MessageDigest.getInstance("MD5").digest(data);
			} catch (NoSuchAlgorithmException e) {
				Log.e(TAG + ":startServer:MD5", "ERROR: " + e.getMessage());
				hash = null;
			}

			if (null != hash) {
				for (int i = 0; i < 32; i += 2) {
					byte b = (byte) (0xf & (hash[i / 2] >> 4));

					if (b >= 0 && b <= 9) {
						retdata[i] = (byte) (b + '0');
					} else {
						retdata[i] = (byte) (b - 10 + 'a');
					}

					b = (byte) (0xf & hash[i / 2]);

					if (b >= 0 && b <= 9) {
						retdata[i + 1] = (byte) (b + '0');
					} else {
						retdata[i + 1] = (byte) (b - 10 + 'a');
					}
				}
			}

		} catch (Exception e) {
			Log.e(TAG + ":startServer:MD5", "ERROR: " + e.getMessage());
		}

		/*
		 * 00000000-1111-2222-3333-444444444444
		 */
		String _ret = EncodingUtils.getString(retdata, "UTF-8");

		String ret = _ret.substring(0, "00000000".length()) + "-";
		ret += _ret.substring("00000000".length(), "000000001111".length())
			+ "-";
		ret += _ret.substring("000000001111".length(),
			"0000000011112222".length())
			+ "-";
		ret += _ret.substring("0000000011112222".length(),
			"00000000111122223333".length()) + "-";
		ret += _ret.substring("00000000111122223333".length(),
			"00000000111122223333444444444444".length());

		return ret;
	} /* makeUUID */


	public boolean startServer (String serverName, String uuid4server,
		OnClientListener onClicentListener) {
		try {
			try {
				if (null != this.serverThread) {
					this.serverThread.finish("".getBytes(), 0, 0);
					this.serverThread = null;
				}
			} catch (Exception e) {
				;
			}

			if (null == serverName) {
				serverName = this.myName();
			}

			if (null == uuid4server) {
				uuid4server = this.makeUUID(this.myAddr());
			}

			this.onClicentListener = onClicentListener;

			this.serverThread = new ServerThread(serverName, uuid4server) {

				@Override
				public void onClientConnected (BluetoothSocket clientSocket) {
					if (null != GenericBluetooth.this.onClicentListener) {
						GenericBluetooth.this.onClicentListener
							.onClientConnected(clientSocket);
					}
				}


				@Override
				public void onReceived (BluetoothSocket clientSocket,
					byte[] utf8Data, int sz) {
					if (null != GenericBluetooth.this.onClicentListener) {
						GenericBluetooth.this.onClicentListener.onReceived(
							clientSocket, utf8Data, sz);
					}
				}

			};

			this.serverThread.start();

			return true;
		} catch (Exception e) {
			Log.e(TAG + ":startServer", "ERROR: " + e.getMessage());

			return false;
		}
	} /* startServer */


	public void stopServer (byte[] closeData, int offset, int sz) {
		try {
			if (null != this.serverThread) {
				this.serverThread.finish(closeData, offset, sz);
				this.serverThread = null;
			}

		} catch (Exception e) {
			Log.e(TAG + ":stopServer", "ERROR: " + e.getMessage());
		}
	} /* stopServer */


	public void closeClient (String addr) {
		int n = this.clients.size();

		for (int i = 0; i < n; ++i) {
			if (this.clients.get(i).getRemoteDevice().getAddress()
				.equals(addr)) {
				try {
					this.clients.remove(i).close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				this.clientsReadThreads.remove(i).finish();

				break;
			}
		}
	}


	private ServerThread serverThread = null;

	private OnClientListener onClicentListener = null;


	/* XXX NOTE serverMacAddr and serverUUID is the ... of paired device */
	public boolean startClient (String serverMacAddr, String serverUUID,
		OnServerListener onServerListener) {
		try {
			try {
				if (null != this.clientThread) {
					this.clientThread.finish("".getBytes(), 0, 0);
					this.clientThread = null;
				}
			} catch (Exception e) {
				;
			}

			this.onServerListener = onServerListener;

			this.clientThread = new ClientThread() {

				@Override
				public void onReceived (BluetoothSocket clientSocket,
					byte[] utf8data, int sz) {
					if (null != GenericBluetooth.this.onServerListener) {
						GenericBluetooth.this.onServerListener.onReceived(
							clientSocket, utf8data, sz);
					}
				}


				@Override
				public void onConnected2Server (BluetoothSocket serverSocket) {
					if (null != GenericBluetooth.this.onServerListener) {
						GenericBluetooth.this.onServerListener
							.onConnected2Server(serverSocket);
					}
				}


				@Override
				public void onConnectFail (BluetoothSocket serverSocket,
					String failMsg) {
					if (null != GenericBluetooth.this.onServerListener) {
						GenericBluetooth.this.onServerListener.onConnectFail(
							serverSocket, failMsg);
					}
				}

			};

			if (this.clientThread.startConnectServer(serverMacAddr,
				serverUUID) < 0) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			Log.e(TAG + ":startClient", "ERROR: " + e.getMessage());

			return false;
		}
	} /* startClient */


	public void stopClient (byte[] closeData, int offset, int sz) {
		try {
			if (null != this.clientThread) {
				this.clientThread.finish(closeData, offset, sz);
				this.clientThread = null;
			}

		} catch (Exception e) {
			Log.e(TAG + ":clientThread", "ERROR: " + e.getMessage());
		}
	} /* stopClient */


	private ClientThread clientThread = null;

	private OnServerListener onServerListener = null;


	/* start server when new this ServerThread */
	private abstract class ServerThread extends Thread implements
		OnClientListener {

		public ServerThread (String serverName, String uuid4server) {
			this.serverName = serverName;
			this.uuid4server = uuid4server;

			this.terminate = false;
			this.running = false;
		}


		public void finish (byte[] closeData, int offset, int sz) {
			/* stop server */
			new StopServerThread(closeData, offset, sz).start();
		}


		class StopServerThread extends Thread {
			private byte[] closeData;

			private int offset;

			private int sz;


			public StopServerThread (byte[] closeData, int offset, int sz) {
				this.closeData = closeData;
				this.offset = offset;
				this.sz = sz;
			}


			@Override
			public void run () {
				/* close and send null will let run exit */
				try {
					if (null != GenericBluetooth.this.serverSocket) {
						GenericBluetooth.this.serverSocket.close();
						GenericBluetooth.this.serverSocket = null;

						GenericBluetooth.this.clients.clear();
					}
				} catch (IOException e) {
					;
				}

				/* now: not need stopRunning */
				try {
					ServerThread.this.stopRunning();
				} catch (Exception e) {
					;
				}

				try {
					if (null != GenericBluetooth.this.clients) {
						GenericBluetooth.this.send2AllClients(closeData,
							offset, sz);
						GenericBluetooth.this.clients.clear();

						try {
							Thread.sleep(100);
						} catch (Exception e) {
							;
						}

						try {
							for (ReadThread r : GenericBluetooth.this.clientsReadThreads) {
								try {
									r.finish();
								} catch (Exception e) {
									e.printStackTrace();
									continue;
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

						GenericBluetooth.this.clientsReadThreads.clear();
					}
				} catch (Exception e) {
					;
				}
			};
		} /* StopServerThread */


		@Override
		public void run () {
			this.running = true;

			if (null != GenericBluetooth.this.serverSocket) {
				try {
					GenericBluetooth.this.serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				GenericBluetooth.this.serverSocket = null;

				try {
					if (null != GenericBluetooth.this.clients) {
						GenericBluetooth.this.send2AllClients("".getBytes(),
							0, 0);
						GenericBluetooth.this.clients.clear();

						try {
							Thread.sleep(100);
						} catch (Exception e) {
							;
						}

						try {
							for (ReadThread r : GenericBluetooth.this.clientsReadThreads) {
								try {
									r.finish();
								} catch (Exception e) {
									e.printStackTrace();
									continue;
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

						GenericBluetooth.this.clientsReadThreads.clear();
					}
				} catch (Exception e) {
					;
				}
			}

			/*
			 * 创建一个蓝牙服务器 参数 服务器名称 UUID
			 */
			try {
				GenericBluetooth.this.serverSocket = GenericBluetooth.this.bluetoothAdapter
					.listenUsingRfcommWithServiceRecord(this.serverName,
						UUID.fromString(this.uuid4server));
			} catch (IOException ee) {
				ee.printStackTrace();
			}

			while (!this.terminate) {
				try {
					/* 接受客户端的连接请求 */
					if (null == GenericBluetooth.this.serverSocket) {
						break;
					}

					BluetoothSocket clientSocket;
					try {
						Log.i(TAG, "accept");
						clientSocket = GenericBluetooth.this.serverSocket
							.accept();
						Log.i(TAG, "accept 2");
					} catch (Exception e) {
						Log.e(TAG + ":ServerThread:run:accept",
							"ERROR: " + e.getMessage());

						break;
					}

					onClientConnected(clientSocket);

					GenericBluetooth.this.clients.add(clientSocket);

					/* 启动接受数据 */
					ReadThread readThread = new ReadThread(clientSocket) {

						@Override
						public void onReceived (BluetoothSocket clientSocket,
							byte[] utf8Data, int sz) {
							ServerThread.this.onReceived(clientSocket,
								utf8Data, sz);
						}

					};

					readThread.start();

					GenericBluetooth.this.clientsReadThreads.add(readThread);
				} catch (Exception e) {
					Log.e(TAG + ":ServerThread:run",
						"ERROR: " + e.getMessage());

					break;
				}
			}

			this.running = false;
		}


		public void stopRunning () {
			this.terminate = true;

			while (this.running) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}


		private String uuid4server = null;

		private String serverName = null;

		private boolean terminate = true;

		private boolean running = false;
	}; /* ServerThread */


	List <BluetoothSocket> clients = new ArrayList <BluetoothSocket>();

	List <ReadThread> clientsReadThreads = new ArrayList <ReadThread>();


	public int send2AllClients (byte[] utf8Data, int offset, int count) {
		if (null == this.clients || this.clients.size() <= 0) {
			return 0;
		}

		int n = this.clients.size();

		Log.i(TAG + ":send2AllClients", "n: " + n);

		int ok = 0;
		for (int i = 0; i < n; ++i) {
			OutputStream os = null;
			try {
				os = this.clients.get(i).getOutputStream();
			} catch (Exception e) {
				e.printStackTrace();

				continue;
			}

			try {
				os.write(utf8Data, offset, count);
			} catch (Exception e) {
				e.printStackTrace();

				continue;
			}

			++ok;
		}

		return (n == ok) ? n : ok * -1;
	} /* send2AllClients */


	public int send2server (byte[] utf8Data, int offset, int count) {
		if (null == this.clientSocket) {
			return bluetooth_errno.ENOCONN * -1;
		}

		try {
			OutputStream os = this.clientSocket.getOutputStream();

			os.write(utf8Data, offset, count);

			return count;
		} catch (Exception e) {
			return bluetooth_errno.ESENDEXCEPTION * -1;
		}
	}


	public interface OnReceived {
		public void onReceived (BluetoothSocket readSocket, byte[] utf8Data,
			int sz);
	} /* OnReceived */


	private abstract class ReadThread extends Thread implements OnReceived {
		public ReadThread (BluetoothSocket clientSocket) {
			this.clientSocket = clientSocket;

			this.terminate = false;
			this.running = false;
		}


		public void finish () {
			new Thread() {
				@Override
				public void run () {
					ReadThread.this.stopRunning();

					try {
						ReadThread.this.clientSocket.close();
						ReadThread.this.clientSocket = null;
					} catch (Exception e) {
						;
					}
				}
			}.start();
		}


		@Override
		public void run () {
			this.running = true;

			byte[] buffer = new byte[1024];
			int nbytes;
			InputStream inStream = null;

			try {
				inStream = this.clientSocket.getInputStream();
			} catch (IOException e) {
				Log.e(TAG + ":ServerThread:ReadThread:run",
					"ERROR: " + e.getMessage());
			}

			while (!this.terminate) {
				try {
					/* Read from the InputStream */
					if ((nbytes = inStream.read(buffer)) > 0) {
						byte[] utf8Data = new byte[nbytes];
						for (int i = 0; i < nbytes; i++) {
							utf8Data[i] = buffer[i];
						}

						onReceived(clientSocket, utf8Data, nbytes);
					}
				} catch (Exception e) {
					try {
						inStream.close();
					} catch (IOException ee) {
						;
					}
					break;
				}
			}

			this.running = false;
		}


		public void stopRunning () {
			this.terminate = true;

			this.interrupt();

			while (this.running) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}


		private boolean terminate = true;

		private boolean running = false;

		private BluetoothSocket clientSocket = null;
	} /* ReadThread */


	public interface OnServerListener {
		public void onConnected2Server (BluetoothSocket serverSocket);


		public void onConnectFail (BluetoothSocket serverSocket,
			String failMsg);


		public void onReceived (BluetoothSocket clientSocket,
			byte[] utf8Data, int sz);
	} /* OnServerListener */


	/* start client */
	private abstract class ClientThread extends Thread implements
		OnServerListener {
		public void finish (byte[] closeData, int offset, int sz) {
			/* stop client */
			new StopClientThread(closeData, offset, sz).start();
		}


		class StopClientThread extends Thread {
			public StopClientThread (byte[] closeData, int offset, int sz) {
				this.closeData = closeData;
				this.offset = offset;
				this.sz = sz;
			}


			@Override
			public void run () {
				try {
					if (null != ClientThread.this.readThread) {
						ClientThread.this.readThread.finish();
						ClientThread.this.readThread = null;
					}
				} catch (Exception e) {
					;
				}

				try {
					if (null != GenericBluetooth.this.clientSocket) {
						try {
							OutputStream os = GenericBluetooth.this.clientSocket
								.getOutputStream();
							os.write(closeData, offset, sz);

							os.flush();
						} catch (Exception e) {
							;
						}

						try {
							Thread.sleep(100);
						} catch (Exception e) {
							;
						}

						GenericBluetooth.this.clientSocket.close();

						GenericBluetooth.this.clientSocket = null;
					}
				} catch (IOException e) {
					;
				}
			};


			private byte[] closeData;

			private int offset;

			private int sz;
		}


		public int startConnectServer (String macAddr, String serverUUID) {
			try {
				if (null != GenericBluetooth.this.clientSocket) {
					GenericBluetooth.this.clientSocket.close();
					GenericBluetooth.this.clientSocket = null;
				}

				BluetoothDevice device = GenericBluetooth.this.bluetoothAdapter
					.getRemoteDevice(macAddr);

				if (null == device) {
					return -2;
				}

				GenericBluetooth.this.clientSocket = device
					.createRfcommSocketToServiceRecord(UUID
						.fromString(serverUUID));

				if (null == GenericBluetooth.this.clientSocket) {
					return -3;
				}

				this.start();

				return 0;
			} catch (Exception e) {
				return -1;
			}
		}


		@Override
		public void run () {
			try {
				try {
					GenericBluetooth.this.clientSocket.connect();
				} catch (Exception e) {
					onConnectFail(GenericBluetooth.this.clientSocket,
						e.getMessage());
					return;
				}

				onConnected2Server(GenericBluetooth.this.clientSocket);

				/* 启动接受数据 */
				this.readThread = new ReadThread(
					GenericBluetooth.this.clientSocket) {

					@Override
					public void onReceived (BluetoothSocket serverSocket,
						byte[] utf8Data, int sz) {
						ClientThread.this.onReceived(serverSocket, utf8Data,
							sz);
					}

				};
				this.readThread.start();
			} catch (Exception e) {
				onConnectFail(GenericBluetooth.this.clientSocket,
					e.getMessage());
				return;
			}
		}


		private ReadThread readThread = null;
	};/* ClientThread */


	public String[] getPairedDeviceName () {
		int c = 0;
		Set <BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
		String[] name_list = new String[devices.size()];
		for (BluetoothDevice device : devices) {
			name_list[c] = device.getName();
			c++;
		}
		return name_list;
	}


	public String[] getPairedDeviceAddress () {
		int c = 0;
		Set <BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
		String[] address_list = new String[devices.size()];
		for (BluetoothDevice device : devices) {
			address_list[c] = device.getAddress();
			c++;
		}
		return address_list;
	}


	public interface BluetoothStateListener {
		public void onServiceStateChanged (int state);
	}


	public interface OnDataReceivedListener {
		public void onDataReceived (byte[] data, String message);
	}


	public interface BluetoothConnectionListener {
		public void onDeviceConnected (String name, String address);


		public void onDeviceDisconnected ();


		public void onDeviceConnectionFailed ();
	}


	public interface AutoConnectionListener {
		public void onAutoConnectionStarted ();


		public void onNewConnection (String name, String address);
	}


	private static final String TAG = GenericBluetooth.class.getSimpleName();

	/* this socket used to accept connection(s) from client, only one */
	private BluetoothServerSocket serverSocket = null;

	/* this socket used to connect server, only one */
	private BluetoothSocket clientSocket = null;

	/* Context from activity which call this class */
	private Context context = null;

	/* Local Blue tooth adapter */
	private BluetoothAdapter bluetoothAdapter = null;

	private boolean savedEnable = false;

	/* LE */

	private int le = -1;

	private BluetoothLe LE = null;

	private BluetoothGatt leConnectedGatt = null;

	private String leWriteUUID = "";

	private String leReadUUID = "";

	private BluetoothGattCharacteristic leGattRead = null;

	private BluetoothGattCharacteristic leGattWrite = null;

	private LeScanThread leScanThread = null;

	private LeReadyThread leReadyThread = null;

}
