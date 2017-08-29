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

import android.bluetooth.BluetoothDevice;
import android.net.nsd.NsdServiceInfo;
import android.os.Parcel;
import android.os.Parcelable;

import com.moto.miletus.gson.info.TinyDevice;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * DeviceWrapper
 */
public final class DeviceWrapper implements Parcelable {

    public static final long KEEP_ALIVE_SECS = 60L;
    private final TinyDevice device;
    private final BluetoothDevice bleDevice;
    private final MqttInfoWrapper mqttInfo;
    private final NsdServiceInfo nsdServiceInfo;
    private final String ssidOrigin;
    private long date;

    /**
     * DeviceWrapper
     *
     * @param device     TinyDevice
     * @param bleDevice  BluetoothDevice
     * @param mqttInfo   MqttInfoWrapper
     * @param ssidOrigin String
     */
    public DeviceWrapper(final TinyDevice device,
                         final BluetoothDevice bleDevice,
                         final MqttInfoWrapper mqttInfo,
                         final NsdServiceInfo nsdServiceInfo,
                         final String ssidOrigin) {
        this.device = device;
        this.bleDevice = bleDevice;
        this.mqttInfo = mqttInfo;
        this.nsdServiceInfo = nsdServiceInfo;
        this.ssidOrigin = ssidOrigin;
        this.date = new Date().getTime();
    }

    private DeviceWrapper(Parcel in) {
        device = in.readParcelable(TinyDevice.class.getClassLoader());
        bleDevice = in.readParcelable(BluetoothDevice.class.getClassLoader());
        mqttInfo = in.readParcelable(MqttInfoWrapper.class.getClassLoader());
        nsdServiceInfo = in.readParcelable(NsdServiceInfo.class.getClassLoader());
        ssidOrigin = in.readString();
        date = in.readLong();
    }

    public static final Creator<DeviceWrapper> CREATOR = new Creator<DeviceWrapper>() {
        @Override
        public DeviceWrapper createFromParcel(Parcel in) {
            return new DeviceWrapper(in);
        }

        @Override
        public DeviceWrapper[] newArray(int size) {
            return new DeviceWrapper[size];
        }
    };

    /**
     * getDevice
     *
     * @return TinyDevice
     */
    public TinyDevice getDevice() {
        return device;
    }

    /**
     * getBleDevice
     *
     * @return BluetoothDevice
     */
    public BluetoothDevice getBleDevice() {
        return bleDevice;
    }

    /**
     * getMqttInfo
     *
     * @return MqttInfoWrapper
     */
    public MqttInfoWrapper getMqttInfo() {
        return mqttInfo;
    }

    /**
     * getNsdServiceInfo
     *
     * @return NsdServiceInfo
     */
    public NsdServiceInfo getNsdServiceInfo() {
        return nsdServiceInfo;
    }

    /**
     * getSsidOrigin
     *
     * @return String
     */
    public String getSsidOrigin() {
        return ssidOrigin;
    }

    /**
     * getCreateDate
     *
     * @return String
     */
    private long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    /**
     * getAliveSecs
     *
     * @return long
     */
    public long getAliveSecs() {
        return TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - getDate());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(device, flags);
        dest.writeParcelable(bleDevice, flags);
        dest.writeParcelable(mqttInfo, flags);
        dest.writeParcelable(nsdServiceInfo, flags);
        dest.writeString(ssidOrigin);
        dest.writeLong(date);
    }

}
