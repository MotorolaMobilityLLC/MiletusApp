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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * CustomExceptionHandler
 */
public final class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = CustomExceptionHandler.class.getSimpleName();
    private final Thread.UncaughtExceptionHandler defaultUEH;
    private final Context context;

    public CustomExceptionHandler(final Context context) {
        this.context = context.getApplicationContext();
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        final String sdCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);

        ex.printStackTrace(printWriter);
        String stacktrace = result.toString();
        printWriter.close();

        String dirPath = sdCardPath + File.separator + "Motoi" + File.separator;

        String filePath = sdf.format(new Date()) + ".txt";
        write(stacktrace, dirPath, filePath);
        add(dirPath + filePath);

        defaultUEH.uncaughtException(thread, ex);
    }

    /**
     * Write Exception
     *
     * @param log      String
     * @param dirPath  String
     * @param filePath String
     */
    @SuppressLint("SetWorldWritable")
    private void write(final String log,
                       final String dirPath,
                       final String filePath) {
        try {
            File dir = new File(dirPath);

            if (!dir.exists() && !dir.mkdirs()) {
                throw new IOException();
            }

            File file = new File(dir, filePath);

            if (file.exists()) {
                if (!file.delete()) {
                    throw new IOException();
                }
            } else {
                if (!file.createNewFile()
                        || !file.setWritable(true, false)) {
                    throw new IOException();
                }
            }

            BufferedWriter bos = new BufferedWriter(new FileWriter(file, true));
            bos.write(log + Strings.NEW_LINE);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    /**
     * ACTION_MEDIA_SCANNER_SCAN_FILE
     *
     * @param path String
     */
    private void add(final String path) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }
}
