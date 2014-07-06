package com.makina.ecrins.maps.util;

/**
 * This class implements a low-pass filter.
 * <p/>
 * A low-pass filter is an electronic filter that passes low-frequency signals but attenuates
 * (reduces the amplitude of) signals with frequencies higher than the cutoff frequency.
 * The actual amount of attenuation for each frequency varies from filter to filter.
 * It is sometimes called a high-cut filter, or treble cut filter when used in audio applications.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 * @see <a href="http://blog.thomnichols.org/2011/08/smoothing-sensor-data-with-a-low-pass-filter">http://blog.thomnichols.org/2011/08/smoothing-sensor-data-with-a-low-pass-filter</a>
 */
public final class LowPassSensorValuesFilter {

    /**
     * time smoothing constant for low-pass filter
     * 0 <= alpha <= 1 ; a smaller value basically means more smoothing
     * See: http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
     */
    private static final float ALPHA = 0.15f;

    /**
     * {@link LowPassSensorValuesFilter} instances should NOT be constructed in standard programming.
     */
    private LowPassSensorValuesFilter() {

    }

    /**
     * Filter the given input alues against the previous values and return a low-pass filtered result.
     *
     * @param input  float array to smooth.
     * @param output float array representing the previous values.
     * @see <a href="http://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation">http://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation</a>
     * @see <a href="http://developer.android.com/reference/android/hardware/SensorEvent.html#values">http://developer.android.com/reference/android/hardware/SensorEvent.html#values</a>
     */
    public static float[] lowPass(float[] input, float[] output) {
        if (output == null) {
            return input;
        }

        if (input.length != output.length) {
            throw new IllegalArgumentException("input and output values must be the same length");
        }

        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }

        return output;
    }
}
