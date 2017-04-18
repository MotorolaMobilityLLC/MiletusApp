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
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.moto.miletus.gson.ExecuteCommandHelper;
import com.moto.miletus.utils.Strings;

import org.json.JSONException;

/**
 * SendExecuteGattCommand
 */
public class SendExecuteGattCommand extends SendGattCommand {

    private static final String TAG = SendExecuteGattCommand.class.getSimpleName();
    private final OnBleExecuteResponse onBleExecuteResponse;

    /**
     * SendExecuteGattCommand
     *
     * @param context              Context
     * @param onBleExecuteResponse OnBleExecuteResponse
     * @param bleDevice            BluetoothDevice
     * @param command              String
     */
    public SendExecuteGattCommand(final Context context,
                                  final OnBleExecuteResponse onBleExecuteResponse,
                                  final BluetoothDevice bleDevice,
                                  final String command) {
        super(context,
                bleDevice,
                Strings.BLE_EXECUTE_COMMAND_JSON_PREFIX
                        + command
                        + "&"
                        + Strings.OFFSET);
        this.onBleExecuteResponse = onBleExecuteResponse;
        Log.i(TAG, bleDevice.getName());
    }

    /**
     * chunkFull
     *
     * @param chunk String
     */
    @Override
    protected void chunkFull(@NonNull final String chunk) {
        if (chunk.isEmpty()) {
            onBleExecuteResponse.onBleExecuteResponse(false);
            return;
        }

        try {
            boolean isSuccess = ExecuteCommandHelper.isStateDone(chunk);
            onBleExecuteResponse.onBleExecuteResponse(isSuccess);
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            onBleExecuteResponse.onBleExecuteResponse(false);
        }
    }

    /**
     * OnBleExecuteResponse
     */
    public interface OnBleExecuteResponse {
        /**
         * onBleExecuteResponse
         *
         * @param isSuccess boolean
         */
        void onBleExecuteResponse(final boolean isSuccess);
    }

}
