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

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.moto.miletus.gson.ComponentsHelper;
import com.moto.miletus.utils.Strings;
import com.moto.miletus.wrappers.ComponentWrapper;
import com.moto.miletus.wrappers.DeviceWrapper;
import com.moto.miletus.wrappers.StateWrapper;

import org.json.JSONException;

import java.util.Set;

/**
 * SendComponentsGattCommand
 */
public class SendComponentsGattCommand extends SendGattCommand {

    private static final String TAG = SendComponentsGattCommand.class.getSimpleName();
    private final OnBleComponentsResponse onBleComponentsResponse;
    private final DeviceWrapper device;
    private final Set<ComponentWrapper> components;

    /**
     * SendComponentsGattCommand
     *
     * @param context                 Context
     * @param onBleComponentsResponse OnBleComponentsResponse
     * @param device                  DeviceWrapper
     * @param components              Set<ComponentWrapper>
     */
    public SendComponentsGattCommand(final Context context,
                                     final OnBleComponentsResponse onBleComponentsResponse,
                                     final DeviceWrapper device,
                                     final Set<ComponentWrapper> components) {
        super(context, device.getBleDevice(), Strings.COMPONENTS_BLE);
        this.onBleComponentsResponse = onBleComponentsResponse;
        this.device = device;
        this.components = components;
        Log.i(TAG, device.getBleDevice().getName());
    }

    /**
     * chunkFull
     *
     * @param chunk String
     */
    @Override
    protected void chunkFull(@NonNull final String chunk) {
        if (chunk.isEmpty()) {
            onBleComponentsResponse.onBleComponentsResponse(components, null, device, false);
            return;
        }

        try {
            Set<StateWrapper> stateWrappers = ComponentsHelper.addComponentStates(chunk, components);
            onBleComponentsResponse.onBleComponentsResponse(components, stateWrappers, device, true);
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            onBleComponentsResponse.onBleComponentsResponse(components, null, device, false);
        }
    }

    public interface OnBleComponentsResponse {
        void onBleComponentsResponse(final Set<ComponentWrapper> components,
                                     final Set<StateWrapper> states,
                                     final DeviceWrapper device,
                                     final boolean isSuccess);
    }

}
