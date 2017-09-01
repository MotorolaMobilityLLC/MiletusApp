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

package com.moto.miletus.wrappers;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * ComponentWrapper
 */
public class ComponentWrapper implements Parcelable {

    private String componentName;
    private String traitName;
    private List<CommandWrapper> commands;
    private List<StateWrapper> states;

    /**
     * ComponentWrapper
     */
    public ComponentWrapper() {
        this.commands = new ArrayList<>();
        this.states = new ArrayList<>();
    }

    private ComponentWrapper(Parcel in) {
        componentName = in.readString();
        traitName = in.readString();
        commands = in.createTypedArrayList(CommandWrapper.CREATOR);
        states = in.createTypedArrayList(StateWrapper.CREATOR);
    }

    /**
     * setComponentName
     *
     * @param componentName String
     * @return this
     */
    public ComponentWrapper setComponentName(String componentName) {
        this.componentName = componentName;
        return this;
    }

    /**
     * getComponentName
     *
     * @return String
     */
    public String getComponentName() {
        return componentName;
    }

    /**
     * ComponentWrapper
     *
     * @param traitName String
     * @return this
     */
    public ComponentWrapper setTraitName(String traitName) {
        this.traitName = traitName;
        return this;
    }

    /**
     * getTraitName
     *
     * @return String
     */
    public String getTraitName() {
        return traitName;
    }

    /**
     * setCommands
     *
     * @param commands List<CommandWrapper>
     * @return this
     */
    public ComponentWrapper setCommands(List<CommandWrapper> commands) {
        this.commands = commands;
        return this;
    }

    /**
     * getCommands
     *
     * @return List<CommandWrapper>
     */
    public List<CommandWrapper> getCommands() {
        return commands;
    }

    /**
     * setStates
     *
     * @param states List
     * @return this
     */
    public ComponentWrapper setStates(List<StateWrapper> states) {
        this.states = states;
        return this;
    }

    /**
     * getStates
     *
     * @return List
     */
    public List<StateWrapper> getStates() {
        return states;
    }

    public static final Creator<ComponentWrapper> CREATOR = new Creator<ComponentWrapper>() {
        @Override
        public ComponentWrapper createFromParcel(Parcel in) {
            return new ComponentWrapper(in);
        }

        @Override
        public ComponentWrapper[] newArray(int size) {
            return new ComponentWrapper[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(componentName);
        dest.writeString(traitName);
        dest.writeTypedList(commands);
        dest.writeTypedList(states);
    }
}
