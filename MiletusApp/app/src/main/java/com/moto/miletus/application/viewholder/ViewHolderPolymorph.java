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
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.moto.miletus.application.R;
import com.moto.miletus.wrappers.CommandParameterWrapper;
import com.moto.miletus.application.OnRunCommandListener;
import com.moto.miletus.wrappers.CommandWrapper;
import com.moto.miletus.wrappers.ParameterValue;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewHolderPolymorph
 */
public class ViewHolderPolymorph extends RecyclerView.ViewHolder {

    private static final String TAG = ViewHolderPolymorph.class.getSimpleName();
    private final Button button;

    /**
     * ViewHolderPolymorph
     *
     * @param parentView         View
     * @param button             Button
     * @param command            CommandWrapper
     * @param runCommandListener OnRunCommandListener
     */
    public ViewHolderPolymorph(final View parentView,
                               final Button button,
                               final CommandWrapper command,
                               final OnRunCommandListener runCommandListener) {
        super(parentView);
        this.button = button;
        this.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(v, command, runCommandListener);
                Log.i(TAG, "Button click: " + ViewHolderPolymorph.this.button.getText());
            }
        });
    }

    /**
     * getButton
     *
     * @return Button
     */
    public Button getButton() {
        return button;
    }

    /**
     * showDialog
     *
     * @param v                  View
     * @param command            CommandWrapper
     * @param runCommandListener OnRunCommandListener
     */
    @SuppressLint("SetTextI18n")
    private void showDialog(final View v,
                            final CommandWrapper command,
                            final OnRunCommandListener runCommandListener) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(v.getContext());

        final LinearLayoutCompat.LayoutParams layoutParams = new LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
        final LinearLayoutCompat layout = new LinearLayoutCompat(v.getContext());
        layout.setOrientation(LinearLayoutCompat.VERTICAL);
        layout.setLayoutParams(layoutParams);

        for (CommandParameterWrapper commandParameterWrapper : command.getParameters()) {
            ParameterValue parameter = commandParameterWrapper.getValue();

            switch (parameter.getType()) {
                case ParameterValue.BOOLEAN:
                    final Switch toggler = new Switch(v.getContext());
                    toggler.setLayoutParams(layoutParams);
                    toggler.setText(commandParameterWrapper.getParameterName() + ": ");
                    toggler.setContentDescription(commandParameterWrapper.getParameterName());

                    layout.addView(toggler);
                    break;
                case ParameterValue.STRING:
                    if (parameter.getValues() != null
                            && !parameter.getValues().isEmpty()) {

                        final List<String> list = new ArrayList<>();
                        for (Object value : parameter.getValues()) {
                            list.add(value.toString());
                        }

                        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(v.getContext(),
                                android.R.layout.simple_spinner_item, list);
                        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        final TextView textView = new TextView(v.getContext());
                        textView.setLayoutParams(layoutParams);
                        textView.setText(commandParameterWrapper.getParameterName() + ": ");

                        final Spinner spinner = new Spinner(v.getContext());
                        spinner.setAdapter(arrayAdapter);
                        spinner.setPrompt(commandParameterWrapper.getParameterName() + ": ");
                        spinner.setLayoutParams(layoutParams);
                        spinner.setContentDescription(commandParameterWrapper.getParameterName());

                        layout.addView(textView);
                        layout.addView(spinner);
                    } else {
                        final EditText input = new EditText(v.getContext());
                        input.setLayoutParams(layoutParams);
                        input.setHint(commandParameterWrapper.getParameterName());

                        layout.addView(input);
                    }
                    break;
                case ParameterValue.INTEGER:
                case ParameterValue.NUMBER:
                    if (parameter.getMaximum() != null
                            && parameter.getMinimum() != null) {

                        final TextView textView = new TextView(v.getContext());
                        textView.setLayoutParams(layoutParams);
                        textView.setText(commandParameterWrapper.getParameterName() + ": ");

                        final NumberPicker input = new NumberPicker(v.getContext());
                        input.setMaxValue((int) Float.parseFloat(parameter.getMaximum()));
                        input.setMinValue((int) Float.parseFloat(parameter.getMinimum()));
                        input.setContentDescription(commandParameterWrapper.getParameterName());

                        layout.addView(textView);
                        layout.addView(input);
                    } else {
                        final EditText input = new EditText(v.getContext());
                        input.setLayoutParams(layoutParams);
                        input.setHint(commandParameterWrapper.getParameterName());
                        input.setInputType(InputType.TYPE_CLASS_NUMBER);

                        layout.addView(input);
                    }
                    break;
            }
        }

        dialog.setView(layout);
        dialog.setPositiveButton(R.string.set_value,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        for (int i = 0; i < layout.getChildCount(); i++) {
                            View v = layout.getChildAt(i);
                            if (v instanceof EditText) {
                                EditText editText = (EditText) v;

                                for (CommandParameterWrapper parameter : command.getParameters()) {
                                    if (parameter.getParameterName().equals(editText.getHint())) {

                                        String value = editText.getText().toString();
                                        parameter.setValue(new ParameterValue(ParameterValue.INTEGER, value));
                                    }
                                }
                            } else if (v instanceof NumberPicker) {
                                NumberPicker numberPicker = (NumberPicker) v;

                                for (CommandParameterWrapper parameter : command.getParameters()) {
                                    if (parameter.getParameterName().equals(numberPicker.getContentDescription())) {
                                        parameter.setValue(new ParameterValue(ParameterValue.INTEGER, numberPicker.getValue() + ""));
                                    }
                                }
                            } else if (v instanceof Switch) {
                                Switch toggler = (Switch) v;

                                for (CommandParameterWrapper parameter : command.getParameters()) {
                                    if (parameter.getParameterName().equals(toggler.getContentDescription())) {
                                        parameter.setValue(new ParameterValue(ParameterValue.BOOLEAN, toggler.isChecked() + ""));
                                    }
                                }
                            } else if (v instanceof Spinner) {
                                Spinner spinner = (Spinner) v;

                                for (CommandParameterWrapper parameter : command.getParameters()) {
                                    if (parameter.getParameterName().equals(spinner.getContentDescription())) {
                                        parameter.setValue(new ParameterValue(ParameterValue.STRING, spinner.getSelectedItem().toString()));
                                    }
                                }
                            }
                        }

                        // Run the command
                        runCommandListener.onRunCommand(command.getCommand());
                        Log.i(TAG, "Button click: " + ViewHolderPolymorph.this.button.getText().toString());
                    }
                });

        dialog.show();
    }
}
