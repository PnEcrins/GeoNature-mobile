package com.makina.ecrins.maps;

/**
 * Define all available rendering qualities.
 * <p>
 * The default rendering quality used is {@link RenderQualityEnum#AUTO}.
 * </p>
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public enum RenderQualityEnum {

    /**
     * Default rendering quality : 'device-dpi'
     */
    AUTO(0, "device-dpi"),

    /**
     * Low rendering quality : 'low-dpi'
     */
    LOW(1, "low-dpi"),

    /**
     * Medium rendering quality : 'medium-dpi'
     */
    MEDIUM(2, "medium-dpi"),

    /**
     * High rendering quality : 'high-dpi'
     */
    HIGH(3, "high-dpi");

    private final int value;
    private final String valueAsString;

    private RenderQualityEnum(int value, String valueAsString) {
        this.value = value;
        this.valueAsString = valueAsString;
    }

    public int getValue() {
        return value;
    }

    public String getValueAsString() {
        return valueAsString;
    }

    public static RenderQualityEnum asRenderQuality(int value) {
        for (RenderQualityEnum renderQuality : values()) {
            int valueToCompare = renderQuality.getValue();

            if (valueToCompare == value) {
                return renderQuality;
            }
        }

        return RenderQualityEnum.AUTO;
    }

    public static RenderQualityEnum asRenderQuality(String valueAsString) {
        for (RenderQualityEnum renderQuality : values()) {
            String valueAsStringToCompare = renderQuality.getValueAsString();

            if (valueAsStringToCompare.equalsIgnoreCase(valueAsString)) {
                return renderQuality;
            }
        }

        return RenderQualityEnum.AUTO;
    }
}
