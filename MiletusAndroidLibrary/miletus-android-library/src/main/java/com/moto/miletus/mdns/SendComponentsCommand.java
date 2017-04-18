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

import android.os.AsyncTask;
import android.util.Log;

import com.moto.miletus.gson.ComponentsHelper;
import com.moto.miletus.gson.info.TinyDevice;
import com.moto.miletus.utils.Strings;
import com.moto.miletus.wrappers.ComponentWrapper;
import com.moto.miletus.wrappers.StateWrapper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;

import java.io.IOException;
import java.util.Set;

/**
 * SendComponentsCommand
 */
@SuppressWarnings("deprecation")
public class SendComponentsCommand extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = SendComponentsCommand.class.getSimpleName();
    private final TinyDevice device;
    private final OnComponentsResponse onComponentsResponse;
    private final Set<ComponentWrapper> components;

    public SendComponentsCommand(final TinyDevice device,
                                 final OnComponentsResponse onComponentsResponse,
                                 final Set<ComponentWrapper> components) {
        this.device = device;
        this.onComponentsResponse = onComponentsResponse;
        this.components = components;
    }

    public interface OnComponentsResponse {
        void onComponentsResponse(final Set<ComponentWrapper> components,
                                  final Set<StateWrapper> states,
                                  final TinyDevice device,
                                  final boolean isSuccess);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        boolean isSuccess = false;

        try {
            HttpGet httpGet = new HttpGet(Strings.HTTP + device.getDiscoveryTransport().getLanTransport().getHttpAddress() + Strings.COMPONENTS);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(httpGet);

            int status = response.getStatusLine().getStatusCode();

            if (status == 200) {
                HttpEntity entity = response.getEntity();
                String states = EntityUtils.toString(entity);
                HttpHelper.consume(entity);

                Set<StateWrapper> stateWrappers = ComponentsHelper.addComponentStates(states, this.components);

                onComponentsResponse.onComponentsResponse(components, stateWrappers, device, true);

                isSuccess = true;
            }
        } catch (IOException | IllegalArgumentException | JSONException e) {
            Log.e(TAG, e.toString());

            onComponentsResponse.onComponentsResponse(null, null, device, false);
        } finally {
            cancel(true);
        }

        return isSuccess;
    }
}
