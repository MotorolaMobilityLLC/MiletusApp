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

package com.moto.miletus.mdns;

import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import com.moto.miletus.gson.info.TinyDevice;
import com.moto.miletus.gson.InfoHelper;
import com.moto.miletus.utils.Strings;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import org.json.JSONException;

import java.io.IOException;

/**
 * SendInfoCommand
 */
@SuppressWarnings("deprecation")
public class SendInfoCommand extends SendCommand {

    private static final String TAG = SendInfoCommand.class.getSimpleName();
    private final NsdServiceInfo service;
    private final OnInfoResponse onInfoResponse;

    SendInfoCommand(final NsdServiceInfo service,
                    final OnInfoResponse onInfoResponse) {
        super();
        this.service = service;
        this.onInfoResponse = onInfoResponse;
    }

    @Override
    protected final Boolean doInBackground(Void... voids) {
        try {
            final HttpClient httpClient = new DefaultHttpClient(httpParams);
            final HttpGet httpGet = new HttpGet(Strings.HTTP
                    + service.getHost().getHostName()
                    + Strings.PORT
                    + service.getPort()
                    + Strings.INFO);
            final HttpResponse response = httpClient.execute(httpGet);

            int status = response.getStatusLine().getStatusCode();

            if (status == OK) {
                HttpEntity entity = response.getEntity();
                String data = EntityUtils.toString(entity);
                HttpHelper.consume(entity);

                TinyDevice tinyDevice = InfoHelper.jsonToTinyDevice(data);

                onInfoResponse.onInfoResponse(tinyDevice, service);

                cancel(true);
                return true;
            }
        } catch (IOException | IllegalArgumentException | NullPointerException | JSONException e) {
            Log.e(TAG, e.toString());
        }

        cancel(true);
        return false;
    }

    /**
     * OnInfoResponse
     */
    public interface OnInfoResponse {
        /**
         * onInfoResponse
         *
         * @param device  TinyDevice
         * @param service NsdServiceInfo
         */
        void onInfoResponse(final TinyDevice device,
                            final NsdServiceInfo service);
    }

}
