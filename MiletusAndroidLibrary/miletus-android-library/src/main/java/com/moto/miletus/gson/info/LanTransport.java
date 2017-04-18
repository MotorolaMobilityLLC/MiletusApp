/*************************************************************************
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
 *************************************************************************/

package com.moto.miletus.gson.info;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * LanTransport
 */
public final class LanTransport implements Parcelable {

    @SerializedName("ConnectionStatus")
    @Expose
    private String connectionStatus;
    @SerializedName("HttpAddress")
    @Expose
    private String httpAddress;
    @SerializedName("HttpInfoPort")
    @Expose
    private long httpInfoPort;
    @SerializedName("HttpPort")
    @Expose
    private long httpPort;
    @SerializedName("HttpUpdatesPort")
    @Expose
    private long httpUpdatesPort;
    @SerializedName("HttpsPort")
    @Expose
    private long httpsPort;
    @SerializedName("HttpsUpdatesPort")
    @Expose
    private long httpsUpdatesPort;

    private LanTransport(Parcel in) {
        connectionStatus = in.readString();
        httpAddress = in.readString();
        httpInfoPort = in.readLong();
        httpPort = in.readLong();
        httpUpdatesPort = in.readLong();
        httpsPort = in.readLong();
        httpsUpdatesPort = in.readLong();
    }

    public static final Creator<LanTransport> CREATOR = new Creator<LanTransport>() {
        @Override
        public LanTransport createFromParcel(Parcel in) {
            return new LanTransport(in);
        }

        @Override
        public LanTransport[] newArray(int size) {
            return new LanTransport[size];
        }
    };

    /**
     * @return The connectionStatus
     */
    public String getConnectionStatus() {
        return connectionStatus;
    }

    /**
     * @param connectionStatus The ConnectionStatus
     */
    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    /**
     * @return The httpAddress
     */
    public String getHttpAddress() {
        return httpAddress;
    }

    /**
     * @param httpAddress The HttpAddress
     */
    public void setHttpAddress(String httpAddress) {
        this.httpAddress = httpAddress;
    }

    /**
     * @return The httpInfoPort
     */
    public long getHttpInfoPort() {
        return httpInfoPort;
    }

    /**
     * @param httpInfoPort The HttpInfoPort
     */
    public void setHttpInfoPort(long httpInfoPort) {
        this.httpInfoPort = httpInfoPort;
    }

    /**
     * @return The httpPort
     */
    public long getHttpPort() {
        return httpPort;
    }

    /**
     * @param httpPort The HttpPort
     */
    public void setHttpPort(long httpPort) {
        this.httpPort = httpPort;
    }

    /**
     * @return The httpUpdatesPort
     */
    public long getHttpUpdatesPort() {
        return httpUpdatesPort;
    }

    /**
     * @param httpUpdatesPort The HttpUpdatesPort
     */
    public void setHttpUpdatesPort(long httpUpdatesPort) {
        this.httpUpdatesPort = httpUpdatesPort;
    }

    /**
     * @return The httpsPort
     */
    public long getHttpsPort() {
        return httpsPort;
    }

    /**
     * @param httpsPort The HttpsPort
     */
    public void setHttpsPort(long httpsPort) {
        this.httpsPort = httpsPort;
    }

    /**
     * @return The httpsUpdatesPort
     */
    public long getHttpsUpdatesPort() {
        return httpsUpdatesPort;
    }

    /**
     * @param httpsUpdatesPort The HttpsUpdatesPort
     */
    public void setHttpsUpdatesPort(long httpsUpdatesPort) {
        this.httpsUpdatesPort = httpsUpdatesPort;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(connectionStatus);
        dest.writeString(httpAddress);
        dest.writeLong(httpInfoPort);
        dest.writeLong(httpPort);
        dest.writeLong(httpUpdatesPort);
        dest.writeLong(httpsPort);
        dest.writeLong(httpsUpdatesPort);
    }
}
