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

import android.util.Log;

import com.moto.miletus.gson.ExecuteCommandHelper;
import com.moto.miletus.utils.Strings;
import com.moto.miletus.wrappers.DeviceWrapper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;

import java.io.IOException;

@SuppressWarnings("deprecation")
public class SendExecuteCommand extends SendCommand {

    private static final String TAG = SendExecuteCommand.class.getSimpleName();
    private static final String CONTENT_TYPE = "application/json";
    private final DeviceWrapper device;
    private final OnExecuteCommandResponse onExecuteCommandResponse;
    private final String command;

    public SendExecuteCommand(final DeviceWrapper device,
                              final OnExecuteCommandResponse onExecuteCommandResponse,
                              final String command) {
        super();
        this.device = device;
        this.onExecuteCommandResponse = onExecuteCommandResponse;
        this.command = command;
    }

    @Override
    protected final Boolean doInBackground(Void... voids) {
        boolean isSuccess = false;

        try {
            final HttpPost httpPost = new HttpPost(Strings.HTTP
                    + device.getNsdServiceInfo().getHost().getHostName()
                    + Strings.PORT
                    + device.getNsdServiceInfo().getPort()
                    + Strings.COMMANDS_EXECUTE);
            final StringEntity command = new StringEntity(this.command,
                    HTTP.UTF_8);
            command.setContentType(CONTENT_TYPE);
            httpPost.setEntity(command);

            final HttpClient httpClient = new DefaultHttpClient(httpParams);
            final HttpResponse response = httpClient.execute(httpPost);

            int status = response.getStatusLine().getStatusCode();

            if (status == OK) {
                HttpEntity entity = response.getEntity();
                String state = EntityUtils.toString(entity);
                HttpHelper.consume(entity);

                isSuccess = ExecuteCommandHelper.isStateDone(state);

                onExecuteCommandResponse.onExecuteCommandResponse(isSuccess);
            } else {
                onExecuteCommandResponse.onExecuteCommandResponse(false);
            }
        } catch (IOException | IllegalArgumentException | NullPointerException | JSONException e) {
            Log.e(TAG, e.toString());
            onExecuteCommandResponse.onExecuteCommandResponse(false);
        } finally {
            cancel(true);
        }

        return isSuccess;
    }

    /**
     * OnExecuteCommandResponse
     */
    public interface OnExecuteCommandResponse {
        /**
         * onExecuteCommandResponse
         *
         * @param isSuccess boolean
         */
        void onExecuteCommandResponse(final boolean isSuccess);
    }

}
