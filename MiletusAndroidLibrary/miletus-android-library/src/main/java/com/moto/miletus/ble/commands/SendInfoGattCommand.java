/*************************************************************************
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
 *************************************************************************/

package com.moto.miletus.ble.commands;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.moto.miletus.ble.BleDevicesHolder;
import com.moto.miletus.gson.InfoHelper;
import com.moto.miletus.gson.info.TinyDevice;
import com.moto.miletus.utils.Strings;
import com.moto.miletus.wrappers.DeviceWrapper;

import org.json.JSONException;

/**
 * SendInfoGattCommand
 */
public final class SendInfoGattCommand extends SendGattCommand {

    private static final String TAG = SendInfoGattCommand.class.getSimpleName();

    private final BluetoothDevice bleDevice;
    private final OnBleInfoResponse onBleInfoResponse;

    /**
     * SendInfoGattCommand
     *
     * @param context           Context
     * @param onBleInfoResponse OnBleInfoResponse
     * @param bleDevice         BluetoothDevice
     */
    public SendInfoGattCommand(final Context context,
                               final OnBleInfoResponse onBleInfoResponse,
                               final BluetoothDevice bleDevice) {
        super(context, bleDevice, Strings.INFO_BLE);
        this.onBleInfoResponse = onBleInfoResponse;
        this.bleDevice = bleDevice;
        Log.i(TAG, "New: " + bleDevice.getName());
    }

    public interface OnBleInfoResponse {
        void onBleInfoResponse(final DeviceWrapper device);
    }


    /**
     * chunkFull
     *
     * @param chunk String
     */
    @Override
    protected void chunkFull(@NonNull final String chunk) {
        if (chunk.isEmpty()) {
            return;
        }

        try {
            final TinyDevice tinyDevice = InfoHelper.jsonToTinyDevice(chunk);

            if (tinyDevice != null) {
                BleDevicesHolder.getResolvedBleDevices().add(bleDevice);
                onBleInfoResponse.onBleInfoResponse(new DeviceWrapper(tinyDevice, bleDevice));
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
    }

}
