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

import com.moto.miletus.gson.ExecuteCommandHelper;
import com.moto.miletus.utils.Strings;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;

/**
 * SendMqttExecute
 */
public class SendMqttExecute extends SendMqttCommand {

    private static final String TAG = SendMqttExecute.class.getSimpleName();
    private final OnMqttExecuteResponse onMqttExecuteResponse;
    private final String command;
    private final String topic;

    public SendMqttExecute(final MqttAndroidClient mqttClient,
                           final String topic,
                           final String command,
                           final OnMqttExecuteResponse onMqttExecuteResponse) {
        super(mqttClient, Strings.COMMANDS_EXECUTE);
        this.command = command;
        this.topic = topic;
        this.onMqttExecuteResponse = onMqttExecuteResponse;
    }

    /**
     * OnMqttExecuteResponse
     */
    public interface OnMqttExecuteResponse {
        /**
         * onMqttExecuteResponse
         *
         * @param isSuccess boolean
         */
        void onMqttExecuteResponse(final boolean isSuccess);
    }

    /**
     * execute
     */
    @Override
    public void execute() {
        if (onMqttExecuteResponse == null) {
            return;
        }

        subscribe(topic + OUT + Strings.COMMANDS_EXECUTE);
    }

    /**
     * payload
     */
    @Override
    protected void payload() {
        try {
            publish(topic + IN + Strings.COMMANDS_EXECUTE, command);
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
        if (!isDeliveryComplete
                || Strings.COMMANDS_EXECUTE.equalsIgnoreCase(msg)) {
            return;
        }

        try {
            boolean isSuccess = ExecuteCommandHelper.isStateDone(msg);

            cancel(topic + OUT + Strings.COMMANDS_EXECUTE);

            onMqttExecuteResponse.onMqttExecuteResponse(isSuccess);
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
    }

    /**
     * error
     */
    @Override
    void error() {
        cancel(topic + OUT + Strings.COMMANDS_EXECUTE);
        onMqttExecuteResponse.onMqttExecuteResponse(false);
    }
}
