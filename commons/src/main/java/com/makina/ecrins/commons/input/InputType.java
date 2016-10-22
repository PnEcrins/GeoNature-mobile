package com.makina.ecrins.commons.input;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Describes {@link AbstractInput} type.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public enum InputType {

    @SerializedName("fauna")
    FAUNA(128,
          "fauna"),
    @SerializedName("mortality")
    MORTALITY(64,
              "mortality"),
    @SerializedName("invertebrate")
    INVERTEBRATE(32,
                 "invertebrate"),
    @SerializedName("flora")
    FLORA(16,
          "flora");

    private final int key;
    private final String value;

    InputType(
            int key,
            String value) {

        this.key = key;
        this.value = value;
    }

    public int getKey() {

        return this.key;
    }

    public String getValue() {

        return this.value;
    }

    @Nullable
    public static InputType fromValue(@Nullable final String value) {
        if (TextUtils.isEmpty(value)) {
            return null;
        }

        for (InputType inputType : values()) {
            if (inputType.getValue()
                           .equals(value)) {
                return inputType;
            }
        }

        return null;
    }
}