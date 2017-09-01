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

import android.animation.LayoutTransition;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moto.miletus.application.R;
import com.moto.miletus.application.utils.Strings;
import com.moto.miletus.wrappers.StateWrapper;
import com.moto.miletus.wrappers.ComponentWrapper;
import com.moto.miletus.wrappers.DeviceWrapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;

import java.util.ArrayList;
import java.util.List;

/**
 * For each {@link ComponentWrapper} added to this
 * adapter, a corresponding {@link RecyclerView.ViewHolder} will be created.
 */
class ComponentsAdapter extends RecyclerView.Adapter<ComponentsAdapter.ViewHolder> {

    private static final String TAG = ComponentsAdapter.class.getSimpleName();
    private final List<ComponentWrapper> mDataSet;
    private final DeviceWrapper mDevice;

    /**
     * ComponentsAdapter
     *
     * @param device DeviceWrapper
     */
    ComponentsAdapter(final DeviceWrapper device) {
        this.mDevice = device;
        this.mDataSet = new ArrayList<>();
    }

    /**
     * ViewHolder
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        private final RelativeLayout componentLayout;
        private final RelativeLayout showLayout;
        private final TextView componentName;
        private final TextView stateValue1;
        private final TextView stateValue2;
        private final TextView showMore;
        private final ImageView commandSettings;
        private final ImageView arrow;
        private boolean showStates;

        ViewHolder(final CardView cardView,
                   final RelativeLayout componentLayout,
                   final RelativeLayout showLayout,
                   final TextView componentName,
                   final TextView stateValue1,
                   final TextView stateValue2,
                   final TextView showMore,
                   final ImageView commandSettings,
                   final ImageView arrow) {
            super(cardView);
            this.componentLayout = componentLayout;
            this.showLayout = showLayout;
            this.componentName = componentName;
            this.stateValue1 = stateValue1;
            this.stateValue2 = stateValue2;
            this.showMore = showMore;
            this.commandSettings = commandSettings;
            this.arrow = arrow;
            this.showStates = false;

            View.OnClickListener clickShow = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewHolder.this.showStates = !ViewHolder.this.showStates;

                    if (ViewHolder.this.showStates) {
                        ViewHolder.this.componentLayout.setLayoutTransition(new LayoutTransition());
                        ViewHolder.this.stateValue2.setVisibility(View.VISIBLE);
                        ViewHolder.this.arrow.animate().rotationBy(180f).start();
                        ViewHolder.this.showMore.setText(R.string.hide);
                    } else {
                        ViewHolder.this.componentLayout.setLayoutTransition(null);
                        ViewHolder.this.stateValue2.setVisibility(View.GONE);
                        ViewHolder.this.arrow.animate().rotationBy(-180f).start();
                        ViewHolder.this.showMore.setText(R.string.show_more);
                    }
                }
            };

            View.OnClickListener clickCommandSettings = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final ComponentWrapper componentWrapper = mDataSet.get(getAdapterPosition());
                    Log.i(TAG, "Selecting component: " + componentWrapper.getTraitName());

                    Intent intent = new Intent(v.getContext(), CommandsActivity.class);
                    intent.putExtra(Strings.EXTRA_KEY_DEVICE, mDevice);
                    intent.putExtra(Strings.EXTRA_KEY_DEVICE_COMPONENT, componentWrapper);
                    v.getContext().startActivity(intent);
                }
            };

            this.arrow.setOnClickListener(clickShow);
            this.showMore.setOnClickListener(clickShow);
            this.showLayout.setOnClickListener(clickShow);
            this.commandSettings.setOnClickListener(clickCommandSettings);
            this.stateValue2.setVisibility(View.GONE);
            this.arrow.setVisibility(View.GONE);
            this.showMore.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        final CardView cardView = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_component, parent, false);

        return new ViewHolder(cardView,
                (RelativeLayout) cardView.findViewById(R.id.component_layout),
                (RelativeLayout) cardView.findViewById(R.id.show_layout),
                (TextView) cardView.findViewById(R.id.component_name),
                (TextView) cardView.findViewById(R.id.state_value1),
                (TextView) cardView.findViewById(R.id.state_value2),
                (TextView) cardView.findViewById(R.id.show_more),
                (ImageView) cardView.findViewById(R.id.command_settings),
                (ImageView) cardView.findViewById(R.id.arrow));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ComponentWrapper componentWrapper = mDataSet.get(position);

        holder.componentName.setText(StringUtils.capitalize(componentWrapper.getTraitName().replace("_", "")));

        String value1 = "";
        String value2 = "";
        int count = 0;
        for (StateWrapper stateWrapper : componentWrapper.getStates()) {

            String stateValue = stateWrapper.getValue().getValue();
            String stateName = stateWrapper.getStateName();

            try {
                if (StringUtils.containsIgnoreCase(stateName, Strings.TEMPERATURE)) {
                    stateValue = Precision.round(Float.parseFloat(stateValue), 1) + "";
                    stateValue = stateValue + Strings.CELSIUS;
                } else if (StringUtils.containsIgnoreCase(stateName, Strings.LIGHT)) {
                    stateValue = stateValue + Strings.LUX;
                } else if (StringUtils.containsIgnoreCase(stateName, Strings.HUMIDITY)) {
                    stateValue = Math.round(Precision.round(Float.parseFloat(stateValue), 0)) + "";
                    stateValue = stateValue + "%";
                }
            } catch (NumberFormatException nfe) {
                Log.e(TAG, stateName + ": " + Strings.NEW_LINE + stateValue);
            }

            if (count == 0) {
                value1 = value1
                        + stateName
                        + ": "
                        + stateValue;
            } else {
                value2 = value2
                        + stateName
                        + ": "
                        + stateValue
                        + Strings.NEW_LINE;
            }

            count++;
        }

        value1 = value1.replace("_", "");
        value2 = value2.replace("_", "");

        holder.stateValue1.setText(value1);
        holder.stateValue2.setText(value2);

        if (value2.isEmpty()) {
            holder.arrow.setVisibility(View.GONE);
            holder.showMore.setVisibility(View.GONE);
        } else {
            holder.arrow.setVisibility(View.VISIBLE);
            holder.showMore.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    /**
     * addComponent
     *
     * @param component ComponentWrapper
     */
    void addComponent(ComponentWrapper component) {
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
