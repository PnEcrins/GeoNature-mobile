package com.makina.ecrins.maps.content;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Describes a {@link ITilesLayerDataSource} metadata.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class Metadata {

    protected String name;
    protected String type;
    protected double version;
    protected String description;
    protected String format;

    protected Metadata(@NonNull final String name) {
        this.name = name;
        this.type = "baselayer";
        this.format = "png";
    }

    /**
     * The plain-English name of the tileset.
     *
     * @return the name of the tileset
     */
    @NonNull
    public String getName() {
        return name;
    }

    /**
     * The tileset type ('overlay' or 'baselayer', default 'baselayer').
     *
     * @return the tileset type
     */
    @NonNull
    public String getType() {
        return TextUtils.isEmpty(type) ? "baselayer" : type;
    }

    /**
     * The version of the tileset, as a plain number.
     *
     * @return the version of the tileset
     */
    public double getVersion() {
        return version;
    }

    /**
     * A description of the layer as plain text.
     *
     * @return description of the layer
     */
    @Nullable
    public String getDescription() {
        return description;
    }

    /**
     * The image file format of the tile data (png as default format or jpg)
     *
     * @return the image file format of the tile data
     */
    @NonNull
    public String getFormat() {
        return TextUtils.isEmpty(format) ? "png" : format;
    }

    /**
     * Returns a {@code JSON} representation of this {@link Metadata}.
     *
     * @return a {@code JSON} representation
     */
    @Override
    public String toString() {
        return "{" +
                "\"name\":" + '\"' + name + '\"' +
                ",\"type\":" + '\"' + type + '\"' +
                ",\"version\":" + version +
                ",\"description\":" + (TextUtils.isEmpty(description) ? "null" : '\"' + description) + '\"' +
                ",\"format\":" + '\"' + format + '\"' +
                '}';
    }
}
