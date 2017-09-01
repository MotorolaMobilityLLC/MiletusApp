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

package com.moto.miletus.ble.commands;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.moto.miletus.ble.BleDevicesHolder;
import com.moto.miletus.utils.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * SendGattCommand
 */
abstract class SendGattCommand extends AsyncTask<Void, Void, Void> {

    private static final String TAG = SendGattCommand.class.getSimpleName();
    private static final UUID FFE1 = UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB");
    private static final UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final byte[] ENABLE_NOTIFICATION_VALUE = {0x01, 0x00};
    private static final int GATT_INTERNAL_ERROR = 129;

    private static final int MAX_BLE_PACKET_SIZE = 16;
    private static final int SIZE = 5;
    private static final int TIME_OUT = 30;
    private static final int MAX_RETRY = 10;

    private static final String DEVICE = "Device: ";
    private static final String COMMAND = "Command: ";

    private int retry = 0;
    private ScheduledFuture result;
    private BluetoothGatt mGatt;
    private String chunk = "";
    private int writes = 0;
    private String command;
    private int chunkEnd;
    private final List<String> packets = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Context context;
    final BluetoothDevice bleDevice;
    private boolean isConnected = false;
    private boolean isSuccessful = false;

    /**
     * SendGattCommand
     *
     * @param context   Context
     * @param bleDevice BluetoothDevice
     * @param command   String
     */
    SendGattCommand(final Context context,
                    final BluetoothDevice bleDevice,
                    final String command) {
        this.context = context;
        this.bleDevice = bleDevice;

        initialize(command);

        Log.i(TAG, "new SendGattCommand: " + this.command + Strings.NEW_LINE +
                DEVICE + bleDevice.getName());
    }

    /**
     * initialize
     *
     * @param command String
     */
    private void initialize(final String command) {
        String size = (command.length() + SIZE) + "";
        while (size.length() < SIZE - 1) {
            size = "0" + size;
        }
        this.command = command + size + Strings.BYTE;

        this.packets.add(Strings.LENGTH + size + Strings.BYTE);
        if (this.command.length() >= MAX_BLE_PACKET_SIZE) {
            int index = 0;
            while (index < this.command.length()) {
                this.packets.add(this.command.substring(index,
                        Math.min(index + MAX_BLE_PACKET_SIZE, this.command.length())));
                index += MAX_BLE_PACKET_SIZE;
            }
        } else {
            this.packets.add(this.command);
        }
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt,
                                            int status,
                                            int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server" + Strings.NEW_LINE +
                        DEVICE + bleDevice.getName());
                isConnected = true;
                mGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server" + Strings.NEW_LINE +
                        DEVICE + bleDevice.getName());
                isConnected = false;
                close();
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt,
                                         final int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "onServicesDiscovered: " + gatt.getServices().size());
                sendCommand(gatt);
            } else if (status == GATT_INTERNAL_ERROR) {
                Log.e(TAG, "GATT_INTERNAL_ERROR: " + status);

                Toast.makeText(context,
                        "Please reboot the BLE hardware in your IoT device: "
                                + bleDevice.getName(),
                        Toast.LENGTH_LONG)
                        .show();

                mGatt.discoverServices();
            }  else {
                Log.e(TAG, "onServicesDiscovered received status: " + status);
                disconnect();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "onCharacteristicWrite Success");

                if (packets.isEmpty()
                        || writes == (packets.size() - 1)) {
                    readCommand(gatt);
                } else if (writes < (packets.size() - 1)) {
                    writes++;
                    sendCommand(gatt);
                }
            } else {
                Log.e(TAG, "onCharacteristicWrite received: " + status);
                disconnect();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.i(TAG, "onCharacteristicRead status: " + status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            String result = new String(characteristic.getValue());

            Log.i(TAG, "onCharacteristicChanged result: " + result);

            if (result.contains(Strings.LENGTH)) {
                result = result.replace(Strings.LENGTH, "")
                        .replace(Strings.BYTE, "");
                chunkEnd = Integer.parseInt(result);
                Log.i(TAG, "lenght: " + result);
                return;
            }

            chunk = chunk + result;

            Log.i(TAG, "Chunk: " + chunk);

            if (chunk.length() >= chunkEnd) {
                if (chunk.contains(Strings.OFFSET)) {
                    chunk = chunk.substring(0, chunk.lastIndexOf(Strings.OFFSET));
                }

                chunkFull(chunk);
                isSuccessful = true;
                disconnect();
            } else {
                Log.i(TAG, "Chunk: "
                        + chunk.length()
                        + " of "
                        + chunkEnd);
            }
        }
    };

    /**
     * chunkFull
     *
     * @param chunk String
     */
    protected abstract void chunkFull(@NonNull final String chunk);

    /**
     * sendCommand
     *
     * @param gatt BluetoothGatt
     */
    private void sendCommand(final BluetoothGatt gatt) {
        for (BluetoothGattService service : gatt.getServices()) {
            for (BluetoothGattCharacteristic charact : service.getCharacteristics()) {
                if (FFE1.equals(charact.getUuid())
                        && ((charact.getProperties() | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0)
                        && ((charact.getProperties() | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0)) {
                    Log.i(TAG, "sendCommand start");
                    writeCharacteristic(charact);
                    Log.i(TAG, "sendCommand done");
                    return;
                }
            }
        }

        disconnect();
    }

    /**
     * readCommand
     *
     * @param gatt BluetoothGatt
     */
    private void readCommand(final BluetoothGatt gatt) {
        for (BluetoothGattService service : gatt.getServices()) {
            for (BluetoothGattCharacteristic charact : service.getCharacteristics()) {
                if (FFE1.equals(charact.getUuid())
                        && ((charact.getProperties() | BluetoothGattCharacteristic.PROPERTY_READ) > 0)) {
                    Log.i(TAG, "readCommand start");
                    readCharacteristic(charact);
                    Log.i(TAG, "readCommand done");
                    return;
                }
            }
        }

        disconnect();
    }

    /**
     * readCharacteristic
     *
     * @param characteristic BluetoothGattCharacteristic
     */
    private void readCharacteristic(final BluetoothGattCharacteristic characteristic) {
        if (mGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
            return;
        }

        mGatt.setCharacteristicNotification(characteristic, true);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
        descriptor.setValue(ENABLE_NOTIFICATION_VALUE);

        if (!mGatt.writeDescriptor(descriptor)) {
            Log.e(TAG, "readCharacteristic");
        }
    }

    /**
     * writeCharacteristic
     *
     * @param characteristic BluetoothGattCharacteristic
     */
    private void writeCharacteristic(final BluetoothGattCharacteristic characteristic) {
        if (mGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
            return;
        }

        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

        if (packets.isEmpty()) {
            characteristic.setValue(command);
            Log.i(TAG, "setValue: " + command);
        } else {
            characteristic.setValue(packets.get(writes));
            Log.i(TAG, "setValue: " + packets.get(writes));
        }

        if (!mGatt.writeCharacteristic(characteristic)) {
            Log.e(TAG, "writeCharacteristic");
        }
    }

    /**
     * disconnect
     */
    private void disconnect() {
        if (mGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
            return;
        }

        if (!isConnected) {
            Log.e(TAG, "BluetoothAdapter not connected");
            close();
            return;
        }

        Log.i(TAG, "BluetoothAdapter disconnect");
        mGatt.disconnect();
    }

    /**
     * close
     */
    private void close() {
        Log.i(TAG, "close" + Strings.NEW_LINE +
                DEVICE + bleDevice.getName());

        if (mGatt != null) {
            mGatt.close();
            mGatt = null;
        }

        cancel();
    }

    /**
     * Cancel.
     */
    private void cancel() {
        if (result != null) {
            result.cancel(true);
        }

        if (!isSuccessful) {
            chunkFull("");
        }

        BleDevicesHolder.getDiscoveredBleDevices().remove(bleDevice);
        cancel(true);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        result = scheduler
                .scheduleWithFixedDelay(new Runnable() {
                    public void run() {
                        Log.i(TAG, "Retry: " + retry + Strings.NEW_LINE +
                                COMMAND + command + Strings.NEW_LINE +
                                DEVICE + bleDevice.getName());

                        retry++;

                        if (retry >= MAX_RETRY) {
                            Log.e(TAG, "MAX_RETRY" + Strings.NEW_LINE +
                                    COMMAND + command + Strings.NEW_LINE +
                                    DEVICE + bleDevice.getName());
                            close();
                        } else if (mGatt == null) {
                            connectGatt();
                        } else {
                            Log.e(TAG, "TIME_OUT" + Strings.NEW_LINE +
                                    COMMAND + command + Strings.NEW_LINE +
                                    DEVICE + bleDevice.getName());
                            disconnect();
                        }
                    }
                }, 0, TIME_OUT, TimeUnit.SECONDS);
        return null;
    }

    /**
     * connectGatt
     */
    private void connectGatt() {
        Log.i(TAG, "Connect Gatt" + Strings.NEW_LINE +
                COMMAND + command + Strings.NEW_LINE +
                DEVICE + bleDevice.getName());

        if (mGatt != null) {
            return;
        }

        mGatt = bleDevice.connectGatt(context, false, mGattCallback);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
            mGatt.requestMtu(256);
        }
    }

    /**
     * execute
     */
    public void execute() {
        try {
            super.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (OutOfMemoryError e) {
            super.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }
    }
}
