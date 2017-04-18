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

public final class TinyDevice implements Parcelable {

    @SerializedName("AccountId")
    @Expose
    private String accountId;
    @SerializedName("Id")
    @Expose
    private String id;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("Description")
    @Expose
    private String description;
    @SerializedName("ModelManifestId")
    @Expose
    private String modelManifestId;
    @SerializedName("UiDeviceKind")
    @Expose
    private String uiDeviceKind;
    @SerializedName("Location")
    @Expose
    private String location;
    @SerializedName("LocalId")
    @Expose
    private String localId;
    @SerializedName("DiscoveryTransport")
    @Expose
    private DiscoveryTransport discoveryTransport;

    private TinyDevice(Parcel in) {
        accountId = in.readString();
        id = in.readString();
        name = in.readString();
        description = in.readString();
        modelManifestId = in.readString();
        uiDeviceKind = in.readString();
        location = in.readString();
        localId = in.readString();
        discoveryTransport = in.readParcelable(DiscoveryTransport.class.getClassLoader());
    }

    public static final Creator<TinyDevice> CREATOR = new Creator<TinyDevice>() {
        @Override
        public TinyDevice createFromParcel(Parcel in) {
            return new TinyDevice(in);
        }

        @Override
        public TinyDevice[] newArray(int size) {
            return new TinyDevice[size];
        }
    };

    /**
     * 
     * @return
     *     The accountId
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * 
     * @param accountId
     *     The AccountId
     */
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    /**
     * 
     * @return
     *     The id
     */
    public String getId() {
        return id;
    }

    /**
     * 
     * @param id
     *     The Id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 
     * @return
     *     The name
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * @param name
     *     The Name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * @return
     *     The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * 
     * @param description
     *     The Description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 
     * @return
     *     The modelManifestId
     */
    public String getModelManifestId() {
        return modelManifestId;
    }

    /**
     * 
     * @param modelManifestId
     *     The ModelManifestId
     */
    public void setModelManifestId(String modelManifestId) {
        this.modelManifestId = modelManifestId;
    }

    /**
     * 
     * @return
     *     The uiDeviceKind
     */
    public String getUiDeviceKind() {
        return uiDeviceKind;
    }

    /**
     * 
     * @param uiDeviceKind
     *     The UiDeviceKind
     */
    public void setUiDeviceKind(String uiDeviceKind) {
        this.uiDeviceKind = uiDeviceKind;
    }

    /**
     * 
     * @return
     *     The location
     */
    public String getLocation() {
        return location;
    }

    /**
     * 
     * @param location
     *     The Location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * 
     * @return
     *     The localId
     */
    public String getLocalId() {
        return localId;
    }

    /**
     * 
     * @param localId
     *     The LocalId
     */
    public void setLocalId(String localId) {
        this.localId = localId;
    }

    /**
     * 
     * @return
     *     The discoveryTransport
     */
    public DiscoveryTransport getDiscoveryTransport() {
        return discoveryTransport;
    }

    /**
     * 
     * @param discoveryTransport
     *     The DiscoveryTransport
     */
    public void setDiscoveryTransport(DiscoveryTransport discoveryTransport) {
        this.discoveryTransport = discoveryTransport;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(accountId);
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(modelManifestId);
        dest.writeString(uiDeviceKind);
        dest.writeString(location);
        dest.writeString(localId);
        dest.writeParcelable(discoveryTransport, flags);
    }
}
