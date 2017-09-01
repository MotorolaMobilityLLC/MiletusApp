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

package com.moto.miletus.mqtt;

import android.util.Log;

import com.moto.miletus.gson.TraitsHelper;
import com.moto.miletus.gson.traits.Traits;
import com.moto.miletus.utils.Strings;
import com.moto.miletus.wrappers.ComponentWrapper;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.Set;

/**
 * SendMqttTraits
 */
public class SendMqttTraits extends SendMqttCommand {

    private static final String TAG = SendMqttTraits.class.getSimpleName();
    private final OnMqttTraitsResponse onMqttTraitsResponse;
    private final String topic;

    public SendMqttTraits(final MqttAndroidClient mqttClient,
                          final String topic,
                          final OnMqttTraitsResponse onMqttTraitsResponse) {
        super(mqttClient, Strings.TRAITS);
        this.topic = topic;
        this.onMqttTraitsResponse = onMqttTraitsResponse;
    }

    public interface OnMqttTraitsResponse {
        void onMqttTraitsResponse(final Set<ComponentWrapper> components,
                                  final boolean isSuccess);
    }

    /**
     * execute
     */
    @Override
    public void execute() {
        if (onMqttTraitsResponse == null) {
            return;
        }

        subscribe(topic + OUT + Strings.TRAITS);
    }

    /**
     * payload
     */
    @Override
    protected void payload() {
        try {
            publish(topic + OUT + Strings.TRAITS, Strings.TRAITS);
        } catch (UnsupportedEncodingException | MqttException e) {
            Log.e(TAG, e.toString());
            error();
        }
    }

    /**
     * messageArrived
     *
     * @param msg String
     */
    @Override
    protected void messageArrived(final String msg) {
        if (Strings.TRAITS.equalsIgnoreCase(msg)) {
            return;
        }

        try {
            Traits traits = TraitsHelper.jsonToTraits(msg);

            Set<ComponentWrapper> components = TraitsHelper.traitsToComponentCommands(traits);

            cancel(topic + OUT + Strings.TRAITS);

            onMqttTraitsResponse.onMqttTraitsResponse(components, true);
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
    }

    /**
     * error
     */
    @Override
    void error() {
        cancel(topic + OUT + Strings.TRAITS);
        onMqttTraitsResponse.onMqttTraitsResponse(null, false);
    }
}
