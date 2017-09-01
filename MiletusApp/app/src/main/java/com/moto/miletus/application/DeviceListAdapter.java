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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.moto.miletus.application.ble.neardevice.NearDeviceHolder;
import com.moto.miletus.application.utils.Strings;
import com.moto.miletus.wrappers.DeviceWrapper;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adapter for the list of devices. For each {@link DeviceWrapper} added to this
 * adapter, a corresponding card will be created. When clicked, the card will launch a new
 * {@link DeviceActivity}.
 */
public class DeviceListAdapter
        extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder>
        implements Filterable {

    private static final String TAG = DeviceListAdapter.class.getSimpleName();
    private static List<DeviceWrapper> dataSetOriginal;
    private static List<DeviceWrapper> dataSetFilter;

    /**
     * ViewHolder
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView name;
        private final TextView description;
        private final ImageView deviceImage;
        private final ImageView bleImage;

        ViewHolder(final View parentView, final TextView name,
                   final TextView description, final ImageView deviceImage,
                   final ImageView bleImage) {
            super(parentView);
            this.name = name;
            this.description = description;
            this.deviceImage = deviceImage;
            this.bleImage = bleImage;

            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        final DeviceWrapper device = getDataSetFilter().get(getAdapterPosition());
                        Log.i(TAG, "Selecting device: " + device.getDevice().getId());

                        Intent intent = new Intent(v.getContext(), DeviceActivity.class);
                        intent.putExtra(Strings.EXTRA_KEY_DEVICE, device);
                        v.getContext().startActivity(intent);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Log.i(TAG, "Updating device: " + getAdapterPosition());
                    }
                }
            };

            parentView.setOnClickListener(clickListener);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_device, parent, false);
        return new ViewHolder(v,
                (TextView) v.findViewById(R.id.device_title),
                (TextView) v.findViewById(R.id.device_description),
                (ImageView) v.findViewById(R.id.device_picture),
                (ImageView) v.findViewById(R.id.ble_picture));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DeviceWrapper data = getDataSetFilter().get(position);
        holder.name.setText(data.getDevice().getName().replace(com.moto.miletus.utils.Strings.mSearchName, ""));

        if (data.getBleDevice() != null) {
            holder.deviceImage.setImageResource(R.drawable.ic_bluetooth_enabled);
            holder.description.setText(R.string.ble);
        } else if (data.getMqttInfo() != null) {
            holder.deviceImage.setImageResource(R.drawable.mqtt);
            holder.description.setText(R.string.cloud);
        } else if (data.getNsdServiceInfo() != null) {
            holder.deviceImage.setImageResource(R.drawable.ic_wifi_enabled);
            holder.description.setText(R.string.wifi);
        }

        if (compare(data) == 1) {
            holder.bleImage.setVisibility(View.INVISIBLE);
        } else {
            holder.bleImage.setVisibility(View.VISIBLE);
        }
    }

    /**
     * getDataSetOriginal
     *
     * @return List<DeviceWrapper>
     */
    public static synchronized List<DeviceWrapper> getDataSetOriginal() {
        if (dataSetOriginal == null) {
            dataSetOriginal = new ArrayList<>();
        }
        return dataSetOriginal;
    }

    /**
     * getDataSetFilter
     *
     * @return List<DeviceWrapper>
     */
    private static synchronized List<DeviceWrapper> getDataSetFilter() {
        if (dataSetFilter == null) {
            dataSetFilter = new ArrayList<>();
        }
        return dataSetFilter;
    }

    /**
     * setDataSetFilter
     *
     * @param dataSetFilter List<DeviceWrapper>
     */
    private static synchronized void setDataSetFilter(final List<DeviceWrapper> dataSetFilter) {
        DeviceListAdapter.dataSetFilter = dataSetFilter;
    }

    /**
     * compare
     *
     * @param device DeviceWrapper
     * @return int
     */
    private static int compare(final DeviceWrapper device) {
        if (NearDeviceHolder.getNearDevice() == null) {
            return 1;
        } else if (device.getBleDevice() != null
                && device.getBleDevice().getAddress().equalsIgnoreCase(
                NearDeviceHolder.getNearDevice().getAddress())) {
            return -1;
        } else {
            return 1;
        }
    }

    @Override
    public int getItemCount() {
        return getDataSetFilter().size();
    }

    /**
     * clear
     */
    void clear() {
        Log.i(TAG, "Clear");

        if (!getDataSetOriginal().isEmpty()) {
            getDataSetOriginal().clear();
        }

        if (!getDataSetFilter().isEmpty()) {
            getDataSetFilter().clear();
        }

        notifyDataSetChanged();
    }

    /**
     * add
     *
     * @param device Device
     */
    void add(DeviceWrapper device) {
        getDataSetOriginal().add(device);
        getDataSetFilter().add(device);
        notifyItemInserted(getDataSetFilter().size() - 1);
    }

    /*
     * remove
     *
     * @param device Device
     */
    /*public void remove(final DeviceWrapper device) {
        for (Iterator<DeviceWrapper> it = getDataSetOriginal().iterator(); it.hasNext(); ) {
            DeviceWrapper pair = it.next();
            if (pair.getDevice().getId().equalsIgnoreCase(device.getDevice().getId())) {
                it.remove();
                notifyItemRemoved(getDataSetOriginal().size() - 1);
                break;
            }
        }
    }*/

    /**
     * contains
     *
     * @param device Device
     * @return Device
     */
    DeviceWrapper contains(final DeviceWrapper device) {
        for (final DeviceWrapper deviceWrapper : getDataSetFilter()) {
            if (device.getMqttInfo() != null
                    && deviceWrapper.getMqttInfo() != null
                    && deviceWrapper.getDevice().getName().equalsIgnoreCase(device.getDevice().getName())) {
                return deviceWrapper;
            } else if (device.getMqttInfo() == null
                    && deviceWrapper.getMqttInfo() == null
                    && deviceWrapper.getBleDevice() != null
                    && device.getBleDevice() != null
                    && deviceWrapper.getBleDevice().getAddress().equalsIgnoreCase(device.getBleDevice().getAddress())) {
                return deviceWrapper;
            } else if (deviceWrapper.getDevice().getName().equalsIgnoreCase(device.getDevice().getName())
                    && deviceWrapper.getMqttInfo() == null
                    && device.getMqttInfo() == null
                    && deviceWrapper.getBleDevice() == null
                    && device.getBleDevice() == null) {
                return deviceWrapper;
            }
        }
        return null;
    }

    /**
     * getStringSet
     *
     * @return Set<String>
     */
    private Set<String> getStringSet() {
        final Set<String> devices = new HashSet<>();
        for (final DeviceWrapper device : getDataSetOriginal()) {
            device.setDate(new Date().getTime());
            String string = new Gson().toJson(device);
            devices.add(string);
        }
        return devices;
    }

    /**
     * storeDevices
     */
    void storeDevices(final Context context) {
        final SharedPreferences sp = context.getSharedPreferences(Strings.STORED_DEVICES, 0);
        final SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.putStringSet(Strings.STORED_DEVICES, getStringSet());
        editor.apply();
    }

    /**
     * loadDevices
     *
     * @param context Context
     * @return Set<String>
     */
    Set<String> loadDevices(final Context context) {
        final SharedPreferences sp = context.getSharedPreferences(Strings.STORED_DEVICES, 0);
        return sp.getStringSet(Strings.STORED_DEVICES, new HashSet<String>());
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(final CharSequence constraint) {
                final FilterResults results = new FilterResults();
                final List<DeviceWrapper> dataSetFilter = new ArrayList<>();

                if (constraint.length() == 0) {
                    dataSetFilter.addAll(getDataSetOriginal());
                } else {
                    for (final DeviceWrapper device : getDataSetOriginal()) {
                        if (StringUtils.containsIgnoreCase(device.getDevice().getName(),
                                constraint.toString())) {
                            dataSetFilter.add(device);
                        }
                    }
                }

                results.values = dataSetFilter;
                results.count = dataSetFilter.size();
                return results;
            }

            @Override
            protected void publishResults(final CharSequence constraint,
                                          final FilterResults results) {
                if (results.values instanceof List) {
                    getDataSetFilter().clear();
                    setDataSetFilter((List<DeviceWrapper>) results.values);
                    sort();
                }
            }
        };
    }

    /**
     * sort
     */
    void sort() {
        if (getDataSetFilter().size() > 1) {
            Collections.sort(getDataSetFilter(), getComparator());
        }

        notifyDataSetChanged();
    }

    /**
     * getComparator
     *
     * @return Comparator
     */
    private Comparator<DeviceWrapper> getComparator() {
        return new Comparator<DeviceWrapper>() {
            @Override
            public int compare(DeviceWrapper pair1,
                               DeviceWrapper pair2) {
                return DeviceListAdapter.compare(pair1);
            }
        };
    }

}
