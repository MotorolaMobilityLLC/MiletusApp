/*************************************************************************
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
 *************************************************************************/

package com.moto.miletus.application.viewholder;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

import com.moto.miletus.wrappers.CommandParameterWrapper;
import com.moto.miletus.application.OnRunCommandListener;
import com.moto.miletus.wrappers.CommandWrapper;
import com.moto.miletus.wrappers.ParameterValue;

/**
 * ViewHolderLed
 */
public class ViewHolderLed extends RecyclerView.ViewHolder
        implements View.OnClickListener {

    private static final String TAG = ViewHolderLed.class.getSimpleName();
    private static boolean lightOn = false;
    public final Switch toggler;
    private final CommandWrapper command;
    private final OnRunCommandListener runCommandListener;

    /**
     * ViewHolderLed
     *
     * @param parentView         View
     * @param toggler            Switch
     * @param command            CommandWrapper
     * @param runCommandListener OnRunCommandListener
     */
    public ViewHolderLed(final View parentView,
                         final Switch toggler,
                         final CommandWrapper command,
                         final OnRunCommandListener runCommandListener) {
        super(parentView);
        this.toggler = toggler;
        this.command = command;
        this.runCommandListener = runCommandListener;
        this.toggler.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // Update the internal model
        if (command.getParameters() != null) {
            for (CommandParameterWrapper parameter : command.getParameters()) {
                if (parameter.getValue() != null) {
                    lightOn = Boolean.parseBoolean(parameter.getValue().getValue());
                }

                lightOn = !lightOn;
                parameter.setValue(new ParameterValue(ParameterValue.BOOLEAN, lightOn + ""));
            }
        }

        // Update the UI to reflect new state.
        toggler.setChecked(lightOn);

        // Update the light
        runCommandListener.onRunCommand(command.getCommand());
        Log.i(TAG, "Toggler changed to state: " + ViewHolderLed.this.toggler.isChecked());
    }
}
