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

package com.moto.miletus.application.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.moto.miletus.application.R;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * MqttSettingsActivity
 */
public class MqttSettingsActivity extends AppCompatActivity {

    private static final String TAG = MqttSettingsActivity.class.getSimpleName();
    private TextView statusConn;
    private EditText mqttIp;
    private EditText mqttPort;
    private SharedPreferences sharedPreferences;
    private MqttAndroidClient mqttClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt);

        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(this));
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        final Button saveSettings = (Button) findViewById(R.id.save_settings);

        if (saveSettings != null) {
            saveSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    saveSettings();
                    testConnect();
                }
            });
        }

        initialize();
    }

    /**
     * initialize
     */
    private void initialize() {
        sharedPreferences = getSharedPreferences(Strings.MQTT_SETTINGS, Context.MODE_PRIVATE);

        mqttIp = (EditText) findViewById(R.id.mqtt_ip);
        mqttPort = (EditText) findViewById(R.id.mqtt_port);
        statusConn = (TextView) findViewById(R.id.status_conn);

        mqttIp.setText(sharedPreferences.getString(Strings.MQTT_IP, Strings.MQTT_DEFAULT_IP));
        mqttPort.setText(sharedPreferences.getString(Strings.MQTT_PORT, Strings.MQTT_DEFAULT_PORT));
    }

    /**
     * saveSettings
     */
    private void saveSettings() {
        String mqttIp = this.mqttIp.getText().toString();
        String mqttPort = this.mqttPort.getText().toString();

        if (TextUtils.isEmpty(mqttIp)) {
            this.mqttIp.setError(getString(R.string.field_error));
            return;
        }

        if (TextUtils.isEmpty(mqttPort) || !StringUtils.isNumeric(mqttPort)) {
            this.mqttPort.setError(getString(R.string.field_error));
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(Strings.MQTT_IP, mqttIp);
        editor.putString(Strings.MQTT_PORT, mqttPort);

        editor.apply();
    }

    /**
     * testConnect
     */
    private void testConnect() {
        statusConn.setText(R.string.conn_wait);

        mqttClient = new MqttAndroidClient(getApplicationContext(),
                Strings.TCP
                        + sharedPreferences.getString(Strings.MQTT_IP, Strings.MQTT_DEFAULT_IP)
                        + ":"
                        + sharedPreferences.getString(Strings.MQTT_PORT, Strings.MQTT_DEFAULT_PORT),
                MqttClient.generateClientId());

        try {
            mqttClient.connect(getOptions(), null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "onSuccess");

                    statusConn.setText(R.string.conn_ok);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    Log.e(TAG, "onFailure");

                    statusConn.setText(R.string.conn_nok);
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, e.toString());
            statusConn.setText(R.string.conn_nok);
        }
    }

    @Override
    protected void onDestroy() {
        mqttDisconnect(mqttClient);
        super.onDestroy();
    }

    /**
     * mqttDisconnect
     *
     * @param mqttClient MqttAndroidClient
     */
    public static void mqttDisconnect(final MqttAndroidClient mqttClient) {
        if (mqttClient != null) {
            mqttClient.unregisterResources();
            mqttClient.close();
        }
    }

    /**
     * getOptions
     *
     * @return MqttConnectOptions
     */
    public static MqttConnectOptions getOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        return options;
    }

}
