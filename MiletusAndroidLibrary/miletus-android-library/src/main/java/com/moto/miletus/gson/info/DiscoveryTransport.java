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

package com.moto.miletus.gson.info;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * DiscoveryTransport
 */
public final class DiscoveryTransport implements Parcelable {

    @SerializedName("hasCloud")
    @Expose
    private boolean hasCloud;
    @SerializedName("hasWifi")
    @Expose
    private boolean hasWifi;
    @SerializedName("hasLan")
    @Expose
    private boolean hasLan;
    @SerializedName("hasBle")
    @Expose
    private boolean hasBle;
    @SerializedName("LanTransport")
    @Expose
    private LanTransport lanTransport;
    @SerializedName("CloudTransport")
    @Expose
    private CloudTransport cloudTransport;
    @SerializedName("WifiTransport")
    @Expose
    private WifiTransport wifiTransport;
    @SerializedName("BleTransport")
    @Expose
    private BleTransport bleTransport;

    private DiscoveryTransport(Parcel in) {
        hasCloud = in.readByte() != 0;
        hasWifi = in.readByte() != 0;
        hasLan = in.readByte() != 0;
        hasBle = in.readByte() != 0;
        lanTransport = in.readParcelable(LanTransport.class.getClassLoader());
        cloudTransport = in.readParcelable(CloudTransport.class.getClassLoader());
        wifiTransport = in.readParcelable(WifiTransport.class.getClassLoader());
        bleTransport = in.readParcelable(BleTransport.class.getClassLoader());
    }

    public static final Creator<DiscoveryTransport> CREATOR = new Creator<DiscoveryTransport>() {
        @Override
        public DiscoveryTransport createFromParcel(Parcel in) {
            return new DiscoveryTransport(in);
        }

        @Override
        public DiscoveryTransport[] newArray(int size) {
            return new DiscoveryTransport[size];
        }
    };

    /**
     * @return The hasCloud
     */
    public boolean isHasCloud() {
        return hasCloud;
    }

    /**
     * @param hasCloud The hasCloud
     */
    public void setHasCloud(boolean hasCloud) {
        this.hasCloud = hasCloud;
    }

    /**
     * @return The hasWifi
     */
    public boolean isHasWifi() {
        return hasWifi;
    }

    /**
     * @param hasWifi The hasWifi
     */
    public void setHasWifi(boolean hasWifi) {
        this.hasWifi = hasWifi;
    }

    /**
     * @return The hasLan
     */
    public boolean isHasLan() {
        return hasLan;
    }

    /**
     * @param hasLan The hasLan
     */
    public void setHasLan(boolean hasLan) {
        this.hasLan = hasLan;
    }

    /**
     * @return The hasBle
     */
    public boolean isHasBle() {
        return hasBle;
    }

    /**
     * @param hasBle The hasBle
     */
    public void setHasBle(boolean hasBle) {
        this.hasBle = hasBle;
    }

    /**
     * @return The lanTransport
     */
    public LanTransport getLanTransport() {
        return lanTransport;
    }

    /**
     * @param lanTransport The LanTransport
     */
    public void setLanTransport(LanTransport lanTransport) {
        this.lanTransport = lanTransport;
    }

    /**
     * @return The cloudTransport
     */
    public CloudTransport getCloudTransport() {
        return cloudTransport;
    }

    /**
     * @param cloudTransport The CloudTransport
     */
    public void setCloudTransport(CloudTransport cloudTransport) {
        this.cloudTransport = cloudTransport;
    }

    /**
     * @return The wifiTransport
     */
    public WifiTransport getWifiTransport() {
        return wifiTransport;
    }

    /**
     * @param wifiTransport The WifiTransport
     */
    public void setWifiTransport(WifiTransport wifiTransport) {
        this.wifiTransport = wifiTransport;
    }

    /**
     * @return The bleTransport
     */
    public BleTransport getBleTransport() {
        return bleTransport;
    }

    /**
     * @param bleTransport The BleTransport
     */
    public void setBleTransport(BleTransport bleTransport) {
        this.bleTransport = bleTransport;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (hasCloud ? 1 : 0));
        dest.writeByte((byte) (hasWifi ? 1 : 0));
        dest.writeByte((byte) (hasLan ? 1 : 0));
        dest.writeByte((byte) (hasBle ? 1 : 0));
        dest.writeParcelable(lanTransport, flags);
        dest.writeParcelable(cloudTransport, flags);
        dest.writeParcelable(wifiTransport, flags);
        dest.writeParcelable(bleTransport, flags);
    }
}
