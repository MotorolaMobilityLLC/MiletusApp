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

package com.moto.miletus.wrappers;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * CommandParameterWrapper
 */
public final class CommandParameterWrapper implements Parcelable {

    private final String parameterName;
    private ParameterValue value;

    /**
     * CommandParameterWrapper
     *
     * @param parameterName String
     * @param value         ParameterValue
     */
    public CommandParameterWrapper(String parameterName,
                                   ParameterValue value) {
        this.parameterName = parameterName;
        this.value = value;
    }

    private CommandParameterWrapper(Parcel in) {
        parameterName = in.readString();
        value = in.readParcelable(ParameterValue.class.getClassLoader());
    }

    public static final Creator<CommandParameterWrapper> CREATOR = new Creator<CommandParameterWrapper>() {
        @Override
        public CommandParameterWrapper createFromParcel(Parcel in) {
            return new CommandParameterWrapper(in);
        }

        @Override
        public CommandParameterWrapper[] newArray(int size) {
            return new CommandParameterWrapper[size];
        }
    };

    /**
     * getParameterName
     *
     * @return String
     */
    public String getParameterName() {
        return parameterName;
    }

    /**
     * setValue
     *
     * @param value ParameterValue
     * @return CommandParameterWrapper
     */
    public CommandParameterWrapper setValue(ParameterValue value) {
        this.value = value;
        return this;
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
        dest.writeString(parameterName);
        dest.writeParcelable(value, flags);
    }
}
