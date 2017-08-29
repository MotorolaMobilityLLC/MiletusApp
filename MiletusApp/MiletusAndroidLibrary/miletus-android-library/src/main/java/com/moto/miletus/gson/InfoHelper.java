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

package com.moto.miletus.gson;

import android.util.Log;

import com.google.gson.Gson;
import com.moto.miletus.gson.info.TinyDevice;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * InfoHelper
 */
public final class InfoHelper {

    private static final String TAG = InfoHelper.class.getSimpleName();

    private InfoHelper() {
    }

    /**
     * Json 2 TinyDevice
     *
     * @param json String
     * @return TinyDevice
     */
    public static TinyDevice jsonToTinyDevice(final String json) throws JSONException, IllegalArgumentException {
        Log.i(TAG, json);

        new JSONObject(json);

        return new Gson().fromJson(json, TinyDevice.class);
    }
}
