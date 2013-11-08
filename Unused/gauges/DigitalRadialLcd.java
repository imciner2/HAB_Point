package eu.hansolo.steelseries.gauges;


/**
 *
 * @author hansolo
 */
public class DigitalRadialLcd extends DigitalRadial implements Lcd
{
    // LCD related variables
    private eu.hansolo.steelseries.gauges.DisplaySingle lcd = new eu.hansolo.steelseries.gauges.DisplaySingle();
    private boolean valueCoupled = true;
    private final java.beans.PropertyChangeListener VALUE_LISTENER = new java.beans.PropertyChangeListener()
    {
        @Override
        public void propertyChange(java.beans.PropertyChangeEvent event)
        {
            if (event.getPropertyName().equals("value"))
            {
                lcd.setLcdValue(Double.parseDouble(event.getNewValue().toString()));
            }
        }
    };


    public DigitalRadialLcd()
    {
        super();
        addComponentListener(this);
        setSize(getPreferredSize());
        init(getWidth(), getWidth());
        // LCD related
        lcd.setName("lcd");
        lcd.setLcdUnitStringVisible(false);
        add(lcd);
        lcd.setPreferredSize(new java.awt.Dimension((int) (getWidth() * 0.5), (int) (getWidth() * 0.15)));
        lcd.setSize((int) (getWidth() * 0.5), (int) (getWidth() * 0.15));
        lcd.setLocation((int) ((getWidth() - lcd.getWidth()) / 2.0), (int) (getWidth() * 0.45));
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
            lcd.setLcdUnitStringVisible(false);
        }
        else
        {
            removePropertyChangeListener(VALUE_LISTENER);
            lcd.setLcdUnitStringVisible(true);
        }
        repaint();
    }

    @Override
    public double getLcdValue()
    {
        return lcd.getLcdValue();
    }

    @Override
    public void setLcdValue(double VALUE)
    {
        lcd.setLcdValue(VALUE);
    }

    @Override
    public void setLcdValueAnimated(double VALUE)
    {
        lcd.setLcdValueAnimated(VALUE);
    }

    @Override
    public int getLcdDecimals()
    {
        return lcd.getLcdDecimals();
    }

    @Override
    public void setLcdDecimals(int DECIMALS)
    {
        lcd.setLcdDecimals(DECIMALS);
    }

    @Override
    public String getLcdUnitString()
    {
        return lcd.getLcdUnitString();
    }

    @Override
    public void setLcdUnitString(String UNIT)
    {
        lcd.setLcdUnitString(UNIT);
    }

    @Override
    public boolean isLcdUnitStringVisible()
    {
        return lcd.isLcdUnitStringVisible();
    }

    @Override
    public void setLcdUnitStringVisible(boolean UNIT_STRING_VISIBLE)
    {
        lcd.setLcdUnitStringVisible(UNIT_STRING_VISIBLE);
    }

    @Override
    public boolean isDigitalFont()
    {
        return lcd.isDigitalFont();
    }

    @Override
    public void setDigitalFont(boolean DIGITAL_FONT)
    {
        lcd.setDigitalFont(DIGITAL_FONT);
    }

    @Override
    public boolean getUseCustomLcdUnitFont()
    {
        return lcd.getUseCustomLcdUnitFont();
    }

    @Override
    public void setUseCustomLcdUnitFont(final boolean USE_CUSTOM_LCD_UNIT_FONT)
    {
        lcd.setUseCustomLcdUnitFont(USE_CUSTOM_LCD_UNIT_FONT);
    }

    @Override
    public java.awt.Font getCustomLcdUnitFont()
    {
        return lcd.getCustomLcdUnitFont();
    }

    @Override
    public void setCustomLcdUnitFont(final java.awt.Font CUSTOM_LCD_UNIT_FONT)
    {
        lcd.setCustomLcdUnitFont(CUSTOM_LCD_UNIT_FONT);
    }

    @Override
    public eu.hansolo.steelseries.tools.LcdColor getLcdColor()
    {
        return lcd.getLcdColor();
    }

    @Override
    public void setLcdColor(eu.hansolo.steelseries.tools.LcdColor COLOR)
    {
        lcd.setLcdColor(COLOR);
    }

    @Override
    public String formatLcdValue(double VALUE)
    {
        return lcd.formatLcdValue(VALUE);
    }

    @Override
    public boolean isLcdScientificFormat()
    {
        return lcd.isLcdScientificFormat();
    }

    @Override
    public void setLcdScientificFormat(boolean LCD_SCIENTIFIC_FORMAT)
    {
        lcd.setLcdScientificFormat(LCD_SCIENTIFIC_FORMAT);
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
        lcd.setPreferredSize(new java.awt.Dimension((int) (getWidth() * 0.5), (int) (getWidth() * 0.15)));
        lcd.setSize((int) (getWidth() * 0.5), (int) (getWidth() * 0.15));
        lcd.setLocation((int) ((getWidth() - lcd.getWidth()) / 2.0), (int) (getWidth() * 0.45));
        repaint();
    }

    @Override
    public String toString()
    {
        return "DigitalRadialLcd";
    }
}