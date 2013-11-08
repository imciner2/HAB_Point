package eu.hansolo.steelseries.gauges;


/**
 *
 * @author hansolo
 */
public abstract class AbstractGauge extends javax.swing.JComponent implements java.awt.event.ComponentListener
{
    // Title and label related
    private String title = "Title";
    private String unitString = "unit";
    private boolean labelColorFromTheme = true;
    private java.awt.Color labelColor = java.awt.Color.WHITE;
    private boolean useTitleAndUnitFont = false;
    private java.awt.Font titleAndUnitFont = new java.awt.Font("Verdana", 0, 10);
    // Value related
    private double value;
    private double peakValue;
    protected static final String VALUE_PROPERTY = "value";
    protected static final String THRESHOLD_PROPERTY = "threshold";

    
    abstract public AbstractGauge init(final int WIDTH, final int HEIGHT);

    public double getValue()
    {
        return this.value;
    }

    public void setValue(final double VALUE)
    {
        final double OLD_VALUE = this.value;
        
        if (Double.compare(VALUE, OLD_VALUE) != 0)
        {
            this.value = VALUE;
            firePropertyChange("value", OLD_VALUE, VALUE);
        }
    }

    public double getPeakValue()
    {
        return this.peakValue;
    }

    public void setPeakValue(final double PEAK_VALUE)
    {     
        this.peakValue = PEAK_VALUE;
    }

        /**
     * Returns the title of the gauge.
     * A title could be for example "Temperature".
     * @return the title of the gauge
     */
    public String getTitle()
    {
        return this.title;
    }

    /**
     * Sets the title of the gauge.
     * A title could be for example "Temperature".
     * @param TITLE
     */
    public void setTitle(final String TITLE)
    {
        this.title = TITLE;
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns the unit string of the gauge.
     * A unit string could be for example "[cm]".
     * @return the unit string of the gauge
     */
    public String getUnitString()
    {
        return this.unitString;
    }

    /**
     * Sets the unit string of the gauge.
     * A unit string could be for example "[cm]"
     * @param UNIT_STRING
     */
    public void setUnitString(final String UNIT_STRING)
    {
        this.unitString = UNIT_STRING;
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns true if the color of the tickmarks will be
     * used from the defined background color.
     * @return true if the color for the tickmarks and labels
     * will be used from the selected backgroundcolor
     */
    public boolean useLabelColorFromTheme()
    {
        return this.labelColorFromTheme;
    }

    /**
     * Enables/disables the usage of a separate color for the
     * title and unit string.
     * @param LABEL_COLOR_FROM_THEME
     */
    public void setLabelColorFromTheme(final boolean LABEL_COLOR_FROM_THEME)
    {
        this.labelColorFromTheme = LABEL_COLOR_FROM_THEME;
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns the color of the Title and the Unit string.
     * @return the color of the title and unit string
     */
    public java.awt.Color getLabelColor()
    {
        return this.labelColor;
    }

    /**
     * Sets the color of the Title and the Unit string.
     * @param LABEL_COLOR
     */
    public void setLabelColor(final java.awt.Color LABEL_COLOR)
    {
        this.labelColor = LABEL_COLOR;
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Returns true if a custom font will be used for the title and unit string
     * @return true if a custom font will be used for the title and unit string
     */
    public boolean getUseTitleAndUnitFont()
    {
        return this.useTitleAndUnitFont;
    }

    /**
     * Enables and disables the usage of a custom title and unit string font
     * @param USE_TITLE_AND_UNIT_FONT
     */
    public void setUseTitleAndUnitFont(final boolean USE_TITLE_AND_UNIT_FONT)
    {
        this.useTitleAndUnitFont = USE_TITLE_AND_UNIT_FONT;
        init(getWidth(), getWidth());
        repaint();
    }

    /**
     * Sets the given font for the title and unit string.
     * @return the custom defined font for the title and unit string
     */
    public java.awt.Font getTitleAndUnitFont()
    {
        return this.titleAndUnitFont;
    }

    /**
     * Returns the font that will be used for the title and unit string
     * @param TITLE_UNIT_FONT
     */
    public void setTitleAndUnitFont(final java.awt.Font TITLE_UNIT_FONT)
    {
        this.titleAndUnitFont = TITLE_UNIT_FONT;
        init(getWidth(), getWidth());
        repaint();
    }

    // ComponentListener methods
    @Override
    public void componentResized(java.awt.event.ComponentEvent event)
    {        
        if (getWidth() < getMinimumSize().width || getHeight() < getMinimumSize().height)
        {
            setPreferredSize(getMinimumSize());
            setSize(getMinimumSize());
        }
        setSize(getWidth(), getWidth());
        init(getWidth(), getWidth());
        repaint();
    }

    @Override
    public void componentMoved(java.awt.event.ComponentEvent event)
    {
    }

    @Override
    public void componentShown(java.awt.event.ComponentEvent event)
    {
    }

    @Override
    public void componentHidden(java.awt.event.ComponentEvent event)
    {
    }
}
