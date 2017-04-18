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

package com.moto.miletus.gson.traits;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Parameter
 */
public class Parameter {

    @SerializedName("additionalProperties")
    private boolean hasAdditionalProperties;

    @SerializedName("maxLength")
    private Integer maxLength;

    @SerializedName("maximum")
    private Object maximum;

    @SerializedName("minLength")
    private Integer minLength;

    @SerializedName("minimum")
    private Object minimum;

    @SerializedName("required")
    private List<String> requiredProperties;

    @SerializedName("type")
    private String type;

    @SerializedName("enum")
    private List<String> values;

    /**
     * isHasAdditionalProperties
     *
     * @return boolean
     */
    public boolean isHasAdditionalProperties() {
        return hasAdditionalProperties;
    }

    /**
     * setHasAdditionalProperties
     *
     * @param hasAdditionalProperties boolean
     */
    public void setHasAdditionalProperties(boolean hasAdditionalProperties) {
        this.hasAdditionalProperties = hasAdditionalProperties;
    }

    /**
     * getMaxLength
     *
     * @return Integer
     */
    public Integer getMaxLength() {
        return maxLength;
    }

    /**
     * setMaxLength
     *
     * @param maxLength Integer
     */
    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * getMaximum
     *
     * @return Object
     */
    public Object getMaximum() {
        return maximum;
    }

    /**
     * setMaximum
     *
     * @param maximum Object
     */
    public void setMaximum(Object maximum) {
        this.maximum = maximum;
    }

    /**
     * getMinLength
     *
     * @return Integer
     */
    public Integer getMinLength() {
        return minLength;
    }

    /**
     * setMinLength
     *
     * @param minLength Integer
     */
    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    /**
     * getMinimum
     *
     * @return Object
     */
    public Object getMinimum() {
        return minimum;
    }

    /**
     * setMinimum
     *
     * @param minimum Object
     */
    public void setMinimum(Object minimum) {
        this.minimum = minimum;
    }

    /**
     * getRequiredProperties
     *
     * @return List
     */
    public List<String> getRequiredProperties() {
        return requiredProperties;
    }

    /**
     * setRequiredProperties
     *
     * @param requiredProperties List
     */
    public void setRequiredProperties(List<String> requiredProperties) {
        this.requiredProperties = requiredProperties;
    }

    /**
     * getType
     *
     * @return String
     */
    public String getType() {
        return type;
    }

    /**
     * setType
     *
     * @param type String
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * getValues
     *
     * @return List
     */
    public List<String> getValues() {
        return values;
    }

    /**
     * setValues
     *
     * @param values List
     */
    public void setValues(List<String> values) {
        this.values = values;
    }
}
