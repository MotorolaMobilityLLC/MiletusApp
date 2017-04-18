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

package com.moto.miletus.application.utils;

import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.moto.miletus.application.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * AboutActivity
 */
public class AboutActivity extends AppCompatActivity {

    private static final String TAG = AboutActivity.class.getSimpleName();
    private TextView install;
    private TextView update;
    private TextView build;
    private TextView compile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(this));
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        install = (TextView) findViewById(R.id.install_date_value);
        update = (TextView) findViewById(R.id.update_date_value);
        compile = (TextView) findViewById(R.id.dex_date_value);
        build = (TextView) findViewById(R.id.mf_date_value);

        initialize();
    }

    /**
     * initialize
     */
    private void initialize() {
        try {
            SimpleDateFormat sdf = (SimpleDateFormat) SimpleDateFormat.getInstance();
            PackageManager pm = getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(getPackageName(), 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);

            ZipEntry dex = zf.getEntry("classes.dex");
            ZipEntry mf = zf.getEntry("META-INF/MANIFEST.MF");

            long timeDex = dex.getTime();
            long timeMf = mf.getTime();
            long timeUpdate = packageInfo.lastUpdateTime;
            long timeInstall = packageInfo.firstInstallTime;

            zf.close();

            String dexDate = sdf.format(new java.util.Date(timeDex));
            String mfDate = sdf.format(new java.util.Date(timeMf));
            String installDate = sdf.format(new java.util.Date(timeInstall));
            String updateDate = sdf.format(new java.util.Date(timeUpdate));

            install.setText(installDate);
            compile.setText(dexDate);
            build.setText(mfDate);
            update.setText(updateDate);
        } catch (PackageManager.NameNotFoundException
                | IOException e) {
            Log.e(TAG, e.toString());

            install.setText(R.string.error_retrieving_info);
            compile.setText(R.string.error_retrieving_info);
            build.setText(R.string.error_retrieving_info);
            update.setText(R.string.error_retrieving_info);
        }
    }
}
