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

import com.moto.miletus.gson.InfoHelper;
import com.moto.miletus.gson.info.TinyDevice;
import com.moto.miletus.utils.Strings;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;

/**
 * SendMqttInfo
 */
public class SendMqttInfo extends SendMqttCommand {

    private static final String TAG = SendMqttInfo.class.getSimpleName();
    private final String topic;
    private final OnMqttInfoResponse onMqttInfoResponse;

    public SendMqttInfo(final MqttAndroidClient mqttClient,
                        final String topic,
                        final OnMqttInfoResponse onMqttInfoResponse) {
        super(mqttClient, Strings.INFO);
        this.topic = topic;
        this.onMqttInfoResponse = onMqttInfoResponse;
    }

    /**
     * execute
     */
    @Override
    public void execute() {
        if (onMqttInfoResponse == null) {
            return;
        }

        subscribe(topic + OUT + Strings.INFO);
    }

    /**
     * payload
     */
    @Override
    protected void payload() {
        try {
            publish(topic + OUT + Strings.INFO, Strings.INFO);
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
        if (Strings.INFO.equalsIgnoreCase(msg)) {
            return;
        }

        try {
            TinyDevice tinyDevice = InfoHelper.jsonToTinyDevice(msg);

            cancel(topic + OUT + Strings.INFO);

            onMqttInfoResponse.onSuccess(tinyDevice, topic);
        } catch (JSONException | IllegalArgumentException e) {
            Log.e(TAG, e.toString());
        }
    }

    /**
     * error
     */
    @Override
    void error() {
        cancel(topic + OUT + Strings.INFO);
        onMqttInfoResponse.onFail(topic);
    }

    /**
     * OnMqttInfoResponse
     */
    public interface OnMqttInfoResponse {
        void onSuccess(final TinyDevice device,
                       final String topic);

        void onFail(final String topic);
    }
}