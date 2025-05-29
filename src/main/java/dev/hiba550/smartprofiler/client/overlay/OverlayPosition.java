package dev.hiba550.smartprofiler.client.overlay;

/**
 * Defines screen positions for the performance overlay
 * 
 * Provides predefined positions that work well with different screen sizes
 * and don't interfere with common HUD elements
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
public enum OverlayPosition {
    /**
     * Top-left corner (default)
     * Safe position that doesn't conflict with most HUD mods
     */
    TOP_LEFT("Top Left", "Safe position in top-left corner"),
    
    /**
     * Top-right corner
     * Good alternative when left side is crowded
     */
    TOP_RIGHT("Top Right", "Alternative position in top-right corner"),
    
    /**
     * Bottom-left corner
     * Useful when top positions conflict with other overlays
     */
    BOTTOM_LEFT("Bottom Left", "Lower position avoiding top HUD elements"),
    
    /**
     * Bottom-right corner
     * Good for minimalist setups
     */
    BOTTOM_RIGHT("Bottom Right", "Bottom-right corner position"),
    
    /**
     * Center of screen
     * For debugging or temporary analysis
     */
    CENTER("Center", "Center screen position for temporary analysis");
    
    private final String displayName;
    private final String description;
    
    OverlayPosition(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Gets the next position in the cycle
     */
    public OverlayPosition getNext() {
        OverlayPosition[] positions = values();
        int currentIndex = this.ordinal();
        return positions[(currentIndex + 1) % positions.length];
    }
    
    /**
     * Gets the previous position in the cycle
     */
    public OverlayPosition getPrevious() {
        OverlayPosition[] positions = values();
        int currentIndex = this.ordinal();
        return positions[(currentIndex - 1 + positions.length) % positions.length];
    }
    
    /**
     * Checks if this position is on the left side of the screen
     */
    public boolean isLeftSide() {
        return this == TOP_LEFT || this == BOTTOM_LEFT;
    }
    
    /**
     * Checks if this position is on the right side of the screen
     */
    public boolean isRightSide() {
        return this == TOP_RIGHT || this == BOTTOM_RIGHT;
    }
    
    /**
     * Checks if this position is at the top of the screen
     */
    public boolean isTopSide() {
        return this == TOP_LEFT || this == TOP_RIGHT;
    }
    
    /**
     * Checks if this position is at the bottom of the screen
     */
    public boolean isBottomSide() {
        return this == BOTTOM_LEFT || this == BOTTOM_RIGHT;
    }
}