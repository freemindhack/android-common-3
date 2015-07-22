/*
 * Copyright (C) 2013 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
/*
 * version 1.0.0.1 20150717 AM class BluetoothLe
 */

package nocom.bluetooth.le;


import java.util.List;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;


/**
 * Service for managing connection and data communication with a GATT server
 * hosted on a given Bluetooth LE device.
 */
public class BluetoothLe {
	private final static String TAG = BluetoothLe.class.getSimpleName();

	private BluetoothManager mBluetoothManager;

	private BluetoothAdapter bluetoothAdapter;

	private String bluetoothDevAddr;

	private BluetoothGatt bluetoothGatt;


	public interface OnConnectListener {
		public void onConnect (BluetoothGatt gatt);
	}


	public interface OnDisconnectListener {
		public void onDisconnect (BluetoothGatt gatt);
	}


	public interface OnServiceDiscoverListener {
		public void onServiceDiscover (BluetoothGatt gatt);
	}


	public interface OnReadRemoteRssiListener {
		public void onReadRemoteRssi (android.bluetooth.BluetoothGatt gatt,
			int rssi, int status);
	}


	public interface OnDataAvailableListener {
		public void onCharacteristicRead (BluetoothGatt gatt,
			BluetoothGattCharacteristic characteristic, int status);


		public void onCharacteristicWrote (BluetoothGatt gatt,
			BluetoothGattCharacteristic characteristic, int status);


		public void onCharacteristicChanged (BluetoothGatt gatt,
			BluetoothGattCharacteristic characteristic);
	}


	private OnConnectListener mOnConnectListener;

	private OnDisconnectListener mOnDisconnectListener;

	private OnServiceDiscoverListener mOnServiceDiscoverListener;

	private OnReadRemoteRssiListener onReadRemoteRssiListener;

	private OnDataAvailableListener onDataAvailableListener;

	private Context mContext;

	private String readUUID = "";


	public void setOnConnectListener (OnConnectListener l) {
		mOnConnectListener = l;
	}


	public void setOnDisconnectListener (OnDisconnectListener l) {
		mOnDisconnectListener = l;
	}


	public void setOnServiceDiscoverListener (OnServiceDiscoverListener l) {
		mOnServiceDiscoverListener = l;
	}


	public void OnReadRemoteRssiListener (
		OnReadRemoteRssiListener onReadRemoteRssiListener) {
		BluetoothLe.this.onReadRemoteRssiListener = onReadRemoteRssiListener;
	}


	public void setOnDataAvailableListener (OnDataAvailableListener l) {
		BluetoothLe.this.onDataAvailableListener = l;
	}


	public BluetoothLe (Context c) {
		mContext = c;
	}


	/*
	 * Implements callback methods for GATT events that the app cares about.
	 * For example,
	 * connection change and services discovered.
	 */
	private final BluetoothGattCallback gattcb = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange (BluetoothGatt gatt, int status,
			int newState) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				Log.v(TAG + ":gattcb:onConnectionStateChange",
					"STATE_CONNECTED");

				if (mOnConnectListener != null) {
					mOnConnectListener.onConnect(gatt);
				}

				Log.i(TAG, "Connected to GATT server.");
				// Attempts to discover services after successful connection.
				Log.i(TAG, "Attempting to start service discovery:"
					+ bluetoothGatt.discoverServices());

			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				Log.v(TAG + ":gattcb:onConnectionStateChange",
					"STATE_DISCONNECTED");

				if (mOnDisconnectListener != null) {
					mOnDisconnectListener.onDisconnect(gatt);
				}
				Log.i(TAG, "Disconnected from GATT server.");
			}
		}


		@Override
		public void onServicesDiscovered (BluetoothGatt gatt, int status) {
			Log.v(TAG + ":gattcb", "onServicesDiscovered");

			if (status == BluetoothGatt.GATT_SUCCESS
				&& mOnServiceDiscoverListener != null) {
				mOnServiceDiscoverListener.onServiceDiscover(gatt);
			} else {
				Log.w(TAG, "onServicesDiscovered received: " + status);
			}
		}


		@Override
		public void onCharacteristicRead (BluetoothGatt gatt,
			BluetoothGattCharacteristic characteristic, int status) {
			if (BluetoothLe.this.onDataAvailableListener != null) {
				BluetoothLe.this.onDataAvailableListener
					.onCharacteristicRead(gatt, characteristic, status);
			}
		}


		@Override
		public void onCharacteristicWrite (BluetoothGatt gatt,
			BluetoothGattCharacteristic characteristic, int status) {
			if (BluetoothLe.this.onDataAvailableListener != null) {
				BluetoothLe.this.onDataAvailableListener
					.onCharacteristicWrote(gatt, characteristic, status);
			}
		}


		@Override
		public void onReadRemoteRssi (android.bluetooth.BluetoothGatt gatt,
			int rssi, int status) {
			if (BluetoothLe.this.onReadRemoteRssiListener != null) {
				BluetoothLe.this.onReadRemoteRssiListener.onReadRemoteRssi(
					gatt, rssi, status);
			}
		}


		@Override
		public void onCharacteristicChanged (BluetoothGatt gatt,
			BluetoothGattCharacteristic characteristic) {
			BluetoothLe.this.setReadUUID(characteristic.getUuid().toString());

			if (BluetoothLe.this.onDataAvailableListener != null) {
				BluetoothLe.this.onDataAvailableListener
					.onCharacteristicChanged(gatt, characteristic);
			}
		}
	};


	/**
	 * Initializes a reference to the local Bluetooth adapter.
	 * 
	 * @return Return true if the initialization is successful.
	 */
	public boolean initialize () {
		// For API level 18 and above, get a reference to BluetoothAdapter
		// through
		// BluetoothManager.
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) mContext
				.getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				Log.e(TAG, "Unable to initialize BluetoothManager.");
				return false;
			}
		}

		BluetoothLe.this.bluetoothAdapter = mBluetoothManager.getAdapter();
		if (BluetoothLe.this.bluetoothAdapter == null) {
			Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
			return false;
		}

		return true;
	}


	/**
	 * Connects to the GATT server hosted on the Bluetooth LE device.
	 * 
	 * @param address
	 *            The device address of the destination device.
	 * 
	 * @return Return true if the connection is initiated successfully. The
	 *         connection result is reported asynchronously through the
	 *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 *         callback.
	 */
	public boolean connect (final String address, boolean noExists) {
		if (BluetoothLe.this.bluetoothAdapter == null || address == null) {
			Log.w(TAG,
				"BluetoothAdapter not initialized or unspecified address.");
			return false;
		}

		if (noExists) {
			this.bluetoothDevAddr = "";
		}

		/* Previously connected device. Try to reconnect. */
		if (bluetoothDevAddr != null && address.equals(bluetoothDevAddr)
			&& bluetoothGatt != null) {
			Log.d(TAG,
				"Trying to use an existing bluetoothGatt for connection.");
			if (bluetoothGatt.connect()) {
				return true;
			} else {
				return false;
			}
		}

		final BluetoothDevice device = BluetoothLe.this.bluetoothAdapter
			.getRemoteDevice(address);
		if (device == null) {
			Log.w(TAG, "Device not found.  Unable to connect.");
			return false;
		}
		// We want to directly connect to the device, so we are setting the
		// autoConnect
		// parameter to false.
		bluetoothGatt = device.connectGatt(mContext, false, gattcb);
		Log.d(TAG, "Trying to create a new connection.");
		bluetoothDevAddr = address;
		return true;
	}


	/**
	 * Disconnects an existing connection or cancel a pending connection. The
	 * disconnection result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 * callback.
	 */
	public void disconnect () {
		if (BluetoothLe.this.bluetoothAdapter == null
			|| bluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		bluetoothGatt.disconnect();
	}


	/**
	 * After using a given BLE device, the app must call this method to ensure
	 * resources are released properly.
	 */
	public void close () {
		if (bluetoothGatt == null) {
			return;
		}
		bluetoothGatt.close();
		bluetoothGatt = null;
	}


	public boolean triggerReadRemoteRssi () {
		Log.v(TAG, "triggerReadRemoteRssi");

		if (BluetoothLe.this.bluetoothAdapter == null
			|| bluetoothGatt == null) {
			Log.e(TAG, "BluetoothAdapter not initialized");
			return false;
		}

		return bluetoothGatt.readRemoteRssi();
	}


	/**
	 * Request a read on a given {@code BluetoothGattCharacteristic}. The read
	 * result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
	 * callback.
	 * 
	 * @param characteristic
	 *            The characteristic to read from.
	 */
	public boolean triggerReadCharacteristic (
		BluetoothGattCharacteristic characteristic) {
		Log.v(TAG, "triggerReadCharacteristic");

		if (BluetoothLe.this.bluetoothAdapter == null
			|| bluetoothGatt == null) {
			Log.e(TAG, "BluetoothAdapter not initialized");
			return false;
		}

		return bluetoothGatt.readCharacteristic(characteristic);
	}


	/**
	 * Enables or disables notification on a give characteristic.
	 * 
	 * @param characteristic
	 *            Characteristic to act on.
	 * @param enabled
	 *            If true, enable notification. False otherwise.
	 */
	public void setCharacteristicNotification (
		BluetoothGattCharacteristic characteristic, boolean enabled) {
		if (BluetoothLe.this.bluetoothAdapter == null
			|| bluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
	}


	public void writeCharacteristic (
		BluetoothGattCharacteristic characteristic) {
		Log.v(TAG, "writeCharacteristic");

		bluetoothGatt.writeCharacteristic(characteristic);
	}


	/**
	 * Retrieves a list of supported GATT services on the connected device.
	 * This should be invoked only after
	 * {@code BluetoothGatt#discoverServices()} completes successfully.
	 * 
	 * @return A {@code List} of supported services.
	 */
	public List <BluetoothGattService> getSupportedGattServices () {
		if (bluetoothGatt == null)
			return null;

		return bluetoothGatt.getServices();
	}


	public String getReadUUID () {
		return this.readUUID;
	}


	public void setReadUUID (String readUUID) {
		this.readUUID = readUUID;
	}
}