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

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * SendMqttCommand
 */
abstract class SendMqttCommand implements MqttCallback, IMqttActionListener {

    private static final String TAG = SendMqttCommand.class.getSimpleName();
    private final String command;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture result;
    private final MqttAndroidClient mqttClient;
    private boolean isSubscribed = false;
    boolean isDeliveryComplete = false;

    private static final int TIME_OUT = 10;
    private static final int QOS = 2;
    static final String IN = "/in";
    static final String OUT = "/out";

    SendMqttCommand(final MqttAndroidClient mqttClient,
                    final String command) {
        this.mqttClient = mqttClient;
        this.command = command;
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.e(TAG, "connectionLost: " + command);
        error();
    }

    @Override
    public void messageArrived(final String topic,
                               final MqttMessage msg) {
        Log.i(TAG, "messageArrived: " + msg.toString());
        messageArrived(msg.toString());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.i(TAG, "deliveryComplete: " + command);
        isDeliveryComplete = true;
    }

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        Log.i(TAG, "Successfully subscribed to topic: " + command);
        isSubscribed = true;
        payload();
    }

    @Override
    public void onFailure(IMqttToken token,
                          Throwable cause) {
        Log.e(TAG, "Failure subscribed to topic: " + command);
        isSubscribed = false;
        error();
    }

    public abstract void execute();

    abstract void messageArrived(final String topic);

    abstract void error();

    abstract void payload();

    /**
     * subscribe
     *
     * @param topic String
     */
    void subscribe(final String topic) {
        if (mqttClient == null
                || topic == null
                || !mqttClient.isConnected()) {
            error();
            return;
        }

        result = scheduler
                .schedule(new Runnable() {
                    public void run() {
                        Log.e(TAG, "TIME_OUT: " + TIME_OUT);
                        error();
                    }
                }, TIME_OUT, TimeUnit.SECONDS);

        try {
            mqttClient.subscribe(topic, QOS, null, this);
        } catch (MqttException e) {
            Log.e(TAG, e.toString());
            error();
        }

        mqttClient.setCallback(this);
    }

    /**
     * topic
     *
     * @param topic String
     */
    void cancel(final String topic) {
        if (result != null) {
            result.cancel(true);
        }

        if (isSubscribed
                && topic != null) {
            try {
                mqttClient.unsubscribe(topic);
            } catch (MqttException e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    /**
     * publish
     *
     * @param topic   String
     * @param payload String
     * @throws UnsupportedEncodingException e
     * @throws MqttException                e
     */
    void publish(final String topic,
                 final String payload) throws UnsupportedEncodingException, MqttException {
        mqttClient.publish(topic,
                payload.getBytes("UTF-8"),
                QOS,
                false);
    }

}
