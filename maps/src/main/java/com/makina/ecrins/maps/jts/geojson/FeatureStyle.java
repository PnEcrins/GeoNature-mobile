package com.makina.ecrins.maps.jts.geojson;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;

import com.makina.ecrins.maps.R;

/**
 * Defines styles to be applied to {@link Feature} instances.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 * @see <a href="http://leafletjs.com/reference.html#path">http://leafletjs.com/reference.html#path</a>
 */
public class FeatureStyle
        implements Parcelable {

    private boolean stroke;
    private int colorResourceId;
    private String color;
    private int weight;
    private double opacity;
    private boolean fill;
    private int fillColorResourceId;
    private String fillColor;
    private double fillOpacity;

    private FeatureStyle(boolean stroke,
                         int colorResourceId,
                         String color,
                         int weight,
                         double opacity,
                         boolean fill,
                         int fillColorResourceId,
                         String fillColor,
                         double fillOpacity) {
        this.stroke = stroke;
        this.colorResourceId = colorResourceId;
        this.color = color;
        this.weight = weight;
        this.opacity = opacity;
        this.fill = fill;
        this.fillColorResourceId = fillColorResourceId;
        this.fillColor = fillColor;
        this.fillOpacity = fillOpacity;
    }

    protected FeatureStyle(Parcel source) {
        stroke = (source.readByte() == 1);
        colorResourceId = source.readInt();
        color = source.readString();
        weight = source.readInt();
        opacity = source.readDouble();
        fill = (source.readByte() == 1);
        fillColorResourceId = source.readInt();
        fillColor = source.readString();
        fillOpacity = source.readDouble();
    }

    public boolean isStroke() {
        return stroke;
    }

    @ColorRes
    public int getColorResourceId() {
        return colorResourceId;
    }

    public int getWeight() {
        return weight;
    }

    public double getOpacity() {
        return opacity;
    }

    public boolean isFill() {
        return fill;
    }

    @ColorRes
    public int getFillColorResourceId() {
        return fillColorResourceId;
    }

    public double getFillOpacity() {
        return fillOpacity;
    }

    /**
     * Represents this {@link FeatureStyle} as a compact JSON object string.
     *
     * @return a compact JSON representation of this {@link FeatureStyle}
     */
    @Override
    public String toString() {
        return "{" +
                "\"stroke\":" + stroke +
                ",\"color\":\"" + color +
                "\",\"weight\":" + weight +
                ",\"opacity\":" + opacity +
                ",\"fill\":" + fill +
                ",\"fillColor\":\"" + fillColor +
                "\",\"fillOpacity\":" + fillOpacity +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest,
                              int flags) {
        dest.writeByte((byte) (stroke ? 1 : 0)); // as boolean value
        dest.writeInt(colorResourceId);
        dest.writeString(color);
        dest.writeInt(weight);
        dest.writeDouble(opacity);
        dest.writeByte((byte) (fill ? 1 : 0)); // as boolean value
        dest.writeInt(fillColorResourceId);
        dest.writeString(fillColor);
        dest.writeDouble(fillOpacity);
    }

    /**
     * Builder implementation used to create new {@link FeatureStyle}.
     *
     * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
     */
    public static final class Builder {

        private final Context context;
        private boolean stroke;
        private int colorResourceId;
        private int weight;
        private double opacity;
        private boolean fill;
        private int fillColorResourceId;
        private double fillOpacity;

        private Builder(@NonNull final Context context) {
            this.context = context;
            this.stroke = true;
            this.colorResourceId = R.color.feature_dark_blue;
            this.weight = 5;
            this.opacity = 0.5;
            this.fill = true;
            this.fillColorResourceId = colorResourceId;
            this.fillOpacity = 0.2;
        }

        @NonNull
        public static Builder newInstance(@NonNull final Context context) {
            return new Builder(context);
        }

        /**
         * Whether to draw stroke along the path.
         * Set it to {@code false} to disable borders on geometries.
         *
         * @param stroke enable stroke or not (default: true)
         *
         * @return Fluent interface
         */
        @NonNull
        public Builder setStroke(boolean stroke) {
            this.stroke = stroke;

            return this;
        }

        /**
         * Stroke color.
         *
         * @param colorResourceId the color resource ID to use (default: '#03f')
         *
         * @return Fluent interface
         */
        @NonNull
        public Builder setColorResourceId(@ColorRes int colorResourceId) {
            this.colorResourceId = colorResourceId;

            return this;
        }

        /**
         * Stroke width in pixels.
         *
         * @param weight the stroke width to set (default: 5)
         *
         * @return Fluent interface
         */
        @NonNull
        public Builder setWeight(int weight) {
            this.weight = weight;

            return this;
        }

        /**
         * Stroke opacity.
         *
         * @param opacity the stoke opacity to set (default: 0.5)
         *
         * @return Fluent interface
         */
        @NonNull
        public Builder setOpacity(double opacity) {
            this.opacity = opacity;

            return this;
        }

        /**
         * Whether to fill the path with color.
         * Set it to {@code false} to disable filling on geometries.
         *
         * @param fill enable fill or not (default: true)
         *
         * @return Fluent interface
         */
        @NonNull
        public Builder setFill(boolean fill) {
            this.fill = fill;

            return this;
        }

        /**
         * Fill color.
         *
         * @param fillColorResourceId the color resource ID to use (default: same as stroke color)
         *
         * @return Fluent interface
         */
        @NonNull
        public Builder setFillColorResourceId(@ColorRes int fillColorResourceId) {
            this.fillColorResourceId = fillColorResourceId;

            return this;
        }

        /**
         * Fill opacity.
         *
         * @param fillOpacity the fill opacity to set (default: 0.5)
         *
         * @return Fluent interface
         */
        @NonNull
        public Builder setFillOpacity(double fillOpacity) {
            this.fillOpacity = fillOpacity;
            return this;
        }

        /**
         * Builds a new instance of {@link FeatureStyle}.
         *
         * @return new instance of {@link FeatureStyle}
         */
        @NonNull
        public FeatureStyle build() {
            return new FeatureStyle(stroke,
                                    colorResourceId,
                                    context.getString(colorResourceId),
                                    weight,
                                    opacity,
                                    fill,
                                    fillColorResourceId,
                                    context.getString(colorResourceId),
                                    fillOpacity);
        }
    }

    public static final Creator<FeatureStyle> CREATOR = new Creator<FeatureStyle>() {
        @Override
        public FeatureStyle createFromParcel(Parcel in) {
            return new FeatureStyle(in);
        }

        @Override
        public FeatureStyle[] newArray(int size) {
            return new FeatureStyle[size];
        }
    };
}
