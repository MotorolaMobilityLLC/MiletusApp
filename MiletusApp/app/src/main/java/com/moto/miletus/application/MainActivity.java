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

package com.moto.miletus.application;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.moto.miletus.application.ble.neardevice.NearDeviceHolder;
import com.moto.miletus.application.utils.LicenseDialog;
import com.moto.miletus.ble.BleScanService;
import com.moto.miletus.ble.commands.SendInfoGattCommand;
import com.moto.miletus.gson.info.TinyDevice;
import com.moto.miletus.mdns.NsdHelper;
import com.moto.miletus.mdns.SendInfoCommand;
import com.moto.miletus.application.utils.AboutActivity;
import com.moto.miletus.application.utils.CustomExceptionHandler;
import com.moto.miletus.application.utils.HardwareStateUtil;
import com.moto.miletus.application.utils.Strings;
import com.moto.miletus.wrappers.DeviceWrapper;

/**
 * MainActivity
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private DeviceListAdapter mDeviceListAdapter;
    private final DeviceRoomReceiver deviceRoomReceiver = new DeviceRoomReceiver();
    private final NearDeviceHolder nearDeviceHolder = new NearDeviceHolder(this, System.currentTimeMillis());
    private RecyclerView recyclerView;
    private RelativeLayout progressBarLayout;
    private ProgressBar progressBar;
    private Snackbar error;
    private boolean isBleStarted = false;

    private final SendInfoCommand.OnInfoResponse onInfoResponse = new SendInfoCommand.OnInfoResponse() {
        @Override
        public void onInfoResponse(final TinyDevice device) {
            final DeviceWrapper deviceWrapper = new DeviceWrapper(device, null);
            if (mDeviceListAdapter.containsName(deviceWrapper) == null) {
                addDevice(deviceWrapper);
                Log.i(TAG, "Device added: " + device.getName());
            } else {
                Log.e(TAG, "Device not added: " + device.getName());
            }
        }
    };

    private final SendInfoGattCommand.OnBleInfoResponse onBleInfoResponse = new SendInfoGattCommand.OnBleInfoResponse() {
        @Override
        public void onBleInfoResponse(final DeviceWrapper device) {
            if (mDeviceListAdapter.containsBle(device) == null) {
                addDevice(device);
                Log.i(TAG, "Device BLE added: " + device.getDevice().getName());
            } else {
                Log.e(TAG, "Device BLE not added: " + device.getDevice().getName());
            }
        }
    };

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            BleScanService.LocalBinder binder = (BleScanService.LocalBinder) service;
            binder.setOnBleInfoResponse(onBleInfoResponse);
            binder.setOnBleResolved(nearDeviceHolder);
            unbindService(mConnection);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_devices);

        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(this));
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        recyclerView = (RecyclerView) findViewById(R.id.devices_list);
        progressBarLayout = (RelativeLayout) findViewById(R.id.progressBarLayout);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // specify an adapter
        mDeviceListAdapter = new DeviceListAdapter();
        recyclerView.setAdapter(mDeviceListAdapter);
        mDeviceListAdapter.clear();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NsdHelper.getInstance().setOnInfoResponse(onInfoResponse);
    }

    /**
     * addDevice
     *
     * @param device Device
     */
    private void addDevice(final DeviceWrapper device) {
        runOnUiThread(new Runnable() {
            public void run() {
                progressBar.setVisibility(View.GONE);
                progressBarLayout.setVisibility(View.GONE);

                mDeviceListAdapter.add(device);
                mDeviceListAdapter.sort();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        checkAndroidVersion();
        checkHardwareState();
        checkPermission();
    }

    /**
     * checkPermission
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermission() {
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_COARSE_LOCATION);
        } else {
            startBleSettings();
            isBleStarted = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull final String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startBleSettings();
                    isBleStarted = true;
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover devices with bluetooth.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
            }
        }
    }

    /**
     * startBleSettings
     */
    private void startBleSettings() {
        registerReceiver(deviceRoomReceiver, new IntentFilter(Strings.NEAR_DEVICE));
        Intent intent = new Intent(this, BleScanService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * checkAndroidVersion
     */
    private void checkAndroidVersion() {
        final String androidOS = Build.VERSION.RELEASE;
        if (androidOS.equalsIgnoreCase(Strings._60)) {
            Toast.makeText(this,
                    R.string.error_android_version,
                    Toast.LENGTH_LONG)
                    .show();
            finish();
        }
    }

    /**
     * checkHardwareState
     */
    private void checkHardwareState() {
        if (!HardwareStateUtil.isWifiEnabled(this)
                || !HardwareStateUtil.isNetworkConnected(this)) {
            showError(R.string.error_wifi_off);
        } else if (!HardwareStateUtil.isBluetoothEnabled()
                || !HardwareStateUtil.hasBluetoothLe(this)) {
            showError(R.string.error_bluetooth_off);
        }
    }

    /**
     * showError
     *
     * @param errorId int
     */
    private void showError(final int errorId) {
        Log.e(TAG, "Error Id: " + errorId);

        runOnUiThread(new Runnable() {
            public void run() {
                if (error != null
                        && error.isShown()) {
                    return;
                }

                if (progressBar.getVisibility() != View.VISIBLE
                        || progressBarLayout.getVisibility() != View.VISIBLE) {
                    return;
                }

                if (recyclerView != null) {
                    error = Snackbar.make(recyclerView,
                            errorId,
                            Snackbar.LENGTH_LONG);
                    error.show();
                }
            }
        });
    }

    @Override
    public void onStart() {
        startLocalDiscovery();
        super.onStart();
    }

    @Override
    public void onStop() {
        stopLocalDiscovery();
        super.onStop();
    }

    /**
     * Start mDNS Local Discovery
     */
    private void startLocalDiscovery() {
        NsdHelper.getInstance().discoverServices();
    }

    /**
     * Stop mDNS Local Discovery
     */
    private void stopLocalDiscovery() {
        NsdHelper.getInstance().stopDiscovery();
    }

    final SearchView.OnQueryTextListener listener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            mDeviceListAdapter.getFilter().filter(query);
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            mDeviceListAdapter.getFilter().filter(newText);
            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        final MenuItem searchMenuItem = menu.findItem(R.id.menu_search);
        final SearchView mSearchView = (SearchView) searchMenuItem.getActionView();
        mSearchView.setOnQueryTextListener(listener);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.action_licenses:
                new LicenseDialog().show(getSupportFragmentManager(), "Show licenses");
                return true;
            case R.id.action_refresh:
                super.recreate();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (isBleStarted) {
            stopService(new Intent(this, BleScanService.class));
            unregisterReceiver(deviceRoomReceiver);
            NearDeviceHolder.setNearDevice(null);
        }

        mDeviceListAdapter.clear();
        super.onDestroy();
    }

    /**
     * DeviceRoomReceiver
     */
    public class DeviceRoomReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context,
                              final Intent intent) {
            if (intent.getAction().equals(Strings.NEAR_DEVICE)) {
                Log.i(TAG, Strings.NEAR_DEVICE);

                runOnUiThread(new Runnable() {
                    public void run() {
                        mDeviceListAdapter.sort();
                    }
                });
            }
        }
    }

}
