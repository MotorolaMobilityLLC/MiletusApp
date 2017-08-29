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

package com.moto.miletus.wrappers;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * MqttInfoWrapper
 */
public final class MqttInfoWrapper implements Parcelable {

    private final String mqttServerURI;
    private final String clientId;
    private final String topic;

    public MqttInfoWrapper(final String mqttServerURI,
                           final String clientId,
                           final String topic) {
        this.mqttServerURI = mqttServerURI;
        this.clientId = clientId;
        this.topic = topic;
    }

    private MqttInfoWrapper(Parcel in) {
        mqttServerURI = in.readString();
        clientId = in.readString();
        topic = in.readString();
    }

    public String getMqttServerURI() {
        return mqttServerURI;
    }

    public String getClientId() {
        return clientId;
    }

    public String getTopic() {
        return topic;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mqttServerURI);
        dest.writeString(clientId);
        dest.writeString(topic);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MqttInfoWrapper> CREATOR = new Creator<MqttInfoWrapper>() {
        @Override
        public MqttInfoWrapper createFromParcel(Parcel in) {
            return new MqttInfoWrapper(in);
        }

        @Override
        public MqttInfoWrapper[] newArray(int size) {
            return new MqttInfoWrapper[size];
        }
    };
}
