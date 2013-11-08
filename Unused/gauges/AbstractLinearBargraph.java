/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.hansolo.steelseries.gauges;



/**
 *
 * @author hansolo
 */
public abstract class AbstractLinearBargraph extends AbstractLinear
{

    private int tickLabelPeriod = 10; // Draw value at every nth tickmark
    private eu.hansolo.steelseries.tools.ColorDef barGraphColor = eu.hansolo.steelseries.tools.ColorDef.RED;    
    private boolean peakValueEnabled = false;

    public AbstractLinearBargraph()
    {
        super();
        addComponentListener(this);        
    }

    @Override
    public void setMinValue(final double MIN_VALUE)
    {
        super.setMinValue(MIN_VALUE);
        if (MIN_VALUE < 0)
        {
            setValue(MIN_VALUE);
        }
    }

    public int getTickLabelPeriod()
    {
        return this.tickLabelPeriod;
    }

    public void setTickLabelPeriod(final int TICK_LABEL_PERIOD)
    {
        this.tickLabelPeriod = TICK_LABEL_PERIOD;
        init(getWidth(), getHeight());
        repaint();
    }

    public eu.hansolo.steelseries.tools.ColorDef getBarGraphColor()
    {
        return this.barGraphColor;
    }

    public void setBarGraphColor(final eu.hansolo.steelseries.tools.ColorDef BARGRAPH_COLOR)
    {
        this.barGraphColor = BARGRAPH_COLOR;
        init(getWidth(), getHeight());
        repaint();
    }

    public boolean isPeakValueEnabled()
    {
        return this.peakValueEnabled;
    }

    public void setPeakValueEnabled(final boolean PEAK_VALUE_ENABLED)
    {
        this.peakValueEnabled = PEAK_VALUE_ENABLED;
    }
}
