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

import java.util.ArrayList;
import java.util.List;

/**
 * ParameterValue
 */
public class ParameterValue implements Parcelable {

    public static final String STRING = "string";
    public static final String INTEGER = "integer";
    public static final String NUMBER = "number";
    public static final String BOOLEAN = "boolean";

    private String type;
    private String value;
    private String maximum;
    private String minimum;
    private List<String> values = new ArrayList<>();

    /**
     * ParameterValue
     *
     * @param type String
     */
    public ParameterValue(String type) {
        this.type = type;
    }

    /**
     * ParameterValue
     *
     * @param type  String
     * @param value String
     */
    public ParameterValue(String type, String value) {
        this.type = type;
        this.value = value;
    }

    private ParameterValue(Parcel in) {
        type = in.readString();
        value = in.readString();
        maximum = in.readString();
        minimum = in.readString();
        values = in.createStringArrayList();
    }

    public static final Creator<ParameterValue> CREATOR = new Creator<ParameterValue>() {
        @Override
        public ParameterValue createFromParcel(Parcel in) {
            return new ParameterValue(in);
        }

        @Override
        public ParameterValue[] newArray(int size) {
            return new ParameterValue[size];
        }
    };

    /**
     * getType
     *
     * @return String
     */
    public String getType() {
        return type;
    }

    /**
     * setType
     *
     * @param type String
     * @return ParameterValue
     */
    public ParameterValue setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * getValue
     *
     * @return String
     */
    public String getValue() {
        return value;
    }

    /**
     * setValue
     *
     * @param value String
     * @return ParameterValue
     */
    public ParameterValue setValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * getValues
     *
     * @return List
     */
    public List<String> getValues() {
        return values;
    }

    /**
     * setValues
     *
     * @param values List<String>
     * @return ParameterValue
     */
    public ParameterValue setValues(List<String> values) {
        this.values = values;
        return this;
    }

    /**
     * getMaximum
     *
     * @return String
     */
    public String getMaximum() {
        return maximum;
    }

    /**
     * setMaximum
     *
     * @param maximum String
     * @return ParameterValue
     */
    public ParameterValue setMaximum(String maximum) {
        this.maximum = maximum;
        return this;
    }

    /**
     * getMinimum
     *
     * @return String
     */
    public String getMinimum() {
        return minimum;
    }


    /**
     * setMinimum
     *
     * @param minimum String
     * @return ParameterValue
     */
    public ParameterValue setMinimum(String minimum) {
        this.minimum = minimum;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(value);
        dest.writeString(maximum);
        dest.writeString(minimum);
        dest.writeStringList(values);
    }
}
