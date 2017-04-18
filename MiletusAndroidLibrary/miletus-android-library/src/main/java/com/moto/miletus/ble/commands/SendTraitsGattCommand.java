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

import com.moto.miletus.gson.TraitsHelper;
import com.moto.miletus.gson.traits.Traits;
import com.moto.miletus.utils.Strings;
import com.moto.miletus.wrappers.ComponentWrapper;

import org.json.JSONException;

import java.util.Set;

/**
 * SendTraitsGattCommand
 */
public class SendTraitsGattCommand extends SendGattCommand {

    private static final String TAG = SendTraitsGattCommand.class.getSimpleName();
    private final OnBleTraitsResponse onBleTraitsResponse;

    /**
     * SendTraitsGattCommand
     *
     * @param context             Context
     * @param onBleTraitsResponse OnBleTraitsResponse
     * @param bleDevice           BluetoothDevice
     */
    public SendTraitsGattCommand(final Context context,
                                 final OnBleTraitsResponse onBleTraitsResponse,
                                 final BluetoothDevice bleDevice) {
        super(context, bleDevice, Strings.TRAITS_BLE);
        this.onBleTraitsResponse = onBleTraitsResponse;
        Log.i(TAG, bleDevice.getName());
    }

    public interface OnBleTraitsResponse {
        void onBleTraitsResponse(final Set<ComponentWrapper> components,
                                 final boolean isSuccess);
    }

    /**
     * chunkFull
     *
     * @param chunk String
     */
    @Override
    protected void chunkFull(@NonNull final String chunk) {
        if (chunk.isEmpty()) {
            onBleTraitsResponse.onBleTraitsResponse(null, false);
            return;
        }

        try {
            Traits traits = TraitsHelper.jsonToTraits(chunk);
            Set<ComponentWrapper> components = TraitsHelper.traitsToComponentCommands(traits);
            onBleTraitsResponse.onBleTraitsResponse(components, true);
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            onBleTraitsResponse.onBleTraitsResponse(null, false);
        }
    }

}
