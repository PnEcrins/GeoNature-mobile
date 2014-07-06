package com.makina.ecrins.commons.input;

/**
 * Describes {@link AbstractInput} type.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public enum InputType {

    FAUNA(128, "fauna"),
    MORTALITY(64, "mortality"),
    INVERTEBRATE(32, "invertebrate"),
    FLORA(16, "flora");

    private final int key;
    private final String value;

    InputType(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public int getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }
}