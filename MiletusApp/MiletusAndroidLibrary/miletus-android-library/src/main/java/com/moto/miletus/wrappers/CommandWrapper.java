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
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * CommandWrapper
 */
public class CommandWrapper implements Parcelable {

    private static final String TAG = CommandWrapper.class.getSimpleName();
    private static final String setConfig = "setConfig";
    private static final String name = "name";
    private static final String component = "component";
    private static final String params = "parameters";
    private static final String scape = "\\";

    private String componentName;
    private String traitName;
    private String commandName;
    private List<CommandParameterWrapper> parameters;

    /**
     * CommandWrapper
     */
    public CommandWrapper() {
        this.parameters = new ArrayList<>();
    }

    private CommandWrapper(Parcel in) {
        componentName = in.readString();
        traitName = in.readString();
        commandName = in.readString();
        parameters = in.createTypedArrayList(CommandParameterWrapper.CREATOR);
    }

    public static final Creator<CommandWrapper> CREATOR = new Creator<CommandWrapper>() {
        @Override
        public CommandWrapper createFromParcel(Parcel in) {
            return new CommandWrapper(in);
        }

        @Override
        public CommandWrapper[] newArray(int size) {
            return new CommandWrapper[size];
        }
    };

    /**
     * setComponentName
     *
     * @param componentName String
     * @return this
     */
    public CommandWrapper setComponentName(String componentName) {
        this.componentName = componentName;
        return this;
    }

    /**
     * getComponentName
     *
     * @return String
     */
    private String getComponentName() {
        return componentName;
    }

    /**
     * CommandWrapper
     *
     * @param traitName String
     * @return CommandWrapper
     */
    public CommandWrapper setTraitName(String traitName) {
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
     * setCommandName
     *
     * @param commandName String
     * @return CommandWrapper
     */
    public CommandWrapper setCommandName(String commandName) {
        this.commandName = commandName;
        return this;
    }

    /**
     * getCommandName
     *
     * @return String
     */
    public String getCommandName() {
        if (!commandName.equals(setConfig)) {
            return commandName;
        } else {
            return traitName;
        }
    }

    /**
     * setParameters
     *
     * @param parameters List<CommandParameterWrapper>
     * @return CommandWrapper
     */
    public CommandWrapper setParameters(List<CommandParameterWrapper> parameters) {
        this.parameters = parameters;
        return this;
    }

    /**
     * getParameters
     *
     * @return List<CommandParameterWrapper>
     */
    public List<CommandParameterWrapper> getParameters() {
        return parameters;
    }

    /**
     * getFullName
     *
     * @return String
     */
    private String getFullName() {
        return traitName + "." + commandName;
    }

    /**
     * getCommand
     *
     * @return String
     */
    public String getCommand() {
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put(component, getComponentName());
            jsonObj.put(name, getFullName());

            JSONObject parameters = new JSONObject();

            for (CommandParameterWrapper parameter : getParameters()) {
                parameters.put(parameter.getParameterName(), parameter.getValue().getValue());
            }

            jsonObj.put(params, parameters);

            return jsonObj.toString().replace(scape, "").replace("\"\"", "\"");
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            return "";
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(componentName);
        dest.writeString(traitName);
        dest.writeString(commandName);
        dest.writeTypedList(parameters);
    }
}
