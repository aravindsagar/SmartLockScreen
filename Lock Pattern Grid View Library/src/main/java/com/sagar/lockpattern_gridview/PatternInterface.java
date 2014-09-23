package com.sagar.lockpattern_gridview;

import java.util.List;

/**
 * Created by aravind on 20/9/14.
 * An interface which specifies the functionality which will be available in PatternGridView
 */
public interface PatternInterface {
    /**
     * Clear the pattern; the path will be reset if this function is called
     */
    public void clearPattern();

    /**
     * Get the last pattern entered in the View.
     * @return the last pattern; It is returned as an ordered list of numbers,
     *         each number represents a particular cell in the pattern grid
     */
    public List<Integer> getPattern();

    /**
     * Sets the color of the ring around the dots in the path entered. This color is applied only
     * when the pattern has been set. While the user is still drawing, the color shown will depend
     * upon the theme set in the layout xml file or using {@link com.sagar.lockpattern_gridview.PatternInterface#setPatternType}
     * This color is also applied to the indicator arrows whenever it is applied to the rings
     * @param color The color to be set
     */
    public void setRingColor(int color);

    /**
     * Set the callbacks for different events such as {@link com.sagar.lockpattern_gridview.PatternGridView.PatternState#IN_PROGRESS},
     * {@link com.sagar.lockpattern_gridview.PatternGridView.PatternState#ENTERED}, {@link com.sagar.lockpattern_gridview.PatternGridView.PatternState#BLANK}
     * @param listener An instance of a class which implements {@link com.sagar.lockpattern_gridview.PatternInterface.PatternListener}
     */
    public void setPatternListener(PatternListener listener);

    /**
     * Returns whether touch input is enabled for the view
     * @return true, if the view is accepting touches, false, if the view is ignoring them
     */
    public boolean isInputEnabled();

    /**
     * Sets whether the view should process or ignore the touches
     * @param mIsInputEnabled value to be set
     */
    public void setInputEnabled(boolean mIsInputEnabled);

    /**
     * Returns the patternType. Pattern type is basically a "theme" for the view. 2 types are supported as
     * of now, {@link com.sagar.lockpattern_gridview.PatternGridView#PATTERN_TYPE_CHECK}, which
     * is suitable to be used over a dark background, and {@link com.sagar.lockpattern_gridview.PatternGridView#PATTERN_TYPE_STORE},
     * which is suitable to be drawn over a light background.
     * <p/>
     * The names come from the colors used in setting and getting pattern in Android L(preview) ;)
     *
     * @return the current pattern type
     */
    public int getPatternType();

    /**
     * Set the current pattern type. Pattern type is basically a "theme" for the view. 2 types are supported as
     * of now, {@link com.sagar.lockpattern_gridview.PatternGridView#PATTERN_TYPE_CHECK}, which
     * is suitable to be used over a dark background, and {@link com.sagar.lockpattern_gridview.PatternGridView#PATTERN_TYPE_STORE},
     * which is suitable to be drawn over a light background.
     * <p/>
     * The names come from the colors used in setting and getting pattern in Android L(preview) ;)
     * @param mPatternType new type to be set.
     */
    public void setPatternType(int mPatternType);

    /**
     * Interface to be implemented, to receive callbacks for pattern events.
     */
    public interface PatternListener{
        /**
         * Callback when user has started entering a pattern.
         */
        public void onPatternStarted();

        /**
         * Callback when a pattern has been entered into the view
         * @param pattern the pattern which was entered
         */
        public void onPatternEntered(List<Integer> pattern);

        /**
         * Callback when pattern is cleared. This can happen if the user touches an empty space
         * when input is enabled, or when its cleared using {@link PatternInterface#clearPattern()}
         */
        public void onPatternCleared();
    }
}
