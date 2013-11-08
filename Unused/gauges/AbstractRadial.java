package eu.hansolo.steelseries.gauges;

/**
 *
 * @author hansolo
 */
public abstract class AbstractRadial extends AbstractGauge implements java.awt.event.ActionListener
{
    protected static final eu.hansolo.steelseries.tools.Util UTIL = eu.hansolo.steelseries.tools.Util.INSTANCE;
    protected static final float ANGLE_CONST = 1f / 360f;
    // Tickmark related
    private double minValue = 0;
    private double maxValue = 100;
    private boolean drawTicks = true;
    private boolean drawTickLabels = true;
    private boolean autoResetToZero = false;
    private int scaleDividerPower = 0;
    private boolean tickmarkColorFromTheme = true;
    private java.awt.Color tickmarkColor = java.awt.Color.WHITE;
    private boolean tickmarkSectionsVisible = false;
    private final java.util.ArrayList<eu.hansolo.steelseries.tools.Section> TICKMARK_SECTIONS = new java.util.ArrayList<eu.hansolo.steelseries.tools.Section>();
    private boolean useCustomTickmarkLabels = false;
    private final java.util.ArrayList<Double> CUSTOM_TICKMARK_LABELS = new java.util.ArrayList<Double>();
    // Threshold
    private double threshold = maxValue;
    private boolean thresholdVisible = false;
    // Limits
    private double minMeasuredValue = maxValue;
    private boolean minMeasuredValueVisible = false;
    private double maxMeasuredValue = minValue;
    private boolean maxMeasuredValueVisible = false;
    // Track related
    private boolean trackVisible = false;
    private double trackStart = minValue;
    private double trackSection = (maxValue - minValue) / 2;
    private double trackStop = maxValue;
    private java.awt.Color trackStartColor = new java.awt.Color(0.0f, 1.0f, 0.0f, 0.5f);
    private java.awt.Color trackSectionColor = new java.awt.Color(1.0f, 1.0f, 0.0f, 0.5f);
    private java.awt.Color trackStopColor = new java.awt.Color(1.0f, 0.0f, 0.0f, 0.5f);
    // Area related
    private boolean areaVisible = false;
    private double areaStart = minValue;
    private double areaStop = maxValue;
    private java.awt.Color areaColor = new java.awt.Color(1.0f, 0.0f, 0.0f, 0.3f);
    // Section related
    private boolean sectionsVisible = false;
    private final java.util.ArrayList<eu.hansolo.steelseries.tools.Section> SECTIONS = new java.util.ArrayList<eu.hansolo.steelseries.tools.Section>();
    // Custom layer
    private boolean customLayerVisible = false;
    private java.awt.image.BufferedImage customLayer = null;
    // Background
    private boolean customBackgroundVisible = false;
    private java.awt.Paint customBackground = java.awt.Color.BLACK;
    private eu.hansolo.steelseries.tools.BackgroundColor backgroundColor = eu.hansolo.steelseries.tools.BackgroundColor.DARK_GRAY;
    // Frame
    private eu.hansolo.steelseries.tools.FrameDesign frameDesign = eu.hansolo.steelseries.tools.FrameDesign.METAL;
    // Pointer related variables
    private eu.hansolo.steelseries.tools.PointerType pointerType = eu.hansolo.steelseries.tools.PointerType.TYPE1;
    private eu.hansolo.steelseries.tools.PointerColor pointerColor = eu.hansolo.steelseries.tools.PointerColor.RED;
    // Threshold LED related variables
    private boolean ledVisible = true;
    private double ledPositionX = 0.6;
    private double ledPositionY = 0.4;
    private eu.hansolo.steelseries.tools.LedColor ledColor = eu.hansolo.steelseries.tools.LedColor.RED_LED;
    private java.awt.image.BufferedImage ledImageOff = create_LED_Image(200, 0, ledColor);
    private java.awt.image.BufferedImage ledImageOn = create_LED_Image(200, 1, ledColor);
    private java.awt.image.BufferedImage currentLedImage;
    private final javax.swing.Timer LED_BLINKING_TIMER = new javax.swing.Timer(500, this);
    private boolean ledBlinking = false;
    private boolean ledOn;
    // Animation related variables
    private final org.pushingpixels.trident.Timeline TIMELINE = new org.pushingpixels.trident.Timeline(this);
    private final org.pushingpixels.trident.ease.TimelineEase STANDARD_EASING = new org.pushingpixels.trident.ease.Spline(0.5f);
    private final org.pushingpixels.trident.ease.TimelineEase RETURN_TO_ZERO_EASING = new org.pushingpixels.trident.ease.Sine();
    private final org.pushingpixels.trident.callback.TimelineCallback TIMELINE_CALLBACK = new org.pushingpixels.trident.callback.TimelineCallback()
    {
        @Override
        public void onTimelineStateChanged(org.pushingpixels.trident.Timeline.TimelineState oldState, org.pushingpixels.trident.Timeline.TimelineState newState, float oldValue, float newValue)
        {
            if (oldState == org.pushingpixels.trident.Timeline.TimelineState.PLAYING_FORWARD && newState == org.pushingpixels.trident.Timeline.TimelineState.DONE)
            {
                final double CURRENT_VALUE = getValue();

                // Check if current value exceeds maxMeasuredValue
                if (CURRENT_VALUE > maxMeasuredValue)
                {
                    maxMeasuredValue = CURRENT_VALUE;
                }

                // Check if current value exceeds minMeasuredValue
                if (CURRENT_VALUE < minMeasuredValue)
                {
                    minMeasuredValue = CURRENT_VALUE;
                }
            }

            if (newState == org.pushingpixels.trident.Timeline.TimelineState.IDLE)
            {
                repaint(getBounds());
            }
        }

        @Override
        public void onTimelinePulse(float oldValue, float newValue)
        {
            final double CURRENT_VALUE = getValue();

            // LEE blinking makes only sense when autoResetToZero == OFF
            if (!autoResetToZero)
            {
                // Check if current value exceeds threshold and activate led as indicator
                if (CURRENT_VALUE >= getThreshold())
                {
                    if (!LED_BLINKING_TIMER.isRunning())
                    {
                        LED_BLINKING_TIMER.start();
                        firePropertyChange(THRESHOLD_PROPERTY, false, true);
                    }
                }
                else
                {
                    LED_BLINKING_TIMER.stop();
                    setCurrentLedImage(getLedImageOff());
                    //firePropertyChange(THRESHOLD_PROPERTY, true, false);
                }
            }

            // Check if current value exceeds maxMeasuredValue
            if (CURRENT_VALUE > maxMeasuredValue)
            {
                maxMeasuredValue = CURRENT_VALUE;
            }

            // Check if current value exceeds minMeasuredValue
            if (CURRENT_VALUE < minMeasuredValue)
            {
                minMeasuredValue = CURRENT_VALUE;
            }

            repaint(getBounds());
        }
    };
    // Peak value related
    private boolean peakValueVisible = false;
    private final javax.swing.Timer PEAK_TIMER = new javax.swing.Timer(1000, this);

    public AbstractRadial()
    {
        super();
        addComponentListener(this);
        TIMELINE.addCallback(TIMELINE_CALLBACK);
    }

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    /**
     * Set the VALUE that will be used for drawing
     * of the bar.
     * @param value
     */
    @Override
    public void setValue(double value)
    {
        value = value > maxValue ? maxValue : value;
        value = value < minValue ? minValue : value;

        repaint();

        super.setValue(value);
    }

    /**
     * Uses trident animation library to animate
     * the setting of the value.
     * The method plays a defined trident timeline
     * that calls the setValue(double value) method
     * with a given easing behaviour and duration.
     * You should always use this method to set the
     * gauge to a given value.
     * @param VALUE
     */
    public void setValueAnimated(final double VALUE)
    {
        if (TIMELINE.getState() != org.pushingpixels.trident.Timeline.TimelineState.IDLE)
        {
            TIMELINE.abort();
        }

        final double OVERALL_RANGE = getMaxValue() - getMinValue();
        final double RANGE = Math.abs(getValue() - VALUE);
        final double FRACTION = RANGE / OVERALL_RANGE;

        if (autoResetToZero)
        {
            final org.pushingpixels.trident.TimelineScenario AUTOZERO_SCENARIO = new org.pushingpixels.trident.TimelineScenario.Sequence();

            final org.pushingpixels.trident.Timeline TIMELINE_TO_VALUE = new org.pushingpixels.trident.Timeline(this);
            TIMELINE_TO_VALUE.addPropertyToInterpolate("value", getValue(), VALUE);
            TIMELINE_TO_VALUE.setEase(RETURN_TO_ZERO_EASING);
            TIMELINE_TO_VALUE.setDuration((long) (800 * FRACTION));
            TIMELINE_TO_VALUE.addCallback(new org.pushingpixels.trident.callback.TimelineCallback()
            {
                @Override
                public void onTimelineStateChanged(org.pushingpixels.trident.Timeline.TimelineState oldState, org.pushingpixels.trident.Timeline.TimelineState newState, float oldValue, float newValue)
                {
                    if (oldState == org.pushingpixels.trident.Timeline.TimelineState.PLAYING_FORWARD && newState == org.pushingpixels.trident.Timeline.TimelineState.DONE)
                    {
                        // Set the peak value and start the timer
                        setPeakValue(getValue());
                        setPeakValueVisible(true);
                        if (PEAK_TIMER.isRunning())
                        {
                            PEAK_TIMER.stop();
                        }
                        PEAK_TIMER.start();

                        // Check if current value exceeds maxMeasuredValue
                        if (getValue() > maxMeasuredValue)
                        {
                            maxMeasuredValue = getValue();
                        }
                    }
                }

                @Override
                public void onTimelinePulse(float oldValue, float newValue)
                {
                    final double CURRENT_VALUE = getValue();

                    // Check if current value exceeds maxMeasuredValue
                    if (CURRENT_VALUE > maxMeasuredValue)
                    {
                        maxMeasuredValue = CURRENT_VALUE;
                    }

                    // Check if current value exceeds minMeasuredValue
                    if (CURRENT_VALUE < minMeasuredValue)
                    {
                        minMeasuredValue = CURRENT_VALUE;
                    }
                }
            });

            final org.pushingpixels.trident.Timeline TIMELINE_TO_ZERO = new org.pushingpixels.trident.Timeline(this);
            TIMELINE_TO_ZERO.addPropertyToInterpolate("value", VALUE, 0.0);
            TIMELINE_TO_ZERO.setEase(RETURN_TO_ZERO_EASING);
            TIMELINE_TO_ZERO.setDuration((long) (2000 * FRACTION));

            AUTOZERO_SCENARIO.addScenarioActor(TIMELINE_TO_VALUE);
            AUTOZERO_SCENARIO.addScenarioActor(TIMELINE_TO_ZERO);

            AUTOZERO_SCENARIO.addCallback(new org.pushingpixels.trident.callback.TimelineScenarioCallback()
            {
                @Override
                public void onTimelineScenarioDone()
                {
                }
            });

            AUTOZERO_SCENARIO.play();
        }
        else
        {
            TIMELINE.addPropertyToInterpolate("value", getValue(), VALUE);
            TIMELINE.setEase(STANDARD_EASING);
            TIMELINE.setDuration((long) (4500 * FRACTION));
            TIMELINE.play();
        }
    }

    /**
     * Returns the minimum value of the measurement
     * range of this gauge. This means the gauge could
     * not display values smaller than this value.
     * @return Double that represents the minimum value the cauge could display
     */
    public double getMinValue()
    {
        return this.minValue;
    }

    /**
     * Sets the minimum value of the measurement
     * range of this gauge. This value defines the
     * minimum value the gauge could display.
     * This has nothing to do with MinMeasuredValue
     * which represents the min. value that was
     * measured since the last reset of MinMeasuredValue
     * @param MIN_VALUE
     */
    public void setMinValue(final double MIN_VALUE)
    {
        this.minValue = MIN_VALUE;
        if (MIN_VALUE > 0)
        {
            setValue(MIN_VALUE);
        }
        checkSettings();
        calcAngleStep();
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns the maximum value of the measurement
     * range of this gauge. This means the gauge could
     * not display values larger than this value.
     * @return Double that represents the maximum value the gauge could display
     */
    public double getMaxValue()
    {
        return this.maxValue;
    }

    /**
     * Sets the maximum value of the measurement
     * range of this gauge. This value defines the
     * maximum value the gauge could display.
     * It has nothing to do with MaxMeasuredValue,
     * which represents the max. value that was
     * measured since the last reset of MaxMeasuredValue
     * @param MAX_VALUE
     */
    public void setMaxValue(final double MAX_VALUE)
    {
        this.maxValue = MAX_VALUE;
        checkSettings();
        calcAngleStep();
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns true if the last measured value (peak value)
     * is visible and will be painted.    
     * @return the visibility of the peak value
     */
    public boolean isPeakValueVisible()
    {
        return this.peakValueVisible;
    }

    /**
     * Sets the visbility of the peak value which
     * is the last measured value.
     * @param PEAK_VALUE_VISIBLE
     */
    public void setPeakValueVisible(final boolean PEAK_VALUE_VISIBLE)
    {
        this.peakValueVisible = PEAK_VALUE_VISIBLE;
    }

    /**
     * Returns true if the gauge will be reseted to
     * zero after each value.
     * Means if you set a value the pointer will
     * move to this value and after it reached the
     * given value it will return back to zero.
     * @return true if the value will be set to
     * zero automaticaly
     */
    public boolean isAutoResetToZero()
    {
        return this.autoResetToZero;
    }

    /**
     * Enables/disables the mode where the gauge
     * will return to zero after a value was set.
     * Means if you set a value the pointer will
     * move to this value and after it reached the
     * given value it will return back to zero.
     * @param AUTO_RESET_TO_ZERO
     */
    public void setAutoResetToZero(final boolean AUTO_RESET_TO_ZERO)
    {
        this.autoResetToZero = AUTO_RESET_TO_ZERO;
        if (AUTO_RESET_TO_ZERO)
        {
            setThresholdVisible(false);
            setLedVisible(false);
        }
    }

    /**
     * In normal case the scale is divided into a stepsize of one (10e0). This
     * works for a value range up to 1000. If the gauge should show higher
     * values, you may want to divide the scale through a higher potency.
     * This method returns the power of 10, the scale gets divided through
     * (e.g. 10e2 for power of 2).
     * Common settings for SCALE_DIVIDER_POWER
     * RANGE: 100     -> SCALE_DIVIDER_POWER: 0
     * RANGE: 1000    -> SCALE_DIVIDER_POWER: 0
     * RANGE: 10000   -> SCALE_DIVIDER_POWER: 1
     * RANGE: 100000  -> SCALE_DIVIDER_POWER: 2
     * @return power to 10 the scale gets divided through
     */
    public int getScaleDividerPower()
    {
        return this.scaleDividerPower;
    }

    /**
     * In normal case the scale is divided into a stepsize of one (10e0). This
     * works for a value range up to 1000. If the gauge should show higher
     * values, you may want to divide the scale through a higher potency.
     * This method sets the power of 10, the scale gets divided through
     * (e.g. 10e2 for power of 2).
     * Common settings for SCALE_DIVIDER_POWER
     * RANGE: 100     -> SCALE_DIVIDER_POWER: 0
     * RANGE: 1000    -> SCALE_DIVIDER_POWER: 0
     * RANGE: 10000   -> SCALE_DIVIDER_POWER: 1
     * RANGE: 100000  -> SCALE_DIVIDER_POWER: 2
     * @param SCALE_DIVIDER_POWER to 10 the scale gets divided through
     */
    public void setScaleDividerPower(final int SCALE_DIVIDER_POWER)
    {
        this.scaleDividerPower = SCALE_DIVIDER_POWER;
    }

    /**
     * Returns true if the color of the tickmarks will be
     * used from the defined background color.
     * @return true if tickmarks will use the color defined in the current
     * background color
     */
    public boolean useTickmarkColorFromTheme()
    {
        return this.tickmarkColorFromTheme;
    }

    /**
     * Enables/disables the usage of a separate color for the
     * tickmarks.
     * @param TICKMARK_COLOR_FROM_THEME
     */
    public void setTickmarkColorFromTheme(final boolean TICKMARK_COLOR_FROM_THEME)
    {
        this.tickmarkColorFromTheme = TICKMARK_COLOR_FROM_THEME;
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns the color of the tickmarks and their labels
     * @return the custom defined color for the tickmarks and labels
     */
    public java.awt.Color getTickmarkColor()
    {
        return this.tickmarkColor;
    }

    /**
     * Sets the color of the tickmarks and their labels
     * @param TICKMARK_COLOR
     */
    public void setTickmarkColor(final java.awt.Color TICKMARK_COLOR)
    {
        this.tickmarkColor = TICKMARK_COLOR;
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns true if the tickmarks are visible
     * @return true if the tickmarks are visible
     */
    public boolean isDrawTicks()
    {
        return this.drawTicks;
    }

    /**
     * Enables or disables the visibility of the tickmarks
     * @param DRAW_TICKS
     */
    public void setDrawTicks(final boolean DRAW_TICKS)
    {
        this.drawTicks = DRAW_TICKS;
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns true if the tickmark labels are visible
     * @return true if the tickmark labels are visible
     */
    public boolean isDrawTickLabels()
    {
        return this.drawTickLabels;
    }

    /**
     * Enables or disables the visibility of the tickmark labels
     * @param DRAW_TICK_LABELS
     */
    public void setDrawTickLabels(final boolean DRAW_TICK_LABELS)
    {
        this.drawTickLabels = DRAW_TICK_LABELS;
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns the value that is defined as a threshold.
     * If the current value of the gauge exceeds this
     * threshold, a event will be fired and the led will
     * start blinking (if the led is visible).
     * @return the threshold value where the led starts blinking
     */
    public double getThreshold()
    {
        return this.threshold;
    }

    /**
     * Sets the given value as the threshold.
     * If the current value of the gauge exceeds this
     * threshold, a event will be fired and the led will
     * start blinking (if the led is visible).
     * @param THRESHOLD
     */
    public void setThreshold(final double THRESHOLD)
    {
        this.threshold = THRESHOLD;
        setValue(super.getValue());
    }

    /**
     * Returns the visibility of the threshold indicator.
     * The value of the threshold will be visualized by
     * a small red triangle that points on the threshold
     * value.
     * @return true if the threshold indicator is visible
     */
    public boolean isThresholdVisible()
    {
        return this.thresholdVisible;
    }

    /**
     * Sets the visibility of the threshold indicator.
     * The value of the threshold will be visualized by
     * a small red triangle that points on the threshold
     * value.
     * @param THRESHOLD_VISIBLE
     */
    public void setThresholdVisible(final boolean THRESHOLD_VISIBLE)
    {
        this.thresholdVisible = THRESHOLD_VISIBLE;
        repaint();
    }

    /**
     * Returns the lowest measured value.
     * On every move of the bar/pointer the lowest value
     * will be stored in the minMeasuredValue variable.
     * This value is always in the displayable range of
     * the gauge which is defined by maxValue - minValue
     * @return represents the minimum measured value
     */
    public double getMinMeasuredValue()
    {
        return this.minMeasuredValue;
    }

    /**
     * Returns the visibility of the minMeasuredValue indicator.
     * The lowest value that was measured by the gauge will
     * be visualized by a little blue triangle.
     * @return true if the indicator for the minimum measured
     * value is visible
     */
    public boolean isMinMeasuredValueVisible()
    {
        return this.minMeasuredValueVisible;
    }

    /**
     * Sets the visibility of the minMeasuredValue indicator.
     * The lowest value that was measured by the gauge will
     * be visualized by a little blue triangle. This value is
     * always in the displayable range of the gauge which is defined
     * by maxValue - minValue
     * @param MIN_MEASURED_VALUE_VISIBLE
     */
    public void setMinMeasuredValueVisible(final boolean MIN_MEASURED_VALUE_VISIBLE)
    {
        this.minMeasuredValueVisible = MIN_MEASURED_VALUE_VISIBLE;
        repaint();
    }

    /**
     * Resets the minMeasureValue variable to the maximum value
     * that the gauge could display. So on the next move of the
     * pointer/bar the indicator will be set to the related pointer/bar
     * position again.
     */
    public void resetMinMeasuredValue()
    {
        this.minMeasuredValue = getMaxValue();
    }

    /**
     * Resets the minMeasuredValue variable to the given value.
     * So on the next move of the pointer/bar the indicator will
     * be set to the pointer/bar position again.
     * @param VALUE
     */
    public void resetMinMeasuredValue(final double VALUE)
    {
        this.minMeasuredValue = VALUE;
    }

    /**
     * Returns the biggest measured value.
     * On every move of the bar/pointer the biggest value
     * will be stored in the maxMeasuredValue variable.
     * This value is always in the displayable range of the gauge
     * which is defined by maxValue - minValue.
     * @return represents the maximum measured value
     */
    public double getMaxMeasuredValue()
    {
        return this.maxMeasuredValue;
    }

    /**
     * Returns the visibility of the maxMeasuredValue indicator.
     * The biggest value that was measured by the gauge will
     * be visualized by a little red triangle. The value is
     * always in the displayable range of the gauge that is
     * defined by maxValue - minValue
     * @return true if the indicator of the maximum measured
     * value is visible
     */
    public boolean isMaxMeasuredValueVisible()
    {
        return this.maxMeasuredValueVisible;
    }

    /**
     * Sets the visibility of the maxMeasuredValue indicator.
     * The biggest value that was measured by the gauge will
     * be visualized by a little red triangle.
     * @param MAX_MEASURED_VALUE_VISIBLE
     */
    public void setMaxMeasuredValueVisible(final boolean MAX_MEASURED_VALUE_VISIBLE)
    {
        this.maxMeasuredValueVisible = MAX_MEASURED_VALUE_VISIBLE;
        repaint();
    }

    /**
     * Resets the maxMeasureValue variable to the minimum value
     * that the gauge could display. So on the next move of the
     * pointer/bar the indicator will be set to the pointer/bar
     * position again.
     */
    public void resetMaxMeasuredValue()
    {
        this.maxMeasuredValue = getMinValue();
    }

    /**
     * Resets the maxMeasuredValue variable to the given value.
     * So on the next move of the pointer/bar the indicator will
     * be set to the pointer/bar position again.
     * @param VALUE
     */
    public void resetMaxMeasuredValue(final double VALUE)
    {
        this.maxMeasuredValue = VALUE;
    }

    /**
     * Returns the visibility of the track.
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.
     * @return true if the track is visible
     */
    public boolean isTrackVisible()
    {
        return this.trackVisible;
    }

    /**
     * Sets the visibility of the track.
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.     
     * @param TRACK_VISIBLE
     */
    public void setTrackVisible(final boolean TRACK_VISIBLE)
    {
        this.trackVisible = TRACK_VISIBLE;
        repaint();
    }

    /**
     * Returns the value where the track will start.
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.
     * @return represents the value where the track starts
     */
    public double getTrackStart()
    {
        return this.trackStart;
    }

    /**
     * Sets the value where the track will start.
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.
     * @param TRACK_START
     */
    public void setTrackStart(final double TRACK_START)
    {
        this.trackStart = TRACK_START;
        checkSettings();
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns the value of the point between trackStart and trackStop.
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.
     * @return represents the value where the intermediate position
     * of the track is defined.
     */
    public double getTrackSection()
    {
        return this.trackSection;
    }

    /**
     * Sets the value of the point between trackStart and trackStop.
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.
     * @param TRACK_SECTION
     */
    public void setTrackSection(final double TRACK_SECTION)
    {
        this.trackSection = TRACK_SECTION;
        checkSettings();
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns the value of the point where the track will stop
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.
     * @return represents the position where the track stops
     */
    public double getTrackStop()
    {
        return this.trackStop;
    }

    /**
     * Sets the value of the end of the track.
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.
     * @param TRACK_STOP
     */
    public void setTrackStop(final double TRACK_STOP)
    {
        this.trackStop = TRACK_STOP;
        checkSettings();
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns the color of the point where the track will start.
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.
     * @return represents the color at the point where the track starts
     */
    public java.awt.Color getTrackStartColor()
    {
        return this.trackStartColor;
    }

    /**
     * Sets the color of the point where the track will start.
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.
     * @param TRACK_START_COLOR
     */
    public void setTrackStartColor(final java.awt.Color TRACK_START_COLOR)
    {
        this.trackStartColor = TRACK_START_COLOR;
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns the color of the value between trackStart and trackStop
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.
     * @return represents the color of the intermediate position on the track
     */
    public java.awt.Color getTrackSectionColor()
    {
        return this.trackSectionColor;
    }

    /**
     * Sets the color of the value between trackStart and trackStop
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.
     * @param TRACK_SECTION_COLOR
     */
    public void setTrackSectionColor(final java.awt.Color TRACK_SECTION_COLOR)
    {
        this.trackSectionColor = TRACK_SECTION_COLOR;
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns the color of the point where the track will stop.
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.
     * @return represents the color of the point where the track stops
     */
    public java.awt.Color getTrackStopColor()
    {
        return this.trackStopColor;
    }

    /**
     * Sets the color of the point where the track will stop.
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.
     * @param TRACK_STOP_COLOR
     */
    public void setTrackStopColor(final java.awt.Color TRACK_STOP_COLOR)
    {
        this.trackStopColor = TRACK_STOP_COLOR;
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns the visibility of the area.
     * The area could be defined by a start value and a stop value.
     * This area will be painted with a color.
     * @return true if the area is visible
     */
    public boolean isAreaVisible()
    {
        return this.areaVisible;
    }

    /**
     * Sets the visibility of the area.
     * The area could be defined by a start value and a stop value.
     * This area will be painted with a color.
     * @param AREA_VISIBLE
     */
    public void setAreaVisible(final boolean AREA_VISIBLE)
    {
        this.areaVisible = AREA_VISIBLE;
        repaint();
    }

    /**
     * Returns the value of the start of the area.
     * The area could be defined by a start value and a stop value.
     * This area will be painted with a color.
     * @return represents the point where the area starts
     */
    public double getAreaStart()
    {
        return this.areaStart;
    }

    /**
     * Sets the value of the start of the area.
     * The area could be defined by a start value and a stop value.
     * This area will be painted with a color.
     * @param AREA_START
     */
    public void setAreaStart(final double AREA_START)
    {
        this.areaStart = AREA_START;
        checkSettings();
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns the value of the end of the area.
     * The area could be defined by a start value and a stop value.
     * This area will be painted with a color.
     * @return represents the point where the area stops
     */
    public double getAreaStop()
    {
        return this.areaStop;
    }

    /**
     * Sets the value of the end of the area.
     * The area could be defined by a start value and a stop value.
     * This area will be painted with a color.
     * @param AREA_STOP
     */
    public void setAreaStop(final double AREA_STOP)
    {
        this.areaStop = AREA_STOP;
        checkSettings();
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns the color of the area.
     * The area could be defined by a start value and a stop value.
     * This area will be painted with a color.
     * @return represents the color of the area
     */
    public java.awt.Color getAreaColor()
    {
        return this.areaColor;
    }

    /**
     * Sets the color of the area.
     * The area could be defined by a start value and a stop value.
     * This area will be painted with a color.
     * @param AREA_COLOR
     */
    public void setAreaColor(final java.awt.Color AREA_COLOR)
    {
        this.areaColor = AREA_COLOR;
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns the visibility of the sections.
     * The sections could be defined by a start value, a stop value
     * and a color. One has to create a Section object from the
     * class eu.hansolo.steelseries.tools.Section.
     * The sections are stored in a ArrayList so there could be
     * multiple. This might be a useful feature if you need to have
     * exactly defined areas that you could not visualize with the
     * track feature.
     * @return true if the sections are visible
     */
    public boolean isSectionsVisible()
    {
        return this.sectionsVisible;
    }

    /**
     * Sets the visibility of the sections.
     * The sections could be defined by a start value, a stop value
     * and a color. One has to create a Section object from the
     * class eu.hansolo.steelseries.tools.Section.
     * The sections are stored in a ArrayList so there could be
     * multiple. This might be a useful feature if you need to have
     * exactly defined areas that you could not visualize with the
     * track feature.
     * @param SECTION_VISIBLE
     */
    public void setSectionsVisible(final boolean SECTION_VISIBLE)
    {
        this.sectionsVisible = SECTION_VISIBLE;
        repaint();
    }

    /**
     * Returns a copy of the ArrayList that stores the sections.
     * The sections could be defined by a start value, a stop value
     * and a color. One has to create a Section object from the
     * class eu.hansolo.steelseries.tools.Section.
     * The sections are stored in a ArrayList so there could be
     * multiple. This might be a useful feature if you need to have
     * exactly defined areas that you could not visualize with the
     * track feature.
     * @return a list of sections
     */
    public java.util.ArrayList<eu.hansolo.steelseries.tools.Section> getSections()
    {
        return (java.util.ArrayList<eu.hansolo.steelseries.tools.Section>) this.SECTIONS.clone();
    }

    /**
     * Sets the sections given in a array of sections (Section[])
     * The sections could be defined by a start value, a stop value
     * and a color. One has to create a Section object from the
     * class eu.hansolo.steelseries.tools.Section.
     * The sections are stored in a ArrayList so there could be
     * multiple. This might be a useful feature if you need to have
     * exactly defined areas that you could not visualize with the
     * track feature.
     * @param SECTIONS_ARRAY
     */
    public void setSections(final eu.hansolo.steelseries.tools.Section... SECTIONS_ARRAY)
    {
        SECTIONS.clear();
        SECTIONS.addAll(java.util.Arrays.asList(SECTIONS_ARRAY));
        checkSettings();
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Adds a given section to the list of sections
     * The sections could be defined by a start value, a stop value
     * and a color. One has to create a Section object from the
     * class eu.hansolo.steelseries.tools.Section.
     * The sections are stored in a ArrayList so there could be
     * multiple. This might be a useful feature if you need to have
     * exactly defined areas that you could not visualize with the
     * track feature.
     * @param SECTION
     */
    public void addSection(final eu.hansolo.steelseries.tools.Section SECTION)
    {
        SECTIONS.add(SECTION);
        checkSettings();
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Clear the SECTIONS arraylist
     */
    public void resetSections()
    {
        SECTIONS.clear();
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns the visibility of the tickmark sections.
     * The sections could be defined by a start value, a stop value
     * and a color. One has to create a Section object from the
     * class eu.hansolo.steelseries.tools.Section.
     * The sections are stored in a ArrayList so there could be
     * multiple. This might be a useful feature if you need to have
     * the tickmark labels colored for specific areas.
     * @return true if the tickmark sections are visible
     */
    public boolean isTickmarkSectionsVisible()
    {
        return this.tickmarkSectionsVisible;
    }

    /**
     * Sets the visibility of the tickmark sections.
     * The sections could be defined by a start value, a stop value
     * and a color. One has to create a Section object from the
     * class eu.hansolo.steelseries.tools.Section.
     * The sections are stored in a ArrayList so there could be
     * multiple. This might be a useful feature if you need to have
     * the tickmark labels colored for specific areas.
     * @param TICKMARK_SECTIONS_VISIBLE
     */
    public void setTickmarkSectionsVisible(final boolean TICKMARK_SECTIONS_VISIBLE)
    {
        this.tickmarkSectionsVisible = TICKMARK_SECTIONS_VISIBLE;
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns a copy of the ArrayList that stores the sections.
     * The sections could be defined by a start value, a stop value
     * and a color. One has to create a Section object from the
     * class eu.hansolo.steelseries.tools.Section.
     * The sections are stored in a ArrayList so there could be
     * multiple. This might be a useful feature if you need to have
     * exactly defined areas that you would like to visualize by
     * colored tickmarks.
     * @return a list of sections for the tickmarks
     */
    public java.util.ArrayList<eu.hansolo.steelseries.tools.Section> getTickmarkSections()
    {
        return (java.util.ArrayList<eu.hansolo.steelseries.tools.Section>) this.TICKMARK_SECTIONS.clone();
    }

    /**
     * Sets the sections given in a array of sections (Section[])
     * The sections could be defined by a start value, a stop value
     * and a color. One has to create a Section object from the
     * class eu.hansolo.steelseries.tools.Section.
     * The sections are stored in a ArrayList so there could be
     * multiple. This might be a useful feature if you need to have
     * exactly defined areas that you would like to visualize by
     * by colored tickmarks.
     * @param TICKMARK_SECTIONS_ARRAY
     */
    public void setTickmarkSections(final eu.hansolo.steelseries.tools.Section... TICKMARK_SECTIONS_ARRAY)
    {
        TICKMARK_SECTIONS.clear();
        TICKMARK_SECTIONS.addAll(java.util.Arrays.asList(TICKMARK_SECTIONS_ARRAY));
        checkSettings();
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Adds a given section to the list of sections
     * The sections could be defined by a start value, a stop value
     * and a color. One has to create a Section object from the
     * class eu.hansolo.steelseries.tools.Section.
     * The sections are stored in a ArrayList so there could be
     * multiple. This might be a useful feature if you need to have
     * exactly defined areas that you would like to visualize by
     * by colored tickmarks.
     * @param TICKMARK_SECTION
     */
    public void addTickmarkSection(final eu.hansolo.steelseries.tools.Section TICKMARK_SECTION)
    {
        TICKMARK_SECTIONS.add(TICKMARK_SECTION);
        checkSettings();
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Clear the TICKMARK_SECTIONS arraylist
     */
    public void resetTickmarkSections()
    {
        TICKMARK_SECTIONS.clear();
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns true if customer defined tickmark labels will be
     * used for the scaling.
     * e.g. you only want to show "0, 10, 50, 100" in your
     * gauge scale so you could set the custom tickmarklabels
     * to these values.
     * @return true if custom tickmark labels are visible
     */
    public boolean getUseCustomTickmarkLabels()
    {
        return this.useCustomTickmarkLabels;
    }

    /**
     * Enables/Disables the usage of custom tickmark labels.
     * e.g. you only want to show "0, 10, 50, 100" in your
     * gauge scale so you could set the custom tickmarklabels
     * to these values.
     * @param USE_CUSTOM_TICKMARK_LABELS
     */
    public void setUseCustomTickmarkLabels(final boolean USE_CUSTOM_TICKMARK_LABELS)
    {
        this.useCustomTickmarkLabels = USE_CUSTOM_TICKMARK_LABELS;
        init(getWidth(), getHeight());
        repaint();
    }

    /**
     * Returns a list of the defined custom tickmark labels
     * e.g. you only want to show "0, 10, 50, 100" in your
     * gauge scale so you could set the custom tickmarklabels
     * to these values.
     * @return a list with custom tickmark labels
     */
    public java.util.ArrayList<Double> getCustomTickmarkLabels()
    {
        return (java.util.ArrayList<Double>) this.CUSTOM_TICKMARK_LABELS.clone();
    }

    /**
     * Takes a array of doubles that will be used as custom tickmark labels
     * e.g. you only want to show "0, 10, 50, 100" in your
     * gauge scale so you could set the custom tickmarklabels
     * to these values.
     * @param CUSTOM_TICKMARK_LABELS_ARRAY
     */
    public void setCustomTickmarkLabels(final double... CUSTOM_TICKMARK_LABELS_ARRAY)
    {
        CUSTOM_TICKMARK_LABELS.clear();
        for (Double label : CUSTOM_TICKMARK_LABELS_ARRAY)
        {
            CUSTOM_TICKMARK_LABELS.add(label);
        }
        init(getWidth(), getHeight());
        repaint();
    }

    /**
     * Adds the given double to the list of custom tickmark labels
     * e.g. you only want to show "0, 10, 50, 100" in your
     * gauge scale so you could set the custom tickmarklabels
     * to these values.
     * @param CUSTOM_TICKMARK_LABEL
     */
    public void addCustomTickmarkLabel(final double CUSTOM_TICKMARK_LABEL)
    {
        CUSTOM_TICKMARK_LABELS.add(CUSTOM_TICKMARK_LABEL);
        init(getWidth(), getHeight());
        repaint();
    }

    /**
     * Reset the list of custom tickmark labels, which means clear the list
     */
    public void resetCustomTickmarkLabels()
    {
        CUSTOM_TICKMARK_LABELS.clear();
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns true if the custom layer is visible.
     * The custom layer (which is a buffered image) will be
     * drawn on the background of the gauge and could be used
     * to display logos or icons.
     * @return true if custom layer is visible
     */
    public boolean isCustomLayerVisible()
    {
        return this.customLayerVisible;
    }

    /**
     * Enables/disables the usage of the custom layer.
     * The custom layer (which is a buffered image) will be
     * drawn on the background of the gauge and could be used
     * to display logos or icons.
     * @param CUSTOM_LAYER_VISIBLE
     */
    public void setCustomLayerVisible(final boolean CUSTOM_LAYER_VISIBLE)
    {
        if (this.customLayer != null)
        {
            this.customLayerVisible = CUSTOM_LAYER_VISIBLE;
        }
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns the buffered image that represents the custom layer.
     * The custom layer (which is a buffered image) will be
     * drawn on the background of the gauge and could be used
     * to display logos or icons.
     * @return the buffered image that represents the custom layer
     */
    public java.awt.image.BufferedImage getCustomLayer()
    {
        return this.customLayer;
    }

    /**
     * Sets the buffered image that represents the custom layer.
     * It will automaticaly scale the given image to the bounds.
     * The custom layer (which is a buffered image) will be
     * drawn on the background of the gauge and could be used
     * to display logos or icons.
     * @param CUSTOM_LAYER
     */
    public void setCustomLayer(final java.awt.image.BufferedImage CUSTOM_LAYER)
    {
        if (this.customLayer != null)
        {
            this.customLayer.flush();
        }

        if (CUSTOM_LAYER == null)
        {
            this.customLayerVisible = false;
            return;
        }

        this.customLayer = CUSTOM_LAYER;

        if (customLayerVisible)
        {
            init(getWidth(), getWidth());
            repaint();
        }
    }

    /**
     * Returns true if the background paint will be taken from the variable customBackground.
     * This method is deprecated, please use isCustomBackgroundVisible() method instead.
     * @return true if the background paint will be taken from the variable customBackground
     * @deprecated
     */
    @Deprecated
    public boolean useBackgroundColorFromTheme()
    {
        return !this.customBackgroundVisible;
    }

    /**
     * Enables/disables the usage of a custom paint as replacement for the predefined background colors.
     * This method is depracted, please use setCustomBackgroundVisible() method instead.
     * @param BACKGROUND_COLOR_FROM_THEME
     * @deprecated
     */
    @Deprecated
    public void setBackgroundColorFromTheme(final boolean BACKGROUND_COLOR_FROM_THEME)
    {
        setCustomBackgroundVisible(!BACKGROUND_COLOR_FROM_THEME);
    }

    /**
     * Returns true if the custom background paint will be taken
     * instead of the background from the "theme" (e.g. "DARK_GRAY").
     * @return true if custom background paint is used
     */
    public boolean isCustomBackgroundVisible()
    {
        return this.customBackgroundVisible;
    }

    /**
     * Enables/disables the usage of a custom paint as
     * replacement for the predefined background colors like "DARK_GRAY" etc.
     * @param CUSTOM_BACKGROUND_VISIBLE
     */
    public void setCustomBackgroundVisible(final boolean CUSTOM_BACKGROUND_VISIBLE)
    {
        this.customBackgroundVisible = CUSTOM_BACKGROUND_VISIBLE;
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns the custom background paint that will be used instead of
     * the predefined backgroundcolors like DARK_GRAY, BEIGE etc.
     * @return the custom paint that will be used for the background of the gauge
     */
    public java.awt.Paint getCustomBackground()
    {
        return this.customBackground;
    }

    /**
     * Sets the custom background paint that will be used instead of
     * the predefined backgroundcolors like DARK_GRAY, BEIGE etc.
     * @param CUSTOM_BACKGROUND
     */
    public void setCustomBackground(final java.awt.Paint CUSTOM_BACKGROUND)
    {
        this.customBackground = CUSTOM_BACKGROUND;
        if (this.customBackgroundVisible)
        {
            init(getWidth(), getWidth());
            repaint();
        }
    }

    /**
     * Returns the backgroundcolor of the gauge.
     * The backgroundcolor is not a standard color but more a
     * color scheme with colors and a gradient.
     * The typical backgroundcolor is DARK_GRAY.
     * @return the selected backgroundcolor
     */
    public eu.hansolo.steelseries.tools.BackgroundColor getBackgroundColor()
    {
        return this.backgroundColor;
    }

    /**
     * Sets the backgroundcolor of the gauge.
     * The backgroundcolor is not a standard color but more a
     * color scheme with colors and a gradient.
     * The typical backgroundcolor is DARK_GRAY.
     * @param BACKGROUND_COLOR
     */
    public void setBackgroundColor(final eu.hansolo.steelseries.tools.BackgroundColor BACKGROUND_COLOR)
    {
        this.backgroundColor = BACKGROUND_COLOR;
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns the framedesign of the component.
     * The framedesign is some kind of a color scheme for the
     * frame of the component.
     * The typical framedesign is METAL
     * @return the selected framedesign
     */
    public eu.hansolo.steelseries.tools.FrameDesign getFrameDesign()
    {
        return this.frameDesign;
    }

    /**
     * Sets the framedesign of the component.
     * The framedesign is some kind of a color scheme for the
     * frame of the component.
     * The typical framedesign is METAL
     * @param FRAME_DESIGN
     */
    public void setFrameDesign(final eu.hansolo.steelseries.tools.FrameDesign FRAME_DESIGN)
    {
        this.frameDesign = FRAME_DESIGN;
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns the type of the pointer
     * TYPE1 (standard version) or TYPE2
     * @return the type of the pointer
     */
    public eu.hansolo.steelseries.tools.PointerType getPointerType()
    {
        return this.pointerType;
    }

    /**
     * Sets the type of the pointer
     * @param POINTER_TYPE type of the pointer
     *     eu.hansolo.steelseries.tools.PointerType.TYPE1 (default)
     *     eu.hansolo.steelseries.tools.PointerType.TYPE2
     */
    public void setPointerType(final eu.hansolo.steelseries.tools.PointerType POINTER_TYPE)
    {
        this.pointerType = POINTER_TYPE;
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns the color of the pointer
     * @return the selected color of the pointer
     */
    public eu.hansolo.steelseries.tools.PointerColor getPointerColor()
    {
        return this.pointerColor;
    }

    /**
     * Sets the color of the pointer
     * @param POINTER_COLOR
     */
    public void setPointerColor(final eu.hansolo.steelseries.tools.PointerColor POINTER_COLOR)
    {
        this.pointerColor = POINTER_COLOR;
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns the visiblity of the threshold led.
     * @return true if the threshold led is visible
     */
    public boolean isLedVisible()
    {
        return this.ledVisible;
    }

    /**
     * Sets the visibility of the threshold led.
     * @param LED_VISIBLE
     */
    public void setLedVisible(final boolean LED_VISIBLE)
    {
        this.ledVisible = LED_VISIBLE;
        repaint();
    }

    /**
     * Returns the x position of the threshold led.
     * This is needed because in some gauges the led
     * might be placed on a different position than the
     * default position.
     * @return the x position of the led as a factor
     */
    protected double getLedPositionX()
    {
        return this.ledPositionX;
    }

    /**
     * Sets the x position of the threshold led.
     * This is needed because in some gauges the led
     * might be placed on a different position than the
     * default position.
     * @param LED_POSITION_X
     */
    protected void setLedPositionX(final double LED_POSITION_X)
    {
        this.ledPositionX = LED_POSITION_X;
    }

    /**
     * Returns the y position of the threshold led.
     * This is needed because in some gauges the led
     * might be placed on a different position than the
     * default position.
     * @return the y position of the led as a factor
     */
    protected double getLedPositionY()
    {
        return this.ledPositionY;
    }

    /**
     * Sets the y position of the threshold led.
     * This is needed because in some gauges the led
     * might be placed on a different position than the
     * default position.
     * @param LED_POSITION_Y
     */
    protected void setLedPositionY(final double LED_POSITION_Y)
    {
        this.ledPositionY = LED_POSITION_Y;
    }

    /**
     * Returns the color of the threshold led.
     * The LedColor is not a standard color but defines a
     * color scheme for the led. The default ledcolor is RED
     * @return the selected the color for the led
     */
    public eu.hansolo.steelseries.tools.LedColor getLedColor()
    {
        return this.ledColor;
    }

    /**
     * Sets the color of the threshold led.
     * The LedColor is not a standard color but defines a
     * color scheme for the led. The default ledcolor is RED
     * @param LED_COLOR
     */
    public void setLedColor(final eu.hansolo.steelseries.tools.LedColor LED_COLOR)
    {
        this.ledColor = LED_COLOR;
        final boolean LED_WAS_ON = currentLedImage.equals(ledImageOn) ? true : false;

        ledImageOff = create_LED_Image(getWidth(), 0, LED_COLOR);
        ledImageOn = create_LED_Image(getWidth(), 1, LED_COLOR);

        currentLedImage = LED_WAS_ON == true ? ledImageOn : ledImageOff;

        repaint();
    }

    /**
     * Returns the state of the threshold led.
     * The led could blink which will be triggered by a javax.swing.Timer
     * that triggers every 500 ms. The blinking will be done by switching
     * between two images.
     * @return true if the led is blinking
     */
    public boolean isLedBlinking()
    {
        return this.ledBlinking;
    }

    /**
     * Sets the state of the threshold led.
     * The led could blink which will be triggered by a javax.swing.Timer
     * that triggers every 500 ms. The blinking will be done by switching
     * between two images.
     * @param LED_BLINKING
     */
    public void setLedBlinking(final boolean LED_BLINKING)
    {
        this.ledBlinking = LED_BLINKING;
        if (LED_BLINKING)
        {
            LED_BLINKING_TIMER.start();
        }
        else
        {
            setCurrentLedImage(getLedImageOff());
            LED_BLINKING_TIMER.stop();
        }
    }

    /**
     * Returns the image of the switched on threshold led
     * with the currently active ledcolor.
     * @return the image of the led with the state active
     * and the selected led color
     */
    protected java.awt.image.BufferedImage getLedImageOn()
    {
        return this.ledImageOn;
    }

    /**
     * Returns the image of the switched off threshold led
     * with the currently active ledcolor.
     * @return the image of the led with the state inactive
     * and the selected led color
     */
    protected java.awt.image.BufferedImage getLedImageOff()
    {
        return this.ledImageOff;
    }

    /**
     * Returns the image of the currently used led image.
     * @return the led image at the moment (depends on blinking)
     */
    protected java.awt.image.BufferedImage getCurrentLedImage()
    {
        return this.currentLedImage;
    }

    /**
     * Sets the image of the currently used led image.
     * @param CURRENT_LED_IMAGE
     */
    protected void setCurrentLedImage(final java.awt.image.BufferedImage CURRENT_LED_IMAGE)
    {
        this.currentLedImage = CURRENT_LED_IMAGE;
        repaint();
    }

    abstract protected void calcAngleStep();

    abstract public java.awt.geom.Point2D getCenter();

    abstract public java.awt.geom.Rectangle2D getBounds2D();

    /**
     * This method will check the correctness of the current
     * values in their current combination and adjusts the
     * values that are not set properly.
     */
    private void checkSettings()
    {
        // Adjust threshold settings
        if (threshold < minValue || threshold > maxValue)
        {
            threshold = maxValue;
        }

        // Adjust minMeasuredValue
        resetMinMeasuredValue(maxValue);

        // Adjust maxMeasuredValue
        resetMaxMeasuredValue(minValue);

        // Adjust trackStart setting
        if (trackStart <= minValue || trackStart >= maxValue || trackStart >= trackStop)
        {
            trackStart = minValue;
        }

        // Adjust trackRange setting
        if ((trackStop <= minValue) || trackStop >= maxValue || trackStop <= trackStart)
        {
            trackStop = maxValue;
        }

        // Adjust trackSection setting
        if (trackSection <= minValue || trackSection >= maxValue)
        {
            trackSection = trackStart + (trackStop - trackStart) / 2.0;
        }

        if (trackSection <= trackStart || trackSection >= trackStop)
        {
            trackSection = trackStart + (trackStop - trackStart) / 2.0;
        }

        // Adjust area setting
        if ((areaStart < minValue) || areaStart >= maxValue || areaStart >= areaStop)
        {
            areaStart = minValue;
        }

        if (areaStop < minValue || areaStop > maxValue || areaStop <= areaStart)
        {
            areaStop = maxValue;
        }

        // Adjust section settings
        if (!SECTIONS.isEmpty())
        {
            for (eu.hansolo.steelseries.tools.Section section : SECTIONS)
            {
                if (section.getStart() != -1 && section.getStop() != -1)
                {
                    if ((section.getStart() < minValue) || section.getStart() >= maxValue || section.getStart() >= section.getStop())
                    {
                        section.setStart(minValue);
                    }

                    if (section.getStop() < minValue || section.getStop() > maxValue || section.getStop() <= section.getStart())
                    {
                        section.setStop(maxValue);
                    }
                }
            }
        }

        // Check if AutoResetToZero is possible
        if (minValue > 0 || maxValue < 0)
        {
            autoResetToZero = false;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Image creation methods">
    /**
     * Returns the background image with the currently active backgroundcolor
     * with the given width without a title and a unit string.
     * @param WIDTH
     * @return the background image that is used
     */
    protected java.awt.image.BufferedImage create_BACKGROUND_Image(final int WIDTH)
    {
        return create_BACKGROUND_Image(WIDTH, "", "");
    }

    /**
     * Returns the background image with the currently active backgroundcolor
     * with the given width, title and unitstring.
     * @param WIDTH
     * @return the background image that is used
     */
    protected java.awt.image.BufferedImage create_BACKGROUND_Image(final int WIDTH, final String TITLE, final String UNIT_STRING)
    {
        if (WIDTH <= 0)
        {
            return null;
        }

        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, WIDTH, java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        // Skip frame if frameDesign == NO_FRAME
        if (frameDesign != eu.hansolo.steelseries.tools.FrameDesign.NO_FRAME)
        {
            final java.awt.geom.Ellipse2D E_FRAME_OUTERFRAME = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.0, IMAGE_HEIGHT * 0.0, IMAGE_WIDTH * 1.0, IMAGE_HEIGHT * 1.0);
            final java.awt.Color FILL_COLOR_E_FRAME_OUTERFRAME = new java.awt.Color(0x848484);
            G2.setColor(FILL_COLOR_E_FRAME_OUTERFRAME);
            G2.fill(E_FRAME_OUTERFRAME);

            final java.awt.geom.Ellipse2D FRAME_MAIN = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.004672897048294544, IMAGE_HEIGHT * 0.004672897048294544, IMAGE_WIDTH * 0.9906542301177979, IMAGE_HEIGHT * 0.9906542301177979);
            final java.awt.geom.Point2D FRAME_MAIN_START = new java.awt.geom.Point2D.Double(0, FRAME_MAIN.getBounds2D().getMinY());
            final java.awt.geom.Point2D FRAME_MAIN_STOP = new java.awt.geom.Point2D.Double(0, FRAME_MAIN.getBounds2D().getMaxY());
            final java.awt.geom.Point2D FRAME_MAIN_CENTER = new java.awt.geom.Point2D.Double(FRAME_MAIN.getCenterX(), FRAME_MAIN.getCenterY());

            final float[] FRAME_MAIN_FRACTIONS;
            final java.awt.Color[] FRAME_MAIN_COLORS;
            final java.awt.Paint FRAME_MAIN_GRADIENT;

            switch (this.frameDesign)
            {
                case BLACK_METAL:
                    FRAME_MAIN_FRACTIONS = new float[]
                        {
                            0.0f,
                            45.0f,
                            125.0f,
                            180.0f,
                            245.0f,
                            315.0f,
                            360.0f
                        };

                    FRAME_MAIN_COLORS = new java.awt.Color[]
                        {
                            new java.awt.Color(254, 254, 254, 255),
                            new java.awt.Color(0, 0, 0, 255),
                            new java.awt.Color(153, 153, 153, 255),
                            new java.awt.Color(0, 0, 0, 255),
                            new java.awt.Color(153, 153, 153, 255),
                            new java.awt.Color(0, 0, 0, 255),
                            new java.awt.Color(254, 254, 254, 255)
                        };

                    FRAME_MAIN_GRADIENT = new eu.hansolo.steelseries.tools.ConicalGradientPaint(true, FRAME_MAIN_CENTER, 0, FRAME_MAIN_FRACTIONS, FRAME_MAIN_COLORS);
                    break;

                case METAL:
                    FRAME_MAIN_FRACTIONS = new float[]
                        {
                            0.0f,
                            0.07f,
                            0.12f,
                            1.0f
                        };

                    FRAME_MAIN_COLORS = new java.awt.Color[]
                        {
                            new java.awt.Color(254, 254, 254, 255),
                            new java.awt.Color(210, 210, 210, 255),
                            new java.awt.Color(179, 179, 179, 255),
                            new java.awt.Color(213, 213, 213, 255)
                        };

                    FRAME_MAIN_GRADIENT = new java.awt.LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, FRAME_MAIN_FRACTIONS, FRAME_MAIN_COLORS);
                    break;

                case SHINY_METAL:
                    FRAME_MAIN_FRACTIONS = new float[]
                        {
                            0.0f,
                            45.0f,
                            90.0f,
                            125.0f,
                            180.0f,
                            245.0f,
                            270.0f,
                            315.0f,
                            360.0f
                        };

                    FRAME_MAIN_COLORS = new java.awt.Color[]
                        {
                            new java.awt.Color(254, 254, 254, 255),
                            new java.awt.Color(210, 210, 210, 255),
                            new java.awt.Color(179, 179, 179, 255),
                            new java.awt.Color(238, 238, 238, 255),
                            new java.awt.Color(160, 160, 160, 255),
                            new java.awt.Color(238, 238, 238, 255),
                            new java.awt.Color(179, 179, 179, 255),
                            new java.awt.Color(210, 210, 210, 255),
                            new java.awt.Color(254, 254, 254, 255)
                        };

                    FRAME_MAIN_GRADIENT = new eu.hansolo.steelseries.tools.ConicalGradientPaint(true, FRAME_MAIN_CENTER, 0, FRAME_MAIN_FRACTIONS, FRAME_MAIN_COLORS);
                    break;

                default:
                    FRAME_MAIN_FRACTIONS = new float[]
                        {
                            0.0f,
                            0.07f,
                            0.12f,
                            1.0f
                        };

                    FRAME_MAIN_COLORS = new java.awt.Color[]
                        {
                            new java.awt.Color(254, 254, 254, 255),
                            new java.awt.Color(210, 210, 210, 255),
                            new java.awt.Color(179, 179, 179, 255),
                            new java.awt.Color(213, 213, 213, 255)
                        };

                    FRAME_MAIN_GRADIENT = new java.awt.LinearGradientPaint(FRAME_MAIN_START, FRAME_MAIN_STOP, FRAME_MAIN_FRACTIONS, FRAME_MAIN_COLORS);
                    break;
            }

            G2.setPaint(FRAME_MAIN_GRADIENT);
            G2.fill(FRAME_MAIN);

            final java.awt.geom.Ellipse2D FRAME_INNERFRAME = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.07943925261497498, IMAGE_HEIGHT * 0.07943925261497498, IMAGE_WIDTH * 0.8411215543746948, IMAGE_HEIGHT * 0.8411215543746948);
            G2.setColor(java.awt.Color.WHITE);
            G2.fill(FRAME_INNERFRAME);
        }

        // Background of gauge
        final java.awt.geom.Ellipse2D GAUGE_BACKGROUND = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.08411215245723724, IMAGE_HEIGHT * 0.08411215245723724, IMAGE_WIDTH * 0.8317756652832031, IMAGE_HEIGHT * 0.8317756652832031);
        final java.awt.geom.Point2D GAUGE_BACKGROUND_START = new java.awt.geom.Point2D.Double(0, GAUGE_BACKGROUND.getBounds2D().getMinY());
        final java.awt.geom.Point2D GAUGE_BACKGROUND_STOP = new java.awt.geom.Point2D.Double(0, GAUGE_BACKGROUND.getBounds2D().getMaxY());
        final float[] GAUGE_BACKGROUND_FRACTIONS =
        {
            0.0f,
            0.40f,
            1.0f
        };
        final java.awt.Color[] GAUGE_BACKGROUND_COLORS =
        {
            backgroundColor.GRADIENT_START_COLOR,
            backgroundColor.GRADIENT_FRACTION_COLOR,
            backgroundColor.GRADIENT_STOP_COLOR
        };

        final java.awt.Paint GAUGE_BACKGROUND_GRADIENT;
        if (backgroundColor == eu.hansolo.steelseries.tools.BackgroundColor.BRUSHED_METAL)
        {
            GAUGE_BACKGROUND_GRADIENT = new java.awt.TexturePaint(UTIL.createBrushMetalTexture(null, GAUGE_BACKGROUND.getBounds().width, GAUGE_BACKGROUND.getBounds().height), GAUGE_BACKGROUND.getBounds());
        }
        else
        {
            GAUGE_BACKGROUND_GRADIENT = new java.awt.LinearGradientPaint(GAUGE_BACKGROUND_START, GAUGE_BACKGROUND_STOP, GAUGE_BACKGROUND_FRACTIONS, GAUGE_BACKGROUND_COLORS);
        }

        // Set custom background paint if selected
        if (customBackgroundVisible)
        {
            G2.setPaint(customBackground);
        }
        else
        {
            G2.setPaint(GAUGE_BACKGROUND_GRADIENT);
        }
        G2.fill(GAUGE_BACKGROUND);

        // Draw the custom layer if selected
        if (customLayerVisible)
        {
            G2.drawImage(UTIL.getScaledInstance(customLayer, IMAGE_WIDTH, IMAGE_HEIGHT, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC, true), 0, 0, null);
        }

        final java.awt.geom.Ellipse2D GAUGE_INNERSHADOW = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.08411215245723724, IMAGE_HEIGHT * 0.08411215245723724, IMAGE_WIDTH * 0.8317756652832031, IMAGE_HEIGHT * 0.8317756652832031);
        final java.awt.geom.Point2D GAUGE_INNERSHADOW_CENTER = new java.awt.geom.Point2D.Double((0.5 * IMAGE_WIDTH), (0.5 * IMAGE_HEIGHT));
        final float[] GAUGE_INNERSHADOW_FRACTIONS =
        {
            0.0f,
            0.7f,
            0.71f,
            1.0f
        };
        final java.awt.Color[] GAUGE_INNERSHADOW_COLORS =
        {
            new java.awt.Color(0, 0, 0, 0),
            new java.awt.Color(0, 0, 0, 0),
            new java.awt.Color(0, 0, 0, 0),
            new java.awt.Color(0, 0, 0, 75)
        };
        final java.awt.RadialGradientPaint GAUGE_INNERSHADOW_GRADIENT = new java.awt.RadialGradientPaint(GAUGE_INNERSHADOW_CENTER, (float) (0.4158878504672897 * IMAGE_WIDTH), GAUGE_INNERSHADOW_FRACTIONS, GAUGE_INNERSHADOW_COLORS);
        G2.setPaint(GAUGE_INNERSHADOW_GRADIENT);
        G2.fill(GAUGE_INNERSHADOW);

        final java.awt.font.FontRenderContext RENDER_CONTEXT = new java.awt.font.FontRenderContext(null, true, true);

        if (!TITLE.isEmpty())
        {
            // Use custom label color if selected
            if (useLabelColorFromTheme())
            {
                G2.setColor(backgroundColor.LABEL_COLOR);
            }
            else
            {
                G2.setColor(getLabelColor());
            }

            // Use custom font if selected
            if (getUseTitleAndUnitFont())
            {
                G2.setFont(new java.awt.Font(getTitleAndUnitFont().getFamily(), 0, (int) (0.04672897196261682 * IMAGE_WIDTH)));
            }
            else
            {
                G2.setFont(new java.awt.Font("Verdana", 0, (int) (0.04672897196261682 * IMAGE_WIDTH)));
            }
            final java.awt.font.TextLayout TITLE_LAYOUT = new java.awt.font.TextLayout(TITLE, G2.getFont(), RENDER_CONTEXT);
            final java.awt.geom.Rectangle2D TITLE_BOUNDARY = TITLE_LAYOUT.getBounds();
            G2.drawString(TITLE, (float) ((IMAGE_WIDTH - TITLE_BOUNDARY.getWidth()) / 2.0), 0.3f * IMAGE_HEIGHT + TITLE_LAYOUT.getAscent() - TITLE_LAYOUT.getDescent());
        }

        if (!UNIT_STRING.isEmpty())
        {
            // Use custom label color if selected
            if (useLabelColorFromTheme())
            {
                G2.setColor(backgroundColor.LABEL_COLOR);
            }
            else
            {
                G2.setColor(getLabelColor());
            }

            // Use custom font if selected
            if (getUseTitleAndUnitFont())
            {
                G2.setFont(new java.awt.Font(getTitleAndUnitFont().getFamily(), 0, (int) (0.04672897196261682 * IMAGE_WIDTH)));
            }
            else
            {
                G2.setFont(new java.awt.Font("Verdana", 0, (int) (0.04672897196261682 * IMAGE_WIDTH)));
            }
            final java.awt.font.TextLayout UNIT_LAYOUT = new java.awt.font.TextLayout(UNIT_STRING, G2.getFont(), RENDER_CONTEXT);
            final java.awt.geom.Rectangle2D UNIT_BOUNDARY = UNIT_LAYOUT.getBounds();
            G2.drawString(UNIT_STRING, (float) ((IMAGE_WIDTH - UNIT_BOUNDARY.getWidth()) / 2.0), 0.38f * IMAGE_HEIGHT + UNIT_LAYOUT.getAscent() - UNIT_LAYOUT.getDescent());
        }

        G2.dispose();

        return IMAGE;
    }

    /**
     * Returns the image with the given title and unitstring.
     * @param WIDTH
     * @param TITLE
     * @param UNIT_STRING
     * @return the title and unit string image that is used
     */
    protected java.awt.image.BufferedImage create_TITLE_Image(final int WIDTH, final String TITLE, final String UNIT_STRING)
    {
        if (WIDTH <= 0)
        {
            return null;
        }

        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, WIDTH, java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final java.awt.font.FontRenderContext RENDER_CONTEXT = new java.awt.font.FontRenderContext(null, true, true);

        if (!TITLE.isEmpty())
        {
            // Use custom label color if selected
            if (useLabelColorFromTheme())
            {
                G2.setColor(backgroundColor.LABEL_COLOR);
            }
            else
            {
                G2.setColor(getLabelColor());
            }

            // Use custom font if selected
            if (getUseTitleAndUnitFont())
            {
                G2.setFont(new java.awt.Font(getTitleAndUnitFont().getFamily(), 0, (int) (0.04672897196261682 * IMAGE_WIDTH)));
            }
            else
            {
                G2.setFont(new java.awt.Font("Verdana", 0, (int) (0.04672897196261682 * IMAGE_WIDTH)));
            }
            final java.awt.font.TextLayout TITLE_LAYOUT = new java.awt.font.TextLayout(TITLE, G2.getFont(), RENDER_CONTEXT);
            final java.awt.geom.Rectangle2D TITLE_BOUNDARY = TITLE_LAYOUT.getBounds();
            G2.drawString(TITLE, (float) ((IMAGE_WIDTH - TITLE_BOUNDARY.getWidth()) / 2.0), 0.3f * IMAGE_HEIGHT + TITLE_LAYOUT.getAscent() - TITLE_LAYOUT.getDescent());
        }

        if (!UNIT_STRING.isEmpty())
        {
            // Use custom label color if selected
            if (useLabelColorFromTheme())
            {
                G2.setColor(backgroundColor.LABEL_COLOR);
            }
            else
            {
                G2.setColor(getLabelColor());
            }

            // Use custom font if selected
            if (getUseTitleAndUnitFont())
            {
                G2.setFont(new java.awt.Font(getTitleAndUnitFont().getFamily(), 0, (int) (0.04672897196261682 * IMAGE_WIDTH)));
            }
            else
            {
                G2.setFont(new java.awt.Font("Verdana", 0, (int) (0.04672897196261682 * IMAGE_WIDTH)));
            }
            final java.awt.font.TextLayout UNIT_LAYOUT = new java.awt.font.TextLayout(UNIT_STRING, G2.getFont(), RENDER_CONTEXT);
            final java.awt.geom.Rectangle2D UNIT_BOUNDARY = UNIT_LAYOUT.getBounds();
            G2.drawString(UNIT_STRING, (float) ((IMAGE_WIDTH - UNIT_BOUNDARY.getWidth()) / 2.0), 0.38f * IMAGE_HEIGHT + UNIT_LAYOUT.getAscent() - UNIT_LAYOUT.getDescent());
        }

        G2.dispose();

        return IMAGE;
    }

    /**
     * Returns the track image with a given values.
     * @param WIDTH
     * @param MIN_VALUE
     * @param MAX_VALUE
     * @param ANGLE_STEP
     * @param TRACK_START
     * @param TRACK_SECTION
     * @param TRACK_STOP
     * @param TRACK_START_COLOR
     * @param TRACK_SECTION_COLOR
     * @param TRACK_STOP_COLOR
     * @param ROTATION_OFFSET
     * @return the track image that is used
     */
    protected java.awt.image.BufferedImage create_TRACK_Image(final int WIDTH, final double MIN_VALUE, final double MAX_VALUE, final double ANGLE_STEP, final double TRACK_START, final double TRACK_SECTION, final double TRACK_STOP, final java.awt.Color TRACK_START_COLOR, final java.awt.Color TRACK_SECTION_COLOR, final java.awt.Color TRACK_STOP_COLOR, final double ROTATION_OFFSET)
    {
        if (WIDTH <= 0)
        {
            return null;
        }

        if (TRACK_STOP > MAX_VALUE)
        {
            throw new IllegalArgumentException("Please adjust track start and/or track range values");
        }

        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, WIDTH, java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final java.awt.geom.Point2D CENTER = new java.awt.geom.Point2D.Double(IMAGE_WIDTH / 2.0, IMAGE_HEIGHT / 2.0);

        final java.awt.geom.GeneralPath TRACK = new java.awt.geom.GeneralPath();
        TRACK.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        TRACK.moveTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.11682242990654206);
        TRACK.curveTo(IMAGE_WIDTH * 0.7149532710280374, IMAGE_HEIGHT * 0.11682242990654206, IMAGE_WIDTH * 0.8878504672897196, IMAGE_HEIGHT * 0.2897196261682243, IMAGE_WIDTH * 0.8878504672897196, IMAGE_HEIGHT * 0.5046728971962616);
        TRACK.curveTo(IMAGE_WIDTH * 0.8878504672897196, IMAGE_HEIGHT * 0.7149532710280374, IMAGE_WIDTH * 0.7149532710280374, IMAGE_HEIGHT * 0.8878504672897196, IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.8878504672897196);
        TRACK.curveTo(IMAGE_WIDTH * 0.2897196261682243, IMAGE_HEIGHT * 0.8878504672897196, IMAGE_WIDTH * 0.11682242990654206, IMAGE_HEIGHT * 0.7149532710280374, IMAGE_WIDTH * 0.11682242990654206, IMAGE_HEIGHT * 0.5046728971962616);
        TRACK.curveTo(IMAGE_WIDTH * 0.11682242990654206, IMAGE_HEIGHT * 0.2897196261682243, IMAGE_WIDTH * 0.2897196261682243, IMAGE_HEIGHT * 0.11682242990654206, IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.11682242990654206);
        TRACK.closePath();
        TRACK.moveTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.14953271028037382);
        TRACK.curveTo(IMAGE_WIDTH * 0.6962616822429907, IMAGE_HEIGHT * 0.14953271028037382, IMAGE_WIDTH * 0.8551401869158879, IMAGE_HEIGHT * 0.308411214953271, IMAGE_WIDTH * 0.8551401869158879, IMAGE_HEIGHT * 0.5046728971962616);
        TRACK.curveTo(IMAGE_WIDTH * 0.8551401869158879, IMAGE_HEIGHT * 0.6962616822429907, IMAGE_WIDTH * 0.6962616822429907, IMAGE_HEIGHT * 0.8551401869158879, IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.8551401869158879);
        TRACK.curveTo(IMAGE_WIDTH * 0.308411214953271, IMAGE_HEIGHT * 0.8551401869158879, IMAGE_WIDTH * 0.14953271028037382, IMAGE_HEIGHT * 0.6962616822429907, IMAGE_WIDTH * 0.14953271028037382, IMAGE_HEIGHT * 0.5046728971962616);
        TRACK.curveTo(IMAGE_WIDTH * 0.14953271028037382, IMAGE_HEIGHT * 0.308411214953271, IMAGE_WIDTH * 0.308411214953271, IMAGE_HEIGHT * 0.14953271028037382, IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.14953271028037382);
        TRACK.closePath();

        final java.awt.Color FULLY_TRANSPARENT = new java.awt.Color(0.0f, 0.0f, 0.0f, 0.0f);

        // Calculate the track start and stop position
        final float TRACK_START_POSITION = (float) Math.toDegrees((TRACK_START - MIN_VALUE) * ANGLE_STEP);
        final float TRACK_STOP_POSITION; // = ANGLE_CONST * (float) Math.toDegrees((TRACK_START + TRACK_RANGE - MIN_VALUE) * ANGLE_STEP) - (ANGLE_CONST * TRACK_START_POSITION);

        // Adjust track stop position in case the track stop position >= 0.99f
        if (ANGLE_CONST * (float) Math.toDegrees((TRACK_STOP - MIN_VALUE) * ANGLE_STEP) - (ANGLE_CONST * TRACK_START_POSITION) >= 0.999f)
        {
            TRACK_STOP_POSITION = 0.998f;
        }
        else
        {
            TRACK_STOP_POSITION = ANGLE_CONST * (float) Math.toDegrees((TRACK_STOP - MIN_VALUE) * ANGLE_STEP) - (ANGLE_CONST * TRACK_START_POSITION);
        }

        final float[] TRACK_FRACTIONS;
        final java.awt.Color[] TRACK_COLORS;

        // Three color gradient from trackStart over trackSection to trackStop
        final float TRACK_SECTION_POSITION = ANGLE_CONST * (float) Math.toDegrees((TRACK_SECTION - MIN_VALUE) * ANGLE_STEP) - (ANGLE_CONST * TRACK_START_POSITION);

        TRACK_FRACTIONS = new float[]
            {
                0.0f,
                0.000001f,
                TRACK_SECTION_POSITION,
                TRACK_STOP_POSITION,
                TRACK_STOP_POSITION + 0.001f,
                1.0f
            };

        TRACK_COLORS = new java.awt.Color[]
            {
                FULLY_TRANSPARENT,
                TRACK_START_COLOR,
                TRACK_SECTION_COLOR,
                TRACK_STOP_COLOR,
                FULLY_TRANSPARENT,
                FULLY_TRANSPARENT,
            };

        final float TRACK_OFFSET = -ANGLE_CONST * (360 - (float) Math.toDegrees(ROTATION_OFFSET)) + (ANGLE_CONST * TRACK_START_POSITION);

        final eu.hansolo.steelseries.tools.ConicalGradientPaint TRACK_GRADIENT = new eu.hansolo.steelseries.tools.ConicalGradientPaint(false, CENTER, TRACK_OFFSET, TRACK_FRACTIONS, TRACK_COLORS);
        G2.setPaint(TRACK_GRADIENT);
        G2.fill(TRACK);

        G2.dispose();

        return IMAGE;
    }

    /**
     * Returns the image of the tickmarks.
     * @param WIDTH
     * @param FREE_AREA_ANGLE
     * @param OFFSET
     * @param MIN_VALUE
     * @param MAX_VALUE
     * @param ANGLE_STEP
     * @param TICK_LABEL_PERIOD
     * @return the tickmarks image that is used
     */
    protected java.awt.image.BufferedImage create_TICKMARKS_Image(final int WIDTH, final double FREE_AREA_ANGLE, final double OFFSET, final double MIN_VALUE, final double MAX_VALUE, final double ANGLE_STEP, final int TICK_LABEL_PERIOD, final int SCALE_DIVIDER_POWER)
    {
        return create_TICKMARKS_Image(WIDTH, FREE_AREA_ANGLE, OFFSET, MIN_VALUE, MAX_VALUE, ANGLE_STEP, TICK_LABEL_PERIOD, SCALE_DIVIDER_POWER, true, true, null);
    }

    /**
     * Returns the image of the tickmarks.
     * @param WIDTH
     * @param FREE_AREA_ANGLE
     * @param OFFSET
     * @param MIN_VALUE
     * @param MAX_VALUE
     * @param ANGLE_STEP
     * @param TICK_LABEL_PERIOD
     * @param SCALE_DIVIDER_POWER
     * @param TICKMARK_SECTIONS
     * @return the tickmarks image that is used
     */
    protected java.awt.image.BufferedImage create_TICKMARKS_Image(final int WIDTH, final double FREE_AREA_ANGLE, final double OFFSET, final double MIN_VALUE, final double MAX_VALUE, final double ANGLE_STEP, final int TICK_LABEL_PERIOD, final int SCALE_DIVIDER_POWER, final boolean DRAW_TICKS, final boolean DRAW_TICK_LABELS, final java.util.ArrayList<eu.hansolo.steelseries.tools.Section> TICKMARK_SECTIONS)
    {
        if (WIDTH <= 0)
        {
            return null;
        }

        if (!DRAW_TICKS && !DRAW_TICK_LABELS)
        {
            return null;
        }

        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, WIDTH, java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_FRACTIONALMETRICS, java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        // Definitions
        final java.awt.Font STD_FONT = new java.awt.Font("Verdana", 0, (int) (0.04 * WIDTH));
        final java.awt.BasicStroke STD_STROKE = new java.awt.BasicStroke(1.0f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_BEVEL);
        final java.awt.BasicStroke MEDIUM_STROKE = new java.awt.BasicStroke(0.5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_BEVEL);
        final java.awt.BasicStroke THIN_STROKE = new java.awt.BasicStroke(0.3f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_BEVEL);
        final java.awt.BasicStroke VERY_THIN_STROKE = new java.awt.BasicStroke(0.1f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_BEVEL);
        final int TEXT_DISTANCE = (int) (0.09 * WIDTH);
        final int TICK_01_LENGTH = (int) (0.01 * WIDTH);
        final int TICK_1_LENGTH = (int) (0.0133333333 * WIDTH);
        final int TICK_5_LENGTH = (int) (0.02 * WIDTH);
        final int TICK_10_LENGTH = (int) (0.03 * WIDTH);
        final int TICK_100_LENGTH = (int) (0.04 * WIDTH);

        // Create the ticks itself
        final float RADIUS = IMAGE_WIDTH * 0.38f;
        final java.awt.geom.Point2D CENTER = new java.awt.geom.Point2D.Double(IMAGE_WIDTH / 2.0f, IMAGE_HEIGHT / 2.0f);

        // Draw ticks
        java.awt.geom.Point2D innerPoint;
        java.awt.geom.Point2D outerPoint;
        java.awt.geom.Point2D textPoint = null;
        java.awt.geom.Line2D tick;
        int counter = 0;

        G2.setFont(STD_FONT);

        double sinValue = 0;
        double cosValue = 0;

        // Quarter circle:            0.5 * Math.PI
        // Half circle:               1.0 * Math.PI
        // ThreeQuarter circle:       1.5 * Math.PI
        // Full circle:               2.0 * Math.PI
        // Direction clockwise:       alpha -= ALPHA_STEP
        // Direction counterclockwise alpha += ALPHA_STEP
        // StartPoint offset top clock: Math.PI
        // StartPoint offset right clock: 0.5 * Math.PI
        // StartPoint offset bottom clock: 0
        // StartPoint offset left clock: -0.5 * Math.PI

        // OFFSET
        //final double TOP = Math.PI;
        //final double RIGHT = -0.5 * Math.PI;
        //final double BOTTOM = 0;
        //final double LEFT = 0.5 * Math.PI;

        final double ALPHA_START = -OFFSET - (FREE_AREA_ANGLE / 2.0); // offset angle for the tickmarks

        // The range from min to max
        final double RANGE = MAX_VALUE - MIN_VALUE;

        /*
         * Different styled tickmarks will be drawn after different intervals.
         * e.g. a small, light dot will be drawn every step whereas a strong bar
         * will be drawn after every 100 steps. Sometimes a step of one for the
         * small, light dot is too fine grained. In this case the whole step
         * scaling can be raised to the next power of ten of the values shown.
         */
        final int STEP_MULTIPLIER = (int) (Math.pow(10, SCALE_DIVIDER_POWER));

        final double SCALED_ANGLE_STEP = ANGLE_STEP * STEP_MULTIPLIER;

        final int ONE_POINT_STEP = STEP_MULTIPLIER;
        final int FIVE_POINT_STEP = 5 * STEP_MULTIPLIER;
        final int TEN_POINT_STEP = 10 * STEP_MULTIPLIER;
        final int HUNDRED_POINT_STEP = 100 * STEP_MULTIPLIER;

        /*  Calculation of thresholds where to show/hide tickmarks dependent on the size of the gauge (full, threequarter, twoquarter, quarter)
         *  The RANGE_THRESHOLD_N value defines the threshold where the tickmarks change.
         *  e.g. If the max value of a full radial gauge is smaller than 20, the smallest tickmarks will be drawn
         *  This means for a half radial gauge (two quarter) the max value must be smaller than 10, for a quarter gauge smaller than 5
         */
        final float RANGE_THRESHOLD_FACTOR = (int) Math.toDegrees((RANGE * SCALED_ANGLE_STEP) + FREE_AREA_ANGLE) / 360f;
        final int RANGE_THRESHOLD_20 = (int) (20 * RANGE_THRESHOLD_FACTOR); // if range smaller than 20
        final int RANGE_THRESHOLD_500 = (int) (300 * RANGE_THRESHOLD_FACTOR); // if range larger than 300
        final int RANGE_THRESHOLD_1000 = (int) (800 * RANGE_THRESHOLD_FACTOR); // if range larger than 800

        // alpha => angle for the tickmarks
        // valueCounter => value for the tickmarks
        for (double alpha = ALPHA_START, valueCounter = MIN_VALUE; valueCounter <= MAX_VALUE; alpha -= SCALED_ANGLE_STEP, valueCounter += STEP_MULTIPLIER)
        {
            if (TICKMARK_SECTIONS != null && !TICKMARK_SECTIONS.isEmpty())
            {
                if (tickmarkSectionsVisible)
                {
                    for (eu.hansolo.steelseries.tools.Section section : TICKMARK_SECTIONS)
                    {
                        if (valueCounter >= section.getStart() && valueCounter <= section.getStop())
                        {
                            G2.setColor(section.getColor());
                            break;
                        }
                        else if (tickmarkColorFromTheme)
                        {
                            G2.setColor(backgroundColor.LABEL_COLOR);
                        }
                        else
                        {
                            G2.setColor(tickmarkColor);
                        }
                    }
                }
                else
                {
                    if (tickmarkColorFromTheme)
                    {
                        G2.setColor(backgroundColor.LABEL_COLOR);
                    }
                    else
                    {
                        G2.setColor(tickmarkColor);
                    }
                }
            }
            else
            {
                if (tickmarkColorFromTheme)
                {
                    G2.setColor(backgroundColor.LABEL_COLOR);
                }
                else
                {
                    G2.setColor(tickmarkColor);
                }
            }

            G2.setStroke(MEDIUM_STROKE);
            sinValue = Math.sin(alpha);
            cosValue = Math.cos(alpha);
            textPoint = new java.awt.geom.Point2D.Double(CENTER.getX() + (RADIUS - TEXT_DISTANCE) * sinValue, CENTER.getY() + (RADIUS - TEXT_DISTANCE) * cosValue);
            innerPoint = new java.awt.geom.Point2D.Double(CENTER.getX() + (RADIUS - TICK_1_LENGTH) * sinValue, CENTER.getY() + (RADIUS - TICK_1_LENGTH) * cosValue);
            outerPoint = new java.awt.geom.Point2D.Double(CENTER.getX() + RADIUS * sinValue, CENTER.getY() + RADIUS * cosValue);

            // Very thin tickmark every 0.1 unit
            if (counter % ONE_POINT_STEP == 0 && RANGE <= RANGE_THRESHOLD_20 && Double.compare(alpha, ALPHA_START) != 0)
            {
                if (DRAW_TICKS)
                {
                    G2.setStroke(VERY_THIN_STROKE);
                    for (double innerAlpha = alpha + SCALED_ANGLE_STEP; innerAlpha > alpha; innerAlpha -= SCALED_ANGLE_STEP / 10.0)
                    {
                        innerPoint = new java.awt.geom.Point2D.Double(CENTER.getX() + (RADIUS - TICK_01_LENGTH) * Math.sin(innerAlpha), CENTER.getY() + (RADIUS - TICK_01_LENGTH) * Math.cos(innerAlpha));
                        outerPoint = new java.awt.geom.Point2D.Double(CENTER.getX() + RADIUS * Math.sin(innerAlpha), CENTER.getY() + RADIUS * Math.cos(innerAlpha));
                        tick = new java.awt.geom.Line2D.Double(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                        G2.draw(tick);
                    }
                }
            }

            // Thin tickmark every 1 unit
            if (counter % ONE_POINT_STEP == 0 && counter % FIVE_POINT_STEP != 0 && counter % TEN_POINT_STEP != 0 && counter % HUNDRED_POINT_STEP != 0 && RANGE < RANGE_THRESHOLD_500)
            {
                if (DRAW_TICKS)
                {
                    G2.setStroke(THIN_STROKE);
                    innerPoint = new java.awt.geom.Point2D.Double(CENTER.getX() + (RADIUS - TICK_1_LENGTH) * sinValue, CENTER.getY() + (RADIUS - TICK_1_LENGTH) * cosValue);
                    outerPoint = new java.awt.geom.Point2D.Double(CENTER.getX() + RADIUS * sinValue, CENTER.getY() + RADIUS * cosValue);

                    tick = new java.awt.geom.Line2D.Double(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                    G2.draw(tick);
                }
            }

            // Medium tickmark every 5 units
            if (counter % FIVE_POINT_STEP == 0 && counter % TEN_POINT_STEP != 0 && counter % HUNDRED_POINT_STEP != 0 && RANGE < RANGE_THRESHOLD_1000)
            {
                if (DRAW_TICKS)
                {
                    G2.setStroke(MEDIUM_STROKE);
                    innerPoint = new java.awt.geom.Point2D.Double(CENTER.getX() + (RADIUS - TICK_5_LENGTH) * sinValue, CENTER.getY() + (RADIUS - TICK_5_LENGTH) * cosValue);
                    outerPoint = new java.awt.geom.Point2D.Double(CENTER.getX() + RADIUS * sinValue, CENTER.getY() + RADIUS * cosValue);

                    tick = new java.awt.geom.Line2D.Double(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                    G2.draw(tick);
                }
            }

            // Standard tickmark every 10 units
            if (counter % TEN_POINT_STEP == 0 && counter % HUNDRED_POINT_STEP != 0 || counter == 0 && RANGE < RANGE_THRESHOLD_1000)
            {
                if (DRAW_TICKS)
                {
                    G2.setStroke(STD_STROKE);
                    innerPoint = new java.awt.geom.Point2D.Double(CENTER.getX() + (RADIUS - TICK_10_LENGTH) * sinValue, CENTER.getY() + (RADIUS - TICK_10_LENGTH) * cosValue);
                    outerPoint = new java.awt.geom.Point2D.Double(CENTER.getX() + RADIUS * sinValue, CENTER.getY() + RADIUS * cosValue);

                    tick = new java.awt.geom.Line2D.Double(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                    G2.draw(tick);
                }
            }

            // Longer standard tickmark every 100 units
            if (counter == HUNDRED_POINT_STEP)
            {
                if (DRAW_TICKS)
                {
                    G2.setStroke(STD_STROKE);
                    innerPoint = new java.awt.geom.Point2D.Double(CENTER.getX() + (RADIUS - TICK_100_LENGTH) * sinValue, CENTER.getY() + (RADIUS - TICK_100_LENGTH) * cosValue);
                    outerPoint = new java.awt.geom.Point2D.Double(CENTER.getX() + RADIUS * sinValue, CENTER.getY() + RADIUS * cosValue);

                    tick = new java.awt.geom.Line2D.Double(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());

                    G2.draw(tick);
                }

                counter = 0;
            }

            // Draw the tickmark labels
            if (DRAW_TICK_LABELS)
            {
                if (useCustomTickmarkLabels)
                {
                    // Draw custom tickmark labels if selected
                    for (double tickLabel : CUSTOM_TICKMARK_LABELS)
                    {
                        if (Double.compare(valueCounter, tickLabel) == 0)
                        {
                            G2.fill(UTIL.rotateTextAroundCenter(G2, String.valueOf((int) valueCounter), (int) textPoint.getX(), (int) textPoint.getY(), Math.toDegrees(Math.PI - alpha)));
                            break;
                        }
                    }
                }
                else
                {
                    // Draw the standard tickmark labels
                    if (valueCounter % TICK_LABEL_PERIOD == 0)
                    {
                        G2.fill(UTIL.rotateTextAroundCenter(G2, String.valueOf((int) valueCounter), (int) textPoint.getX(), (int) textPoint.getY(), Math.toDegrees(Math.PI - alpha)));
                    }
                }
            }

            counter += STEP_MULTIPLIER;
        }

        G2.dispose();

        return IMAGE;
    }

    protected java.awt.image.BufferedImage create_POSTS_Image(final int WIDTH, final eu.hansolo.steelseries.tools.PostPosition... POSITIONS)
    {
        if (WIDTH <= 0)
        {
            return null;
        }

        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, WIDTH, java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final java.util.List<eu.hansolo.steelseries.tools.PostPosition> POST_POSITION_LIST = java.util.Arrays.asList(POSITIONS);

        // Draw center knob
        if (POST_POSITION_LIST.contains(eu.hansolo.steelseries.tools.PostPosition.CENTER))
        {
            final java.awt.geom.Ellipse2D CENTER_KNOB_FRAME = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.4579439163208008, IMAGE_HEIGHT * 0.4579439163208008, IMAGE_WIDTH * 0.08411216735839844, IMAGE_HEIGHT * 0.08411216735839844);
            final java.awt.geom.Point2D CENTER_KNOB_FRAME_START = new java.awt.geom.Point2D.Double(0, CENTER_KNOB_FRAME.getBounds2D().getMinY());
            final java.awt.geom.Point2D CENTER_KNOB_FRAME_STOP = new java.awt.geom.Point2D.Double(0, CENTER_KNOB_FRAME.getBounds2D().getMaxY());
            final float[] CENTER_KNOB_FRAME_FRACTIONS =
            {
                0.0f,
                0.46f,
                1.0f
            };
            final java.awt.Color[] CENTER_KNOB_FRAME_COLORS =
            {
                new java.awt.Color(180, 180, 180, 255),
                new java.awt.Color(63, 63, 63, 255),
                new java.awt.Color(40, 40, 40, 255)
            };

            final java.awt.LinearGradientPaint CENTER_KNOB_FRAME_GRADIENT = new java.awt.LinearGradientPaint(CENTER_KNOB_FRAME_START, CENTER_KNOB_FRAME_STOP, CENTER_KNOB_FRAME_FRACTIONS, CENTER_KNOB_FRAME_COLORS);
            G2.setPaint(CENTER_KNOB_FRAME_GRADIENT);
            G2.fill(CENTER_KNOB_FRAME);

            final java.awt.geom.Ellipse2D CENTER_KNOB_MAIN = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.4672897160053253, IMAGE_HEIGHT * 0.4672897160053253, IMAGE_WIDTH * 0.06542053818702698, IMAGE_HEIGHT * 0.06542053818702698);
            final java.awt.geom.Point2D CENTER_KNOB_MAIN_START = new java.awt.geom.Point2D.Double(0, CENTER_KNOB_MAIN.getBounds2D().getMinY());
            final java.awt.geom.Point2D CENTER_KNOB_MAIN_STOP = new java.awt.geom.Point2D.Double(0, CENTER_KNOB_MAIN.getBounds2D().getMaxY());
            final float[] CENTER_KNOB_MAIN_FRACTIONS =
            {
                0.0f,
                1.0f
            };
            final java.awt.Color[] CENTER_KNOB_MAIN_COLORS =
            {
                new java.awt.Color(217, 217, 217, 255),
                new java.awt.Color(191, 191, 191, 255)
            };

            final java.awt.LinearGradientPaint CENTER_KNOB_MAIN_GRADIENT = new java.awt.LinearGradientPaint(CENTER_KNOB_MAIN_START, CENTER_KNOB_MAIN_STOP, CENTER_KNOB_MAIN_FRACTIONS, CENTER_KNOB_MAIN_COLORS);
            G2.setPaint(CENTER_KNOB_MAIN_GRADIENT);
            G2.fill(CENTER_KNOB_MAIN);

            final java.awt.geom.Ellipse2D CENTER_KNOB_INNERSHADOW = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.4672897160053253, IMAGE_HEIGHT * 0.4672897160053253, IMAGE_WIDTH * 0.06542053818702698, IMAGE_HEIGHT * 0.06542053818702698);
            final java.awt.geom.Point2D CENTER_KNOB_INNERSHADOW_CENTER = new java.awt.geom.Point2D.Double((0.4953271028037383 * IMAGE_WIDTH), (0.49065420560747663 * IMAGE_HEIGHT));
            final float[] CENTER_KNOB_INNERSHADOW_FRACTIONS =
            {
                0.0f,
                0.75f,
                0.76f,
                1.0f
            };
            final java.awt.Color[] CENTER_KNOB_INNERSHADOW_COLORS =
            {
                new java.awt.Color(0, 0, 0, 0),
                new java.awt.Color(0, 0, 0, 0),
                new java.awt.Color(0, 0, 0, 1),
                new java.awt.Color(0, 0, 0, 51)
            };
            final java.awt.RadialGradientPaint CENTER_KNOB_INNERSHADOW_GRADIENT = new java.awt.RadialGradientPaint(CENTER_KNOB_INNERSHADOW_CENTER, (float) (0.03271028037383177 * IMAGE_WIDTH), CENTER_KNOB_INNERSHADOW_FRACTIONS, CENTER_KNOB_INNERSHADOW_COLORS);
            G2.setPaint(CENTER_KNOB_INNERSHADOW_GRADIENT);
            G2.fill(CENTER_KNOB_INNERSHADOW);
        }

        // Draw min bottom
        if (POST_POSITION_LIST.contains(eu.hansolo.steelseries.tools.PostPosition.MIN_BOTTOM))
        {
            final java.awt.geom.Ellipse2D MIN_POST_FRAME_BOTTOM = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.336448609828949, IMAGE_HEIGHT * 0.8037382960319519, IMAGE_WIDTH * 0.03738316893577576, IMAGE_HEIGHT * 0.037383198738098145);
            final java.awt.geom.Point2D MIN_POST_FRAME_BOTTOM_START = new java.awt.geom.Point2D.Double(0, MIN_POST_FRAME_BOTTOM.getBounds2D().getMinY());
            final java.awt.geom.Point2D MIN_POST_FRAME_BOTTOM_STOP = new java.awt.geom.Point2D.Double(0, MIN_POST_FRAME_BOTTOM.getBounds2D().getMaxY());
            final float[] MIN_POST_FRAME_BOTTOM_FRACTIONS =
            {
                0.0f,
                0.46f,
                1.0f
            };
            final java.awt.Color[] MIN_POST_FRAME_BOTTOM_COLORS =
            {
                new java.awt.Color(180, 180, 180, 255),
                new java.awt.Color(63, 63, 63, 255),
                new java.awt.Color(40, 40, 40, 255)
            };

            final java.awt.LinearGradientPaint MIN_POST_FRAME_BOTTOM_GRADIENT = new java.awt.LinearGradientPaint(MIN_POST_FRAME_BOTTOM_START, MIN_POST_FRAME_BOTTOM_STOP, MIN_POST_FRAME_BOTTOM_FRACTIONS, MIN_POST_FRAME_BOTTOM_COLORS);
            G2.setPaint(MIN_POST_FRAME_BOTTOM_GRADIENT);
            G2.fill(MIN_POST_FRAME_BOTTOM);

            final java.awt.geom.Ellipse2D MIN_POST_MAIN_BOTTOM = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.34112149477005005, IMAGE_HEIGHT * 0.8084112405776978, IMAGE_WIDTH * 0.02803736925125122, IMAGE_HEIGHT * 0.02803736925125122);
            final java.awt.geom.Point2D MIN_POST_MAIN_BOTTOM_START = new java.awt.geom.Point2D.Double(0, MIN_POST_MAIN_BOTTOM.getBounds2D().getMinY());
            final java.awt.geom.Point2D MIN_POST_MAIN_BOTTOM_STOP = new java.awt.geom.Point2D.Double(0, MIN_POST_MAIN_BOTTOM.getBounds2D().getMaxY());
            final float[] MIN_POST_MAIN_BOTTOM_FRACTIONS =
            {
                0.0f,
                1.0f
            };
            final java.awt.Color[] MIN_POST_MAIN_BOTTOM_COLORS =
            {
                new java.awt.Color(217, 217, 217, 255),
                new java.awt.Color(191, 191, 191, 255)
            };

            final java.awt.LinearGradientPaint MIN_POST_MAIN_BOTTOM_GRADIENT = new java.awt.LinearGradientPaint(MIN_POST_MAIN_BOTTOM_START, MIN_POST_MAIN_BOTTOM_STOP, MIN_POST_MAIN_BOTTOM_FRACTIONS, MIN_POST_MAIN_BOTTOM_COLORS);
            G2.setPaint(MIN_POST_MAIN_BOTTOM_GRADIENT);
            G2.fill(MIN_POST_MAIN_BOTTOM);

            final java.awt.geom.Ellipse2D MIN_POST_INNERSHADOW_BOTTOM = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.34112149477005005, IMAGE_HEIGHT * 0.8084112405776978, IMAGE_WIDTH * 0.02803736925125122, IMAGE_HEIGHT * 0.02803736925125122);
            final java.awt.geom.Point2D MIN_POST_INNERSHADOW_BOTTOM_CENTER = new java.awt.geom.Point2D.Double((0.35514018691588783 * IMAGE_WIDTH), (0.8177570093457944 * IMAGE_HEIGHT));
            final float[] MIN_POST_INNERSHADOW_BOTTOM_FRACTIONS =
            {
                0.0f,
                0.75f,
                0.76f,
                1.0f
            };
            final java.awt.Color[] MIN_POST_INNERSHADOW_BOTTOM_COLORS =
            {
                new java.awt.Color(0, 0, 0, 0),
                new java.awt.Color(0, 0, 0, 0),
                new java.awt.Color(0, 0, 0, 1),
                new java.awt.Color(0, 0, 0, 51)
            };
            final java.awt.RadialGradientPaint MIN_POST_INNERSHADOW_BOTTOM_GRADIENT = new java.awt.RadialGradientPaint(MIN_POST_INNERSHADOW_BOTTOM_CENTER, (float) (0.014018691588785047 * IMAGE_WIDTH), MIN_POST_INNERSHADOW_BOTTOM_FRACTIONS, MIN_POST_INNERSHADOW_BOTTOM_COLORS);
            G2.setPaint(MIN_POST_INNERSHADOW_BOTTOM_GRADIENT);
            G2.fill(MIN_POST_INNERSHADOW_BOTTOM);
        }

        // Draw max bottom post
        if (POST_POSITION_LIST.contains(eu.hansolo.steelseries.tools.PostPosition.MAX_BOTTOM))
        {
            final java.awt.geom.Ellipse2D MAX_POST_FRAME_BOTTOM = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.6261682510375977, IMAGE_HEIGHT * 0.8037382960319519, IMAGE_WIDTH * 0.03738313913345337, IMAGE_HEIGHT * 0.037383198738098145);
            final java.awt.geom.Point2D MAX_POST_FRAME_BOTTOM_START = new java.awt.geom.Point2D.Double(0, MAX_POST_FRAME_BOTTOM.getBounds2D().getMinY());
            final java.awt.geom.Point2D MAX_POST_FRAME_BOTTOM_STOP = new java.awt.geom.Point2D.Double(0, MAX_POST_FRAME_BOTTOM.getBounds2D().getMaxY());
            final float[] MAX_POST_FRAME_BOTTOM_FRACTIONS =
            {
                0.0f,
                0.46f,
                1.0f
            };
            final java.awt.Color[] MAX_POST_FRAME_BOTTOM_COLORS =
            {
                new java.awt.Color(180, 180, 180, 255),
                new java.awt.Color(63, 63, 63, 255),
                new java.awt.Color(40, 40, 40, 255)
            };

            final java.awt.LinearGradientPaint MAX_POST_FRAME_BOTTOM_GRADIENT = new java.awt.LinearGradientPaint(MAX_POST_FRAME_BOTTOM_START, MAX_POST_FRAME_BOTTOM_STOP, MAX_POST_FRAME_BOTTOM_FRACTIONS, MAX_POST_FRAME_BOTTOM_COLORS);
            G2.setPaint(MAX_POST_FRAME_BOTTOM_GRADIENT);
            G2.fill(MAX_POST_FRAME_BOTTOM);

            final java.awt.geom.Ellipse2D MAX_POST_MAIN_BOTTOM = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.6308411359786987, IMAGE_HEIGHT * 0.8084112405776978, IMAGE_WIDTH * 0.02803736925125122, IMAGE_HEIGHT * 0.02803736925125122);
            final java.awt.geom.Point2D MAX_POST_MAIN_BOTTOM_START = new java.awt.geom.Point2D.Double(0, MAX_POST_MAIN_BOTTOM.getBounds2D().getMinY());
            final java.awt.geom.Point2D MAX_POST_MAIN_BOTTOM_STOP = new java.awt.geom.Point2D.Double(0, MAX_POST_MAIN_BOTTOM.getBounds2D().getMaxY());
            final float[] MAX_POST_MAIN_BOTTOM_FRACTIONS =
            {
                0.0f,
                1.0f
            };
            final java.awt.Color[] MAX_POST_MAIN_BOTTOM_COLORS =
            {
                new java.awt.Color(217, 217, 217, 255),
                new java.awt.Color(191, 191, 191, 255)
            };

            final java.awt.LinearGradientPaint MAX_POST_MAIN_BOTTOM_GRADIENT = new java.awt.LinearGradientPaint(MAX_POST_MAIN_BOTTOM_START, MAX_POST_MAIN_BOTTOM_STOP, MAX_POST_MAIN_BOTTOM_FRACTIONS, MAX_POST_MAIN_BOTTOM_COLORS);
            G2.setPaint(MAX_POST_MAIN_BOTTOM_GRADIENT);
            G2.fill(MAX_POST_MAIN_BOTTOM);

            final java.awt.geom.Ellipse2D MAX_POST_INNERSHADOW_BOTTOM = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.6308411359786987, IMAGE_HEIGHT * 0.8084112405776978, IMAGE_WIDTH * 0.02803736925125122, IMAGE_HEIGHT * 0.02803736925125122);
            final java.awt.geom.Point2D MAX_POST_INNERSHADOW_BOTTOM_CENTER = new java.awt.geom.Point2D.Double((0.6448598130841121 * IMAGE_WIDTH), (0.8177570093457944 * IMAGE_HEIGHT));
            final float[] MAX_POST_INNERSHADOW_BOTTOM_FRACTIONS =
            {
                0.0f,
                0.75f,
                0.76f,
                1.0f
            };
            final java.awt.Color[] MAX_POST_INNERSHADOW_BOTTOM_COLORS =
            {
                new java.awt.Color(0, 0, 0, 0),
                new java.awt.Color(0, 0, 0, 0),
                new java.awt.Color(0, 0, 0, 1),
                new java.awt.Color(0, 0, 0, 51)
            };
            final java.awt.RadialGradientPaint MAX_POST_INNERSHADOW_BOTTOM_GRADIENT = new java.awt.RadialGradientPaint(MAX_POST_INNERSHADOW_BOTTOM_CENTER, (float) (0.014018691588785047 * IMAGE_WIDTH), MAX_POST_INNERSHADOW_BOTTOM_FRACTIONS, MAX_POST_INNERSHADOW_BOTTOM_COLORS);
            G2.setPaint(MAX_POST_INNERSHADOW_BOTTOM_GRADIENT);
            G2.fill(MAX_POST_INNERSHADOW_BOTTOM);
        }

        // Draw max center bottom post
        if (POST_POSITION_LIST.contains(eu.hansolo.steelseries.tools.PostPosition.MAX_CENTER_BOTTOM))
        {
            final java.awt.geom.Ellipse2D MAX_POST_FRAME_CENTERBOTTOM = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.5233644843101501, IMAGE_HEIGHT * 0.8317757248878479, IMAGE_WIDTH * 0.037383198738098145, IMAGE_HEIGHT * 0.03738313913345337);
            final java.awt.geom.Point2D MAX_POST_FRAME_CENTERBOTTOM_START = new java.awt.geom.Point2D.Double(0, MAX_POST_FRAME_CENTERBOTTOM.getBounds2D().getMinY());
            final java.awt.geom.Point2D MAX_POST_FRAME_CENTERBOTTOM_STOP = new java.awt.geom.Point2D.Double(0, MAX_POST_FRAME_CENTERBOTTOM.getBounds2D().getMaxY());
            final float[] MAX_POST_FRAME_CENTERBOTTOM_FRACTIONS =
            {
                0.0f,
                0.46f,
                1.0f
            };
            final java.awt.Color[] MAX_POST_FRAME_CENTERBOTTOM_COLORS =
            {
                new java.awt.Color(180, 180, 180, 255),
                new java.awt.Color(63, 63, 63, 255),
                new java.awt.Color(40, 40, 40, 255)
            };

            final java.awt.LinearGradientPaint MAX_POST_FRAME_CENTERBOTTOM_GRADIENT = new java.awt.LinearGradientPaint(MAX_POST_FRAME_CENTERBOTTOM_START, MAX_POST_FRAME_CENTERBOTTOM_STOP, MAX_POST_FRAME_CENTERBOTTOM_FRACTIONS, MAX_POST_FRAME_CENTERBOTTOM_COLORS);
            G2.setPaint(MAX_POST_FRAME_CENTERBOTTOM_GRADIENT);
            G2.fill(MAX_POST_FRAME_CENTERBOTTOM);

            final java.awt.geom.Ellipse2D MAX_POST_MAIN_CENTERBOTTOM = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.5280373692512512, IMAGE_HEIGHT * 0.836448609828949, IMAGE_WIDTH * 0.02803736925125122, IMAGE_HEIGHT * 0.02803736925125122);
            final java.awt.geom.Point2D MAX_POST_MAIN_CENTERBOTTOM_START = new java.awt.geom.Point2D.Double(0, MAX_POST_MAIN_CENTERBOTTOM.getBounds2D().getMinY());
            final java.awt.geom.Point2D MAX_POST_MAIN_CENTERBOTTOM_STOP = new java.awt.geom.Point2D.Double(0, MAX_POST_MAIN_CENTERBOTTOM.getBounds2D().getMaxY());
            final float[] MAX_POST_MAIN_CENTERBOTTOM_FRACTIONS =
            {
                0.0f,
                1.0f
            };
            final java.awt.Color[] MAX_POST_MAIN_CENTERBOTTOM_COLORS =
            {
                new java.awt.Color(217, 217, 217, 255),
                new java.awt.Color(191, 191, 191, 255)
            };

            final java.awt.LinearGradientPaint MAX_POST_MAIN_CENTERBOTTOM_GRADIENT = new java.awt.LinearGradientPaint(MAX_POST_MAIN_CENTERBOTTOM_START, MAX_POST_MAIN_CENTERBOTTOM_STOP, MAX_POST_MAIN_CENTERBOTTOM_FRACTIONS, MAX_POST_MAIN_CENTERBOTTOM_COLORS);
            G2.setPaint(MAX_POST_MAIN_CENTERBOTTOM_GRADIENT);
            G2.fill(MAX_POST_MAIN_CENTERBOTTOM);

            final java.awt.geom.Ellipse2D MAX_POST_INNERSHADOW_CENTERBOTTOM = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.5280373692512512, IMAGE_HEIGHT * 0.836448609828949, IMAGE_WIDTH * 0.02803736925125122, IMAGE_HEIGHT * 0.02803736925125122);
            final java.awt.geom.Point2D MAX_POST_INNERSHADOW_CENTERBOTTOM_CENTER = new java.awt.geom.Point2D.Double((0.5420560747663551 * IMAGE_WIDTH), (0.8457943925233645 * IMAGE_HEIGHT));
            final float[] MAX_POST_INNERSHADOW_CENTERBOTTOM_FRACTIONS =
            {
                0.0f,
                0.75f,
                0.76f,
                1.0f
            };
            final java.awt.Color[] MAX_POST_INNERSHADOW_CENTERBOTTOM_COLORS =
            {
                new java.awt.Color(0, 0, 0, 0),
                new java.awt.Color(0, 0, 0, 0),
                new java.awt.Color(0, 0, 0, 1),
                new java.awt.Color(0, 0, 0, 51)
            };
            final java.awt.RadialGradientPaint MAX_POST_INNERSHADOW_CENTERBOTTOM_GRADIENT = new java.awt.RadialGradientPaint(MAX_POST_INNERSHADOW_CENTERBOTTOM_CENTER, (float) (0.014018691588785047 * IMAGE_WIDTH), MAX_POST_INNERSHADOW_CENTERBOTTOM_FRACTIONS, MAX_POST_INNERSHADOW_CENTERBOTTOM_COLORS);
            G2.setPaint(MAX_POST_INNERSHADOW_CENTERBOTTOM_GRADIENT);
            G2.fill(MAX_POST_INNERSHADOW_CENTERBOTTOM);
        }

        // Draw max center top post
        if (POST_POSITION_LIST.contains(eu.hansolo.steelseries.tools.PostPosition.MAX_CENTER_TOP))
        {
            final java.awt.geom.Ellipse2D MAX_POST_FRAME_CENTERTOP = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.5233644843101501, IMAGE_HEIGHT * 0.13084112107753754, IMAGE_WIDTH * 0.037383198738098145, IMAGE_HEIGHT * 0.03738318383693695);
            final java.awt.geom.Point2D MAX_POST_FRAME_CENTERTOP_START = new java.awt.geom.Point2D.Double(0, MAX_POST_FRAME_CENTERTOP.getBounds2D().getMinY());
            final java.awt.geom.Point2D MAX_POST_FRAME_CENTERTOP_STOP = new java.awt.geom.Point2D.Double(0, MAX_POST_FRAME_CENTERTOP.getBounds2D().getMaxY());
            final float[] MAX_POST_FRAME_CENTERTOP_FRACTIONS =
            {
                0.0f,
                0.46f,
                1.0f
            };
            final java.awt.Color[] MAX_POST_FRAME_CENTERTOP_COLORS =
            {
                new java.awt.Color(180, 180, 180, 255),
                new java.awt.Color(63, 63, 63, 255),
                new java.awt.Color(40, 40, 40, 255)
            };

            final java.awt.LinearGradientPaint MAX_POST_FRAME_CENTERTOP_GRADIENT = new java.awt.LinearGradientPaint(MAX_POST_FRAME_CENTERTOP_START, MAX_POST_FRAME_CENTERTOP_STOP, MAX_POST_FRAME_CENTERTOP_FRACTIONS, MAX_POST_FRAME_CENTERTOP_COLORS);
            G2.setPaint(MAX_POST_FRAME_CENTERTOP_GRADIENT);
            G2.fill(MAX_POST_FRAME_CENTERTOP);

            final java.awt.geom.Ellipse2D MAX_POST_MAIN_CENTERTOP = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.5280373692512512, IMAGE_HEIGHT * 0.1355140209197998, IMAGE_WIDTH * 0.02803736925125122, IMAGE_HEIGHT * 0.028037384152412415);
            final java.awt.geom.Point2D MAX_POST_MAIN_CENTERTOP_START = new java.awt.geom.Point2D.Double(0, MAX_POST_MAIN_CENTERTOP.getBounds2D().getMinY());
            final java.awt.geom.Point2D MAX_POST_MAIN_CENTERTOP_STOP = new java.awt.geom.Point2D.Double(0, MAX_POST_MAIN_CENTERTOP.getBounds2D().getMaxY());
            final float[] MAX_POST_MAIN_CENTERTOP_FRACTIONS =
            {
                0.0f,
                1.0f
            };
            final java.awt.Color[] MAX_POST_MAIN_CENTERTOP_COLORS =
            {
                new java.awt.Color(217, 217, 217, 255),
                new java.awt.Color(191, 191, 191, 255)
            };

            final java.awt.LinearGradientPaint MAX_POST_MAIN_CENTERTOP_GRADIENT = new java.awt.LinearGradientPaint(MAX_POST_MAIN_CENTERTOP_START, MAX_POST_MAIN_CENTERTOP_STOP, MAX_POST_MAIN_CENTERTOP_FRACTIONS, MAX_POST_MAIN_CENTERTOP_COLORS);
            G2.setPaint(MAX_POST_MAIN_CENTERTOP_GRADIENT);
            G2.fill(MAX_POST_MAIN_CENTERTOP);

            final java.awt.geom.Ellipse2D MAX_POST_INNERSHADOW_CENTERTOP = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.5280373692512512, IMAGE_HEIGHT * 0.1355140209197998, IMAGE_WIDTH * 0.02803736925125122, IMAGE_HEIGHT * 0.028037384152412415);
            final java.awt.geom.Point2D MAX_POST_INNERSHADOW_CENTERTOP_CENTER = new java.awt.geom.Point2D.Double((0.5420560747663551 * IMAGE_WIDTH), (0.14485981308411214 * IMAGE_HEIGHT));
            final float[] MAX_POST_INNERSHADOW_CENTERTOP_FRACTIONS =
            {
                0.0f,
                0.75f,
                0.76f,
                1.0f
            };
            final java.awt.Color[] MAX_POST_INNERSHADOW_CENTERTOP_COLORS =
            {
                new java.awt.Color(0, 0, 0, 0),
                new java.awt.Color(0, 0, 0, 0),
                new java.awt.Color(0, 0, 0, 1),
                new java.awt.Color(0, 0, 0, 51)
            };
            final java.awt.RadialGradientPaint MAX_POST_INNERSHADOW_CENTERTOP_GRADIENT = new java.awt.RadialGradientPaint(MAX_POST_INNERSHADOW_CENTERTOP_CENTER, (float) (0.014018691588785047 * IMAGE_WIDTH), MAX_POST_INNERSHADOW_CENTERTOP_FRACTIONS, MAX_POST_INNERSHADOW_CENTERTOP_COLORS);
            G2.setPaint(MAX_POST_INNERSHADOW_CENTERTOP_GRADIENT);
            G2.fill(MAX_POST_INNERSHADOW_CENTERTOP);
        }

        // Draw max right post
        if (POST_POSITION_LIST.contains(eu.hansolo.steelseries.tools.PostPosition.MAX_RIGHT))
        {
            final java.awt.geom.Ellipse2D MAX_POST_FRAME_RIGHT = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.8317757248878479, IMAGE_HEIGHT * 0.514018714427948, IMAGE_WIDTH * 0.03738313913345337, IMAGE_HEIGHT * 0.03738313913345337);
            final java.awt.geom.Point2D MAX_POST_FRAME_RIGHT_START = new java.awt.geom.Point2D.Double(0, MAX_POST_FRAME_RIGHT.getBounds2D().getMinY());
            final java.awt.geom.Point2D MAX_POST_FRAME_RIGHT_STOP = new java.awt.geom.Point2D.Double(0, MAX_POST_FRAME_RIGHT.getBounds2D().getMaxY());
            final float[] MAX_POST_FRAME_RIGHT_FRACTIONS =
            {
                0.0f,
                0.46f,
                1.0f
            };
            final java.awt.Color[] MAX_POST_FRAME_RIGHT_COLORS =
            {
                new java.awt.Color(180, 180, 180, 255),
                new java.awt.Color(63, 63, 63, 255),
                new java.awt.Color(40, 40, 40, 255)
            };

            final java.awt.LinearGradientPaint MAX_POST_FRAME_RIGHT_GRADIENT = new java.awt.LinearGradientPaint(MAX_POST_FRAME_RIGHT_START, MAX_POST_FRAME_RIGHT_STOP, MAX_POST_FRAME_RIGHT_FRACTIONS, MAX_POST_FRAME_RIGHT_COLORS);
            G2.setPaint(MAX_POST_FRAME_RIGHT_GRADIENT);
            G2.fill(MAX_POST_FRAME_RIGHT);

            final java.awt.geom.Ellipse2D MAX_POST_MAIN_RIGHT = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.836448609828949, IMAGE_HEIGHT * 0.5186915993690491, IMAGE_WIDTH * 0.02803736925125122, IMAGE_HEIGHT * 0.02803736925125122);
            final java.awt.geom.Point2D MAX_POST_MAIN_RIGHT_START = new java.awt.geom.Point2D.Double(0, MAX_POST_MAIN_RIGHT.getBounds2D().getMinY());
            final java.awt.geom.Point2D MAX_POST_MAIN_RIGHT_STOP = new java.awt.geom.Point2D.Double(0, MAX_POST_MAIN_RIGHT.getBounds2D().getMaxY());
            final float[] MAX_POST_MAIN_RIGHT_FRACTIONS =
            {
                0.0f,
                1.0f
            };
            final java.awt.Color[] MAX_POST_MAIN_RIGHT_COLORS =
            {
                new java.awt.Color(217, 217, 217, 255),
                new java.awt.Color(191, 191, 191, 255)
            };

            final java.awt.LinearGradientPaint MAX_POST_MAIN_RIGHT_GRADIENT = new java.awt.LinearGradientPaint(MAX_POST_MAIN_RIGHT_START, MAX_POST_MAIN_RIGHT_STOP, MAX_POST_MAIN_RIGHT_FRACTIONS, MAX_POST_MAIN_RIGHT_COLORS);
            G2.setPaint(MAX_POST_MAIN_RIGHT_GRADIENT);
            G2.fill(MAX_POST_MAIN_RIGHT);

            final java.awt.geom.Ellipse2D MAX_POST_INNERSHADOW_RIGHT = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.836448609828949, IMAGE_HEIGHT * 0.5186915993690491, IMAGE_WIDTH * 0.02803736925125122, IMAGE_HEIGHT * 0.02803736925125122);
            final java.awt.geom.Point2D MAX_POST_INNERSHADOW_RIGHT_CENTER = new java.awt.geom.Point2D.Double((0.8504672897196262 * IMAGE_WIDTH), (0.5280373831775701 * IMAGE_HEIGHT));
            final float[] MAX_POST_INNERSHADOW_RIGHT_FRACTIONS =
            {
                0.0f,
                0.75f,
                0.76f,
                1.0f
            };
            final java.awt.Color[] MAX_POST_INNERSHADOW_RIGHT_COLORS =
            {
                new java.awt.Color(0, 0, 0, 0),
                new java.awt.Color(0, 0, 0, 0),
                new java.awt.Color(0, 0, 0, 1),
                new java.awt.Color(0, 0, 0, 51)
            };
            final java.awt.RadialGradientPaint MAX_POST_INNERSHADOW_RIGHT_GRADIENT = new java.awt.RadialGradientPaint(MAX_POST_INNERSHADOW_RIGHT_CENTER, (float) (0.014018691588785047 * IMAGE_WIDTH), MAX_POST_INNERSHADOW_RIGHT_FRACTIONS, MAX_POST_INNERSHADOW_RIGHT_COLORS);
            G2.setPaint(MAX_POST_INNERSHADOW_RIGHT_GRADIENT);
            G2.fill(MAX_POST_INNERSHADOW_RIGHT);
        }

        // Draw min left post
        if (POST_POSITION_LIST.contains(eu.hansolo.steelseries.tools.PostPosition.MIN_LEFT))
        {
            final java.awt.geom.Ellipse2D MIN_POST_FRAME_LEFT = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.13084112107753754, IMAGE_HEIGHT * 0.514018714427948, IMAGE_WIDTH * 0.03738318383693695, IMAGE_HEIGHT * 0.03738313913345337);
            final java.awt.geom.Point2D MIN_POST_FRAME_LEFT_START = new java.awt.geom.Point2D.Double(0, MIN_POST_FRAME_LEFT.getBounds2D().getMinY());
            final java.awt.geom.Point2D MIN_POST_FRAME_LEFT_STOP = new java.awt.geom.Point2D.Double(0, MIN_POST_FRAME_LEFT.getBounds2D().getMaxY());
            final float[] MIN_POST_FRAME_LEFT_FRACTIONS =
            {
                0.0f,
                0.46f,
                1.0f
            };
            final java.awt.Color[] MIN_POST_FRAME_LEFT_COLORS =
            {
                new java.awt.Color(180, 180, 180, 255),
                new java.awt.Color(63, 63, 63, 255),
                new java.awt.Color(40, 40, 40, 255)
            };

            final java.awt.LinearGradientPaint MIN_POST_FRAME_LEFT_GRADIENT = new java.awt.LinearGradientPaint(MIN_POST_FRAME_LEFT_START, MIN_POST_FRAME_LEFT_STOP, MIN_POST_FRAME_LEFT_FRACTIONS, MIN_POST_FRAME_LEFT_COLORS);
            G2.setPaint(MIN_POST_FRAME_LEFT_GRADIENT);
            G2.fill(MIN_POST_FRAME_LEFT);

            final java.awt.geom.Ellipse2D MIN_POST_MAIN_LEFT = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.1355140209197998, IMAGE_HEIGHT * 0.5186915993690491, IMAGE_WIDTH * 0.028037384152412415, IMAGE_HEIGHT * 0.02803736925125122);
            final java.awt.geom.Point2D MIN_POST_MAIN_LEFT_START = new java.awt.geom.Point2D.Double(0, MIN_POST_MAIN_LEFT.getBounds2D().getMinY());
            final java.awt.geom.Point2D MIN_POST_MAIN_LEFT_STOP = new java.awt.geom.Point2D.Double(0, MIN_POST_MAIN_LEFT.getBounds2D().getMaxY());
            final float[] MIN_POST_MAIN_LEFT_FRACTIONS =
            {
                0.0f,
                1.0f
            };
            final java.awt.Color[] MIN_POST_MAIN_LEFT_COLORS =
            {
                new java.awt.Color(217, 217, 217, 255),
                new java.awt.Color(191, 191, 191, 255)
            };

            final java.awt.LinearGradientPaint MIN_POST_MAIN_LEFT_GRADIENT = new java.awt.LinearGradientPaint(MIN_POST_MAIN_LEFT_START, MIN_POST_MAIN_LEFT_STOP, MIN_POST_MAIN_LEFT_FRACTIONS, MIN_POST_MAIN_LEFT_COLORS);
            G2.setPaint(MIN_POST_MAIN_LEFT_GRADIENT);
            G2.fill(MIN_POST_MAIN_LEFT);

            final java.awt.geom.Ellipse2D MIN_POST_INNERSHADOW_LEFT = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.1355140209197998, IMAGE_HEIGHT * 0.5186915993690491, IMAGE_WIDTH * 0.028037384152412415, IMAGE_HEIGHT * 0.02803736925125122);
            final java.awt.geom.Point2D MIN_POST_INNERSHADOW_LEFT_CENTER = new java.awt.geom.Point2D.Double((0.14953271028037382 * IMAGE_WIDTH), (0.5280373831775701 * IMAGE_HEIGHT));
            final float[] MIN_POST_INNERSHADOW_LEFT_FRACTIONS =
            {
                0.0f,
                0.75f,
                0.76f,
                1.0f
            };
            final java.awt.Color[] MIN_POST_INNERSHADOW_LEFT_COLORS =
            {
                new java.awt.Color(0, 0, 0, 0),
                new java.awt.Color(0, 0, 0, 0),
                new java.awt.Color(0, 0, 0, 1),
                new java.awt.Color(0, 0, 0, 51)
            };
            final java.awt.RadialGradientPaint MIN_POST_INNERSHADOW_LEFT_GRADIENT = new java.awt.RadialGradientPaint(MIN_POST_INNERSHADOW_LEFT_CENTER, (float) (0.014018691588785047 * IMAGE_WIDTH), MIN_POST_INNERSHADOW_LEFT_FRACTIONS, MIN_POST_INNERSHADOW_LEFT_COLORS);
            G2.setPaint(MIN_POST_INNERSHADOW_LEFT_GRADIENT);
            G2.fill(MIN_POST_INNERSHADOW_LEFT);
        }

        // Draw lower center post
        if (POST_POSITION_LIST.contains(eu.hansolo.steelseries.tools.PostPosition.LOWER_CENTER))
        {
            final java.awt.geom.Ellipse2D LOWERCENTER_KNOB_FRAME = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.4579439163208008, IMAGE_HEIGHT * 0.6915887594223022, IMAGE_WIDTH * 0.08411216735839844, IMAGE_HEIGHT * 0.08411216735839844);
            final java.awt.geom.Point2D LOWERCENTER_KNOB_FRAME_START = new java.awt.geom.Point2D.Double(0, LOWERCENTER_KNOB_FRAME.getBounds2D().getMinY());
            final java.awt.geom.Point2D LOWERCENTER_KNOB_FRAME_STOP = new java.awt.geom.Point2D.Double(0, LOWERCENTER_KNOB_FRAME.getBounds2D().getMaxY());
            final float[] LOWERCENTER_KNOB_FRAME_FRACTIONS =
            {
                0.0f,
                0.46f,
                1.0f
            };
            final java.awt.Color[] LOWERCENTER_KNOB_FRAME_COLORS =
            {
                new java.awt.Color(180, 180, 180, 255),
                new java.awt.Color(63, 63, 63, 255),
                new java.awt.Color(40, 40, 40, 255)
            };

            final java.awt.LinearGradientPaint LOWERCENTER_KNOB_FRAME_GRADIENT = new java.awt.LinearGradientPaint(LOWERCENTER_KNOB_FRAME_START, LOWERCENTER_KNOB_FRAME_STOP, LOWERCENTER_KNOB_FRAME_FRACTIONS, LOWERCENTER_KNOB_FRAME_COLORS);
            G2.setPaint(LOWERCENTER_KNOB_FRAME_GRADIENT);
            G2.fill(LOWERCENTER_KNOB_FRAME);

            final java.awt.geom.Ellipse2D LOWERCENTER_KNOB_MAIN = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.4672897160053253, IMAGE_HEIGHT * 0.7009345889091492, IMAGE_WIDTH * 0.06542053818702698, IMAGE_HEIGHT * 0.06542056798934937);
            final java.awt.geom.Point2D LOWERCENTER_KNOB_MAIN_START = new java.awt.geom.Point2D.Double(0, LOWERCENTER_KNOB_MAIN.getBounds2D().getMinY());
            final java.awt.geom.Point2D LOWERCENTER_KNOB_MAIN_STOP = new java.awt.geom.Point2D.Double(0, LOWERCENTER_KNOB_MAIN.getBounds2D().getMaxY());
            final float[] LOWERCENTER_KNOB_MAIN_FRACTIONS =
            {
                0.0f,
                1.0f
            };
            final java.awt.Color[] LOWERCENTER_KNOB_MAIN_COLORS =
            {
                new java.awt.Color(217, 217, 217, 255),
                new java.awt.Color(191, 191, 191, 255)
            };

            final java.awt.LinearGradientPaint LOWERCENTER_KNOB_MAIN_GRADIENT = new java.awt.LinearGradientPaint(LOWERCENTER_KNOB_MAIN_START, LOWERCENTER_KNOB_MAIN_STOP, LOWERCENTER_KNOB_MAIN_FRACTIONS, LOWERCENTER_KNOB_MAIN_COLORS);
            G2.setPaint(LOWERCENTER_KNOB_MAIN_GRADIENT);
            G2.fill(LOWERCENTER_KNOB_MAIN);

            final java.awt.geom.Ellipse2D LOWERCENTER_KNOB_INNERSHADOW = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.4672897160053253, IMAGE_HEIGHT * 0.7009345889091492, IMAGE_WIDTH * 0.06542053818702698, IMAGE_HEIGHT * 0.06542056798934937);
            final java.awt.geom.Point2D LOWERCENTER_KNOB_INNERSHADOW_CENTER = new java.awt.geom.Point2D.Double((0.4953271028037383 * IMAGE_WIDTH), (0.7242990654205608 * IMAGE_HEIGHT));
            final float[] LOWERCENTER_KNOB_INNERSHADOW_FRACTIONS =
            {
                0.0f,
                0.75f,
                0.76f,
                1.0f
            };
            final java.awt.Color[] LOWERCENTER_KNOB_INNERSHADOW_COLORS =
            {
                new java.awt.Color(0, 0, 0, 0),
                new java.awt.Color(0, 0, 0, 0),
                new java.awt.Color(0, 0, 0, 1),
                new java.awt.Color(0, 0, 0, 51)
            };
            final java.awt.RadialGradientPaint LOWERCENTER_KNOB_INNERSHADOW_GRADIENT = new java.awt.RadialGradientPaint(LOWERCENTER_KNOB_INNERSHADOW_CENTER, (float) (0.03271028037383177 * IMAGE_WIDTH), LOWERCENTER_KNOB_INNERSHADOW_FRACTIONS, LOWERCENTER_KNOB_INNERSHADOW_COLORS);
            G2.setPaint(LOWERCENTER_KNOB_INNERSHADOW_GRADIENT);
            G2.fill(LOWERCENTER_KNOB_INNERSHADOW);
        }

        // Draw small gauge right post
        if (POST_POSITION_LIST.contains(eu.hansolo.steelseries.tools.PostPosition.SMALL_GAUGE_MAX_RIGHT))
        {
            final java.awt.geom.Ellipse2D MAX_POST_FRAME_SMALLGAUGE_RIGHT = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.7803738117218018, IMAGE_HEIGHT * 0.44859811663627625, IMAGE_WIDTH * 0.037383198738098145, IMAGE_HEIGHT * 0.037383198738098145);
            final java.awt.geom.Point2D MAX_POST_FRAME_SMALLGAUGE_RIGHT_START = new java.awt.geom.Point2D.Double(0, MAX_POST_FRAME_SMALLGAUGE_RIGHT.getBounds2D().getMinY());
            final java.awt.geom.Point2D MAX_POST_FRAME_SMALLGAUGE_RIGHT_STOP = new java.awt.geom.Point2D.Double(0, MAX_POST_FRAME_SMALLGAUGE_RIGHT.getBounds2D().getMaxY());
            final float[] MAX_POST_FRAME_SMALLGAUGE_RIGHT_FRACTIONS =
            {
                0.0f,
                0.46f,
                1.0f
            };
            final java.awt.Color[] MAX_POST_FRAME_SMALLGAUGE_RIGHT_COLORS =
            {
                new java.awt.Color(180, 180, 180, 255),
                new java.awt.Color(63, 63, 63, 255),
                new java.awt.Color(40, 40, 40, 255)
            };

            final java.awt.LinearGradientPaint MAX_POST_FRAME_SMALLGAUGE_RIGHT_GRADIENT = new java.awt.LinearGradientPaint(MAX_POST_FRAME_SMALLGAUGE_RIGHT_START, MAX_POST_FRAME_SMALLGAUGE_RIGHT_STOP, MAX_POST_FRAME_SMALLGAUGE_RIGHT_FRACTIONS, MAX_POST_FRAME_SMALLGAUGE_RIGHT_COLORS);
            G2.setPaint(MAX_POST_FRAME_SMALLGAUGE_RIGHT_GRADIENT);
            G2.fill(MAX_POST_FRAME_SMALLGAUGE_RIGHT);

            final java.awt.geom.Ellipse2D MAX_POST_MAIN_SMALLGAUGE_RIGHT = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.7850467562675476, IMAGE_HEIGHT * 0.4532710313796997, IMAGE_WIDTH * 0.02803736925125122, IMAGE_HEIGHT * 0.02803736925125122);
            final java.awt.geom.Point2D MAX_POST_MAIN_SMALLGAUGE_RIGHT_START = new java.awt.geom.Point2D.Double(0, MAX_POST_MAIN_SMALLGAUGE_RIGHT.getBounds2D().getMinY());
            final java.awt.geom.Point2D MAX_POST_MAIN_SMALLGAUGE_RIGHT_STOP = new java.awt.geom.Point2D.Double(0, MAX_POST_MAIN_SMALLGAUGE_RIGHT.getBounds2D().getMaxY());
            final float[] MAX_POST_MAIN_SMALLGAUGE_RIGHT_FRACTIONS =
            {
                0.0f,
                1.0f
            };
            final java.awt.Color[] MAX_POST_MAIN_SMALLGAUGE_RIGHT_COLORS =
            {
                new java.awt.Color(217, 217, 217, 255),
                new java.awt.Color(191, 191, 191, 255)
            };

            final java.awt.LinearGradientPaint MAX_POST_MAIN_SMALLGAUGE_RIGHT_GRADIENT = new java.awt.LinearGradientPaint(MAX_POST_MAIN_SMALLGAUGE_RIGHT_START, MAX_POST_MAIN_SMALLGAUGE_RIGHT_STOP, MAX_POST_MAIN_SMALLGAUGE_RIGHT_FRACTIONS, MAX_POST_MAIN_SMALLGAUGE_RIGHT_COLORS);
            G2.setPaint(MAX_POST_MAIN_SMALLGAUGE_RIGHT_GRADIENT);
            G2.fill(MAX_POST_MAIN_SMALLGAUGE_RIGHT);

            final java.awt.geom.Ellipse2D MAX_POST_INNERSHADOW_SMALLGAUGE_RIGHT = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.7850467562675476, IMAGE_HEIGHT * 0.4532710313796997, IMAGE_WIDTH * 0.02803736925125122, IMAGE_HEIGHT * 0.02803736925125122);
            final java.awt.geom.Point2D MAX_POST_INNERSHADOW_SMALLGAUGE_RIGHT_CENTER = new java.awt.geom.Point2D.Double((0.794392523364486 * IMAGE_WIDTH), (0.46261682242990654 * IMAGE_HEIGHT));
            final float[] MAX_POST_INNERSHADOW_SMALLGAUGE_RIGHT_FRACTIONS =
            {
                0.0f,
                0.75f,
                0.76f,
                1.0f
            };
            final java.awt.Color[] MAX_POST_INNERSHADOW_SMALLGAUGE_RIGHT_COLORS =
            {
                new java.awt.Color(0, 0, 0, 0),
                new java.awt.Color(0, 0, 0, 0),
                new java.awt.Color(0, 0, 0, 1),
                new java.awt.Color(0, 0, 0, 51)
            };
            final java.awt.RadialGradientPaint MAX_POST_INNERSHADOW_SMALLGAUGE_RIGHT_GRADIENT = new java.awt.RadialGradientPaint(MAX_POST_INNERSHADOW_SMALLGAUGE_RIGHT_CENTER, (float) (0.014018691588785047 * IMAGE_WIDTH), MAX_POST_INNERSHADOW_SMALLGAUGE_RIGHT_FRACTIONS, MAX_POST_INNERSHADOW_SMALLGAUGE_RIGHT_COLORS);
            G2.setPaint(MAX_POST_INNERSHADOW_SMALLGAUGE_RIGHT_GRADIENT);
            G2.fill(MAX_POST_INNERSHADOW_SMALLGAUGE_RIGHT);
        }

        // Draw small gauge left post
        if (POST_POSITION_LIST.contains(eu.hansolo.steelseries.tools.PostPosition.SMALL_GAUGE_MIN_LEFT))
        {
            final java.awt.geom.Ellipse2D MIN_POST_FRAME_SMALLGAUGE_LEFT = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.1822429895401001, IMAGE_HEIGHT * 0.44859811663627625, IMAGE_WIDTH * 0.03738318383693695, IMAGE_HEIGHT * 0.037383198738098145);
            final java.awt.geom.Point2D MIN_POST_FRAME_SMALLGAUGE_LEFT_START = new java.awt.geom.Point2D.Double(0, MIN_POST_FRAME_SMALLGAUGE_LEFT.getBounds2D().getMinY());
            final java.awt.geom.Point2D MIN_POST_FRAME_SMALLGAUGE_LEFT_STOP = new java.awt.geom.Point2D.Double(0, MIN_POST_FRAME_SMALLGAUGE_LEFT.getBounds2D().getMaxY());
            final float[] MIN_POST_FRAME_SMALLGAUGE_LEFT_FRACTIONS =
            {
                0.0f,
                0.46f,
                1.0f
            };
            final java.awt.Color[] MIN_POST_FRAME_SMALLGAUGE_LEFT_COLORS =
            {
                new java.awt.Color(180, 180, 180, 255),
                new java.awt.Color(63, 63, 63, 255),
                new java.awt.Color(40, 40, 40, 255)
            };

            final java.awt.LinearGradientPaint MIN_POST_FRAME_SMALLGAUGE_LEFT_GRADIENT = new java.awt.LinearGradientPaint(MIN_POST_FRAME_SMALLGAUGE_LEFT_START, MIN_POST_FRAME_SMALLGAUGE_LEFT_STOP, MIN_POST_FRAME_SMALLGAUGE_LEFT_FRACTIONS, MIN_POST_FRAME_SMALLGAUGE_LEFT_COLORS);
            G2.setPaint(MIN_POST_FRAME_SMALLGAUGE_LEFT_GRADIENT);
            G2.fill(MIN_POST_FRAME_SMALLGAUGE_LEFT);

            final java.awt.geom.Ellipse2D MIN_POST_MAIN_SMALLGAUGE_LEFT = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.18691588938236237, IMAGE_HEIGHT * 0.4532710313796997, IMAGE_WIDTH * 0.028037384152412415, IMAGE_HEIGHT * 0.02803736925125122);
            final java.awt.geom.Point2D MIN_POST_MAIN_SMALLGAUGE_LEFT_START = new java.awt.geom.Point2D.Double(0, MIN_POST_MAIN_SMALLGAUGE_LEFT.getBounds2D().getMinY());
            final java.awt.geom.Point2D MIN_POST_MAIN_SMALLGAUGE_LEFT_STOP = new java.awt.geom.Point2D.Double(0, MIN_POST_MAIN_SMALLGAUGE_LEFT.getBounds2D().getMaxY());
            final float[] MIN_POST_MAIN_SMALLGAUGE_LEFT_FRACTIONS =
            {
                0.0f,
                1.0f
            };
            final java.awt.Color[] MIN_POST_MAIN_SMALLGAUGE_LEFT_COLORS =
            {
                new java.awt.Color(217, 217, 217, 255),
                new java.awt.Color(191, 191, 191, 255)
            };

            final java.awt.LinearGradientPaint MIN_POST_MAIN_SMALLGAUGE_LEFT_GRADIENT = new java.awt.LinearGradientPaint(MIN_POST_MAIN_SMALLGAUGE_LEFT_START, MIN_POST_MAIN_SMALLGAUGE_LEFT_STOP, MIN_POST_MAIN_SMALLGAUGE_LEFT_FRACTIONS, MIN_POST_MAIN_SMALLGAUGE_LEFT_COLORS);
            G2.setPaint(MIN_POST_MAIN_SMALLGAUGE_LEFT_GRADIENT);
            G2.fill(MIN_POST_MAIN_SMALLGAUGE_LEFT);

            final java.awt.geom.Ellipse2D MIN_POST_INNERSHADOW_SMALLGAUGE_LEFT = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.18691588938236237, IMAGE_HEIGHT * 0.4532710313796997, IMAGE_WIDTH * 0.028037384152412415, IMAGE_HEIGHT * 0.02803736925125122);
            final java.awt.geom.Point2D MIN_POST_INNERSHADOW_SMALLGAUGE_LEFT_CENTER = new java.awt.geom.Point2D.Double((0.20093457943925233 * IMAGE_WIDTH), (0.46261682242990654 * IMAGE_HEIGHT));
            final float[] MIN_POST_INNERSHADOW_SMALLGAUGE_LEFT_FRACTIONS =
            {
                0.0f,
                0.75f,
                0.76f,
                1.0f
            };
            final java.awt.Color[] MIN_POST_INNERSHADOW_SMALLGAUGE_LEFT_COLORS =
            {
                new java.awt.Color(0, 0, 0, 0),
                new java.awt.Color(0, 0, 0, 0),
                new java.awt.Color(0, 0, 0, 1),
                new java.awt.Color(0, 0, 0, 51)
            };
            final java.awt.RadialGradientPaint MIN_POST_INNERSHADOW_SMALLGAUGE_LEFT_GRADIENT = new java.awt.RadialGradientPaint(MIN_POST_INNERSHADOW_SMALLGAUGE_LEFT_CENTER, (float) (0.014018691588785047 * IMAGE_WIDTH), MIN_POST_INNERSHADOW_SMALLGAUGE_LEFT_FRACTIONS, MIN_POST_INNERSHADOW_SMALLGAUGE_LEFT_COLORS);
            G2.setPaint(MIN_POST_INNERSHADOW_SMALLGAUGE_LEFT_GRADIENT);
            G2.fill(MIN_POST_INNERSHADOW_SMALLGAUGE_LEFT);
        }

        G2.dispose();

        return IMAGE;
    }

    /**
     * Returns the image of the threshold indicator.
     * @param WIDTH
     * @return the threshold image that is used
     */
    protected java.awt.image.BufferedImage create_THRESHOLD_Image(final int WIDTH)
    {
        if (WIDTH <= 0)
        {
            return null;
        }

        final int IMAGE_HEIGHT = (int) (WIDTH * 0.046728972);
        final int IMAGE_WIDTH = (int) (IMAGE_HEIGHT * 0.9);

        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage(IMAGE_WIDTH, IMAGE_HEIGHT, java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        final java.awt.geom.GeneralPath THRESHOLD = new java.awt.geom.GeneralPath();
        THRESHOLD.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        THRESHOLD.moveTo(IMAGE_WIDTH * 0.5, 0.1);
        THRESHOLD.lineTo(IMAGE_WIDTH * 0.9, IMAGE_HEIGHT * 0.9);
        THRESHOLD.lineTo(IMAGE_WIDTH * 0.1, IMAGE_HEIGHT * 0.9);
        THRESHOLD.lineTo(IMAGE_WIDTH * 0.5, 0.1);
        THRESHOLD.closePath();

        final java.awt.geom.Point2D THRESHOLD_START = new java.awt.geom.Point2D.Double(0, THRESHOLD.getBounds2D().getMaxY());
        final java.awt.geom.Point2D THRESHOLD_STOP = new java.awt.geom.Point2D.Double(0, THRESHOLD.getBounds2D().getMinY());

        final float[] THRESHOLD_FRACTIONS =
        {
            0.0f,
            0.3f,
            0.59f,
            1.0f
        };
        final java.awt.Color[] THRESHOLD_COLORS =
        {
            new java.awt.Color(82, 0, 0, 255),
            new java.awt.Color(252, 29, 0, 255),
            new java.awt.Color(252, 29, 0, 255),
            new java.awt.Color(82, 0, 0, 255)
        };
        final java.awt.LinearGradientPaint THRESHOLD_GRADIENT = new java.awt.LinearGradientPaint(THRESHOLD_START, THRESHOLD_STOP, THRESHOLD_FRACTIONS, THRESHOLD_COLORS);
        G2.setPaint(THRESHOLD_GRADIENT);
        G2.fill(THRESHOLD);

        G2.setColor(java.awt.Color.WHITE);
        G2.setStroke(new java.awt.BasicStroke(1.0f, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_MITER));
        G2.draw(THRESHOLD);

        G2.dispose();

        return IMAGE;
    }

    /**
     * Returns the image of the MinMeasuredValue and MaxMeasuredValue dependend
     * @param WIDTH
     * @param COLOR
     * @return the image of the min or max measured value
     */
    protected java.awt.image.BufferedImage create_MEASURED_VALUE_Image(final int WIDTH, final java.awt.Color COLOR)
    {
        if (WIDTH <= 0)
        {
            return null;
        }

        final int IMAGE_HEIGHT = (int) (WIDTH * 0.0280373832);
        final int IMAGE_WIDTH = IMAGE_HEIGHT;

        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage(IMAGE_WIDTH, IMAGE_HEIGHT, java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);

        final java.awt.geom.GeneralPath INDICATOR = new java.awt.geom.GeneralPath();
        INDICATOR.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        INDICATOR.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT);
        INDICATOR.lineTo(0.0, 0.0);
        INDICATOR.lineTo(IMAGE_WIDTH, 0.0);
        INDICATOR.closePath();

        G2.setColor(COLOR);
        G2.fill(INDICATOR);

        G2.dispose();

        return IMAGE;
    }

    /**
     * Returns the image of the pointer. This pointer is centered in the gauge and of PointerType TYPE1.
     * @param WIDTH
     * @return the pointer image that is used in all gauges that have a centered pointer
     */
    protected java.awt.image.BufferedImage create_POINTER_Image(final int WIDTH)
    {
        return create_POINTER_Image(WIDTH, eu.hansolo.steelseries.tools.PointerType.TYPE1);
    }

    /**
     * Returns the image of the pointer. This pointer is centered in the gauge.
     * @param WIDTH
     * @param POINTER_TYPE
     * @return the pointer image that is used in all gauges that have a centered pointer
     */
    protected java.awt.image.BufferedImage create_POINTER_Image(final int WIDTH, final eu.hansolo.steelseries.tools.PointerType POINTER_TYPE)
    {
        if (WIDTH <= 0)
        {
            return null;
        }

        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, WIDTH, java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final java.awt.geom.GeneralPath POINTER;
        final java.awt.geom.Point2D POINTER_START;
        final java.awt.geom.Point2D POINTER_STOP;
        final float[] POINTER_FRACTIONS;
        final java.awt.Color[] POINTER_COLORS;
        final java.awt.Paint POINTER_GRADIENT;

        switch (POINTER_TYPE)
        {
            case TYPE1:
                POINTER = new java.awt.geom.GeneralPath();
                POINTER.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
                POINTER.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.4158878504672897, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.40186915887850466);
                POINTER.curveTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.38317757009345793, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1308411214953271, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1308411214953271);
                POINTER.curveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1308411214953271, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.38317757009345793, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.397196261682243);
                POINTER.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.4158878504672897, IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4719626168224299);
                POINTER.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5);
                POINTER.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5327102803738317);
                POINTER.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5);
                POINTER.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
                POINTER.closePath();
                POINTER_START = new java.awt.geom.Point2D.Double(0, POINTER.getBounds2D().getMinY() );
                POINTER_STOP = new java.awt.geom.Point2D.Double(0, POINTER.getBounds2D().getMaxY() );
                POINTER_FRACTIONS = new float[]
                {
                    0.0f,
                    0.3f,
                    0.59f,
                    1.0f
                };
                POINTER_COLORS = new java.awt.Color[]
                {
                    getPointerColor().DARK,
                    getPointerColor().LIGHT,
                    getPointerColor().LIGHT,
                    getPointerColor().DARK
                };
                POINTER_GRADIENT = new java.awt.LinearGradientPaint(POINTER_START, POINTER_STOP, POINTER_FRACTIONS, POINTER_COLORS);
                G2.setPaint(POINTER_GRADIENT);
                G2.fill(POINTER);
                final java.awt.Color STROKE_COLOR_POINTER = getPointerColor().LIGHT;
                G2.setColor(STROKE_COLOR_POINTER);
                G2.setStroke(new java.awt.BasicStroke(1.0f, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_MITER));
                G2.draw(POINTER);
                break;

            case TYPE2:
                POINTER = new java.awt.geom.GeneralPath();
                POINTER.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
                POINTER.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.46261682242990654);
                POINTER.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.3411214953271028);
                POINTER.lineTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.1308411214953271);
                POINTER.lineTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.1308411214953271);
                POINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.3411214953271028);
                POINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.46261682242990654);
                POINTER.lineTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4719626168224299);
                POINTER.curveTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4719626168224299, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5);
                POINTER.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5327102803738317);
                POINTER.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5);
                POINTER.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
                POINTER.closePath();
                POINTER_START = new java.awt.geom.Point2D.Double(0, POINTER.getBounds2D().getMaxY() );
                POINTER_STOP = new java.awt.geom.Point2D.Double(0, POINTER.getBounds2D().getMinY() );
                POINTER_FRACTIONS = new float[]
                {
                    0.0f,
                    0.36f,
                    0.3601f,
                    1.0f
                };
                POINTER_COLORS = new java.awt.Color[]
                {
                    new java.awt.Color(0, 0, 0, 255),
                    new java.awt.Color(0, 0, 0, 255),
                    getPointerColor().LIGHT,
                    getPointerColor().LIGHT
                };
                POINTER_GRADIENT = new java.awt.LinearGradientPaint(POINTER_START, POINTER_STOP, POINTER_FRACTIONS, POINTER_COLORS);
                G2.setPaint(POINTER_GRADIENT);
                G2.fill(POINTER);
                break;
        }

        G2.dispose();

        return IMAGE;
    }

    /**
     * Returns the image of the pointer shadow. This shadow is centered in the gauge
     * @param WIDTH
     * @return the pointer shadow image that is used in all gauges that have a centered pointer
     */
    protected java.awt.image.BufferedImage create_POINTER_SHADOW_Image(final int WIDTH)
    {
        return create_POINTER_SHADOW_Image(WIDTH, eu.hansolo.steelseries.tools.PointerType.TYPE1);
    }

    /**
     * Returns the image of the pointer shadow. This shadow is centered in the gauge
     * @param WIDTH
     * @return the pointer shadow image that is used in all gauges that have a centered pointer
     */
    protected java.awt.image.BufferedImage create_POINTER_SHADOW_Image(final int WIDTH, final eu.hansolo.steelseries.tools.PointerType POINTER_TYPE)
    {
        if (WIDTH <= 0)
        {
            return null;
        }
        final java.awt.Color SHADOW_COLOR = new java.awt.Color(0.0f, 0.0f, 0.0f, 0.65f);

        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, WIDTH, java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final java.awt.geom.GeneralPath POINTER;

        switch(POINTER_TYPE)
        {
            case TYPE1:
                POINTER = new java.awt.geom.GeneralPath();
                POINTER.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
                POINTER.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.4158878504672897, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.40186915887850466);
                POINTER.curveTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.38317757009345793, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1308411214953271, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1308411214953271);
                POINTER.curveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1308411214953271, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.38317757009345793, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.397196261682243);
                POINTER.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.4158878504672897, IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4719626168224299);
                POINTER.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5);
                POINTER.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5327102803738317);
                POINTER.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5);
                POINTER.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;

            case TYPE2:
                POINTER = new java.awt.geom.GeneralPath();
                POINTER.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
                POINTER.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
                POINTER.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.46261682242990654);
                POINTER.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.3411214953271028);
                POINTER.lineTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.1308411214953271);
                POINTER.lineTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.1308411214953271);
                POINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.3411214953271028);
                POINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.46261682242990654);
                POINTER.lineTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4719626168224299);
                POINTER.curveTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4719626168224299, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5);
                POINTER.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5327102803738317);
                POINTER.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5);
                POINTER.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
                POINTER.closePath();
                G2.setColor(SHADOW_COLOR);
                G2.fill(POINTER);
                break;
        }

        G2.dispose();

        return IMAGE;
    }

    /**
     * Returns the image of the glasseffect with a centered knob
     * @param WIDTH
     * @return the foreground image that is used (in principle only the glass effect)
     */
    protected java.awt.image.BufferedImage create_FOREGROUND_Image(final int WIDTH)
    {
        return create_FOREGROUND_Image(WIDTH, true);
    }

    /**
     * Returns the image of the glasseffect and a centered knob if wanted
     * @param WIDTH
     * @param WITH_CENTER_KNOB
     * @return the foreground image that is used (in principle only the glass effect)
     */
    protected java.awt.image.BufferedImage create_FOREGROUND_Image(final int WIDTH, final boolean WITH_CENTER_KNOB)
    {
        if (WIDTH <= 0)
        {
            return null;
        }

        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, WIDTH, java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        if (WITH_CENTER_KNOB)
        {
            final java.awt.geom.Ellipse2D E_CENTER_KNOB_FRAME = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.4579439163208008, IMAGE_HEIGHT * 0.4579439163208008, IMAGE_WIDTH * 0.08411216735839844, IMAGE_HEIGHT * 0.08411216735839844);
            final java.awt.geom.Point2D E_CENTER_KNOB_FRAME_START = new java.awt.geom.Point2D.Double(0, E_CENTER_KNOB_FRAME.getBounds2D().getMinY());
            final java.awt.geom.Point2D E_CENTER_KNOB_FRAME_STOP = new java.awt.geom.Point2D.Double(0, E_CENTER_KNOB_FRAME.getBounds2D().getMaxY());
            final float[] E_CENTER_KNOB_FRAME_FRACTIONS =
            {
                0.0f,
                0.46f,
                1.0f
            };
            final java.awt.Color[] E_CENTER_KNOB_FRAME_COLORS =
            {
                new java.awt.Color(180, 180, 180, 255),
                new java.awt.Color(63, 63, 63, 255),
                new java.awt.Color(40, 40, 40, 255)
            };
            final java.awt.LinearGradientPaint E_CENTER_KNOB_FRAME_GRADIENT = new java.awt.LinearGradientPaint(E_CENTER_KNOB_FRAME_START, E_CENTER_KNOB_FRAME_STOP, E_CENTER_KNOB_FRAME_FRACTIONS, E_CENTER_KNOB_FRAME_COLORS);
            G2.setPaint(E_CENTER_KNOB_FRAME_GRADIENT);
            G2.fill(E_CENTER_KNOB_FRAME);

            final java.awt.geom.Ellipse2D E_CENTER_KNOB_MAIN = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.4672897160053253, IMAGE_HEIGHT * 0.4672897160053253, IMAGE_WIDTH * 0.06542053818702698, IMAGE_HEIGHT * 0.06542053818702698);
            final java.awt.geom.Point2D E_CENTER_KNOB_MAIN_START = new java.awt.geom.Point2D.Double(0, E_CENTER_KNOB_MAIN.getBounds2D().getMinY());
            final java.awt.geom.Point2D E_CENTER_KNOB_MAIN_STOP = new java.awt.geom.Point2D.Double(0, E_CENTER_KNOB_MAIN.getBounds2D().getMaxY());
            final float[] E_CENTER_KNOB_MAIN_FRACTIONS =
            {
                0.0f,
                1.0f
            };
            final java.awt.Color[] E_CENTER_KNOB_MAIN_COLORS =
            {
                new java.awt.Color(217, 217, 217, 255),
                new java.awt.Color(191, 191, 191, 255)
            };
            final java.awt.LinearGradientPaint E_CENTER_KNOB_MAIN_GRADIENT = new java.awt.LinearGradientPaint(E_CENTER_KNOB_MAIN_START, E_CENTER_KNOB_MAIN_STOP, E_CENTER_KNOB_MAIN_FRACTIONS, E_CENTER_KNOB_MAIN_COLORS);
            G2.setPaint(E_CENTER_KNOB_MAIN_GRADIENT);
            G2.fill(E_CENTER_KNOB_MAIN);

            final java.awt.geom.Ellipse2D E_CENTER_KNOB_INNERSHADOW = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.4672897160053253, IMAGE_HEIGHT * 0.4672897160053253, IMAGE_WIDTH * 0.06542053818702698, IMAGE_HEIGHT * 0.06542053818702698);
            final java.awt.geom.Point2D E_CENTER_KNOB_INNERSHADOW_CENTER = new java.awt.geom.Point2D.Double((0.4953271028037383 * IMAGE_WIDTH), (0.49065420560747663 * IMAGE_HEIGHT));
            final float[] E_CENTER_KNOB_INNERSHADOW_FRACTIONS =
            {
                0.0f,
                0.75f,
                0.76f,
                1.0f
            };
            final java.awt.Color[] E_CENTER_KNOB_INNERSHADOW_COLORS =
            {
                new java.awt.Color(0, 0, 0, 0),
                new java.awt.Color(0, 0, 0, 0),
                new java.awt.Color(0, 0, 0, 1),
                new java.awt.Color(0, 0, 0, 51)
            };
            final java.awt.RadialGradientPaint E_CENTER_KNOB_INNERSHADOW_GRADIENT = new java.awt.RadialGradientPaint(E_CENTER_KNOB_INNERSHADOW_CENTER, (float) (0.03271028037383177 * IMAGE_WIDTH), E_CENTER_KNOB_INNERSHADOW_FRACTIONS, E_CENTER_KNOB_INNERSHADOW_COLORS);
            G2.setPaint(E_CENTER_KNOB_INNERSHADOW_GRADIENT);
            G2.fill(E_CENTER_KNOB_INNERSHADOW);
        }

        final java.awt.geom.GeneralPath HIGHLIGHT = new java.awt.geom.GeneralPath();
        HIGHLIGHT.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        HIGHLIGHT.moveTo(IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.49065420560747663);
        HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.5, IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.5046728971962616, IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.5093457943925234);
        HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.20093457943925233, IMAGE_HEIGHT * 0.4532710280373832, IMAGE_WIDTH * 0.32710280373831774, IMAGE_HEIGHT * 0.4158878504672897, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.4158878504672897);
        HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.6588785046728972, IMAGE_HEIGHT * 0.4158878504672897, IMAGE_WIDTH * 0.794392523364486, IMAGE_HEIGHT * 0.4439252336448598, IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.514018691588785);
        HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.5046728971962616, IMAGE_WIDTH * 0.9205607476635514, IMAGE_HEIGHT * 0.5, IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.49065420560747663);
        HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.9158878504672897, IMAGE_HEIGHT * 0.2757009345794392, IMAGE_WIDTH * 0.7476635514018691, IMAGE_HEIGHT * 0.08411214953271028, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.08411214953271028);
        HIGHLIGHT.curveTo(IMAGE_WIDTH * 0.2523364485981308, IMAGE_HEIGHT * 0.08411214953271028, IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.2803738317757009, IMAGE_WIDTH * 0.08411214953271028, IMAGE_HEIGHT * 0.49065420560747663);
        HIGHLIGHT.closePath();
        final java.awt.geom.Point2D HIGHLIGHT_START = new java.awt.geom.Point2D.Double(0, HIGHLIGHT.getBounds2D().getMinY());
        final java.awt.geom.Point2D HIGHLIGHT_STOP = new java.awt.geom.Point2D.Double(0, HIGHLIGHT.getBounds2D().getMaxY());
        final float[] HIGHLIGHT_FRACTIONS =
        {
            0.0f,
            1.0f
        };
        final java.awt.Color[] HIGHLIGHT_COLORS =
        {
            new java.awt.Color(255, 255, 255, 63),
            new java.awt.Color(255, 255, 255, 12)
        };
        final java.awt.LinearGradientPaint HIGHLIGHT_GRADIENT = new java.awt.LinearGradientPaint(HIGHLIGHT_START, HIGHLIGHT_STOP, HIGHLIGHT_FRACTIONS, HIGHLIGHT_COLORS);
        G2.setPaint(HIGHLIGHT_GRADIENT);
        G2.fill(HIGHLIGHT);

        G2.dispose();

        return IMAGE;
    }

    /**
     * Returns a image of a led with the given size, state and color.
     * @param SIZE
     * @param STATE
     * @param LED_COLOR
     * @return the led image 
     */
    protected java.awt.image.BufferedImage create_LED_Image(final int SIZE, final int STATE, final eu.hansolo.steelseries.tools.LedColor LED_COLOR)
    {
        if (SIZE <= 0)
        {
            return null;
        }

        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage((int) (SIZE * 0.0934579439), (int) (SIZE * 0.0934579439), java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();

        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        // Define led data
        final java.awt.geom.Ellipse2D LED = new java.awt.geom.Ellipse2D.Double(0.25 * IMAGE_WIDTH, 0.25 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.5 * IMAGE_HEIGHT);
        final java.awt.geom.Ellipse2D LED_CORONA = new java.awt.geom.Ellipse2D.Double(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

        final java.awt.geom.Point2D LED_CENTER = new java.awt.geom.Point2D.Double(LED.getCenterX(), LED.getCenterY());

        final float[] LED_FRACTIONS =
        {
            0.0f,
            0.2f,
            1.0f
        };

        final java.awt.Color[] LED_OFF_COLORS =
        {
            LED_COLOR.INNER_COLOR1_OFF,
            LED_COLOR.INNER_COLOR2_OFF,
            LED_COLOR.OUTER_COLOR_OFF
        };

        final java.awt.Color[] LED_ON_COLORS =
        {
            LED_COLOR.INNER_COLOR1_ON,
            LED_COLOR.INNER_COLOR2_ON,
            LED_COLOR.OUTER_COLOR_ON
        };

        final float[] LED_INNER_SHADOW_FRACTIONS =
        {
            0.0f,
            0.8f,
            1.0f
        };

        final java.awt.Color[] LED_INNER_SHADOW_COLORS =
        {
            new java.awt.Color(0.0f, 0.0f, 0.0f, 0.0f),
            new java.awt.Color(0.0f, 0.0f, 0.0f, 0.0f),
            new java.awt.Color(0.0f, 0.0f, 0.0f, 0.4f),
        };

        final float[] LED_ON_CORONA_FRACTIONS =
        {
            0.0f,
            0.6f,
            0.7f,
            0.8f,
            0.85f,
            1.0f
        };

        final java.awt.Color[] LED_ON_CORONA_COLORS =
        {
            UTIL.setAlpha(LED_COLOR.CORONA_COLOR, 0.0f),
            UTIL.setAlpha(LED_COLOR.CORONA_COLOR, 0.4f),
            UTIL.setAlpha(LED_COLOR.CORONA_COLOR, 0.25f),
            UTIL.setAlpha(LED_COLOR.CORONA_COLOR, 0.15f),
            UTIL.setAlpha(LED_COLOR.CORONA_COLOR, 0.05f),
            UTIL.setAlpha(LED_COLOR.CORONA_COLOR, 0.0f)
        };

        // Define gradients for the lower led
        final java.awt.RadialGradientPaint LED_OFF_GRADIENT = new java.awt.RadialGradientPaint(LED_CENTER, 0.25f * IMAGE_WIDTH, LED_FRACTIONS, LED_OFF_COLORS);
        final java.awt.RadialGradientPaint LED_ON_GRADIENT = new java.awt.RadialGradientPaint(LED_CENTER, 0.25f * IMAGE_WIDTH, LED_FRACTIONS, LED_ON_COLORS);
        final java.awt.RadialGradientPaint LED_INNER_SHADOW_GRADIENT = new java.awt.RadialGradientPaint(LED_CENTER, 0.25f * IMAGE_WIDTH, LED_INNER_SHADOW_FRACTIONS, LED_INNER_SHADOW_COLORS);
        final java.awt.RadialGradientPaint LED_ON_CORONA_GRADIENT = new java.awt.RadialGradientPaint(LED_CENTER, 0.5f * IMAGE_WIDTH, LED_ON_CORONA_FRACTIONS, LED_ON_CORONA_COLORS);


        // Define light reflex data
        final java.awt.geom.Ellipse2D LED_LIGHTREFLEX = new java.awt.geom.Ellipse2D.Double(0.4 * IMAGE_WIDTH, 0.35 * IMAGE_WIDTH, 0.2 * IMAGE_WIDTH, 0.15 * IMAGE_WIDTH);
        final java.awt.geom.Point2D LED_LIGHTREFLEX_START = new java.awt.geom.Point2D.Double(0, LED_LIGHTREFLEX.getMinY());
        final java.awt.geom.Point2D LED_LIGHTREFLEX_STOP = new java.awt.geom.Point2D.Double(0, LED_LIGHTREFLEX.getMaxY());

        final float[] LIGHT_REFLEX_FRACTIONS =
        {
            0.0f,
            1.0f
        };

        final java.awt.Color[] LIGHTREFLEX_COLORS =
        {
            new java.awt.Color(1.0f, 1.0f, 1.0f, 0.4f),
            new java.awt.Color(1.0f, 1.0f, 1.0f, 0.0f)
        };

        // Define light reflex gradients
        final java.awt.LinearGradientPaint LED_LIGHTREFLEX_GRADIENT = new java.awt.LinearGradientPaint(LED_LIGHTREFLEX_START, LED_LIGHTREFLEX_STOP, LIGHT_REFLEX_FRACTIONS, LIGHTREFLEX_COLORS);


        switch (STATE)
        {
            case 0:
                // LED OFF
                G2.setPaint(LED_OFF_GRADIENT);
                G2.fill(LED);
                G2.setPaint(LED_INNER_SHADOW_GRADIENT);
                G2.fill(LED);
                G2.setPaint(LED_LIGHTREFLEX_GRADIENT);
                G2.fill(LED_LIGHTREFLEX);
                break;
            case 1:
                // LED ON
                G2.setPaint(LED_ON_CORONA_GRADIENT);
                G2.fill(LED_CORONA);
                G2.setPaint(LED_ON_GRADIENT);
                G2.fill(LED);
                G2.setPaint(LED_INNER_SHADOW_GRADIENT);
                G2.fill(LED);
                G2.setPaint(LED_LIGHTREFLEX_GRADIENT);
                G2.fill(LED_LIGHTREFLEX);
                break;
            default:
                // LED OFF
                G2.setPaint(LED_OFF_GRADIENT);
                G2.fill(LED);
                G2.setPaint(LED_INNER_SHADOW_GRADIENT);
                G2.fill(LED);
                G2.setPaint(LED_LIGHTREFLEX_GRADIENT);
                G2.fill(LED_LIGHTREFLEX);
                break;
        }

        G2.dispose();

        return IMAGE;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Size related methods">
    @Override
    public java.awt.Dimension getSize()
    {
        if (getWidth() < getMinimumSize().width || getHeight() < getMinimumSize().height)
        {
            return (getMinimumSize());
        }
        else
        {
            return new java.awt.Dimension(getWidth(), getWidth());
        }
    }

    @Override
    public java.awt.Dimension getMinimumSize()
    {
        return new java.awt.Dimension(50, 50);
    }

    @Override
    public java.awt.Dimension getPreferredSize()
    {
        return new java.awt.Dimension(200, 200);
    }

    @Override
    public java.awt.Dimension getSize(java.awt.Dimension dim)
    {
        if (getWidth() < getMinimumSize().width || getHeight() < getMinimumSize().height)
        {
            return (getMinimumSize());
        }
        else
        {
            return new java.awt.Dimension(getWidth(), getWidth());
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ComponentListener methods">
    @Override
    public void componentResized(java.awt.event.ComponentEvent event)
    {
        final int SIZE = getWidth() < getHeight() ? getWidth() : getHeight();
        setPreferredSize(new java.awt.Dimension(SIZE, SIZE));
        setSize(SIZE, SIZE);

        if (SIZE < getMinimumSize().width || SIZE < getMinimumSize().height)
        {
            setSize(getMinimumSize());
        }

        ledImageOff = create_LED_Image(getWidth(), 0, ledColor);
        ledImageOn = create_LED_Image(getWidth(), 1, ledColor);
        if (ledOn)
        {
            setCurrentLedImage(ledImageOn);
        }
        else
        {
            setCurrentLedImage(ledImageOff);
        }

        init(getWidth(), getHeight());

        repaint();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ActionListener methods">
    @Override
    public void actionPerformed(java.awt.event.ActionEvent event)
    {
        if (event.getSource().equals(LED_BLINKING_TIMER))
        {
            currentLedImage = ledOn == true ? getLedImageOn() : getLedImageOff();
            ledOn ^= true;

            repaint((int) (getWidth() * ledPositionX), (int) (getHeight() * ledPositionY), currentLedImage.getWidth(), currentLedImage.getHeight());
        }

        if (event.getSource().equals(PEAK_TIMER))
        {
            peakValueVisible = false;
            PEAK_TIMER.stop();
        }
    }
    // </editor-fold>

    @Override
    public String toString()
    {
        return "AbstractRadial";
    }
}
