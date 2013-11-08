package eu.hansolo.steelseries.gauges;


/**
 *
 * @author hansolo
 */
public class Radial1Lcd extends Radial1 implements Lcd
{
    // LCD related variables
    private final eu.hansolo.steelseries.gauges.DisplaySingle LCD = new eu.hansolo.steelseries.gauges.DisplaySingle();
    private boolean valueCoupled = true;
    private final java.beans.PropertyChangeListener VALUE_LISTENER = new java.beans.PropertyChangeListener()
    {
        @Override
        public void propertyChange(java.beans.PropertyChangeEvent event)
        {
            if (event.getPropertyName().equals("value"))
            {
                LCD.setLcdValue(Double.parseDouble(event.getNewValue().toString()));
            }
        }
    };

    public Radial1Lcd()
    {
        super();
        // LCD related
        LCD.setName("lcd");
        LCD.setLcdUnitStringVisible(false);
        add(LCD);
        LCD.setPreferredSize(new java.awt.Dimension((int) (getWidth() * 0.55), (int) (getWidth() * 0.15)));
        LCD.setSize((int) (getWidth() * 0.55), (int) (getWidth() * 0.15));
        LCD.setLocation((int) ((getWidth() - LCD.getWidth()) / 2.0), (int) (getWidth() * 0.6));
        addPropertyChangeListener(VALUE_LISTENER);
        repaint();
    }

    // LCD related methods
    public boolean isValueCoupled()
    {
        return this.valueCoupled;
    }

    public void setValueCoupled(final boolean VALUE_COUPLED)
    {
        this.valueCoupled = VALUE_COUPLED;
        if (VALUE_COUPLED)
        {
            addComponentListener(this);
            addPropertyChangeListener(VALUE_LISTENER);
            LCD.setLcdUnitStringVisible(false);
        }
        else
        {
            removePropertyChangeListener(VALUE_LISTENER);
            LCD.setLcdUnitStringVisible(true);
        }
        repaint();
    }

    @Override
    public double getLcdValue()
    {
        return LCD.getLcdValue();
    }

    @Override
    public void setLcdValue(double VALUE)
    {
        LCD.setLcdValue(VALUE);
    }

    @Override
    public void setLcdValueAnimated(double VALUE)
    {
        LCD.setLcdValueAnimated(VALUE);
    }

    @Override
    public int getLcdDecimals()
    {
        return LCD.getLcdDecimals();
    }

    @Override
    public void setLcdDecimals(int DECIMALS)
    {
        LCD.setLcdDecimals(DECIMALS);
    }

    @Override
    public String getLcdUnitString()
    {
        return LCD.getLcdUnitString();
    }

    @Override
    public void setLcdUnitString(String UNIT)
    {
        LCD.setLcdUnitString(UNIT);
    }

    @Override
    public boolean isLcdUnitStringVisible()
    {
        return LCD.isLcdUnitStringVisible();
    }

    @Override
    public void setLcdUnitStringVisible(boolean UNIT_STRING_VISIBLE)
    {
        LCD.setLcdUnitStringVisible(UNIT_STRING_VISIBLE);
    }

    @Override
    public boolean isDigitalFont()
    {
        return LCD.isDigitalFont();
    }

    @Override
    public void setDigitalFont(boolean DIGITAL_FONT)
    {
        LCD.setDigitalFont(DIGITAL_FONT);
    }

    @Override
    public boolean getUseCustomLcdUnitFont()
    {
        return LCD.getUseCustomLcdUnitFont();
    }

    @Override
    public void setUseCustomLcdUnitFont(final boolean USE_CUSTOM_LCD_UNIT_FONT)
    {
        LCD.setUseCustomLcdUnitFont(USE_CUSTOM_LCD_UNIT_FONT);
    }

    @Override
    public java.awt.Font getCustomLcdUnitFont()
    {
        return LCD.getCustomLcdUnitFont();
    }

    @Override
    public void setCustomLcdUnitFont(final java.awt.Font CUSTOM_LCD_UNIT_FONT)
    {
        LCD.setCustomLcdUnitFont(CUSTOM_LCD_UNIT_FONT);
    }

    @Override
    public eu.hansolo.steelseries.tools.LcdColor getLcdColor()
    {
        return LCD.getLcdColor();
    }

    @Override
    public void setLcdColor(eu.hansolo.steelseries.tools.LcdColor COLOR)
    {
        LCD.setLcdColor(COLOR);
    }

    @Override
    public String formatLcdValue(double VALUE)
    {
        return LCD.formatLcdValue(VALUE);
    }

    @Override
    public boolean isLcdScientificFormat()
    {
        return LCD.isLcdScientificFormat();
    }

    @Override
    public void setLcdScientificFormat(boolean LCD_SCIENTIFIC_FORMAT)
    {
        LCD.setLcdScientificFormat(LCD_SCIENTIFIC_FORMAT);
    }

    // ComponentListener methods
    @Override
    public void componentResized(java.awt.event.ComponentEvent event)
    {
        final int SIZE = getWidth() < getHeight() ? getWidth() : getHeight();
        setSize(SIZE, SIZE);

        if (SIZE < getMinimumSize().width || SIZE < getMinimumSize().height)
        {
            setSize(getMinimumSize());
        }

        setPreferredSize(new java.awt.Dimension(getWidth(), getWidth())); // Set the area that will be repainted directly
        setSize(getWidth(), getWidth()); // Set the area where the repaint will happen
        init(getWidth(), getWidth());
        LCD.setPreferredSize(new java.awt.Dimension((int) (getWidth() * 0.55), (int) (getWidth() * 0.15)));
        LCD.setSize((int) (getWidth() * 0.55), (int) (getWidth() * 0.15));
        LCD.setLocation((int) ((getWidth() - LCD.getWidth()) / 2.0), (int) (getWidth() * 0.6));
        repaint();
    }

    @Override
    public String toString()
    {
        return "Radial1Lcd";
    }
}
