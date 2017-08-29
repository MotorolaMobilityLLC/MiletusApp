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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.moto.miletus.application.OnRunCommandListener;
import com.moto.miletus.application.R;
import com.moto.miletus.application.viewholder.ViewHolderButton;
import com.moto.miletus.application.viewholder.ViewHolderEditNumber;
import com.moto.miletus.application.viewholder.ViewHolderEditNumberPicker;
import com.moto.miletus.application.viewholder.ViewHolderEditText;
import com.moto.miletus.application.viewholder.ViewHolderLed;
import com.moto.miletus.application.viewholder.ViewHolderPolymorph;
import com.moto.miletus.application.viewholder.ViewHolderSpinner;
import com.moto.miletus.wrappers.CommandParameterWrapper;
import com.moto.miletus.wrappers.CommandWrapper;
import com.moto.miletus.wrappers.ParameterValue;

import java.util.ArrayList;
import java.util.List;

/**
 * For each {@link CommandWrapper} added to this
 * adapter, a corresponding {@link RecyclerView.ViewHolder} will be created that, when clicked, will trigger a
 * callback on the given {@link OnRunCommandListener}.
 */
class CommandsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final OnRunCommandListener runCommandListener;
    private final List<CommandWrapper> mDataSet;

    /**
     * CommandsAdapter
     *
     * @param runCommandListener OnRunCommandListener
     */
    CommandsAdapter(OnRunCommandListener runCommandListener) {
        this.runCommandListener = runCommandListener;
        this.mDataSet = new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        final CommandWrapper componentCommand = mDataSet.get(position);
        CommandParameterWrapper parameter;

        if (componentCommand.getParameters() == null
                || componentCommand.getParameters().isEmpty()) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listitem_button, parent, false);

            return new ViewHolderButton(v,
                    (Button) v.findViewById(R.id.button),
                    componentCommand,
                    runCommandListener);
        } else if (componentCommand.getParameters().size() >= 2) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listitem_button, parent, false);

            return new ViewHolderPolymorph(v,
                    (Button) v.findViewById(R.id.button),
                    componentCommand,
                    runCommandListener);
        } else {
            parameter = componentCommand.getParameters().get(0);
        }

        switch (parameter.getValue().getType()) {
            case ParameterValue.STRING:
                if (parameter.getValue().getValues() != null
                        && !parameter.getValue().getValues().isEmpty()) {
                    View v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.listitem_spinner, parent, false);

                    return new ViewHolderSpinner(v,
                            (Spinner) v.findViewById(R.id.spinner),
                            componentCommand,
                            runCommandListener);
                } else {
                    View v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.listitem_editvalue, parent, false);

                    return new ViewHolderEditText(v,
                            (TextView) v.findViewById(R.id.textlabel),
                            (Button) v.findViewById(R.id.editvalue),
                            componentCommand,
                            runCommandListener);
                }
            case ParameterValue.NUMBER:
            case ParameterValue.INTEGER:
                if (parameter.getValue().getMaximum() != null
                        && parameter.getValue().getMinimum() != null) {
                    View v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.listitem_editvalue, parent, false);

                    return new ViewHolderEditNumberPicker(v,
                            (TextView) v.findViewById(R.id.textlabel),
                            (Button) v.findViewById(R.id.editvalue),
                            parameter.getValue().getMaximum(),
                            parameter.getValue().getMinimum(),
                            componentCommand,
                            runCommandListener);
                } else {
                    View v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.listitem_editvalue, parent, false);

                    return new ViewHolderEditNumber(v,
                            (TextView) v.findViewById(R.id.textlabel),
                            (Button) v.findViewById(R.id.editvalue),
                            componentCommand,
                            runCommandListener);
                }
            case ParameterValue.BOOLEAN:
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_led, parent, false);

                return new ViewHolderLed(v,
                        (Switch) v.findViewById(R.id.toggler),
                        componentCommand,
                        runCommandListener);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final CommandWrapper componentCommand = mDataSet.get(position);

        if (holder instanceof ViewHolderButton) {
            final ViewHolderButton viewHolderButton = (ViewHolderButton) holder;

            viewHolderButton.getButton().setText(componentCommand.getCommandName());
        } else if (holder instanceof ViewHolderPolymorph) {
            final ViewHolderPolymorph viewHolderPolymorph = (ViewHolderPolymorph) holder;

            viewHolderPolymorph.getButton().setText(componentCommand.getCommandName());
        }

        CommandParameterWrapper parameter = null;
        if (componentCommand.getParameters() != null
                && !componentCommand.getParameters().isEmpty()) {
            parameter = componentCommand.getParameters().get(0);
        }
        if (parameter == null) {
            return;
        }

        String text;
        if (holder instanceof ViewHolderLed) {
            final ViewHolderLed viewHolderLed = (ViewHolderLed) holder;

            text = componentCommand.getCommandName() + " " + parameter.getParameterName() + ":";
            text = text.replace("_", "");

            viewHolderLed.getToggler().setText(text);

            if (parameter.getValue() != null) {
                boolean isLightOn = Boolean.parseBoolean(parameter.getValue().getValue());

                viewHolderLed.getToggler().setChecked(isLightOn);
            }
        } else if (holder instanceof ViewHolderSpinner) {
            final ViewHolderSpinner viewHolderSpinner = (ViewHolderSpinner) holder;

            text = parameter.getParameterName() + ":";
            text = text.replace("_", "");

            viewHolderSpinner.getSpinner().setPrompt(text);

            if (parameter.getValue() != null) {
                String value = parameter.getValue().getValue();

                viewHolderSpinner.setOnItemSelectedListener(false);
                viewHolderSpinner.getSpinner().setSelection(viewHolderSpinner.getArrayAdapter().getPosition(value));
                viewHolderSpinner.setOnItemSelectedListener(true);
            } else {
                viewHolderSpinner.setOnItemSelectedListener(true);
            }
        } else if (holder instanceof ViewHolderEditNumber) {
            final ViewHolderEditNumber viewHolderEditNumber = (ViewHolderEditNumber) holder;

            text = componentCommand.getCommandName() + " (" + parameter.getParameterName() + "): ";
            text = text.replace("_", "");

            viewHolderEditNumber.getTextView().setText(text);

            if (parameter.getValue() == null) {
                return;
            }

            if (parameter.getValue().getValue().contains(".")) {
                Double value = Double.parseDouble(parameter.getValue().getValue());
                text = value.toString();
            } else {
                Long value = Long.parseLong(parameter.getValue().getValue());
                text = value.toString();
            }

            viewHolderEditNumber.getButton().setText(text);
        } else if (holder instanceof ViewHolderEditNumberPicker) {
            final ViewHolderEditNumberPicker viewHolderEditNumberPicker = (ViewHolderEditNumberPicker) holder;

            text = componentCommand.getCommandName() + " (" + parameter.getParameterName() + "): ";
            text = text.replace("_", "");

            viewHolderEditNumberPicker.getTextView().setText(text);

            if (parameter.getValue() != null) {
                Long value = Long.parseLong(parameter.getValue().getValue());

                text = value.toString();

                viewHolderEditNumberPicker.getButton().setText(text);
            }
        } else if (holder instanceof ViewHolderEditText) {
            final ViewHolderEditText viewHolderEditNumber = (ViewHolderEditText) holder;

            text = componentCommand.getCommandName() + " (" + parameter.getParameterName() + "): ";
            text = text.replace("_", "");

            viewHolderEditNumber.getTextView().setText(text);

            if (parameter.getValue() != null) {
                viewHolderEditNumber.getButton().setText(parameter.getValue().getValue());
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    /**
     * addComponent
     *
     * @param component CommandWrapper
     */
    void addComponent(CommandWrapper component) {
        mDataSet.add(component);
        notifyItemInserted(mDataSet.size() - 1);
    }

    /**
     * clear
     */
    void clear() {
        mDataSet.clear();
        notifyDataSetChanged();
    }
}
