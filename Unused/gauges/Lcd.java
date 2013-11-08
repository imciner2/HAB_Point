/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.hansolo.steelseries.gauges;

/**
 *
 * @author hansolo
 */
public interface Lcd
{
    public double getLcdValue();

    public void setLcdValue(final double VALUE);

    public void setLcdValueAnimated(final double VALUE);

    public int getLcdDecimals();

    public void setLcdDecimals(final int DECIMALS);

    public String getLcdUnitString();

    public void setLcdUnitString(final String UNIT);

    public boolean isLcdUnitStringVisible();

    public void setLcdUnitStringVisible(final boolean UNIT_STRING_VISIBLE);

    public boolean getUseCustomLcdUnitFont();

    public void setUseCustomLcdUnitFont(final boolean USE_CUSTOM_LCD_UNIT_FONT);

    public java.awt.Font getCustomLcdUnitFont();

    public void setCustomLcdUnitFont(final java.awt.Font CUSTOM_LCD_UNIT_FONT);

    public boolean isDigitalFont();

    public void setDigitalFont(final boolean DIGITAL_FONT);

    public eu.hansolo.steelseries.tools.LcdColor getLcdColor();

    public void setLcdColor(final eu.hansolo.steelseries.tools.LcdColor COLOR);

    public String formatLcdValue(final double VALUE);
    
    public boolean isLcdScientificFormat();

    public void setLcdScientificFormat(final boolean LCD_SCIENTIFIC_FORMAT);
}
