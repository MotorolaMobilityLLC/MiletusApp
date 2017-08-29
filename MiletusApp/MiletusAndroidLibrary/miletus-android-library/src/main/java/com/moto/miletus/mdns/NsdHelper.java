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
import android.net.nsd.NsdManager;
import android.os.SystemClock;
import android.util.Log;

import com.moto.miletus.utils.Strings;

import java.util.HashSet;
import java.util.Set;

/**
 * NsdHelper
 */
public final class NsdHelper {

    private NsdManager mNsdManager;
    private NsdServiceInfo mService;
    private NsdManager.RegistrationListener mRegistrationListener;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private SendInfoCommand.OnInfoResponse onInfoResponse;
    private int discoveryErrors = 0;
    private int resolveErrors = 0;
    private final Set<String> onResolveFailed = new HashSet<>();
    private String mServiceName = "";
    private static final int SLEEP_MS = 500;
    private static final int MAX_ERRORS = SLEEP_MS;
    private static final String TAG = NsdHelper.class.getSimpleName();
    private static final String SERVICE_TYPE = "_http._tcp.";
    private static NsdHelper nsdHelper;

    private NsdHelper() {
    }

    /**
     * getInstance
     *
     * @return NsdHelper
     */
    public static synchronized NsdHelper getInstance() {
        if (nsdHelper == null) {
            nsdHelper = new NsdHelper();
        }
        return nsdHelper;
    }

    /**
     * initializeNsd
     *
     * @param mNsdManager NsdManager
     */
    public void initializeNsd(final NsdManager mNsdManager) {
        this.mNsdManager = mNsdManager;
        initializeDiscoveryListener();
        initializeRegistrationListener();
    }

    /**
     * setOnInfoResponse
     *
     * @param onInfoResponse SendInfoCommand.OnInfoResponse
     */
    public void setOnInfoResponse(final SendInfoCommand.OnInfoResponse onInfoResponse) {
        this.onInfoResponse = onInfoResponse;
    }

    /**
     * initializeDiscoveryListener
     */
    private void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Log.d(TAG, "Service discovery success: " + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same machine: " + mServiceName);
                } else if (service.getServiceName().contains(Strings.mSearchName)) {
                    Log.d(TAG, "resolveService");
                    mNsdManager.resolveService(service, new ResolveListener());
                } else {
                    Log.d(TAG, "Service discovery success: " + service + Strings.NEW_LINE
                            + "Name: " + service.getServiceName());
                }
            }

            @Override
            public void onServiceLost(final NsdServiceInfo service) {
                Log.e(TAG, "service lost: " + service);

                if (onResolveFailed.contains(service.getServiceName())) {
                    onResolveFailed.remove(service.getServiceName());
                }

                if (service.equals(mService)) {
                    mService = null;
                }
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    /**
     * initializeRegistrationListener
     */
    private void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
                mServiceName = nsdServiceInfo.getServiceName();
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            }

        };
    }

    /**
     * registerService
     *
     * @param port int
     */
    private void registerService(int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setPort(port);
        serviceInfo.setServiceName(mServiceName);
        serviceInfo.setServiceType(SERVICE_TYPE);

        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);

    }

    /**
     * discoverServices
     */
    public void discoverServices() {
        Log.i(TAG, "discoverServices");

        try {
            mNsdManager.discoverServices(SERVICE_TYPE,
                    NsdManager.PROTOCOL_DNS_SD,
                    mDiscoveryListener);
            Log.i(TAG, "discoverServices OK");
            return;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.toString());
            discoveryErrors++;
        }

        if (discoveryErrors >= MAX_ERRORS) {
            Log.e(TAG, "maxDiscoveryErrors");
            discoveryErrors = 0;
        } else {
            Log.e(TAG, "discoveryErrors: " + discoveryErrors);
            discoverServices();
        }
    }

    /**
     * stopDiscovery
     */
    public void stopDiscovery() {
        Log.i(TAG, "stopDiscovery");

        try {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.toString());
        }
    }

    /**
     * tearDown
     */
    private void tearDown() {
        mNsdManager.unregisterService(mRegistrationListener);
    }

    /**
     * ResolveListener
     */
    private class ResolveListener implements NsdManager.ResolveListener {
        @Override
        public void onResolveFailed(NsdServiceInfo service, int errorCode) {
            Log.e(TAG, "Resolve failed (" + errorCode + "): " + service);
            onResolveFailed.add(service.getServiceName());
            SystemClock.sleep(SLEEP_MS);
            if (resolveErrors <= MAX_ERRORS
                    && onResolveFailed.contains(service.getServiceName())) {
                mNsdManager.resolveService(service, new ResolveListener());
                resolveErrors++;
            }

            Log.e(TAG, "resolveErrors: " + resolveErrors);
        }

        @Override
        public void onServiceResolved(NsdServiceInfo service) {
            Log.i(TAG, "Resolve Succeeded: " + service);
            if (onResolveFailed.contains(service.getServiceName())) {
                onResolveFailed.remove(service.getServiceName());
            }

            if (onResolveFailed.isEmpty()) {
                resolveErrors = 0;
            }

            mService = service;

            if (onInfoResponse != null) {
                new SendInfoCommand(mService, onInfoResponse).execute();
            }
        }
    }
}
