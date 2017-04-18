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
 * StateWrapper
 */
public class StateWrapper implements Parcelable {

    private final String traitName;
    private final String stateName;
    private final ParameterValue value;

    /**
     * StateWrapper
     *
     * @param traitName String
     * @param stateName String
     * @param value     String
     */
    public StateWrapper(String traitName,
                        String stateName,
                        ParameterValue value) {
        this.traitName = traitName;
        this.stateName = stateName;
        this.value = value;
    }

    private StateWrapper(Parcel in) {
        traitName = in.readString();
        stateName = in.readString();
        value = in.readParcelable(ParameterValue.class.getClassLoader());
    }

    public static final Creator<StateWrapper> CREATOR = new Creator<StateWrapper>() {
        @Override
        public StateWrapper createFromParcel(Parcel in) {
            return new StateWrapper(in);
        }

        @Override
        public StateWrapper[] newArray(int size) {
            return new StateWrapper[size];
        }
    };

    /**
     * getTraitName
     *
     * @return String
     */
    public String getTraitName() {
        return traitName;
    }

    /**
     * getStateName
     *
     * @return String
     */
    public String getStateName() {
        return stateName;
    }

    /**
     * getValue
     *
     * @return ParameterValue
     */
    public ParameterValue getValue() {
        return value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(traitName);
        dest.writeString(stateName);
        dest.writeParcelable(value, flags);
    }
}
