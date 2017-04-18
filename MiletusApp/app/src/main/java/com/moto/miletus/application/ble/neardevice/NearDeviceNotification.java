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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.moto.miletus.application.DeviceListAdapter;
import com.moto.miletus.application.MainActivity;
import com.moto.miletus.application.R;
import com.moto.miletus.ble.commands.SendComponentsGattCommand;
import com.moto.miletus.gson.info.TinyDevice;
import com.moto.miletus.application.utils.Strings;
import com.moto.miletus.wrappers.ComponentWrapper;
import com.moto.miletus.wrappers.DeviceWrapper;
import com.moto.miletus.wrappers.ParameterValue;
import com.moto.miletus.wrappers.StateWrapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;

import java.util.HashSet;
import java.util.Set;

/**
 * NearDeviceNotification
 */
public class NearDeviceNotification
        extends BroadcastReceiver
        implements SendComponentsGattCommand.OnBleComponentsResponse {

    private static final String TAG = NearDeviceNotification.class.getSimpleName();
    private Context context;
    private NotificationManager systemService;

    @Override
    public void onReceive(final Context context,
                          final Intent intent) {
        Log.i(TAG, intent.toString());

        if (!intent.getAction().equals(Strings.NEAR_DEVICE)) {
            return;
        }

        this.context = context;
        this.systemService = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        findDevice();
    }

    /**
     * findDevice
     */
    private void findDevice() {
        if (NearDeviceHolder.getNearDevice() == null
                || DeviceListAdapter.getDataSetOriginal().isEmpty()) {
            notification(null, null, null);
            return;
        }

        for (final DeviceWrapper device : DeviceListAdapter.getDataSetOriginal()) {
            if (device.getBleDevice() != null
                    && NearDeviceHolder.getNearDevice().getAddress()
                    .equalsIgnoreCase(device.getBleDevice().getAddress())) {
                getLibMiletusBleState(device);
                return;
            }
        }
    }

    /**
     * getLibMiletusBleState
     *
     * @param device DeviceWrapper
     */
    private void getLibMiletusBleState(final DeviceWrapper device) {
        Log.i(TAG, "getLibMiletusBleState");

        final SendComponentsGattCommand sendComponentsGattCommand = new SendComponentsGattCommand(context,
                this,
                device,
                new HashSet<ComponentWrapper>());
        try {
            sendComponentsGattCommand.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (OutOfMemoryError e) {
            Log.e(TAG, e.toString());
            sendComponentsGattCommand.execute();
        }
    }

    /**
     * notification
     *
     * @param room  String
     * @param light ParameterValue
     * @param temp  ParameterValue
     */
    private void notification(final String room,
                              final ParameterValue light,
                              final ParameterValue temp) {
        String msg;
        if (room == null) {
            systemService.cancelAll();
            return;
        } else if (light != null
                && temp != null) {
            msg = "Welcome to: " + room + Strings.NEW_LINE
                    + "Light: " + light.getValue() + Strings.lux + Strings.NEW_LINE;

            try {
                String tempRound = Precision.round(Float.parseFloat(temp.getValue()), 1) + "";
                msg = msg
                        + "Temp: " + tempRound + Strings.celsius + Strings.NEW_LINE;
            } catch (NumberFormatException ex) {
                Log.e(TAG, ex.toString());
            }
        } else {
            msg = "Welcome to: " + room + Strings.NEW_LINE;
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.miletus)
                        .setContentTitle("Room Status")
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setContentText(msg);

        Intent resultIntent = new Intent(context, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        systemService.notify(9812, mBuilder.build());

        Log.i(TAG, msg);
    }

    @Override
    public void onBleComponentsResponse(final Set<ComponentWrapper> components,
                                        final Set<StateWrapper> states,
                                        final DeviceWrapper device,
                                        final boolean isSuccess) {
        if (!isSuccess) {
            Log.e(TAG, "Failure BLE querying for " + device.getDevice().getName());

            findDevice();

            return;
        }

        getDeviceStates(device, states);
    }

    /**
     * getDeviceStates
     *
     * @param device DeviceWrapper
     * @param states Set<StateWrapper>
     */
    private void getDeviceStates(final DeviceWrapper device,
                                 final Set<StateWrapper> states) {
        ParameterValue light = null;
        ParameterValue temp = null;

        for (final StateWrapper state : states) {
            if (StringUtils.containsIgnoreCase(state.getStateName(), Strings.light)) {
                light = state.getValue();
            }

            if (StringUtils.containsIgnoreCase(state.getStateName(), Strings.temperature)) {
                temp = state.getValue();
            }
        }

        notification(device.getDevice().getName(), light, temp);
    }

}
