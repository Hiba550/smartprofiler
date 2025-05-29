package dev.hiba550.smartprofiler.util;

import net.minecraft.util.math.MathHelper;

/**
 * Utility class for color manipulation and calculations
 * Provides efficient color blending, alpha manipulation, and interpolation
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
public class ColorUtils {
    
    /**
     * Multiplies the alpha channel of a color by the specified factor
     * 
     * @param color The original ARGB color
     * @param alphaFactor Factor to multiply alpha by (0.0 to 1.0)
     * @return Color with modified alpha channel
     */
    public static int multiplyAlpha(int color, float alphaFactor) {
        int alpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        
        int newAlpha = (int)(alpha * MathHelper.clamp(alphaFactor, 0.0f, 1.0f));
        
        return (newAlpha << 24) | (red << 16) | (green << 8) | blue;
    }
    
    /**
     * Interpolates between two colors
     * 
     * @param color1 First color (ARGB)
     * @param color2 Second color (ARGB)
     * @param factor Interpolation factor (0.0 = color1, 1.0 = color2)
     * @return Interpolated color
     */
    public static int lerpColor(int color1, int color2, float factor) {
        factor = MathHelper.clamp(factor, 0.0f, 1.0f);
        
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        
        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        
        int a = (int)(a1 + (a2 - a1) * factor);
        int r = (int)(r1 + (r2 - r1) * factor);
        int g = (int)(g1 + (g2 - g1) * factor);
        int b = (int)(b1 + (b2 - b1) * factor);
        
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    
    /**
     * Creates a color from HSV values
     * 
     * @param hue Hue (0.0 to 360.0)
     * @param saturation Saturation (0.0 to 1.0)
     * @param value Value/Brightness (0.0 to 1.0)
     * @param alpha Alpha (0.0 to 1.0)
     * @return ARGB color
     */
    public static int fromHSV(float hue, float saturation, float value, float alpha) {
        hue = hue % 360.0f;
        saturation = MathHelper.clamp(saturation, 0.0f, 1.0f);
        value = MathHelper.clamp(value, 0.0f, 1.0f);
        alpha = MathHelper.clamp(alpha, 0.0f, 1.0f);
        
        float c = value * saturation;
        float x = c * (1 - Math.abs((hue / 60.0f) % 2 - 1));
        float m = value - c;
        
        float r, g, b;
        
        if (hue < 60) {
            r = c; g = x; b = 0;
        } else if (hue < 120) {
            r = x; g = c; b = 0;
        } else if (hue < 180) {
            r = 0; g = c; b = x;
        } else if (hue < 240) {
            r = 0; g = x; b = c;
        } else if (hue < 300) {
            r = x; g = 0; b = c;
        } else {
            r = c; g = 0; b = x;
        }
        
        int red = (int)((r + m) * 255);
        int green = (int)((g + m) * 255);
        int blue = (int)((b + m) * 255);
        int alphaInt = (int)(alpha * 255);
        
        return (alphaInt << 24) | (red << 16) | (green << 8) | blue;
    }
    
    /**
     * Gets a performance color based on a normalized value
     * 
     * @param normalizedValue Value from 0.0 (best) to 1.0 (worst)
     * @return Color representing performance level
     */
    public static int getPerformanceColor(float normalizedValue) {
        normalizedValue = MathHelper.clamp(normalizedValue, 0.0f, 1.0f);
        
        // Green to Red gradient based on performance
        // Green (good) -> Yellow (warning) -> Red (critical)
        if (normalizedValue <= 0.5f) {
            // Green to Yellow
            return lerpColor(0xFF40FF40, 0xFFFFFF40, normalizedValue * 2.0f);
        } else {
            // Yellow to Red
            return lerpColor(0xFFFFFF40, 0xFFFF4040, (normalizedValue - 0.5f) * 2.0f);
        }
    }
    
    /**
     * Applies a pulsing effect to a color
     * 
     * @param baseColor Base color to pulse
     * @param pulseTime Current pulse time (continuously increasing)
     * @param pulseSpeed Speed of the pulse effect
     * @param pulseIntensity Intensity of the pulse (0.0 to 1.0)
     * @return Pulsing color
     */
    public static int applyPulse(int baseColor, float pulseTime, float pulseSpeed, float pulseIntensity) {
        float pulse = 0.5f + 0.5f * (float)Math.sin(pulseTime * pulseSpeed);
        float alpha = 1.0f - (pulseIntensity * (1.0f - pulse));
        return multiplyAlpha(baseColor, alpha);
    }
}