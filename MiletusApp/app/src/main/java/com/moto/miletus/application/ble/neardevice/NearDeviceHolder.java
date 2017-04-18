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

package com.moto.miletus.application.ble.neardevice;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.v4.util.Pair;
import android.util.Log;

import com.moto.miletus.ble.BleScanService;
import com.moto.miletus.application.utils.Strings;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.rank.Median;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class NearDeviceHolder implements BleScanService.OnBleResolved {

    private static final String TAG = NearDeviceHolder.class.getSimpleName();
    private static final int SEGS = 4000;
    private static BluetoothDevice nearDevice;

    private long timeStamp;
    private final Context context;
    private final List<Pair<BluetoothDevice, Double>> list = new ArrayList<>();

    public NearDeviceHolder(final Context context,
                            final long timeStamp) {
        this.context = context;
        this.timeStamp = timeStamp;
    }

    /**
     * clear
     */
    private void clear() {
        list.clear();
        timeStamp = System.currentTimeMillis();
    }

    /**
     * onBleResolved
     *
     * @param device BluetoothDevice
     * @param rssi   rssi
     */
    @Override
    public void onBleResolved(final BluetoothDevice device,
                              final int rssi) {

        if (device.getName().contains(com.moto.miletus.utils.Strings.mSearchNameBle)) {
            list.add(new Pair<>(device, (double) rssi));
        }

        if ((System.currentTimeMillis() - timeStamp) >= SEGS) {
            Log.i(TAG, "Delta: " + (System.currentTimeMillis() - timeStamp) / 1000 + "s");

            whoWins();

            clear();
        }
    }

    /**
     * whoWins
     */
    private void whoWins() {
        if (list.isEmpty()
                || list.size() == 1) {
            broadcastDeviceRoom(null);
            setNearDevice(null);
            return;
        }

        Set<BluetoothDevice> devices = new HashSet<>();
        for (Pair<BluetoothDevice, Double> pair : list) {
            devices.add(pair.first);
        }

        Set<Pair<BluetoothDevice, Double>> medians = new HashSet<>(devices.size());
        for (BluetoothDevice device : devices) {
            List<Double> rssi = new ArrayList<>();
            for (Pair<BluetoothDevice, Double> pair : list) {
                if (pair.first.equals(device)) {
                    rssi.add(pair.second);
                }
            }

            Median median = new Median();
            median.setData(ArrayUtils.toPrimitive(rssi.toArray(new Double[rssi.size()])));
            medians.add(new Pair<>(device, median.evaluate()));
        }

        Pair<BluetoothDevice, Double> winner = new Pair<>(null, -999999d);
        for (Pair<BluetoothDevice, Double> median : medians) {
            if (median.second > winner.second) {
                winner = median;
            }
        }

        broadcastDeviceRoom(winner.first);
        setNearDevice(winner.first);
    }

    /**
     * broadcastDeviceRoom
     *
     * @param device BluetoothDevice
     */
    private void broadcastDeviceRoom(final BluetoothDevice device) {
        Intent i = new Intent(Strings.NEAR_DEVICE);

        if (device == null
                && getNearDevice() == null) {
            return;
        } else if (device != null
                && getNearDevice() != null
                && device.equals(getNearDevice())) {
            return;
        }

        context.sendBroadcast(i);

        if (device != null) {
            Log.i(TAG, "Near device: " + device.getAddress());
        } else {
            Log.i(TAG, "No near device.");
        }
    }

    /**
     * setNearDevice
     *
     * @param device BluetoothDevice
     */
    public static synchronized void setNearDevice(BluetoothDevice device) {
        nearDevice = device;
    }

    /**
     * getNearDevice
     *
     * @return BluetoothDevice
     */
    public static synchronized BluetoothDevice getNearDevice() {
        return nearDevice;
    }

}
