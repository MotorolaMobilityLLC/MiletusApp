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

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.moto.miletus.wrappers.CommandParameterWrapper;
import com.moto.miletus.application.OnRunCommandListener;
import com.moto.miletus.wrappers.CommandWrapper;
import com.moto.miletus.wrappers.ParameterValue;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewHolderSpinner
 */
public class ViewHolderSpinner extends RecyclerView.ViewHolder {

    private static final String TAG = ViewHolderSpinner.class.getSimpleName();

    private final Spinner spinner;
    private final ArrayAdapter<String> arrayAdapter;
    private int checkFirst = 0;
    private AdapterView.OnItemSelectedListener onItemSelectedListener = null;

    /**
     * ViewHolderSpinner
     *
     * @param parentView         View
     * @param spinner            Spinner
     * @param command            CommandWrapper
     * @param runCommandListener OnRunCommandListener
     */
    public ViewHolderSpinner(final View parentView,
                             final Spinner spinner,
                             final CommandWrapper command,
                             final OnRunCommandListener runCommandListener) {
        super(parentView);
        this.spinner = spinner;

        final List<String> list = new ArrayList<>();
        for (String value : command.getParameters().get(0).getValue().getValues()) {
            list.add(value);
        }

        this.arrayAdapter = new ArrayAdapter<>(parentView.getContext(),
                android.R.layout.simple_spinner_item,
                list);
        this.arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        this.spinner.setAdapter(arrayAdapter);

        this.onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemSelected");

                checkFirst++;

                if (checkFirst <= 1) {
                    return;
                }

                final CommandParameterWrapper parameter = command.getParameters().get(0);
                final String item = parent.getItemAtPosition(position).toString();

                // Update the internal model
                parameter.setValue(new ParameterValue(ParameterValue.STRING, item));

                // Update the UI to reflect new state.
                spinner.setSelection(position);

                // Update the spinner
                runCommandListener.onRunCommand(command.getCommand());
                Log.i(TAG, "Spinner changed to state: " + ViewHolderSpinner.this.spinner.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.i(TAG, "onNothingSelected");
            }
        };
    }

    /**
     * getSpinner
     *
     * @return Spinner
     */
    public Spinner getSpinner() {
        return spinner;
    }

    /**
     * getArrayAdapter
     *
     * @return ArrayAdapter
     */
    public ArrayAdapter<String> getArrayAdapter() {
        return arrayAdapter;
    }

    /**
     * setOnItemSelectedListener
     *
     * @param isActive boolean
     */
    public void setOnItemSelectedListener(boolean isActive) {
        if (isActive) {
            this.spinner.setOnItemSelectedListener(onItemSelectedListener);
        } else {
            this.spinner.setOnItemSelectedListener(null);
        }
    }
}
