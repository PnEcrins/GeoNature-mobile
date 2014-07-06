package com.makina.ecrins.maps.control;

/**
 * Utility class for {@link IControl} instances.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class ControlUtils {
    /**
     * {@link ControlUtils} instances should NOT be constructed in standard programming.
     */
    private ControlUtils() {

    }

    /**
     * Builds the {@link IControl} name.
     *
     * @param control the {@link IControl} instance on which to build its given name
     * @return the generated {@link IControl} name.
     * @see #getControlName(Class)
     * @see IControl#getName()
     */
    public static String getControlName(IControl control) {
        return getControlName(control.getClass());
    }

    /**
     * Builds the {@link IControl} name.
     *
     * @param controlClass {@link IControl} class on which to build its given name
     * @return the generated {@link IControl} name.
     * @see #getControlName(Class)
     * @see IControl#getName()
     */
    public static String getControlName(Class<? extends IControl> controlClass) {
        return controlClass.getSimpleName() + "Handler";
    }
}
