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

package com.moto.miletus.application.tabs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.moto.miletus.application.R;
import com.moto.miletus.application.utils.MqttSettingsActivity;
import com.moto.miletus.application.utils.Strings;
import com.moto.miletus.gson.info.TinyDevice;
import com.moto.miletus.mqtt.SendMqttComponents;
import com.moto.miletus.mqtt.SendMqttTraits;
import com.moto.miletus.wrappers.DeviceProvider;
import com.moto.miletus.ble.commands.SendComponentsGattCommand;
import com.moto.miletus.ble.commands.SendTraitsGattCommand;
import com.moto.miletus.mdns.SendComponentsCommand;
import com.moto.miletus.mdns.SendTraitsCommand;
import com.moto.miletus.application.utils.CustomExceptionHandler;
import com.moto.miletus.wrappers.DeviceWrapper;
import com.moto.miletus.wrappers.ComponentWrapper;
import com.moto.miletus.wrappers.StateWrapper;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Set;

/**
 * Handles the RecyclerView
 */
public class ComponentsFragment extends Fragment
        implements SendTraitsCommand.OnTraitsResponse,
        SendComponentsCommand.OnComponentsResponse,
        SendTraitsGattCommand.OnBleTraitsResponse,
        SendComponentsGattCommand.OnBleComponentsResponse,
        SendMqttTraits.OnMqttTraitsResponse,
        SendMqttComponents.OnMqttComponentsResponse {
    private static final String TAG = ComponentsFragment.class.getSimpleName();

    private ComponentsAdapter componentsAdapter;
    private RelativeLayout progressBarLayout;
    private ProgressBar progressBar;
    private DeviceWrapper mDevice;
    private MqttAndroidClient mqttClient;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        final View commandsLayout = inflater.inflate(R.layout.fragment_components, container, false);

        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(this.getContext()));
        }

        RecyclerView recyclerViewCommands = (RecyclerView) commandsLayout.findViewById(R.id.componentsList);
        progressBarLayout = (RelativeLayout) commandsLayout.findViewById(R.id.progressBarLayoutComponents);
        progressBar = (ProgressBar) commandsLayout.findViewById(R.id.progressBarComponents);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerViewCommands.setHasFixedSize(true);

        // use a linear layout manager
        recyclerViewCommands.setLayoutManager(new LinearLayoutManager(getActivity()));

        retrieveState(savedInstanceState != null ? savedInstanceState : getActivity().getIntent().getExtras());

        // specify an adapter (see also next example)
        componentsAdapter = new ComponentsAdapter(mDevice);
        recyclerViewCommands.setAdapter(componentsAdapter);
        recyclerViewCommands.setHasFixedSize(true);

        return commandsLayout;
    }

    /**
     * retrieveState
     *
     * @param state Bundle
     */
    private void retrieveState(@NonNull final Bundle state) {
        mDevice = state.getParcelable(Strings.EXTRA_KEY_DEVICE);
        if (mDevice == null) {
            mDevice = ((DeviceProvider) getActivity()).getDevice();
            if (mDevice == null) {
                throw new IllegalArgumentException("No Device set in intent extra " +
                        Strings.EXTRA_KEY_DEVICE);
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        retrieveState(savedInstanceState != null ? savedInstanceState : getActivity().getIntent().getExtras());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(Strings.EXTRA_KEY_DEVICE, mDevice);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getTraits();
    }

    @Override
    public void onPause() {
        super.onPause();
        MqttSettingsActivity.mqttDisconnect(mqttClient);
    }

    /**
     * sendMqttTraits
     */
    private void sendMqttTraits() {
        if (mqttClient != null
                && mqttClient.isConnected()) {
            new SendMqttTraits(mqttClient,
                    mDevice.getMqttInfo().getTopic(),
                    ComponentsFragment.this).execute();
            return;
        } else {
            setMqttClient();
        }

        try {
            mqttClient.connect(MqttSettingsActivity.getOptions(), null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "onSuccess");
                    new SendMqttTraits(mqttClient,
                            mDevice.getMqttInfo().getTopic(),
                            ComponentsFragment.this).execute();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    Log.e(TAG, "onFailure");
                    mqttClient = null;
                    showSnackbar(R.string.conn_fail);
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, e.toString());
            showSnackbar(R.string.conn_fail);
        }
    }

    /**
     * setMqttClient
     */
    private void setMqttClient() {
        if (mqttClient == null) {
            mqttClient = new MqttAndroidClient(this.getContext(),
                    mDevice.getMqttInfo().getMqttServerURI(),
                    mDevice.getMqttInfo().getClientId());
        }
    }

    /**
     * sendMqttComponents
     *
     * @param components Set<ComponentWrapper>
     */
    private void sendMqttComponents(final Set<ComponentWrapper> components) {
        if (mqttClient != null
                && mqttClient.isConnected()) {
            new SendMqttComponents(mqttClient,
                    mDevice.getMqttInfo().getTopic(),
                    components,
                    ComponentsFragment.this).execute();
            return;
        } else {
            setMqttClient();
        }

        try {
            mqttClient.connect(MqttSettingsActivity.getOptions(), null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "onSuccess");
                    new SendMqttComponents(mqttClient,
                            mDevice.getMqttInfo().getTopic(),
                            components,
                            ComponentsFragment.this).execute();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    Log.e(TAG, "onFailure");
                    mqttClient = null;
                    showSnackbar(R.string.conn_fail);
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, e.toString());
            showSnackbar(R.string.conn_error);
        }
    }

    /**
     * showSnackbar
     *
     * @param id int
     */
    private void showSnackbar(int id) {
        if (ComponentsFragment.this.getView() != null) {
            Snackbar.make(ComponentsFragment.this.getView(),
                    id,
                    Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    /**
     * Call getTraitDefinitions API
     */
    public void getTraits() {
        if (mDevice.getMqttInfo() != null) {
            sendMqttTraits();
        } else if (mDevice.getBleDevice() != null) {
            new SendTraitsGattCommand(this.getContext(),
                    this,
                    mDevice.getBleDevice()).execute();
        } else {
            new SendTraitsCommand(mDevice,
                    this).execute();
        }
    }

    @Override
    public void onMqttTraitsResponse(final Set<ComponentWrapper> components,
                                     boolean isSuccess) {
        if (!isSuccess) {
            Log.e(TAG, "Failure getting TraitDefinitions by MQTT.");
            showSnackbar(R.string.error_getting_traits);
        } else {
            Log.i(TAG, "Success getting TraitDefinitions by MQTT: " + components.size());
            getStates(components);
        }
    }

    @Override
    public void onBleTraitsResponse(final Set<ComponentWrapper> components,
                                    final boolean isSuccess) {
        if (!isSuccess) {
            Log.e(TAG, "Failure getting TraitDefinitions by BLE.");
            showSnackbar(R.string.error_getting_traits);
            new SendTraitsGattCommand(this.getContext(),
                    this,
                    mDevice.getBleDevice()).execute();
        } else {
            Log.i(TAG, "Success getting TraitDefinitions by BLE: " + components.size());
            getStates(components);
        }
    }

    @Override
    public void onTraitsResponse(final Set<ComponentWrapper> components,
                                 final boolean isSuccess) {
        if (!isSuccess) {
            Log.e(TAG, "Failure getting TraitDefinitions by Wifi.");
            showSnackbar(R.string.error_getting_traits);
        } else {
            Log.i(TAG, "Success getting TraitDefinitions by Wifi: " + components.size());
            getStates(components);
        }
    }

    /**
     * Queries the device for its current state,
     * and extracts the data related to the current state.
     */
    private void getStates(final Set<ComponentWrapper> components) {
        if (mDevice.getMqttInfo() != null) {
            sendMqttComponents(components);
        } else if (mDevice.getBleDevice() == null) {
            new SendComponentsCommand(mDevice,
                    this,
                    components).execute();
        } else {
            new SendComponentsGattCommand(this.getContext(),
                    this,
                    mDevice,
                    components).execute();
        }
    }

    @Override
    public void onBleComponentsResponse(final Set<ComponentWrapper> components,
                                        final Set<StateWrapper> states,
                                        final DeviceWrapper device,
                                        final boolean isSuccess) {
        if (!isSuccess) {
            Log.e(TAG, "Failure querying for state by BLE.");
            showSnackbar(R.string.error_querying_state);
            new SendComponentsGattCommand(this.getContext(),
                    this,
                    mDevice,
                    components).execute();
            return;
        } else {
            Log.i(TAG, "Success getting states by BLE: " + states.size());
        }

        addComponents(components);
    }

    @Override
    public void onComponentsResponse(final Set<ComponentWrapper> components,
                                     final Set<StateWrapper> states,
                                     final TinyDevice device,
                                     final boolean isSuccess) {
        if (!isSuccess) {
            Log.e(TAG, "Failure querying for state by Wifi.");
            showSnackbar(R.string.error_querying_state);
            return;
        } else {
            Log.i(TAG, "Success getting states by Wifi: " + states.size());
        }

        addComponents(components);
    }

    @Override
    public void onMqttComponentsResponse(final Set<ComponentWrapper> components,
                                         final boolean isSuccess) {

        if (!isSuccess) {
            Log.e(TAG, "Failure querying states by MQTT.");
            showSnackbar(R.string.error_querying_state);
            return;
        } else {
            Log.i(TAG, "Success getting states by MQTT: " + components.size());
        }

        addComponents(components);
    }

    /**
     * addComponents
     *
     * @param components Set<ComponentWrapper>
     */
    private void addComponents(final Set<ComponentWrapper> components) {
        if (getActivity() == null
                || getActivity().isFinishing()) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                progressBar.setVisibility(View.GONE);
                progressBarLayout.setVisibility(View.GONE);

                componentsAdapter.clear();

                for (ComponentWrapper component : components) {
                    componentsAdapter.addComponent(component);
                }
            }
        });
    }
}
