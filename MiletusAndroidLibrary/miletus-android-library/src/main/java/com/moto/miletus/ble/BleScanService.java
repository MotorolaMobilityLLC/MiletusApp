/*
 * The MIT License (MIT)
 * Copyright (c) 2016 Gustavo Frederico Temple Pedrosa -- gustavof@motorola.com
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.moto.miletus.ble;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.moto.miletus.ble.commands.SendInfoGattCommand;
import com.moto.miletus.utils.Strings;

import java.util.ArrayList;

/**
 * BleScanService
 */
public final class BleScanService extends Service {

    private static final String TAG = BleScanService.class.getSimpleName();
    private static BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mLeScanner;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    private ScanCallback mScanCallback;
    private boolean mScanning;
    private final IBinder mBinder = new LocalBinder();
    private OnBleResolved onBleResolved;
    private SendInfoGattCommand.OnBleInfoResponse onBleInfoResponse;

    public interface OnBleResolved {
        void onBleResolved(final BluetoothDevice device,
                           final int rssi);
    }

    @Override
    public int onStartCommand(Intent intent,
                              int flags,
                              int startId) {
        if (mBluetoothAdapter != null) {
            return START_STICKY;
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_LONG).show();
            stopSelf();
            return START_NOT_STICKY;
        }

        BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if (!isEnabled()) {
            Toast.makeText(this, "Turn on the Bluetooth", Toast.LENGTH_LONG).show();
            stopSelf();
            return START_NOT_STICKY;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mScanCallback = getScanCallback();
            scanBleNew(mScanCallback);
        } else {
            mLeScanCallback = getLeScanCallback();
            scanBleOld(mLeScanCallback);
        }

        Log.i(TAG, "START_STICKY");
        return START_STICKY;
    }

    /**
     * isEnabled
     *
     * @return boolean
     */
    private static boolean isEnabled() {
        return !(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled());
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onDestroy() {
        Log.i(TAG, "onDestroy");

        if (isEnabled() && mScanning) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mLeScanner.stopScan(mScanCallback);
            } else {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }

        mBluetoothAdapter = null;
        BleDevicesHolder.clear();

        super.onDestroy();
    }

    /**
     * scanBleOld
     *
     * @param mLeScanCallback LeScanCallback
     */
    @SuppressWarnings("deprecation")
    private void scanBleOld(final BluetoothAdapter.LeScanCallback mLeScanCallback) {
        if (isEnabled()) {
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            Log.i(TAG, "startLeScan");
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            Log.i(TAG, "stopLeScan");
        }
    }

    /**
     * scanBleNew
     *
     * @param mScanCallback ScanCallback
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void scanBleNew(final ScanCallback mScanCallback) {
        mLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (isEnabled()) {
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            mLeScanner.startScan(new ArrayList<ScanFilter>(), settings, mScanCallback);
            Log.i(TAG, "startScan");
            mScanning = true;
        } else {
            mLeScanner.stopScan(mScanCallback);
            Log.i(TAG, "stopScan");
            mScanning = false;
        }
    }

    /**
     * getLeScanCallback
     *
     * @return LeScanCallback
     */
    private BluetoothAdapter.LeScanCallback getLeScanCallback() {
        return new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device,
                                 final int rssi,
                                 final byte[] scanRecord) {

                if (onBleResolved != null) {
                    onBleResolved.onBleResolved(device, rssi);
                }

                if (device.getName().contains(Strings.mSearchNameBle)
                        && !BleDevicesHolder.getResolvedBleDevices().contains(device)
                        && !BleDevicesHolder.getDiscoveredBleDevices().contains(device)
                        && onBleInfoResponse != null) {
                    BleDevicesHolder.getDiscoveredBleDevices().add(device);
                    onBleLibMiletusResolved(device);
                }
            }
        };
    }

    /**
     * onBleLibMiletusResolved
     *
     * @param device BluetoothDevice
     */
    private void onBleLibMiletusResolved(BluetoothDevice device) {
        if (onBleInfoResponse == null) {
            return;
        }

        final SendInfoGattCommand sendGattCommand = new SendInfoGattCommand(this, onBleInfoResponse, device);
        try {
            sendGattCommand.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (OutOfMemoryError e) {
            Log.e(TAG, e.toString());
            sendGattCommand.execute();
        }
    }

    /**
     * getScanCallback
     *
     * @return ScanCallback
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private ScanCallback getScanCallback() {
        return new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                if (result == null
                        || result.getDevice() == null
                        || result.getDevice().getName() == null) {
                    return;
                }

                if (onBleResolved != null) {
                    onBleResolved.onBleResolved(result.getDevice(), result.getRssi());
                }

                if (result.getDevice().getName().contains(Strings.mSearchNameBle)
                        && !BleDevicesHolder.getResolvedBleDevices().contains(result.getDevice())
                        && !BleDevicesHolder.getDiscoveredBleDevices().contains(result.getDevice())
                        && onBleInfoResponse != null) {
                    BleDevicesHolder.getDiscoveredBleDevices().add(result.getDevice());
                    onBleLibMiletusResolved(result.getDevice());
                }
            }
        };
    }

    /**
     * Inner class that represents the receiver of the action:
     * android.bluetooth.adapter.action.STATE_CHANGED
     */
    public static class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context,
                              final Intent intent) {

            if (!intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                return;
            }

            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

            if ((state == BluetoothAdapter.STATE_OFF
                    || state == BluetoothAdapter.STATE_TURNING_OFF)
                    && (mBluetoothAdapter != null)) {
                context.stopService(new Intent(context, BleScanService.class));
            } else if ((state == BluetoothAdapter.STATE_ON
                    || state == BluetoothAdapter.STATE_TURNING_ON)
                    && (mBluetoothAdapter == null)) {
                context.startService(new Intent(context, BleScanService.class));
            }

        }
    }

    /**
     * LocalBinder
     */
    public class LocalBinder extends Binder {
        public void setOnBleInfoResponse(final SendInfoGattCommand.OnBleInfoResponse bleInfoResponse) {
            onBleInfoResponse = bleInfoResponse;
        }

        public void setOnBleResolved(final OnBleResolved bleResolved) {
            onBleResolved = bleResolved;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
