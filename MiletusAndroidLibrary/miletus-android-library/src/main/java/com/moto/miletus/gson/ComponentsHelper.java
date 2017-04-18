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

package com.moto.miletus.gson;

import android.util.Log;

import com.moto.miletus.wrappers.CommandParameterWrapper;
import com.moto.miletus.wrappers.CommandWrapper;
import com.moto.miletus.wrappers.ParameterValue;
import com.moto.miletus.wrappers.StateWrapper;
import com.moto.miletus.wrappers.ComponentWrapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * ComponentsHelper
 */
public final class ComponentsHelper {

    private static final String TAG = ComponentsHelper.class.getSimpleName();
    private static final String COMPONENTS = "components";
    private static final String TRAITS = "traits";
    private static final String STATE = "state";

    private ComponentsHelper() {
    }

    /**
     * addComponentStates
     *
     * @param json              String
     * @param componentsWrapper Set<ComponentWrapper>
     * @return Set<StateWrapper>
     * @throws JSONException JSONException
     */
    public static Set<StateWrapper> addComponentStates(final String json,
                                                       final Set<ComponentWrapper> componentsWrapper)
            throws JSONException {

        Log.i(TAG, json);

        Set<StateWrapper> statesWrappers = new HashSet<>();

        JSONObject object = (JSONObject) new JSONTokener(json).nextValue();
        JSONObject components = object.getJSONObject(COMPONENTS);
        Iterator componentsKeys = components.keys();
        while (componentsKeys.hasNext()) {
            String componentName = (String) componentsKeys.next();
            JSONObject componentValue = components.getJSONObject(componentName);

            List<String> traitsList = new ArrayList<>();
            JSONArray traits = componentValue.getJSONArray(TRAITS);
            for (int i = 0; i < traits.length(); ++i) {
                String trait = traits.getString(i);
                traitsList.add(trait);
            }

            JSONObject states = componentValue.getJSONObject(STATE);
            Iterator statesKeys = states.keys();
            while (statesKeys.hasNext()) {
                String stateKey = (String) statesKeys.next();

                if (!traitsList.contains(stateKey)) {
                    throw new JSONException(TAG);
                }

                JSONObject state = states.getJSONObject(stateKey);
                Iterator stateKeys = state.keys();

                if (!stateKeys.hasNext()
                        && componentsWrapper != null) {
                    for (ComponentWrapper component : componentsWrapper) {
                        if (component.getTraitName().equalsIgnoreCase(stateKey)) {
                            component.setComponentName(componentName);
                            for (CommandWrapper command : component.getCommands()) {
                                if (command.getTraitName().equalsIgnoreCase(stateKey)) {
                                    command.setComponentName(componentName);
                                }
                            }
                        }
                    }
                }

                while (stateKeys.hasNext()) {
                    String stateName = (String) stateKeys.next();
                    ParameterValue typedData = new ParameterValue(null);
                    typedData.setValue(state.get(stateName).toString());

                    Type stateType = Type.valueOf(state.get(stateName).getClass().getSimpleName());
                    switch (stateType) {
                        case Boolean:
                            typedData.setType(ParameterValue.BOOLEAN);
                            break;
                        case String:
                            typedData.setType(ParameterValue.STRING);
                            break;
                        case Integer:
                        case Long:
                            typedData.setType(ParameterValue.INTEGER);
                            break;
                        case Float:
                        case Double:
                            typedData.setType(ParameterValue.NUMBER);
                            break;
                    }

                    StateWrapper stateWrapper = new StateWrapper(stateKey, stateName, typedData);
                    statesWrappers.add(stateWrapper);

                    if (componentsWrapper == null) {
                        continue;
                    }

                    for (ComponentWrapper component : componentsWrapper) {
                        if (component.getTraitName().equalsIgnoreCase(stateKey)) {
                            component.setComponentName(componentName)
                                    .getStates().add(stateWrapper);
                            for (CommandWrapper command : component.getCommands()) {
                                if (command.getTraitName().equalsIgnoreCase(stateKey)) {
                                    command.setComponentName(componentName);
                                    for (CommandParameterWrapper param : command.getParameters()) {
                                        if (param.getParameterName().equalsIgnoreCase(stateName)) {
                                            param.setValue(typedData);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return statesWrappers;
    }

    private enum Type {
        Boolean,
        Integer,
        Float,
        String,
        Long,
        Double
    }
}
