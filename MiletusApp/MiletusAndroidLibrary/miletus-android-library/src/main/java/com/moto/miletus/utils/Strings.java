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

package com.moto.miletus.utils;

/**
 * Strings
 */
public final class Strings {

    private Strings() {
    }

    public static final String NEW_LINE = System.getProperty("line.separator");

    public static final String HTTP = "http://";

    public static final String PORT = ":";

    public static final String COMMANDS_EXECUTE = "/commands/execute";

    public static final String INFO = "/info";

    public static final String TRAITS = "/traits";

    public static final String COMPONENTS = "/components";

    public static final String mSearchName = "_miletus";

    public static final String mSearchNameBle = ".mi";

    public static final String OFFSET = "offset=";

    public static final String INFO_BLE = INFO + "?" + OFFSET;

    public static final String TRAITS_BLE = TRAITS + "?"  + OFFSET;

    public static final String COMPONENTS_BLE = COMPONENTS + "?" + OFFSET;

    public static final String EXECUTE_COMMAND_JSON_PREFIX = COMMANDS_EXECUTE + "?application=json&value=";

    public static final String BYTE = "B";

    public static final String LENGTH = "length=";
}
