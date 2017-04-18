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

import com.google.gson.Gson;
import com.moto.miletus.gson.traits.Command;
import com.moto.miletus.gson.traits.Parameter;
import com.moto.miletus.gson.traits.TraitDef;
import com.moto.miletus.gson.traits.Traits;
import com.moto.miletus.wrappers.CommandParameterWrapper;
import com.moto.miletus.wrappers.CommandWrapper;
import com.moto.miletus.wrappers.ComponentWrapper;
import com.moto.miletus.wrappers.ParameterValue;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TraitsHelper {

    private static final String TAG = TraitsHelper.class.getSimpleName();

    private TraitsHelper() {
    }

    /**
     * Json 2 Traits
     *
     * @param json String
     * @return Traits
     * @throws JSONException JSONException
     */
    public static Traits jsonToTraits(final String json) throws JSONException {
        Log.i(TAG, json);

        new JSONObject(json);

        return new Gson().fromJson(json, Traits.class);
    }

    /**
     * Traits 2 ComponentWrapper
     *
     * @param traits Traits
     * @return Set<ComponentWrapper>
     * @throws JSONException JSONException
     */
    public static Set<ComponentWrapper> traitsToComponentCommands(final Traits traits) throws JSONException {
        if (traits == null) {
            throw new JSONException("Traits null");
        }

        final Set<ComponentWrapper> components = new LinkedHashSet<>();
        for (Map.Entry<String, TraitDef> traitDefEntry : traits.getTraits().entrySet()) {
            ComponentWrapper component = new ComponentWrapper();
            List<CommandWrapper> commands = new ArrayList<>();

            if (traitDefEntry.getValue().getCommands() != null) {
                for (Map.Entry<String, Command> commandEntry : traitDefEntry.getValue().getCommands().entrySet()) {
                    CommandWrapper command = new CommandWrapper();
                    command
                            .setTraitName(traitDefEntry.getKey())
                            .setCommandName(commandEntry.getKey());
                    commands.add(command);

                    if (commandEntry.getValue().getParameters() == null) {
                        continue;
                    }

                    for (Map.Entry<String, Parameter> parameterEntry : commandEntry.getValue().getParameters().entrySet()) {
                        ParameterValue commandParameter = new ParameterValue(parameterEntry.getValue().getType());
                        commandParameter.setValues(parameterEntry.getValue().getValues());
                        CommandParameterWrapper commandParameterWrapper = new CommandParameterWrapper(
                                parameterEntry.getKey(),
                                commandParameter);
                        command.getParameters().add(commandParameterWrapper);
                    }
                }
            }

            component
                    .setTraitName(traitDefEntry.getKey())
                    .setCommands(commands);
            components.add(component);
        }

        return components;
    }
}
