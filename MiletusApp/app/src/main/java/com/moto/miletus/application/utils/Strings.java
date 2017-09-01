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

import com.moto.miletus.application.BuildConfig;

public final class Strings {

    private Strings() {
    }

    public static final String NEAR_DEVICE = "NearDevice";

    public static final String CLOUD_SERVICE = "CloudService";

    public static final String NEW_LINE = System.getProperty("line.separator");

    public static final String EXTRA_KEY_DEVICE = BuildConfig.APPLICATION_ID + ".device";

    public static final String EXTRA_KEY_DEVICE_COMPONENT = BuildConfig.APPLICATION_ID + ".device_component";

    public static final String LIGHT = "light";

    public static final String TEMPERATURE = "temperature";

    public static final String HUMIDITY = "humidity";

    public static final String CELSIUS = "°C";

    public static final String LUX = " lux";

    public static final String HASH_MAP = "HashMap";

    public static final String DEVICE_NAME = "DeviceName";

    public static final String SHIELD = "SHIELD";

    public static final String _60 = "6.0";

    public static final String STORED_DEVICES = "StoredDevices";

    public static final String MQTT_SETTINGS = "MqttSettings";

    public static final String MQTT_IP = "mqttIp";

    public static final String MQTT_PORT = "mqttPort";

    public static final String MQTT_DEFAULT_IP = "10.10.197.65";

    public static final String MQTT_DEFAULT_PORT = "1883";

    public static final String TCP = "tcp://";
}
