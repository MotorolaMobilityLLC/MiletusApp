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

package com.moto.miletus.application.cloud;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.moto.miletus.application.DeviceListAdapter;
import com.moto.miletus.application.utils.Strings;
import com.moto.miletus.ble.commands.SendComponentsGattCommand;
import com.moto.miletus.gson.info.TinyDevice;
import com.moto.miletus.mdns.SendComponentsCommand;
import com.moto.miletus.wrappers.ComponentWrapper;
import com.moto.miletus.wrappers.DeviceWrapper;
import com.moto.miletus.wrappers.StateWrapper;

import java.util.HashMap;
import java.util.Set;

/**
 * ScheduleReceiver
 */
public class ScheduleReceiver extends BroadcastReceiver
        implements SendComponentsCommand.OnComponentsResponse,
        SendComponentsGattCommand.OnBleComponentsResponse {

    private static final String TAG = ScheduleReceiver.class.getSimpleName();
    private static AlarmManager alarmManager = null;
    private static PendingIntent pendingIntent = null;
    private Context context;

    @Override
    public void onReceive(final Context context,
                          final Intent intent) {
        if (!intent.getAction().equals(Strings.CLOUD_SERVICE)) {
            return;
        }

        if (alarmManager == null
                || pendingIntent == null) {
            schedule(context, intent);
            return;
        }

        this.context = context;
        final Intent i = new Intent(context, CloudService.class);
        context.startService(i);

        for (DeviceWrapper device : DeviceListAdapter.getDataSetOriginal()) {
            if (device.getBleDevice() == null) {
                new SendComponentsCommand(
                        device,
                        this,
                        null).execute();
            } else {
                new SendComponentsGattCommand(context,
                        this,
                        device,
                        null).execute();
            }
        }
    }

    @Override
    public void onComponentsResponse(final Set<ComponentWrapper> components,
                                     final Set<StateWrapper> states,
                                     final TinyDevice device,
                                     boolean isSuccess) {
        if (!isSuccess
                || states == null) {
            Log.e(TAG, "Failure querying for state.");
        } else {
            Log.i(TAG, "Success getting states: " + states.size());

            final HashMap<String, String> hashMap = new HashMap<>(states.size());

            for (final StateWrapper state : states) {
                hashMap.put(state.getTraitName()
                                + "."
                                + state.getStateName(),
                        state.getValue().getValue());
            }

            final Intent i = new Intent(context, CloudService.class);
            i.putExtra(Strings.DEVICE_NAME, device.getName());
            i.putExtra(Strings.HASH_MAP, hashMap);
            i.putExtra(Strings.SHIELD, "WIFI");
            context.startService(i);
        }
    }

    @Override
    public void onBleComponentsResponse(final Set<ComponentWrapper> components,
                                        final Set<StateWrapper> states,
                                        final DeviceWrapper device,
                                        final boolean isSuccess) {
        if (!isSuccess
                || states == null) {
            Log.e(TAG, "Failure querying for state.");
        } else {
            Log.i(TAG, "Success getting states: " + states.size());

            final HashMap<String, String> hashMap = new HashMap<>(states.size());

            for (final StateWrapper state : states) {
                hashMap.put(state.getTraitName()
                                + "."
                                + state.getStateName(),
                        state.getValue().getValue());
            }

            final Intent i = new Intent(context, CloudService.class);
            i.putExtra(Strings.DEVICE_NAME, device.getDevice().getName());
            i.putExtra(Strings.HASH_MAP, hashMap);
            i.putExtra(Strings.SHIELD, "BLE");
            context.startService(i);
        }
    }

    /**
     * schedule
     *
     * @param context Context
     * @param intent  Intent
     */
    private void schedule(final Context context,
                          final Intent intent) {
        pendingIntent = PendingIntent.getBroadcast(context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                pendingIntent);
    }

    /*
     * cancel
     */
    /*public void cancel() {
        if (alarmManager != null
                && pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            alarmManager = null;
            pendingIntent = null;
        }
    }*/
}

