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

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;

import com.moto.miletus.application.utils.Strings;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CloudService
 */
@SuppressWarnings("deprecation")
public final class CloudService
        extends IntentService
        implements SensorEventListener {

    private static final String TAG = CloudService.class.getSimpleName();
    private static final String HOST = "http://inner-exchange-109114.appspot.com/add";
    private static final String CHANNEL_ID = "channel_id";
    private static final String CHANNEL = "5558569239838720";
    private static final String FIELD_1 = "field1";
    private static final String FIELD_2 = "field2";
    private static final String FIELD_3 = "field3";
    private static final String FIELD_4 = "field4";
    private static final String FIELD_5 = "field5";
    private static final String FIELD_6 = "field6";
    private static final String FIELD_7 = "field7";
    private static final String DELIMITER = ",";
    private static final String NATIVE = "NATIVE";
    private static SensorManager mSensorManager;
    private static Sensor sensorTemperature;
    private static Sensor sensorLight;
    private static Sensor sensorHumidity;
    private static float lastTemperature;
    private static float lastLight;
    private static float lastHumidity;

    public CloudService() {
        super(TAG);
        setIntentRedelivery(true);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (mSensorManager == null) {
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sensorTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            sensorLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            sensorHumidity = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        }
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        if (sensorTemperature != null) {
            mSensorManager.registerListener(this, sensorTemperature, SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (sensorLight != null) {
            mSensorManager.registerListener(this, sensorLight, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (sensorHumidity != null) {
            mSensorManager.registerListener(this, sensorHumidity, SensorManager.SENSOR_DELAY_NORMAL);
        }
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
            mSensorManager = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null
                || !intent.hasExtra(Strings.DEVICE_NAME)
                || !intent.hasExtra(Strings.HASH_MAP)
                || !intent.hasExtra(Strings.SHIELD)) {
            sendData(getNameValuePairList(null));
        } else {
            sendData(getNameValuePairList(intent));
        }
    }

    /**
     * getNameValuePairList
     *
     * @param intent Intent
     * @return List<NameValuePair>
     */
    @SuppressWarnings("unchecked")
    private List<NameValuePair> getNameValuePairList(@Nullable Intent intent) {
        final List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair(CHANNEL_ID, CHANNEL));

        HashMap<String, String> hashMap;
        if (intent == null) {
            hashMap = new HashMap<>();

            if (sensorTemperature != null) {
                hashMap.put(Strings.TEMPERATURE, lastTemperature + "");
            }
            if (sensorLight != null) {
                hashMap.put(Strings.LIGHT, lastLight + "");
            }
            if (sensorHumidity != null) {
                hashMap.put(Strings.HUMIDITY, lastHumidity + "");
            }

            nameValuePairs.add(new BasicNameValuePair(FIELD_1, Build.MANUFACTURER + " " + Build.MODEL));
            nameValuePairs.add(new BasicNameValuePair(FIELD_7, NATIVE));
        } else {
            Serializable serializable = intent.getSerializableExtra(Strings.HASH_MAP);
            hashMap = (HashMap<String, String>) serializable;

            nameValuePairs.add(new BasicNameValuePair(FIELD_1, intent.getStringExtra(Strings.DEVICE_NAME)));
            nameValuePairs.add(new BasicNameValuePair(FIELD_7, intent.getStringExtra(Strings.SHIELD)));
        }

        final StringBuilder buf2 = new StringBuilder();
        final StringBuilder buf3 = new StringBuilder();

        int i = 1;
        for (final Map.Entry<String, String> entry : hashMap.entrySet()) {
            buf2.append(entry.getKey());
            buf3.append(entry.getValue());

            if (i < hashMap.size()) {
                buf2.append(DELIMITER);
                buf3.append(DELIMITER);
            }

            i++;
        }

        String field2 = buf2.toString();
        String field3 = buf3.toString();

        nameValuePairs.add(new BasicNameValuePair(FIELD_2, field2));
        nameValuePairs.add(new BasicNameValuePair(FIELD_3, field3));
        nameValuePairs.add(new BasicNameValuePair(FIELD_4, hashMap.size() + ""));
        nameValuePairs.add(new BasicNameValuePair(FIELD_5, Build.MANUFACTURER));
        nameValuePairs.add(new BasicNameValuePair(FIELD_6, Build.MODEL));

        Log.i(TAG, nameValuePairs.toString());

        return nameValuePairs;
    }

    /**
     * sendData
     *
     * @param nameValuePairList List<NameValuePair>
     */
    private void sendData(final List<NameValuePair> nameValuePairList) {
        try {
            HttpPost httpPost = new HttpPost(HOST);
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairList));

            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(httpPost);

            int status = response.getStatusLine().getStatusCode();

            if (status == 200) {
                HttpEntity entity = response.getEntity();
                String data = EntityUtils.toString(entity);
                consume(entity);
                Log.i(TAG, data);
            } else {
                Log.e(TAG, response.getStatusLine().toString());
            }
        } catch (IOException | IllegalArgumentException e) {
            Log.e(TAG, e.toString());
        }
    }

    /**
     * consume
     *
     * @param entity HttpEntity
     * @throws IOException IOException
     */
    private void consume(final HttpEntity entity) throws IOException {
        if (entity == null) {
            return;
        }

        if (entity.isStreaming()) {
            final InputStream inputStream = entity.getContent();
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                lastTemperature = event.values[0];
                break;
            case Sensor.TYPE_LIGHT:
                lastLight = event.values[0];
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                lastHumidity = event.values[0];
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
