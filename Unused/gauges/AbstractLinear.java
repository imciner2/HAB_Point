package eu.hansolo.steelseries.gauges;


/**
 *
 * @author hansolo
 */
public abstract class AbstractLinear extends AbstractGauge implements java.awt.event.ActionListener
{
    protected static final eu.hansolo.steelseries.tools.Util UTIL = eu.hansolo.steelseries.tools.Util.INSTANCE;
    private int orientation = javax.swing.SwingConstants.HORIZONTAL;
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
    // Threshold related
    private double threshold = maxValue;
    private boolean thresholdVisible = false;
    // Track related
    private boolean trackVisible = false;
    private double trackStart = minValue;
    private double trackSection = (maxValue - minValue) / 2;
    private double trackStop = maxValue;
    private java.awt.Color trackStartColor = new java.awt.Color(0.0f, 1.0f, 0.0f, 0.5f);
    private java.awt.Color trackSectionColor = new java.awt.Color(1.0f, 1.0f, 0.0f, 0.5f);
    private java.awt.Color trackStopColor = new java.awt.Color(1.0f, 0.0f, 0.0f, 0.5f);
    // Custom layer
    private boolean customLayerVisible = false;
    private java.awt.image.BufferedImage customLayer = null;
    // Background related
    private boolean customBackgroundVisible = false;
    private java.awt.Paint customBackground = java.awt.Color.BLACK;
    private eu.hansolo.steelseries.tools.BackgroundColor backgroundColor = eu.hansolo.steelseries.tools.BackgroundColor.DARK_GRAY;
    // Frame related
    private eu.hansolo.steelseries.tools.FrameDesign frameDesign = eu.hansolo.steelseries.tools.FrameDesign.METAL;
    // Bar related
    private boolean startingFromZero = false;
    private eu.hansolo.steelseries.tools.ColorDef valueColor = eu.hansolo.steelseries.tools.ColorDef.RED;
    // Measured value related variables
    private double minMeasuredValue = maxValue;
    private boolean minMeasuredValueVisible = false;
    private double maxMeasuredValue = minValue;
    private boolean maxMeasuredValueVisible = false;
    // Threshold LED related variables
    private boolean ledVisible = true;
    private double ledPositionX = (140 - 20) / 2;
    private double ledPositionY = 18 + 2;
    private eu.hansolo.steelseries.tools.LedColor ledColor = eu.hansolo.steelseries.tools.LedColor.RED_LED;
    private java.awt.image.BufferedImage ledImageOff = create_LED_Image(140, 0, ledColor);
    private java.awt.image.BufferedImage ledImageOn = create_LED_Image(140, 1, ledColor);
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

    
    public AbstractLinear()
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
     * range of this gauge.
     * @return
     */
    public double getMinValue()
    {
        return this.minValue;
    }

    /**
     * Sets the minimum value of the measurement
     * range of this gauge. This value defines the
     * minimum value the gauge could display.
     * @param MIN_VALUE
     */
    public void setMinValue(final double MIN_VALUE)
    {
        this.minValue = MIN_VALUE;
        if (MIN_VALUE > 0)
        {
            setValue(minValue);
        }
        checkSettings();
        init(getWidth(), getHeight());
        repaint();
    }

    /**
     * Returns the maximum value of the measurement
     * range of this gauge.
     * @return
     */
    public double getMaxValue()
    {
        return this.maxValue;
    }

    /**
     * Sets the maximum value of the measurement
     * range of this gauge. This value defines the
     * maximum value the gauge could display.
     * @param MAX_VALUE
     */
    public void setMaxValue(final double MAX_VALUE)
    {
        this.maxValue = MAX_VALUE;
        checkSettings();
        init(getWidth(), getHeight());
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
     * @return
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
     * @return
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
        init(getWidth(), getHeight());
        repaint();
    }

    /**
     * Returns the color of the tickmarks and their labels
     * @return
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
        init(getWidth(), getHeight());
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
        init(getWidth(), getHeight());
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
        init(getWidth(), getHeight());
        repaint();
    }

    /**
     * Returns the value that is defined as a threshold.
     * If the current value of the gauge exceeds this
     * threshold, a event will be fired and the led will
     * start blinking (if the led is visible).
     * @return
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
     * @return
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
     * @return
     */
    public double getMinMeasuredValue()
    {
        return this.minMeasuredValue;
    }

    /**
     * Returns the visibility of the minMeasuredValue indicator.
     * The lowest value that was measured by the gauge will
     * be visualized by a little blue triangle.
     * @return
     */
    public boolean isMinMeasuredValueVisible()
    {
        return this.minMeasuredValueVisible;
    }

    /**
     * Sets the visibility of the minMeasuredValue indicator.
     * The lowest value that was measured by the gauge will
     * be visualized by a little blue triangle.
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
     * pointer/bar the indicator will be set to the pointer/bar
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
     * @return
     */
    public double getMaxMeasuredValue()
    {
        return this.maxMeasuredValue;
    }

    /**
     * Returns the visibility of the maxMeasuredValue indicator.
     * The biggest value that was measured by the gauge will
     * be visualized by a little red triangle.
     * @return
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
        repaint();
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
     * @return
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
     * @return
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
        init(getWidth(), getHeight());
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
     * @return
     */
    public double getTrackSection()
    {
        return this.trackSection;
    }

    /**
     * Sets the valueof the point between trackStart and trackStop.
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
        init(getWidth(), getHeight());
        repaint();
    }

    /**
     * Returns value of the end of the track.
     * The track is a area that could be defined by a start value,
     * a section stop value. This area will be painted with a
     * gradient that uses two or three given colors.
     * E.g. a critical area of a thermometer could be defined between
     * 30 and 100 degrees celsius and could have a gradient from
     * green over yellow to red. In this case the start
     * value would be 30, the stop value would be 100 and the section could
     * be somewhere between 30 and 100 degrees.
     * @return
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
        init(getWidth(), getHeight());
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
     * @return
     */
    public java.awt.Color getTrackStartColor()
    {
        return this.trackStartColor;
    }

    /**
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
        init(getWidth(), getHeight());
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
     * @return
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
        init(getWidth(), getHeight());
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
     * @return
     */
    public java.awt.Color getTrackStopColor()
    {
        return this.trackStopColor;
    }

    /**
     * Sets the color of the value where the track ends
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
        init(getWidth(), getHeight());
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
     * @return
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
        init(getWidth(), getHeight());
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
        init(getWidth(), getHeight());
        repaint();
    }

    /**
     * Clear the TICKMARK_SECTIONS arraylist
     */
    public void resetTickmarkSections()
    {
        TICKMARK_SECTIONS.clear();
        init(getWidth(), getHeight());
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
        init(getWidth(), getHeight());
        repaint();
    }

    /**
     * Returns true if customer defined tickmark labels will be
     * used for the scaling.
     * e.g. you only want to show "0, 10, 50, 100" in your
     * gauge scale so you could set the custom tickmarklabels
     * to these values.
     * @return
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
     * @return
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
        init(getWidth(), getHeight());
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
        init(getWidth(), getHeight());
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
            init(getWidth(), getHeight());
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
        init(getWidth(), getHeight());
        repaint();
    }

    /**
     * Returns the custom background paint that will be used instead of
     * the predefined backgroundcolors like DARK_GRAY, BEIGE etc.
     * @return
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
            init(getWidth(), getHeight());
            repaint();
        }
    }

    /**
     * Returns the backgroundcolor of the gauge.
     * The backgroundcolor is not a standard color but more a
     * color scheme with colors and a gradient.
     * The typical backgroundcolor is DARK_GRAY.
     * @return
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
        init(getWidth(), getHeight());
        repaint();
    }

    /**
     * Returns the framedesign of the component.
     * The framedesign is some kind of a color scheme for the
     * frame of the component.
     * The typical framedesign is METAL
     * @return
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
        init(getWidth(), getHeight());
        repaint();
    }

    /**
     * Returns the color of the bar
     * @return the selected color of the bar
     */
    public eu.hansolo.steelseries.tools.ColorDef getValueColor()
    {
        return this.valueColor;
    }

    /**
     * Sets the color of the bar
     * @param VALUE_COLOR
     */
    public void setValueColor(final eu.hansolo.steelseries.tools.ColorDef VALUE_COLOR)
    {
        this.valueColor = VALUE_COLOR;
        repaint();
    }

    /**
     * Returns true if the bar/bargraph will always start from zero instead from
     * the minValue. This could be useful if you would like to create something
     * like a g-force meter, where 0 is in the center of the range and the bar
     * could move in negative and positive direction. In combination with 
     * AutoReturnToZero this feature might be useful.
     * @return true if the bar/bargraph will always start to in-/decrease from zero
     */
    public boolean isStartingFromZero()
    {
        return this.startingFromZero;
    }

    /**
     * Enables/Disables the feature that the bar/bargraph will always start from zero
     * instead from the minValue. This could be useful if you would like to create
     * something like a g-force meter, where 0 is in the center of the range and
     * the bar could move in negative and positive direction. In combination with
     * AutoReturnToZero this feature might be useful.
     * @param STARTING_FROM_ZERO
     */
    public void setStartingFromZero(final boolean STARTING_FROM_ZERO)
    {
        this.startingFromZero = STARTING_FROM_ZERO;
    }

    /**
     * Returns the visiblity of the threshold led.
     * @return
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
     * @return
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
     * @return
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
     * @return
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

        if (orientation == javax.swing.SwingConstants.HORIZONTAL)
        {
            // Horizontal
            ledImageOff = create_LED_Image(getHeight(), 0, ledColor);
            ledImageOn = create_LED_Image(getHeight(), 1, ledColor);
        }
        else
        {
            // Vertical
            ledImageOff = create_LED_Image(getWidth(), 0, ledColor);
            ledImageOn = create_LED_Image(getWidth(), 1, ledColor);
        }   
        
        currentLedImage = LED_WAS_ON == true ? ledImageOn : ledImageOff;

        repaint();
    }

    /**
     * Returns the state of the threshold led.
     * The led could blink which will be triggered by a javax.swing.Timer
     * that triggers every 500 ms. The blinking will be done by switching
     * between two images.
     * @return
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

    public void createLedImages()
    {
        if (orientation == javax.swing.SwingConstants.VERTICAL)
        {
            ledImageOff = create_LED_Image(getWidth(), 0, ledColor);
            ledImageOn = create_LED_Image(getWidth(), 1, ledColor);
        }
        else
        {
            ledImageOff = create_LED_Image(getHeight(), 0, ledColor);
            ledImageOn = create_LED_Image(getHeight(), 1, ledColor);
        }
    }

    public java.awt.image.BufferedImage getCurrentLedImage(final boolean ACTIVE)
    {
        if (ACTIVE)
        {
            return ledImageOn;
        }
        else
        {
            return ledImageOff;
        }
    }

    /**
     * Returns true if the led is active
     * @return
     */
    public boolean isLedOn()
    {
        return this.ledOn;
    }

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

        // Check if AutoResetToZero is possible
        if (minValue > 0 || maxValue < 0)
        {
            autoResetToZero = false;
        }
    }

    /**
     * Returns the image of the switched on threshold led
     * with the currently active ledcolor.
     * @return
     */
    protected java.awt.image.BufferedImage getLedImageOn()
    {
        return this.ledImageOn;
    }

    /**
     * Returns the image of the switched off threshold led
     * with the currently active ledcolor.
     * @return
     */
    protected java.awt.image.BufferedImage getLedImageOff()
    {
        return this.ledImageOff;
    }

    /**
     * Returns the image of the currently used led image.
     * @return
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

    /**
     * Returns the integer that represents the orientation of the
     * component. The integer could be validated by using
     * javax.swing.SwingConstants.HORIZONTAL
     * javax.swing.SwingConstants.VERTICAL
     * @return the int that represents the orientation (using SwingConstants)
     */
    public int getOrientation()
    {
        return this.orientation;
    }

    /**
     * Sets the orientation of the component using SwingConstants.
     * This method is needed for the LinearLcd component.
     * @param ORIENTATION
     */
    protected void setOrientation(final int ORIENTATION)
    {
        this.orientation = ORIENTATION;
    }

    abstract protected java.awt.geom.Point2D getCenter();

    abstract protected java.awt.geom.Rectangle2D getBounds2D();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Image creation methods">
    /**
     * Returns the background image with the currently active backgroundcolor
     * with the given width and height.
     * @param WIDTH
     * @param HEIGHT
     * @return
     */
    protected java.awt.image.BufferedImage create_BACKGROUND_Image(final int WIDTH, final int HEIGHT)
    {
        if (WIDTH <= 0 || HEIGHT <= 0)
        {
            return null;
        }

        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, HEIGHT, java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final double OUTER_FRAME_CORNER_RADIUS;
        if (IMAGE_WIDTH >= IMAGE_HEIGHT)
        {
            OUTER_FRAME_CORNER_RADIUS = IMAGE_HEIGHT * 0.05;
        }
        else
        {
            OUTER_FRAME_CORNER_RADIUS = IMAGE_WIDTH * 0.05;
        }

        final java.awt.geom.RoundRectangle2D OUTER_FRAME = new java.awt.geom.RoundRectangle2D.Double(0.0, 0.0, IMAGE_WIDTH, IMAGE_HEIGHT, OUTER_FRAME_CORNER_RADIUS, OUTER_FRAME_CORNER_RADIUS);
        final java.awt.Color COLOR_OUTER_FRAME = new java.awt.Color(0x848484);
        G2.setColor(COLOR_OUTER_FRAME);
        if (frameDesign != eu.hansolo.steelseries.tools.FrameDesign.NO_FRAME)
        {
            G2.fill(OUTER_FRAME);
        }

        final double FRAME_MAIN_CORNER_RADIUS;
        if (IMAGE_WIDTH >= IMAGE_HEIGHT)
        {
            FRAME_MAIN_CORNER_RADIUS = OUTER_FRAME_CORNER_RADIUS - ((OUTER_FRAME.getHeight() - IMAGE_HEIGHT - 2) / 2.0);
        }
        else
        {
            FRAME_MAIN_CORNER_RADIUS = OUTER_FRAME_CORNER_RADIUS - ((OUTER_FRAME.getWidth() - IMAGE_WIDTH - 2) / 2.0);
        }
        final java.awt.geom.RoundRectangle2D FRAME_MAIN = new java.awt.geom.RoundRectangle2D.Double(1.0, 1.0, IMAGE_WIDTH - 2, IMAGE_HEIGHT - 2, FRAME_MAIN_CORNER_RADIUS, FRAME_MAIN_CORNER_RADIUS);
        final java.awt.geom.Point2D FRAME_MAIN_START = new java.awt.geom.Point2D.Double(0, FRAME_MAIN.getBounds2D().getMinY() );
        final java.awt.geom.Point2D FRAME_MAIN_STOP = new java.awt.geom.Point2D.Double(0, FRAME_MAIN.getBounds2D().getMaxY() );
        final java.awt.geom.Point2D FRAME_MAIN_CENTER = new java.awt.geom.Point2D.Double(FRAME_MAIN.getCenterX(), FRAME_MAIN.getCenterY());

        final float ANGLE_OFFSET = (float) Math.toDegrees(Math.atan((IMAGE_HEIGHT / 8.0f) / (IMAGE_WIDTH / 2.0f)));
        final float[] FRAME_MAIN_FRACTIONS;
        final java.awt.Color[] FRAME_MAIN_COLORS;
        final java.awt.Paint FRAME_MAIN_GRADIENT;

        switch(this.frameDesign)
        {
            case BLACK_METAL:               
                FRAME_MAIN_FRACTIONS = new float[]
                {
                    0.0f,
                    90.0f - 2 * ANGLE_OFFSET,
                    90.0f,
                    90.0f + 3 * ANGLE_OFFSET,
                    180.0f,
                    270.0f - 3 * ANGLE_OFFSET,
                    270.0f,
                    270.0f + 2 * ANGLE_OFFSET,
                    1.0f
                };

                FRAME_MAIN_COLORS = new java.awt.Color[]
                {
                    new java.awt.Color(254, 254, 254, 255),
                    new java.awt.Color(0, 0, 0, 255),
                    new java.awt.Color(153, 153, 153, 255),
                    new java.awt.Color(0, 0, 0, 255),
                    new java.awt.Color(0, 0, 0, 255),
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
                    90.0f - 2 * ANGLE_OFFSET,
                    90.0f,
                    90.0f + 3 * ANGLE_OFFSET,
                    180.0f,
                    270.0f - 3 * ANGLE_OFFSET,
                    270.0f,
                    270.0f + 2 * ANGLE_OFFSET,
                    1.0f
                };

                FRAME_MAIN_COLORS = new java.awt.Color[]
                {
                    new java.awt.Color(254, 254, 254, 255),
                    new java.awt.Color(179, 179, 179, 255),
                    new java.awt.Color(238, 238, 238, 255),
                    new java.awt.Color(179, 179, 179, 255),
                    new java.awt.Color(179, 179, 179, 255),
                    new java.awt.Color(179, 179, 179, 255),
                    new java.awt.Color(238, 238, 238, 255),
                    new java.awt.Color(179, 179, 179, 255),
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
        if (frameDesign != eu.hansolo.steelseries.tools.FrameDesign.NO_FRAME)
        {
            G2.fill(FRAME_MAIN);
        }

        final double INNER_FRAME_CORNER_RADIUS;
        if (IMAGE_WIDTH >= IMAGE_HEIGHT)
        {
            INNER_FRAME_CORNER_RADIUS = IMAGE_HEIGHT * 0.02857143;
        }
        else
        {
            INNER_FRAME_CORNER_RADIUS = IMAGE_WIDTH * 0.02857143;
        }

        final java.awt.geom.RoundRectangle2D INNER_FRAME = new java.awt.geom.RoundRectangle2D.Double(FRAME_MAIN.getX() + 16, FRAME_MAIN.getY() + 16, FRAME_MAIN.getWidth() - 32, FRAME_MAIN.getHeight() - 32, INNER_FRAME_CORNER_RADIUS, INNER_FRAME_CORNER_RADIUS);
        G2.setColor(java.awt.Color.WHITE);
        if (frameDesign != eu.hansolo.steelseries.tools.FrameDesign.NO_FRAME)
        {
            G2.fill(INNER_FRAME);
        }
        
        final double BACKGROUND_CORNER_RADIUS = INNER_FRAME_CORNER_RADIUS - 1;

        final java.awt.geom.RoundRectangle2D BACKGROUND = new java.awt.geom.RoundRectangle2D.Double(INNER_FRAME.getX() + 1, INNER_FRAME.getY() + 1, INNER_FRAME.getWidth() - 2, INNER_FRAME.getHeight() - 2, BACKGROUND_CORNER_RADIUS, BACKGROUND_CORNER_RADIUS);
        final java.awt.geom.Point2D BACKGROUND_START = new java.awt.geom.Point2D.Double(0, BACKGROUND.getBounds2D().getMinY() );
        final java.awt.geom.Point2D BACKGROUND_STOP = new java.awt.geom.Point2D.Double(0, BACKGROUND.getBounds2D().getMaxY() );
        final float[] BACKGROUND_FRACTIONS =
        {
            0.0f,
            0.4f,            
            1.0f
        };
        final java.awt.Color[] BACKGROUND_COLORS =
        {
            backgroundColor.GRADIENT_START_COLOR,
            backgroundColor.GRADIENT_FRACTION_COLOR,
            backgroundColor.GRADIENT_STOP_COLOR
        };
        
        final java.awt.Paint BACKGROUND_PAINT;

        if (backgroundColor == eu.hansolo.steelseries.tools.BackgroundColor.BRUSHED_METAL)
        {
            BACKGROUND_PAINT = new java.awt.TexturePaint(UTIL.createBrushMetalTexture(null, BACKGROUND.getBounds().width, BACKGROUND.getBounds().height), BACKGROUND.getBounds());
        }
        else
        {
            BACKGROUND_PAINT = new java.awt.LinearGradientPaint(BACKGROUND_START, BACKGROUND_STOP, BACKGROUND_FRACTIONS, BACKGROUND_COLORS);
        }

        // Set custom background paint if selected
        if (customBackgroundVisible)
        {
            G2.setPaint(customBackground);
        }
        else
        {            
            G2.setPaint(BACKGROUND_PAINT);
        }
        G2.fill(BACKGROUND);

        // Create inner shadow on background shape
        final java.awt.image.BufferedImage CLP;
        if (customBackgroundVisible)
        {
            CLP = eu.hansolo.steelseries.tools.Shadow.INSTANCE.createInnerShadow((java.awt.Shape) BACKGROUND, customBackground, 0, 0.65f, java.awt.Color.BLACK, 20, 315);
        }
        else
        {
            CLP = eu.hansolo.steelseries.tools.Shadow.INSTANCE.createInnerShadow((java.awt.Shape) BACKGROUND, BACKGROUND_PAINT, 0, 0.65f, java.awt.Color.BLACK, 20, 315);
        }
        G2.drawImage(CLP, BACKGROUND.getBounds().x, BACKGROUND.getBounds().y, null);

        // Draw the custom layer if selected
        if (customLayerVisible)
        {
            G2.drawImage(UTIL.getScaledInstance(customLayer, IMAGE_WIDTH, IMAGE_HEIGHT, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC, true), 0, 0, null);
        }

        G2.dispose();

        return IMAGE;
    }

    /**
     * Returns the track image with the given values.
     * @param WIDTH
     * @param HEIGHT
     * @param MIN_VALUE
     * @param MAX_VALUE
     * @param TRACK_START
     * @param TRACK_STOP
     * @param TRACK_START_COLOR
     * @param TRACK_STOP_COLOR
     * @return
     */
    protected java.awt.image.BufferedImage create_TRACK_Image(final int WIDTH, final int HEIGHT, final double MIN_VALUE, final double MAX_VALUE, final double TRACK_START, final double TRACK_SECTION, final double TRACK_STOP, final java.awt.Color TRACK_START_COLOR, final java.awt.Color TRACK_SECTION_COLOR, final java.awt.Color TRACK_STOP_COLOR)
    {
        if (WIDTH <= 0 || HEIGHT <= 0)
        {
            return null;
        }

        if (TRACK_STOP > MAX_VALUE)
        {
            throw new IllegalArgumentException("Please adjust track start and/or track stop values");
        }
        
        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, HEIGHT, java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();
        
        final java.awt.geom.Rectangle2D TRACK;
        final java.awt.geom.Point2D TRACK_START_POINT;
        final java.awt.geom.Point2D TRACK_STOP_POINT;

        if (orientation == javax.swing.SwingConstants.VERTICAL)
        {            
                // Vertical orientation
                TRACK = new java.awt.geom.Rectangle2D.Double(IMAGE_WIDTH * 0.315, IMAGE_HEIGHT * 0.1276, IMAGE_WIDTH * 0.05, IMAGE_HEIGHT * 0.7280);
                TRACK_START_POINT = new java.awt.geom.Point2D.Double(0, TRACK.getMaxY());
                TRACK_STOP_POINT = new java.awt.geom.Point2D.Double(0, TRACK.getMinY());
        }
        else
        {
                // Horizontal orientation
                TRACK = new java.awt.geom.Rectangle2D.Double(IMAGE_WIDTH * 0.139, IMAGE_HEIGHT * 0.6285714285714286, IMAGE_WIDTH * 0.735, IMAGE_HEIGHT * 0.05);
                TRACK_START_POINT = new java.awt.geom.Point2D.Double(TRACK.getMinX(), 0);
                TRACK_STOP_POINT = new java.awt.geom.Point2D.Double(TRACK.getMaxX(), 0);        
        }
        
        // Calculate the track start and stop position for the gradient
        final float TRACK_START_POSITION = (float) (TRACK_START / (MAX_VALUE - MIN_VALUE));
        final float TRACK_STOP_POSITION = (float) (TRACK_STOP / (MAX_VALUE - MIN_VALUE));

        final java.awt.Color FULLY_TRANSPARENT = new java.awt.Color(0.0f, 0.0f, 0.0f, 0.0f);

        final float[] TRACK_FRACTIONS;
        final java.awt.Color[] TRACK_COLORS;

        // Three color gradient from trackStart over trackSection to trackStop
        final float TRACK_SECTION_POSITION = (float) (TRACK_SECTION / (MAX_VALUE - MIN_VALUE));

        TRACK_FRACTIONS = new float[]
        {
            0.0f,
            TRACK_START_POSITION + 0.001f,
            TRACK_START_POSITION + 0.002f,
            TRACK_SECTION_POSITION,
            TRACK_STOP_POSITION - 0.002f,
            TRACK_STOP_POSITION - 0.001f,
            1.0f
        };

        TRACK_COLORS = new java.awt.Color[]
        {
            FULLY_TRANSPARENT,
            FULLY_TRANSPARENT,
            TRACK_START_COLOR,
            TRACK_SECTION_COLOR,
            TRACK_STOP_COLOR,
            FULLY_TRANSPARENT,
            FULLY_TRANSPARENT
        };
       
        final java.awt.LinearGradientPaint TRACK_GRADIENT = new java.awt.LinearGradientPaint(TRACK_START_POINT, TRACK_STOP_POINT, TRACK_FRACTIONS, TRACK_COLORS);
        G2.setPaint(TRACK_GRADIENT);
        G2.fill(TRACK);

        G2.dispose();

        return IMAGE;
    }

    /**
     * Returns the image of the title.
     * @param WIDTH
     * @param HEIGHT
     * @param UNIT_STRING_VISIBLE
     * @return
     */
    protected java.awt.image.BufferedImage create_TITLE_Image(final int WIDTH, final int HEIGHT, final boolean UNIT_STRING_VISIBLE)
    {
        if (WIDTH <= 0 || HEIGHT <= 0)
        {
            return null;
        }
        
        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, HEIGHT, java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_FRACTIONALMETRICS, java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final java.awt.font.FontRenderContext RENDER_CONTEXT = new java.awt.font.FontRenderContext(null, true, true);

        if (useLabelColorFromTheme())
        {
            G2.setColor(backgroundColor.LABEL_COLOR);
        }
        else
        {
            G2.setColor(getLabelColor());
        }

        final java.awt.font.TextLayout LAYOUT_TITLE;

        if (orientation == javax.swing.SwingConstants.VERTICAL)
        {            
                // Vertical orientation
            // Draw title
            // Use custom font if selected
            if (getUseTitleAndUnitFont())
            {
                G2.setFont(new java.awt.Font(getTitleAndUnitFont().getFamily(), 0, (int) (0.1 * IMAGE_WIDTH)));
            }
            else
            {
                G2.setFont(new java.awt.Font("Verdana", 0, (int) (0.1 * IMAGE_WIDTH)));
            }
            LAYOUT_TITLE = new java.awt.font.TextLayout(getTitle(), G2.getFont(), RENDER_CONTEXT);
            final java.awt.geom.AffineTransform OLD_TRANSFORM = G2.getTransform();
            G2.translate(0.0, -0.05 * IMAGE_HEIGHT);
            G2.rotate(1.5707963267948966, 0.6714285714285714f * IMAGE_WIDTH, 0.1375f * IMAGE_HEIGHT + LAYOUT_TITLE.getAscent() - LAYOUT_TITLE.getDescent());
            G2.drawString(getTitle(), 0.6714285714285714f * IMAGE_WIDTH, 0.1375f * IMAGE_HEIGHT + LAYOUT_TITLE.getAscent() - LAYOUT_TITLE.getDescent());
            G2.setTransform(OLD_TRANSFORM);

            // Draw unit string
            if (UNIT_STRING_VISIBLE)
            {
                if (getUseTitleAndUnitFont())
                {
                    G2.setFont(new java.awt.Font(getTitleAndUnitFont().getFamily(), 0, (int) (0.07142857142857142 * IMAGE_WIDTH)));
                }
                else
                {
                    G2.setFont(new java.awt.Font("Verdana", 0, (int) (0.07142857142857142 * IMAGE_WIDTH)));
                }
                final java.awt.font.TextLayout LAYOUT_UNIT = new java.awt.font.TextLayout(getUnitString(), G2.getFont(), RENDER_CONTEXT);
                final java.awt.geom.Rectangle2D UNIT_BOUNDARY = LAYOUT_UNIT.getBounds();
                G2.drawString(getUnitString(), (float) (IMAGE_WIDTH - UNIT_BOUNDARY.getWidth()) / 2f, 0.8875f * IMAGE_HEIGHT + LAYOUT_UNIT.getAscent() - LAYOUT_UNIT.getDescent());
            }
        }
        else
        {
            // Horizontal orientation
            // Draw title
            if (getUseTitleAndUnitFont())
            {
                G2.setFont(new java.awt.Font(getTitleAndUnitFont().getFamily(), 0, (int) (0.1 * IMAGE_HEIGHT)));
            }
            else
            {
                G2.setFont(new java.awt.Font("Verdana", 0, (int) (0.1 * IMAGE_HEIGHT)));
            }
            LAYOUT_TITLE = new java.awt.font.TextLayout(getTitle(), G2.getFont(), RENDER_CONTEXT);
            G2.drawString(getTitle(), 0.15f * IMAGE_WIDTH, 0.25f * IMAGE_HEIGHT + LAYOUT_TITLE.getAscent() - LAYOUT_TITLE.getDescent());

            // Draw unit string
            if (UNIT_STRING_VISIBLE)
            {
                if (getUseTitleAndUnitFont())
                {
                    G2.setFont(new java.awt.Font(getTitleAndUnitFont().getFamily(), 0, (int) (0.025 * IMAGE_WIDTH)));
                }
                else
                {
                    G2.setFont(new java.awt.Font("Verdana", 0, (int) (0.025 * IMAGE_WIDTH)));
                }
                final java.awt.font.TextLayout LAYOUT_UNIT = new java.awt.font.TextLayout(getUnitString(), G2.getFont(), RENDER_CONTEXT);
                G2.drawString(getUnitString(), 0.0625f * IMAGE_WIDTH, 0.7f * IMAGE_HEIGHT + LAYOUT_UNIT.getAscent() - LAYOUT_UNIT.getDescent());
            }
        }

        G2.dispose();

        return IMAGE;
    }

    /**
     * Returns the image of the tickmarks
     * @param WIDTH
     * @param HEIGHT
     * @param MIN_VALUE
     * @param MAX_VALUE
     * @param TICK_LABEL_PERIOD
     * @return
     */
    protected java.awt.image.BufferedImage create_TICKMARKS_Image(final int WIDTH, final int HEIGHT, final double MIN_VALUE, final double MAX_VALUE, final int TICK_LABEL_PERIOD)
    {
        return create_TICKMARKS_Image(WIDTH, HEIGHT, MIN_VALUE, MAX_VALUE, TICK_LABEL_PERIOD, 0, true, true, null);
    }

    /**
     * Returns the image of the tickmarks
     * @param WIDTH
     * @param HEIGHT
     * @param MIN_VALUE
     * @param MAX_VALUE
     * @param TICK_LABEL_PERIOD
     * @return
     */
    protected java.awt.image.BufferedImage create_TICKMARKS_Image(final int WIDTH, final int HEIGHT, final double MIN_VALUE, final double MAX_VALUE, final int TICK_LABEL_PERIOD, final int SCALE_DIVIDER_POWER, final boolean DRAW_TICKS, final boolean DRAW_TICK_LABELS, final java.util.ArrayList<eu.hansolo.steelseries.tools.Section> TICKMARK_SECTIONS)
    {
        if (WIDTH <= 0 || HEIGHT <= 0)
        {
            return null;
        }

        if (!DRAW_TICKS && !DRAW_TICK_LABELS)
        {
            return null;
        }
        
        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, HEIGHT, java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Definitions
        final java.awt.Font STD_FONT;
        final java.awt.BasicStroke STD_STROKE = new java.awt.BasicStroke(1.0f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_BEVEL);
        final java.awt.BasicStroke MEDIUM_STROKE = new java.awt.BasicStroke(0.5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_BEVEL);
        final java.awt.BasicStroke THIN_STROKE = new java.awt.BasicStroke(0.3f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_BEVEL);
        final java.awt.BasicStroke VERY_THIN_STROKE = new java.awt.BasicStroke(0.1f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_BEVEL);
        
        final double TICK_MAX;
        final double TICK_MIN;

        if (orientation == javax.swing.SwingConstants.VERTICAL)
        {            
            // Vertical orientation
            STD_FONT = new java.awt.Font("Verdana", 0, (int) (0.062 * WIDTH));
            TICK_MIN = HEIGHT * 0.8567961165048543; // position of min value
            TICK_MAX =  HEIGHT * 0.12864077669902912; // position of max value
        }
        else
        {
            // Horizontal orientation
            STD_FONT = new java.awt.Font("Verdana", 0, (int) (0.062 * HEIGHT));
            TICK_MIN = WIDTH * 0.14285714285714285;
            TICK_MAX =  WIDTH * 0.8710124827; // position of max value
        }
        final double RANGE = maxValue - minValue;
        final double TICK_STEP = (TICK_MAX - TICK_MIN) / RANGE; // pixel per tick

        G2.setFont(STD_FONT);
        
        final java.awt.font.FontRenderContext RENDER_CONTEXT = new java.awt.font.FontRenderContext(null, true, true);
        final java.awt.font.TextLayout TEXT_LAYOUT = new java.awt.font.TextLayout(String.valueOf(maxValue), G2.getFont(), RENDER_CONTEXT);
        final java.awt.geom.Rectangle2D MAX_BOUNDS = TEXT_LAYOUT.getBounds(); // needed to align the numbers on the right (in vertical layout)

        java.awt.font.TextLayout currentLayout;
        java.awt.geom.Rectangle2D currentBounds;
        float textOffset = 0;
                
        java.awt.geom.Line2D tick;
        int counter = 0;
        int tickCounter = 0;
 
//        if (useTickmarkColorFromTheme())
//        {
//            G2.setColor(backgroundColor.LABEL_COLOR);
//        }
//        else
//        {
//            G2.setColor(tickmarkColor);
//        }

        for (double pos = minValue ; pos < maxValue + 1 ; pos++)
        {
            if (TICKMARK_SECTIONS != null && !TICKMARK_SECTIONS.isEmpty())
            {
                if (tickmarkSectionsVisible)
                {
                    for (eu.hansolo.steelseries.tools.Section section : TICKMARK_SECTIONS)
                    {
                        if (pos >= section.getStart() && pos <= section.getStop())
                        {
                            G2.setColor(section.getColor());
                            break;
                        }
                        else if(tickmarkColorFromTheme)
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
                    if(tickmarkColorFromTheme)
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
                if(tickmarkColorFromTheme)
                {
                    G2.setColor(backgroundColor.LABEL_COLOR);
                }
                else
                {
                    G2.setColor(tickmarkColor);
                }
            }

            double currentPos;
            if (minValue <= 0)
            {
                currentPos = TICK_MIN + (pos * TICK_STEP) + Math.abs(minValue) * TICK_STEP;
            }
            else
            {
                currentPos = TICK_MIN + (pos * TICK_STEP) - Math.abs(minValue) * TICK_STEP;
            }


            // Very thin tickmark every 0.1 unit
            if (RANGE <= 10 && counter != 0)
            {                
                final double STEP_SIZE = TICK_STEP / 10.0;
                G2.setStroke(VERY_THIN_STROKE);
                if (DRAW_TICKS)
                {
                    for (double tmpPos = (currentPos - STEP_SIZE) ; tmpPos < (currentPos - TICK_STEP) ; tmpPos -= STEP_SIZE)
                    {
                        if (orientation == javax.swing.SwingConstants.VERTICAL)
                        {
                            // Vertical orientation
                            tick = new java.awt.geom.Line2D.Double(WIDTH * 0.35, (int) tmpPos, WIDTH * 0.36, tmpPos);
                        }
                        else
                        {
                            // Horizontal orientation
                            tick = new java.awt.geom.Line2D.Double((int) tmpPos, HEIGHT * 0.66, (int) tmpPos, HEIGHT * 0.63);
                        }
                        G2.draw(tick);
                    }
                }
            }

            // Thin tickmark every 1 unit
            if (counter % 5 != 0 && counter % 10 != 0 && counter % 100 != 0 && RANGE < 500)
            {
                if (DRAW_TICKS)
                {
                    G2.setStroke(THIN_STROKE);
                    if (orientation == javax.swing.SwingConstants.VERTICAL)
                    {
                        // Vertical orientation
                        tick = new java.awt.geom.Line2D.Double(WIDTH * 0.34, currentPos, WIDTH * 0.36, currentPos);
                    }
                    else
                    {
                        // Horizontal orientation
                        tick = new java.awt.geom.Line2D.Double(currentPos, HEIGHT * 0.65, currentPos, HEIGHT * 0.63);
                    }
                    G2.draw(tick);
                }
            }

            // Medium tickmark every 5 units
            if (counter % 5 == 0 && counter % 10 != 0 && counter % 100 != 0 && RANGE < 1000)
            {
                if (DRAW_TICKS)
                {
                    G2.setStroke(MEDIUM_STROKE);
                    if (orientation == javax.swing.SwingConstants.VERTICAL)
                    {
                        // Vertical orientation
                        tick = new java.awt.geom.Line2D.Double(WIDTH * 0.33, currentPos, WIDTH * 0.36, currentPos);
                    }
                    else
                    {
                        // Horizontal orientation
                        tick = new java.awt.geom.Line2D.Double(currentPos, HEIGHT * 0.66, currentPos, HEIGHT * 0.63);
                    }
                    G2.draw(tick);
                }
            }

            // Standard tickmark every 10 units
            if (counter % 10 == 0 && counter % 100 != 0 || counter == 0 && RANGE < 1000)
            {
                if (DRAW_TICKS)
                {
                    G2.setStroke(STD_STROKE);
                    if (orientation == javax.swing.SwingConstants.VERTICAL)
                    {
                        // Vertical orientation
                        tick = new java.awt.geom.Line2D.Double(WIDTH * 0.32, currentPos, WIDTH * 0.36, currentPos);
                    }
                    else
                    {
                        // Horizontal orientation
                        tick = new java.awt.geom.Line2D.Double(currentPos, HEIGHT * 0.67, currentPos, HEIGHT * 0.63);
                    }
                    G2.draw(tick);
                }
            }

            // Longer standard tickmark every 100 units
            if (counter == 100)
            {
                if (DRAW_TICKS)
                {
                    G2.setStroke(STD_STROKE);
                    if (orientation == javax.swing.SwingConstants.VERTICAL)
                    {
                        // Vertical orientation
                        tick = new java.awt.geom.Line2D.Double(WIDTH * 0.31, currentPos, WIDTH * 0.36, currentPos);
                    }
                    else
                    {
                        // Horizontal orientation
                        tick = new java.awt.geom.Line2D.Double(currentPos, HEIGHT * 0.68, currentPos, HEIGHT * 0.63);
                    }
                    G2.draw(tick);
                }
                counter = 0;
                tickCounter++;
            }

            // Draw text
            // Draw the tickmark labels
            if (DRAW_TICK_LABELS)
            {
                if (useCustomTickmarkLabels)
                {
                    // Draw custom tickmark labels if selected
                    for (double tickLabel : CUSTOM_TICKMARK_LABELS)
                    {
                        if (Double.compare(pos, tickLabel) == 0)
                        {
                            currentLayout = new java.awt.font.TextLayout(String.valueOf(pos), G2.getFont(), RENDER_CONTEXT);
                            currentBounds = currentLayout.getBounds();                
                            if (orientation == javax.swing.SwingConstants.VERTICAL)
                            {
                                // Vertical orientation
                                textOffset = (float) (MAX_BOUNDS.getWidth() - currentBounds.getWidth());
                                G2.drawString(String.valueOf((int)pos), 0.18f * WIDTH + textOffset, (float) (currentPos - currentBounds.getHeight() / 2.0 + currentBounds.getHeight()));
                            }
                            else
                            {
                                // Horizontal orientation
                                G2.drawString(String.valueOf((int)pos), (float) (currentPos - currentBounds.getWidth() / 3.0), (float) (HEIGHT * 0.68 + 1.5 * currentBounds.getHeight()));
                            }
                        }
                    }
                }
                else
                {
                    // Draw the standard tickmark labels
                    if (pos % TICK_LABEL_PERIOD == 0)
                    {
                        currentLayout = new java.awt.font.TextLayout(String.valueOf(pos), G2.getFont(), RENDER_CONTEXT);
                        currentBounds = currentLayout.getBounds();                
                        if (orientation == javax.swing.SwingConstants.VERTICAL)
                        {
                            // Vertical orientation
                            textOffset = (float) (MAX_BOUNDS.getWidth() - currentBounds.getWidth());
                            G2.drawString(String.valueOf((int)pos), 0.18f * WIDTH + textOffset, (float) (currentPos - currentBounds.getHeight() / 2.0 + currentBounds.getHeight()));
                        }
                        else
                        {
                            // Horizontal orientation
                            G2.drawString(String.valueOf((int)pos), (float) (currentPos - currentBounds.getWidth() / 3.0), (float) (HEIGHT * 0.68 + 1.5 * currentBounds.getHeight()));
                        }
                    }
                }
            }

            counter++;
        }

        G2.dispose();

        return IMAGE;
    }

    /**
     * Returns the image of the threshold indicator
     * @param WIDTH
     * @param HEIGHT
     * @return
     */
    protected java.awt.image.BufferedImage create_THRESHOLD_Image(final int WIDTH, final int HEIGHT)
    {
        if (WIDTH <= 0 || HEIGHT <= 0)
        {
            return null;
        }
                
        final int IMAGE_WIDTH;
        final int IMAGE_HEIGHT;
        if (orientation == javax.swing.SwingConstants.VERTICAL)
        {            
                // Vertical orientation
                IMAGE_WIDTH = (int) (WIDTH * 0.0714285714);
                IMAGE_HEIGHT = (int) (IMAGE_WIDTH * 0.8);
        }
        else
        {
                // Horizontal orientation
                IMAGE_HEIGHT = (int) (HEIGHT * 0.0714285714);
                IMAGE_WIDTH = (int) (IMAGE_HEIGHT * 0.8);        
        }
        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage(IMAGE_WIDTH, IMAGE_HEIGHT, java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        G2.translate(0, IMAGE_WIDTH * 0.005);
        final java.awt.geom.GeneralPath THRESHOLD = new java.awt.geom.GeneralPath();
        THRESHOLD.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        final java.awt.geom.Point2D THRESHOLD_START;
        final java.awt.geom.Point2D THRESHOLD_STOP;

        if (orientation == javax.swing.SwingConstants.VERTICAL)
        {
            // Vertical orientation
            THRESHOLD.moveTo(IMAGE_WIDTH * 0.1, IMAGE_HEIGHT * 0.5);
            THRESHOLD.lineTo(IMAGE_WIDTH * 0.9, IMAGE_HEIGHT * 0.1);
            THRESHOLD.lineTo(IMAGE_WIDTH * 0.9, IMAGE_HEIGHT * 0.9);
            THRESHOLD.lineTo(IMAGE_WIDTH * 0.1, IMAGE_HEIGHT * 0.5);
            THRESHOLD.closePath();
            THRESHOLD_START = new java.awt.geom.Point2D.Double(THRESHOLD.getBounds2D().getMinX(), 0);
            THRESHOLD_STOP = new java.awt.geom.Point2D.Double(THRESHOLD.getBounds2D().getMaxX(), 0);
        }
        else
        {
            // Horizontal orientation
            THRESHOLD.moveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.9);
            THRESHOLD.lineTo(IMAGE_WIDTH * 1.0, IMAGE_HEIGHT * 0.1);
            THRESHOLD.lineTo(IMAGE_WIDTH * 0.1, IMAGE_HEIGHT * 0.1);
            THRESHOLD.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.9);
            THRESHOLD.closePath();
            THRESHOLD_START = new java.awt.geom.Point2D.Double(0, THRESHOLD.getBounds2D().getMaxY() );
            THRESHOLD_STOP = new java.awt.geom.Point2D.Double(0, THRESHOLD.getBounds2D().getMinY() );
        }
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
     * on the given color
     * @param WIDTH
     * @param HEIGHT
     * @param COLOR
     * @return
     */
    protected java.awt.image.BufferedImage create_MEASURED_VALUE_Image(final int WIDTH, final int HEIGHT, final java.awt.Color COLOR)
    {
        if (WIDTH <= 0 || HEIGHT <= 0)
        {
            return null;
        }
                
        final int IMAGE_WIDTH;
        final int IMAGE_HEIGHT;
        if (orientation == javax.swing.SwingConstants.VERTICAL)
        {
            // Vertical orientation
            IMAGE_WIDTH = (int) (WIDTH * 0.05);
            IMAGE_HEIGHT = IMAGE_WIDTH;
        }
        else
        {
            // Horizontal orientation
            IMAGE_HEIGHT = (int) (HEIGHT * 0.05);
            IMAGE_WIDTH = IMAGE_HEIGHT;
        }
       
        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage(IMAGE_WIDTH, IMAGE_HEIGHT, java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);  

        final java.awt.geom.GeneralPath INDICATOR = new java.awt.geom.GeneralPath();
        INDICATOR.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        if (orientation == javax.swing.SwingConstants.VERTICAL)
        {
            INDICATOR.moveTo(IMAGE_WIDTH, IMAGE_HEIGHT * 0.5);
            INDICATOR.lineTo(0.0, 0.0);
            INDICATOR.lineTo(0.0, IMAGE_HEIGHT);
            INDICATOR.lineTo(IMAGE_WIDTH, IMAGE_HEIGHT * 0.5);
            INDICATOR.closePath();
        }
        else
        {
            INDICATOR.moveTo(IMAGE_WIDTH * 0.5, 0.0);
            INDICATOR.lineTo(IMAGE_WIDTH, IMAGE_HEIGHT);
            INDICATOR.lineTo(0.0, IMAGE_HEIGHT);
            INDICATOR.lineTo(IMAGE_WIDTH * 0.5, 0.0);
            INDICATOR.closePath();
        }
        G2.setColor(COLOR);
        G2.fill(INDICATOR);

        G2.dispose();

        return IMAGE;
    }

    /**
     * Returns the image of the glasseffect image
     * @param WIDTH
     * @param HEIGHT
     * @return
     */
    protected java.awt.image.BufferedImage create_FOREGROUND_Image(final int WIDTH, final int HEIGHT)
    {
        if (WIDTH <= 0 || HEIGHT <= 0)
        {
            return null;
        }

        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, HEIGHT, java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final java.awt.geom.GeneralPath GLASSEFFECT = new java.awt.geom.GeneralPath();
        GLASSEFFECT.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        final java.awt.geom.Point2D GLASSEFFECT_START;
        final java.awt.geom.Point2D GLASSEFFECT_STOP;
        final java.awt.LinearGradientPaint GLASSEFFECT_GRADIENT;

        if (WIDTH >= HEIGHT)
        {
            // Horizontal glass effect
            GLASSEFFECT.moveTo(18, IMAGE_HEIGHT - 18);
            GLASSEFFECT.lineTo(IMAGE_WIDTH - 18, IMAGE_HEIGHT - 18);
            GLASSEFFECT.curveTo(IMAGE_WIDTH - 18, IMAGE_HEIGHT - 18, IMAGE_WIDTH - 27, IMAGE_HEIGHT * 0.7, IMAGE_WIDTH - 27, IMAGE_HEIGHT * 0.5);
            GLASSEFFECT.curveTo(IMAGE_WIDTH - 27, 27, IMAGE_WIDTH - 18, 18, IMAGE_WIDTH - 18, 18);
            GLASSEFFECT.lineTo(18, 18);
            GLASSEFFECT.curveTo(18, 18, 27, IMAGE_HEIGHT * 0.2857142857142857, 27, IMAGE_HEIGHT * 0.5);
            GLASSEFFECT.curveTo(27, IMAGE_HEIGHT * 0.7, 18, IMAGE_HEIGHT - 18, 18, IMAGE_HEIGHT - 18);
            GLASSEFFECT.closePath();
            GLASSEFFECT_START = new java.awt.geom.Point2D.Double(0, GLASSEFFECT.getBounds2D().getMaxY() );
            GLASSEFFECT_STOP = new java.awt.geom.Point2D.Double(0, GLASSEFFECT.getBounds2D().getMinY() );
        }
        else
        {
            // Vertical glass effect
            GLASSEFFECT.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
            GLASSEFFECT.moveTo(18, 18);
            GLASSEFFECT.lineTo(18, IMAGE_HEIGHT - 18);
            GLASSEFFECT.curveTo(18, IMAGE_HEIGHT - 18, 27, IMAGE_HEIGHT - 27, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT - 27);
            GLASSEFFECT.curveTo(IMAGE_WIDTH - 27, IMAGE_HEIGHT - 27, IMAGE_WIDTH - 18, IMAGE_HEIGHT - 18, IMAGE_WIDTH - 18, IMAGE_HEIGHT - 18);
            GLASSEFFECT.lineTo(IMAGE_WIDTH - 18, 18);
            GLASSEFFECT.curveTo(IMAGE_WIDTH - 18, 18, IMAGE_WIDTH - 27, 27, IMAGE_WIDTH * 0.5, 27);
            GLASSEFFECT.curveTo(27, 27, 18, 18, 18, 18);
            GLASSEFFECT.closePath();
            GLASSEFFECT_START = new java.awt.geom.Point2D.Double(GLASSEFFECT.getBounds2D().getMinX(), 0);
            GLASSEFFECT_STOP = new java.awt.geom.Point2D.Double(GLASSEFFECT.getBounds2D().getMaxX(), 0);
        }
        
        final float[] GLASSEFFECT_FRACTIONS =
            {
                0.0f,
                0.06f,
                0.07f,
                0.12f,
                0.17f,
                0.1701f,
                0.79f,
                0.8f,
                0.84f,
                0.93f,
                0.94f,
                0.96f,
                0.97f,
                1.0f
            };
        final java.awt.Color[] GLASSEFFECT_COLORS =
        {
            new java.awt.Color(255, 255, 255, 0),
            new java.awt.Color(255, 255, 255, 0),
            new java.awt.Color(255, 255, 255, 0),
            new java.awt.Color(255, 255, 255, 0),
            new java.awt.Color(255, 255, 255, 3),
            new java.awt.Color(255, 255, 255, 5),
            new java.awt.Color(255, 255, 255, 5),
            new java.awt.Color(255, 255, 255, 5),
            new java.awt.Color(255, 255, 255, 20),
            new java.awt.Color(255, 255, 255, 73),
            new java.awt.Color(255, 255, 255, 76),
            new java.awt.Color(255, 255, 255, 30),
            new java.awt.Color(255, 255, 255, 10),
            new java.awt.Color(255, 255, 255, 5)
        };
        GLASSEFFECT_GRADIENT = new java.awt.LinearGradientPaint(GLASSEFFECT_START, GLASSEFFECT_STOP, GLASSEFFECT_FRACTIONS, GLASSEFFECT_COLORS);
        G2.setPaint(GLASSEFFECT_GRADIENT);
        G2.fill(GLASSEFFECT);

        G2.dispose();

        return IMAGE;
    }

    /**
     * Returns a image of a led with the given size, state and color.
     * @param SIZE
     * @param STATE
     * @param LED_COLOR
     * @return
     */
    protected java.awt.image.BufferedImage create_LED_Image(final int SIZE, final int STATE, final eu.hansolo.steelseries.tools.LedColor LED_COLOR)
    {
        if (SIZE <= 0 || HEIGHT <= 0)
        {
            return null;
        }

        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage((int) (SIZE * 0.1428571429), (int) (SIZE * 0.1428571429), java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();

        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);

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

        switch(STATE)
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
    public java.awt.Dimension getMinimumSize()
    {
        return new java.awt.Dimension(50, 50);
    }

    @Override
    public java.awt.Dimension getPreferredSize()
    {
        return new java.awt.Dimension(140, 140);
    }

    @Override
    public java.awt.Dimension getSize(java.awt.Dimension dim)
    {
        if (getWidth() < getMinimumSize().width && getHeight() < getMinimumSize().height)
        {
            return (getMinimumSize());
        }
        else
        {
            return new java.awt.Dimension(getWidth(), getHeight());
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ComponentListener methods">
    // ComponentListener methods
    @Override
    public void componentResized(java.awt.event.ComponentEvent event)
    {
        setPreferredSize(new java.awt.Dimension(getWidth(), getHeight()));
        setSize(getWidth(), getHeight());
        
        if (getWidth() >= getHeight())
        {
            // Horizontal
            orientation = javax.swing.SwingConstants.HORIZONTAL;
            ledImageOff = create_LED_Image(getHeight(), 0, ledColor);
            ledImageOn = create_LED_Image(getHeight(), 1, ledColor);
            if (ledOn)
            {
                setCurrentLedImage(ledImageOn);
            }
            else
            {
                setCurrentLedImage(ledImageOff);
            }
            ledPositionX = getWidth() - 18 - ledImageOn.getWidth() * 0.85;
            ledPositionY = (getHeight() - ledImageOn.getHeight()) / 2.0;
        }
        else
        {
            // Vertical
            orientation = javax.swing.SwingConstants.VERTICAL;
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
            ledPositionX = (getWidth() - ledImageOn.getWidth()) / 2.0;
            ledPositionY =  18;
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

            repaint((int) (ledPositionX), (int) (ledPositionY), currentLedImage.getWidth(), currentLedImage.getHeight());
        }

        if (event.getSource().equals(PEAK_TIMER))
        {
            this.peakValueVisible = false;
            PEAK_TIMER.stop();
        }
    }
    // </editor-fold>

    @Override
    public String toString()
    {
        return "AbstractLinear";
    }
}
