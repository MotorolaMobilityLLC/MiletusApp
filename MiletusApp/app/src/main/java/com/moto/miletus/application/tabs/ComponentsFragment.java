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

import android.os.AsyncTask;
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
import com.moto.miletus.application.utils.Strings;
import com.moto.miletus.gson.info.TinyDevice;
import com.moto.miletus.wrappers.DeviceProvider;
import com.moto.miletus.ble.commands.SendComponentsGattCommand;
import com.moto.miletus.ble.commands.SendTraitsGattCommand;
import com.moto.miletus.mdns.SendComponentsCommand;
import com.moto.miletus.mdns.SendTraitsCommand;
import com.moto.miletus.application.utils.CustomExceptionHandler;
import com.moto.miletus.wrappers.DeviceWrapper;
import com.moto.miletus.wrappers.StateWrapper;
import com.moto.miletus.wrappers.ComponentWrapper;

import java.util.Set;

/**
 * Handles the RecyclerView
 */
public class ComponentsFragment extends Fragment
        implements SendTraitsCommand.OnTraitsResponse,
        SendComponentsCommand.OnComponentsResponse,
        SendTraitsGattCommand.OnBleTraitsResponse,
        SendComponentsGattCommand.OnBleComponentsResponse {
    private static final String TAG = ComponentsFragment.class.getSimpleName();

    private ComponentsAdapter componentsAdapter;
    private RelativeLayout progressBarLayout;
    private ProgressBar progressBar;
    private DeviceWrapper mDevice;

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

    /**
     * Call getTraitDefinitions API
     */
    public void getTraits() {
        if (mDevice.getBleDevice() == null) {
            final SendTraitsCommand sendTraitsCommand = new SendTraitsCommand(mDevice.getDevice(),
                    this);
            try {
                sendTraitsCommand.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (OutOfMemoryError e) {
                Log.e(TAG, e.toString());
                sendTraitsCommand.execute();
            }
        } else {
            final SendTraitsGattCommand sendTraitsGattCommand = new SendTraitsGattCommand(this.getContext(),
                    this,
                    mDevice.getBleDevice());
            try {
                sendTraitsGattCommand.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (OutOfMemoryError e) {
                Log.e(TAG, e.toString());
                sendTraitsGattCommand.execute();
            }
        }
    }

    @Override
    public void onBleTraitsResponse(final Set<ComponentWrapper> components,
                                    final boolean isSuccess) {
        if (!isSuccess) {
            Log.e(TAG, "Failure getting TraitDefinitions.");
            if (ComponentsFragment.this.getView() != null) {
                Snackbar.make(ComponentsFragment.this.getView(),
                        R.string.error_getting_traits,
                        Snackbar.LENGTH_LONG)
                        .show();
            }
            final SendTraitsGattCommand sendTraitsGattCommand = new SendTraitsGattCommand(this.getContext(),
                    this,
                    mDevice.getBleDevice());
            try {
                sendTraitsGattCommand.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (OutOfMemoryError e) {
                Log.e(TAG, e.toString());
                sendTraitsGattCommand.execute();
            }
        } else {
            Log.i(TAG, "Success getting TraitDefinitions: " + components.size());

            getStates(components);
        }
    }

    @Override
    public void onTraitsResponse(final Set<ComponentWrapper> components,
                                 final boolean isSuccess) {
        if (!isSuccess) {
            Log.e(TAG, "Failure getting TraitDefinitions.");
            if (ComponentsFragment.this.getView() != null) {
                Snackbar.make(ComponentsFragment.this.getView(),
                        R.string.error_getting_traits,
                        Snackbar.LENGTH_LONG)
                        .show();
            }
        } else {
            Log.i(TAG, "Success getting TraitDefinitions: " + components.size());

            getStates(components);
        }
    }

    /**
     * Queries the device for its current state,
     * and extracts the data related to the current state.
     */
    private void getStates(final Set<ComponentWrapper> components) {
        if (mDevice.getBleDevice() == null) {
            final SendComponentsCommand sendComponentsCommand = new SendComponentsCommand(mDevice.getDevice(),
                    this,
                    components);
            try {
                sendComponentsCommand.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (OutOfMemoryError e) {
                Log.e(TAG, e.toString());
                sendComponentsCommand.execute();
            }
        } else {
            final SendComponentsGattCommand sendComponentsGattCommand = new SendComponentsGattCommand(this.getContext(),
                    this,
                    mDevice,
                    components);
            try {
                sendComponentsGattCommand.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (OutOfMemoryError e) {
                Log.e(TAG, e.toString());
                sendComponentsGattCommand.execute();
            }
        }
    }

    @Override
    public void onBleComponentsResponse(final Set<ComponentWrapper> components,
                                        final Set<StateWrapper> states,
                                        final DeviceWrapper device,
                                        final boolean isSuccess) {
        if (!isSuccess) {
            Log.e(TAG, "Failure querying for state.");
            if (ComponentsFragment.this.getView() != null) {
                Snackbar.make(ComponentsFragment.this.getView(),
                        R.string.error_querying_state,
                        Snackbar.LENGTH_LONG)
                        .show();
            }

            final SendComponentsGattCommand sendComponentsGattCommand = new SendComponentsGattCommand(this.getContext(),
                    this,
                    mDevice,
                    components);
            try {
                sendComponentsGattCommand.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (OutOfMemoryError e) {
                Log.e(TAG, e.toString());
                sendComponentsGattCommand.execute();
            }

            return;
        } else {
            Log.i(TAG, "Success getting states: " + components.size());
        }

        addComponents(components);
    }

    @Override
    public void onComponentsResponse(final Set<ComponentWrapper> components,
                                     final Set<StateWrapper> states,
                                     final TinyDevice device,
                                     final boolean isSuccess) {
        if (!isSuccess) {
            Log.e(TAG, "Failure querying for state.");
            if (ComponentsFragment.this.getView() != null) {
                Snackbar.make(ComponentsFragment.this.getView(),
                        R.string.error_querying_state,
                        Snackbar.LENGTH_LONG)
                        .show();
            }
            return;
        } else {
            Log.i(TAG, "Success getting states: " + components.size());
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
