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

package com.moto.miletus.mdns;

import android.os.AsyncTask;
import android.util.Log;

import com.moto.miletus.gson.ExecuteCommandHelper;
import com.moto.miletus.gson.info.TinyDevice;
import com.moto.miletus.utils.Strings;

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
public class SendExecuteCommand extends AsyncTask<String, Void, Boolean> {

    private static final String TAG = SendExecuteCommand.class.getSimpleName();
    private static final String CONTENT_TYPE = "application/json";
    private final TinyDevice device;
    private final OnExecuteCommandResponse onExecuteCommandResponse;
    private final String command;

    public SendExecuteCommand(final TinyDevice device,
                              final OnExecuteCommandResponse onExecuteCommandResponse,
                              final String command) {
        this.device = device;
        this.onExecuteCommandResponse = onExecuteCommandResponse;
        this.command = command;
    }

    @Override
    protected final Boolean doInBackground(String... strings) {
        boolean isSuccess = false;

        try {
            HttpPost httpPost = new HttpPost(Strings.HTTP
                    + device.getDiscoveryTransport().getLanTransport().getHttpAddress()
                    + Strings.COMMANDS_EXECUTE);
            StringEntity command = new StringEntity(this.command,
                    HTTP.UTF_8);
            command.setContentType(CONTENT_TYPE);
            httpPost.setEntity(command);

            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(httpPost);
            int status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                HttpEntity entity = response.getEntity();
                String state = EntityUtils.toString(entity);
                HttpHelper.consume(entity);

                isSuccess = ExecuteCommandHelper.isStateDone(state);

                onExecuteCommandResponse.onExecuteCommandResponse(isSuccess);
            } else {
                onExecuteCommandResponse.onExecuteCommandResponse(false);
            }
        } catch (IOException | IllegalArgumentException | JSONException e) {
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
