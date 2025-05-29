package dev.hiba550.smartprofiler.util;

/**
 * Mathematical utilities for performance analysis and rendering
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
public class MathUtils {
    
    /**
     * Calculates a smoothed average from an array of values
     * Uses exponential weighted moving average for smooth transitions
     * 
     * @param values Array of values
     * @param smoothingFactor Factor for smoothing (0.0 to 1.0)
     * @return Smoothed average
     */
    public static float calculateSmoothedAverage(float[] values, float smoothingFactor) {
        if (values.length == 0) return 0.0f;
        
        float result = values[0];
        for (int i = 1; i < values.length; i++) {
            result = result * (1.0f - smoothingFactor) + values[i] * smoothingFactor;
        }
        return result;
    }
    
    /**
     * Calculates the 95th percentile of an array of values
     * Useful for performance analysis to ignore outliers
     * 
     * @param values Array of values
     * @return 95th percentile value
     */
    public static float calculate95thPercentile(float[] values) {
        if (values.length == 0) return 0.0f;
        
        float[] sorted = values.clone();
        java.util.Arrays.sort(sorted);
        
        int index = (int)(sorted.length * 0.95f);
        return sorted[Math.min(index, sorted.length - 1)];
    }
    
    /**
     * Normalizes a value to a 0-1 range
     * 
     * @param value Input value
     * @param min Minimum expected value
     * @param max Maximum expected value
     * @return Normalized value (0.0 to 1.0)
     */
    public static float normalize(float value, float min, float max) {
        if (max <= min) return 0.0f;
        return Math.max(0.0f, Math.min(1.0f, (value - min) / (max - min)));
    }
}