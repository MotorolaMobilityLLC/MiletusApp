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

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.moto.miletus.application.R;
import com.moto.miletus.wrappers.DeviceProvider;
import com.moto.miletus.application.utils.CustomExceptionHandler;
import com.moto.miletus.application.utils.Strings;
import com.moto.miletus.wrappers.ComponentWrapper;
import com.moto.miletus.wrappers.DeviceWrapper;
import com.moto.miletus.wrappers.ComponentProvider;

import org.apache.commons.lang3.StringUtils;

/**
 * Show controls on a given {@link DeviceWrapper} and {@link ComponentWrapper}.
 */
public final class CommandsActivity extends AppCompatActivity
        implements DeviceProvider, ComponentProvider {

    private DeviceWrapper mDevice;
    private ComponentWrapper mComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_commands);

        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(this));
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        final Bundle state = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
        mComponent = state.getParcelable(Strings.EXTRA_KEY_DEVICE_COMPONENT);
        mDevice = state.getParcelable(Strings.EXTRA_KEY_DEVICE);
        if (mDevice == null
                || mComponent == null) {
            throw new IllegalArgumentException("Error in intent extra");
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(StringUtils.capitalize(mDevice.getDevice().getName().replace(com.moto.miletus.utils.Strings.mSearchName, "")));
            actionBar.setSubtitle(StringUtils.capitalize(getComponent().getTraitName().replace("_", "")));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_commands, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_commands:
                CommandsFragment fragment = (CommandsFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.commands_fragment);
                fragment.onResume();
                return true;
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public DeviceWrapper getDevice() {
        return mDevice;
    }

    @Override
    public ComponentWrapper getComponent() {
        return mComponent;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(Strings.EXTRA_KEY_DEVICE, getDevice());
        outState.putParcelable(Strings.EXTRA_KEY_DEVICE_COMPONENT, getComponent());
        super.onSaveInstanceState(outState);
    }

    @Override
    public Intent getParentActivityIntent() {
        Intent intent = super.getParentActivityIntent();
        if (intent != null) {
            intent.putExtra(Strings.EXTRA_KEY_DEVICE, getDevice());
            intent.putExtra(Strings.EXTRA_KEY_DEVICE_COMPONENT, getComponent());
        }
        return intent;
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        Intent intent = super.getSupportParentActivityIntent();
        if (intent != null) {
            intent.putExtra(Strings.EXTRA_KEY_DEVICE, getDevice());
            intent.putExtra(Strings.EXTRA_KEY_DEVICE_COMPONENT, getComponent());
        }
        return intent;
    }
}
