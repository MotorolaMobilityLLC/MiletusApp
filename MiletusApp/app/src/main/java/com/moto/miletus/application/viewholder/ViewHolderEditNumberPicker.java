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

package com.moto.miletus.application.viewholder;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.moto.miletus.application.R;
import com.moto.miletus.wrappers.CommandParameterWrapper;
import com.moto.miletus.application.OnRunCommandListener;
import com.moto.miletus.wrappers.CommandWrapper;
import com.moto.miletus.wrappers.ParameterValue;

/**
 * ViewHolderEditNumberPicker
 */
public class ViewHolderEditNumberPicker extends ViewHolderEditValue {

    private static final String TAG = ViewHolderEditNumberPicker.class.getSimpleName();

    private final String max;
    private final String min;

    public ViewHolderEditNumberPicker(final View parentView,
                                      final TextView textView,
                                      final Button button,
                                      final String max,
                                      final String min,
                                      final CommandWrapper command,
                                      final OnRunCommandListener runCommandListener) {
        super(parentView,
                textView,
                button,
                command,
                runCommandListener);
        this.max = max;
        this.min = min;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void showDialog(final View v,
                              final CommandWrapper command,
                              final OnRunCommandListener runCommandListener) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(v.getContext());

        final NumberPicker input = new NumberPicker(v.getContext());
        input.setMaxValue(Integer.parseInt(max));
        input.setMinValue(Integer.parseInt(min));
        input.setGravity(Gravity.START);

        dialog.setView(input);
        dialog.setPositiveButton(R.string.set_value,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Update the internal model
                        for (CommandParameterWrapper parameter : command.getParameters()) {
                            parameter.setValue(new ParameterValue(ParameterValue.INTEGER, input.getValue() + ""));
                        }

                        // Update the UI to reflect new state.
                        getButton().setText(input.getValue() + "");

                        // Run the command
                        runCommandListener.onRunCommand(command.getCommand());
                        Log.i(TAG, "Button click: " + ViewHolderEditNumberPicker.this.getButton().getText().toString());
                    }
                });

        dialog.show();
    }
}
