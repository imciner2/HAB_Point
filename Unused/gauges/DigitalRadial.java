package eu.hansolo.steelseries.gauges;


public class DigitalRadial extends AbstractRadial
{        
    private int noOfActiveLeds = 0;        
    private java.awt.image.BufferedImage backgroundImage;
    private java.awt.image.BufferedImage foregroundImage;
    private java.awt.image.BufferedImage ledGreenOff;
    private java.awt.image.BufferedImage ledYellowOff;
    private java.awt.image.BufferedImage ledRedOff;
    private java.awt.image.BufferedImage ledGreenOn;
    private java.awt.image.BufferedImage ledYellowOn;
    private java.awt.image.BufferedImage ledRedOn;
    private java.awt.Color valueColor = new java.awt.Color(255, 0, 0, 255);
    private java.awt.Point[] ledPosition;            
    private enum LedColor
    {
        GREEN,
        YELLOW,
        RED
    };


    public DigitalRadial()
    {
        super();
        addComponentListener(this);
        setSize(getPreferredSize());
        init(getWidth(), getWidth());
    }

    @Override
    public final AbstractGauge init(final int WIDTH, final int HEIGHT)
    {                
        backgroundImage = create_BACKGROUND_Image(WIDTH, getTitle(), getUnitString());
        foregroundImage = create_FOREGROUND_Image(WIDTH, false);
        
        ledPosition = new java.awt.Point[]
        {
            // LED 1
            new java.awt.Point((int) (WIDTH * 0.186915887850467), (int)(WIDTH *0.649532710280374)),
            // LED 2
            new java.awt.Point((int) (WIDTH * 0.116822429906542), (int)(WIDTH *0.546728971962617)),
            // LED 3
            new java.awt.Point((int) (WIDTH * 0.088785046728972), (int)(WIDTH *0.41588785046729)),
            // LED 4
            new java.awt.Point((int) (WIDTH * 0.116822429906542), (int)(WIDTH *0.285046728971963)),
            // LED 5
            new java.awt.Point((int) (WIDTH * 0.177570093457944), (int)(WIDTH *0.182242990654206)),
            // LED 6
            new java.awt.Point((int) (WIDTH * 0.280373831775701), (int)(WIDTH *0.117222429906542)),
            // LED 7
            new java.awt.Point((int) (WIDTH * 0.411214953271028), (int)(WIDTH *0.0794392523364486)),
            // LED 8
            new java.awt.Point((int) (WIDTH * 0.542056074766355), (int)(WIDTH *0.117222429906542)),
            // LED 9
            new java.awt.Point((int) (WIDTH * 0.649532710280374), (int)(WIDTH *0.182242990654206)),
            // LED 10
            new java.awt.Point((int) (WIDTH * 0.719626168224299), (int)(WIDTH *0.285046728971963)),
            // LED 11
            new java.awt.Point((int) (WIDTH * 0.738317757009346), (int)(WIDTH *0.41588785046729)),
            // LED 12
            new java.awt.Point((int) (WIDTH * 0.710280373831776), (int)(WIDTH *0.546728971962617)),
            // LED 13
            new java.awt.Point((int) (WIDTH * 0.64018691588785), (int)(WIDTH *0.649532710280374))
        };
        
        ledGreenOff = create_LED_OFF_Image(WIDTH, LedColor.GREEN);
        ledYellowOff = create_LED_OFF_Image(WIDTH, LedColor.YELLOW);
        ledRedOff = create_LED_OFF_Image(WIDTH, LedColor.RED);
        ledGreenOn = create_LED_ON_Image(WIDTH, LedColor.GREEN);
        ledYellowOn = create_LED_ON_Image(WIDTH, LedColor.YELLOW);
        ledRedOn = create_LED_ON_Image(WIDTH, LedColor.RED);

        return this;
    }

    @Override
    protected void paintComponent(java.awt.Graphics g)
    {
        super.paintComponent(g);
        final java.awt.Graphics2D G2 = (java.awt.Graphics2D) g;

        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        G2.drawImage(backgroundImage, 0, 0, null);

        for (int i = 0 ; i < 13 ; i++)
        {
            if (i < 7)
            {
                if (i < noOfActiveLeds)
                {
                    G2.drawImage(ledGreenOn, ledPosition[i].x, ledPosition[i].y, null);
                }
                else
                {
                    G2.drawImage(ledGreenOff, ledPosition[i].x, ledPosition[i].y, null);
                }
            }

            if (i >= 7 && i < 12)
            {
                if (i < noOfActiveLeds)
                {
                    G2.drawImage(ledYellowOn, ledPosition[i].x, ledPosition[i].y, null);
                }
                else
                {
                    G2.drawImage(ledYellowOff, ledPosition[i].x, ledPosition[i].y, null);
                }
            }

            if (i == 12)
            {
                if (i < noOfActiveLeds)
                {
                    G2.drawImage(ledRedOn, ledPosition[i].x, ledPosition[i].y, null);
                }
                else
                {
                    G2.drawImage(ledRedOff, ledPosition[i].x, ledPosition[i].y, null);
                }
            }
        }

        G2.drawImage(foregroundImage, 0, 0, null);
    }
    
    @Override
    public void setValue(double value)
    {
        value = value > getMaxValue() ? getMaxValue() : value;
        value = value < getMinValue() ? getMinValue() : value;

        final double OLD_VALUE = getValue();
        super.setValue(value);

        // Set active leds relating to the new value
        calcNoOfActiveLed();
        
        firePropertyChange(VALUE_PROPERTY, OLD_VALUE, value);
        repaint();
    }

    @Override
    public void setMinValue(final double MIN_VALUE)
    {
        super.setMinValue(MIN_VALUE);
        calcNoOfActiveLed();
        repaint();
    }

    @Override
    public void setMaxValue(final double MAX_VALUE)
    {
        super.setMaxValue(MAX_VALUE);
        calcNoOfActiveLed();
        repaint();
    }

    private void calcNoOfActiveLed()
    {
        noOfActiveLeds = (int) (13 / (getMaxValue() - getMinValue()) * getValue());
    }

    public java.awt.Color getValueColor()
    {
        return this.valueColor;
    }

    public void setValueColor(final java.awt.Color VALUE_COLOR)
    {
        this.valueColor = VALUE_COLOR;
        repaint();
    }

    @Override
    protected void calcAngleStep()
    {

    }

    @Override
    public java.awt.geom.Point2D getCenter()
    {
        return new java.awt.geom.Point2D.Double(backgroundImage.getWidth() / 2.0, backgroundImage.getHeight() / 2.0);
    }

    @Override
    public java.awt.geom.Rectangle2D getBounds2D()
    {
        return new java.awt.geom.Rectangle2D.Double(backgroundImage.getMinX(), backgroundImage.getMinY(), backgroundImage.getWidth(), backgroundImage.getHeight());
    }

    private java.awt.image.BufferedImage create_LED_OFF_Image(final int WIDTH, final LedColor LED_COLOR)
    {
        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage((int) (WIDTH * 0.1775700935), (int) (WIDTH * 0.1775700935), java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        // Led background
        final java.awt.geom.Ellipse2D E_LED1_BG = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.21052631735801697, IMAGE_HEIGHT * 0.21052631735801697, IMAGE_WIDTH * 0.5526316165924072, IMAGE_HEIGHT * 0.5526316165924072);
        final java.awt.geom.Point2D E_LED1_BG_START = new java.awt.geom.Point2D.Double(0, E_LED1_BG.getBounds2D().getMinY() );
        final java.awt.geom.Point2D E_LED1_BG_STOP = new java.awt.geom.Point2D.Double(0, E_LED1_BG.getBounds2D().getMaxY() );
        final float[] E_LED1_BG_FRACTIONS =
        {
            0.0f,
            1.0f
        };
        final java.awt.Color[] E_LED1_BG_COLORS =
        {
            new java.awt.Color(0, 0, 0, 229),
            new java.awt.Color(153, 153, 153, 255)
        };        
        final java.awt.LinearGradientPaint E_LED1_BG_GRADIENT = new java.awt.LinearGradientPaint(E_LED1_BG_START, E_LED1_BG_STOP, E_LED1_BG_FRACTIONS, E_LED1_BG_COLORS);
        G2.setPaint(E_LED1_BG_GRADIENT);
        G2.fill(E_LED1_BG);

        // Led foreground
        final java.awt.geom.Ellipse2D LED_FG = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.2368421107530594, IMAGE_HEIGHT * 0.2368421107530594, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5);
        final java.awt.geom.Point2D LED_FG_CENTER = new java.awt.geom.Point2D.Double(LED_FG.getCenterX(), LED_FG.getCenterY());
        final float[] LED_FG_FRACTIONS =
        {
            0.0f,
            0.14f,
            0.15f,
            1.0f
        };
        java.awt.Color[] ledFgColors;

        switch(LED_COLOR)
        {
            case GREEN:              
                ledFgColors = new java.awt.Color[]
                {
                    new java.awt.Color(28, 126, 0, 255),
                    new java.awt.Color(28, 126, 0, 255),
                    new java.awt.Color(28, 126, 0, 255),
                    new java.awt.Color(27, 100, 0, 255)
                };
                break;

            case YELLOW:
                ledFgColors = new java.awt.Color[]
                {
                    new java.awt.Color(164, 128, 8, 255),
                    new java.awt.Color(158, 125, 10, 255),
                    new java.awt.Color(158, 125, 10, 255),
                    new java.awt.Color(130, 96, 25, 255)
                };
                break;

            case RED:
                ledFgColors = new java.awt.Color[]
                {
                    new java.awt.Color(248, 0, 0, 255),
                    new java.awt.Color(248, 0, 0, 255),
                    new java.awt.Color(248, 0, 0, 255),
                    new java.awt.Color(63, 0, 0, 255)
                };
                break;

            default:
                ledFgColors = new java.awt.Color[]
                {
                    new java.awt.Color(28, 126, 0, 255),
                    new java.awt.Color(28, 126, 0, 255),
                    new java.awt.Color(28, 126, 0, 255),
                    new java.awt.Color(27, 100, 0, 255)
                };
                break;
        }

        final java.awt.RadialGradientPaint LED_FG_GRADIENT = new java.awt.RadialGradientPaint(LED_FG_CENTER, 0.25f * IMAGE_WIDTH, LED_FG_FRACTIONS, ledFgColors);
        G2.setPaint(LED_FG_GRADIENT);
        G2.fill(LED_FG);

        // Led inner shadow
        final java.awt.geom.Ellipse2D E_LED1_INNERSHADOW = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.2368421107530594, IMAGE_HEIGHT * 0.2368421107530594, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5);
        final java.awt.geom.Point2D E_LED1_INNERSHADOW_CENTER = new java.awt.geom.Point2D.Double( (0.47368421052631576 * IMAGE_WIDTH), (0.47368421052631576 * IMAGE_HEIGHT) );
        final float[] E_LED1_INNERSHADOW_FRACTIONS =
        {
            0.0f,
            0.86f,
            1.0f
        };
        final java.awt.Color[] E_LED1_INNERSHADOW_COLORS =
        {
            new java.awt.Color(0, 0, 0, 0),
            new java.awt.Color(0, 0, 0, 88),
            new java.awt.Color(0, 0, 0, 102)
        };
        final java.awt.RadialGradientPaint E_LED1_INNERSHADOW_GRADIENT = new java.awt.RadialGradientPaint(E_LED1_INNERSHADOW_CENTER, (float)(0.25 * IMAGE_WIDTH), E_LED1_INNERSHADOW_FRACTIONS, E_LED1_INNERSHADOW_COLORS);
        G2.setPaint(E_LED1_INNERSHADOW_GRADIENT);
        G2.fill(E_LED1_INNERSHADOW);

        // Led highlight
        final java.awt.geom.Ellipse2D E_LED1_HL = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.3947368562221527, IMAGE_HEIGHT * 0.31578946113586426, IMAGE_WIDTH * 0.21052631735801697, IMAGE_HEIGHT * 0.1315789520740509);
        final java.awt.geom.Point2D E_LED1_HL_START = new java.awt.geom.Point2D.Double(0, E_LED1_HL.getBounds2D().getMinY() );
        final java.awt.geom.Point2D E_LED1_HL_STOP = new java.awt.geom.Point2D.Double(0, E_LED1_HL.getBounds2D().getMaxY() );
        final float[] E_LED1_HL_FRACTIONS =
        {
            0.0f,
            1.0f
        };
        final java.awt.Color[] E_LED1_HL_COLORS =
        {
            new java.awt.Color(255, 255, 255, 102),
            new java.awt.Color(255, 255, 255, 0)
        };
        final java.awt.LinearGradientPaint E_LED1_HL_GRADIENT = new java.awt.LinearGradientPaint(E_LED1_HL_START, E_LED1_HL_STOP, E_LED1_HL_FRACTIONS, E_LED1_HL_COLORS);
        G2.setPaint(E_LED1_HL_GRADIENT);
        G2.fill(E_LED1_HL);

        G2.dispose();

        return IMAGE;
    }

    private java.awt.image.BufferedImage create_LED_ON_Image(final int WIDTH, final LedColor LED_COLOR)
    {
        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage((int) (WIDTH * 0.1775700935), (int) (WIDTH * 0.1775700935), java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        // Led background
        final java.awt.geom.Ellipse2D E_LED1_BG = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.21052631735801697, IMAGE_HEIGHT * 0.21052631735801697, IMAGE_WIDTH * 0.5526316165924072, IMAGE_HEIGHT * 0.5526316165924072);
        final java.awt.geom.Point2D E_LED1_BG_START = new java.awt.geom.Point2D.Double(0, E_LED1_BG.getBounds2D().getMinY() );
        final java.awt.geom.Point2D E_LED1_BG_STOP = new java.awt.geom.Point2D.Double(0, E_LED1_BG.getBounds2D().getMaxY() );
        final float[] E_LED1_BG_FRACTIONS =
        {
            0.0f,
            1.0f
        };
        final java.awt.Color[] E_LED1_BG_COLORS =
        {
            new java.awt.Color(0, 0, 0, 229),
            new java.awt.Color(153, 153, 153, 255)
        };
        final java.awt.LinearGradientPaint E_LED1_BG_GRADIENT = new java.awt.LinearGradientPaint(E_LED1_BG_START, E_LED1_BG_STOP, E_LED1_BG_FRACTIONS, E_LED1_BG_COLORS);
        G2.setPaint(E_LED1_BG_GRADIENT);
        G2.fill(E_LED1_BG);

        // Led glow
        final java.awt.geom.Ellipse2D LED_GLOW = new java.awt.geom.Ellipse2D.Double(0.0, 0.0, IMAGE_WIDTH, IMAGE_HEIGHT);
        final java.awt.geom.Point2D LED_GLOW_CENTER = new java.awt.geom.Point2D.Double(LED_GLOW.getCenterX(), LED_GLOW.getCenterY());
        final float[] LED_GLOW_FRACTIONS =
        {
            0.0f,
            0.57f,
            0.71f,
            0.72f,
            0.85f,
            0.93f,
            0.9301f,
            0.99f
        };
        java.awt.Color[] ledGlowColors;

        switch(LED_COLOR)
        {
            case GREEN:
                ledGlowColors = new java.awt.Color[]
                {
                    new java.awt.Color(165, 255, 0, 255),
                    new java.awt.Color(165, 255, 0, 101),
                    new java.awt.Color(165, 255, 0, 63),
                    new java.awt.Color(165, 255, 0, 62),
                    new java.awt.Color(165, 255, 0, 31),
                    new java.awt.Color(165, 255, 0, 13),
                    new java.awt.Color(165, 255, 0, 12),
                    new java.awt.Color(165, 255, 0, 0)
                };
                break;

            case YELLOW:
                ledGlowColors = new java.awt.Color[]
                {
                    new java.awt.Color(255, 102, 0, 255),
                    new java.awt.Color(255, 102, 0, 101),
                    new java.awt.Color(255, 102, 0, 63),
                    new java.awt.Color(255, 102, 0, 62),
                    new java.awt.Color(255, 102, 0, 31),
                    new java.awt.Color(255, 102, 0, 13),
                    new java.awt.Color(255, 102, 0, 12),
                    new java.awt.Color(255, 102, 0, 0)
                };
                break;

            case RED:
                ledGlowColors = new java.awt.Color[]
                {
                    new java.awt.Color(255, 0, 0, 255),
                    new java.awt.Color(255, 0, 0, 101),
                    new java.awt.Color(255, 0, 0, 63),
                    new java.awt.Color(255, 0, 0, 62),
                    new java.awt.Color(255, 0, 0, 31),
                    new java.awt.Color(255, 0, 0, 13),
                    new java.awt.Color(255, 0, 0, 12),
                    new java.awt.Color(255, 0, 0, 0)
                };
                break;

            default:
                ledGlowColors = new java.awt.Color[]
                {
                    new java.awt.Color(165, 255, 0, 255),
                    new java.awt.Color(165, 255, 0, 101),
                    new java.awt.Color(165, 255, 0, 63),
                    new java.awt.Color(165, 255, 0, 62),
                    new java.awt.Color(165, 255, 0, 31),
                    new java.awt.Color(165, 255, 0, 13),
                    new java.awt.Color(165, 255, 0, 12),
                    new java.awt.Color(165, 255, 0, 0)
                };
                break;
        }
        final java.awt.RadialGradientPaint LED_GLOW_GRADIENT = new java.awt.RadialGradientPaint(LED_GLOW_CENTER, 0.5f * IMAGE_WIDTH, LED_GLOW_FRACTIONS, ledGlowColors);
        G2.setPaint(LED_GLOW_GRADIENT);
        G2.fill(LED_GLOW);

        // Led foreground
        final java.awt.geom.Ellipse2D LED_FG = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.2368421107530594, IMAGE_HEIGHT * 0.2368421107530594, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5);
        final java.awt.geom.Point2D LED_FG_CENTER = new java.awt.geom.Point2D.Double(LED_FG.getCenterX(), LED_FG.getCenterY());
        final float[] LED_FG_FRACTIONS =
        {
            0.0f,
            0.14f,
            0.15f,
            1.0f
        };
        java.awt.Color[] ledFgColors;

        switch(LED_COLOR)
        {
            case GREEN:
                ledFgColors = new java.awt.Color[]
                {
                    new java.awt.Color(154, 255, 137, 255),
                    new java.awt.Color(154, 255, 137, 255),
                    new java.awt.Color(154, 255, 137, 255),
                    new java.awt.Color(89, 255, 42, 255)
                };
                break;

            case YELLOW:
                ledFgColors = new java.awt.Color[]
                {
                    new java.awt.Color(251, 255, 140, 255),
                    new java.awt.Color(251, 255, 140, 255),
                    new java.awt.Color(251, 255, 140, 255),
                    new java.awt.Color(250, 249, 60, 255)
                };
                break;

            case RED:
                ledFgColors = new java.awt.Color[]
                {
                    new java.awt.Color(252, 53, 55, 255),
                    new java.awt.Color(252, 53, 55, 255),
                    new java.awt.Color(252, 53, 55, 255),
                    new java.awt.Color(255, 0, 0, 255)
                };
                break;

            default:
                ledFgColors = new java.awt.Color[]
                {
                    new java.awt.Color(154, 255, 137, 255),
                    new java.awt.Color(154, 255, 137, 255),
                    new java.awt.Color(154, 255, 137, 255),
                    new java.awt.Color(89, 255, 42, 255)
                };
                break;
        }
        
        final java.awt.RadialGradientPaint LED_FG_GRADIENT = new java.awt.RadialGradientPaint(LED_FG_CENTER, 0.25f * IMAGE_WIDTH, LED_FG_FRACTIONS, ledFgColors);
        G2.setPaint(LED_FG_GRADIENT);
        G2.fill(LED_FG);

        // Led inner shadow
        final java.awt.geom.Ellipse2D E_LED1_INNERSHADOW = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.2368421107530594, IMAGE_HEIGHT * 0.2368421107530594, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5);
        final java.awt.geom.Point2D E_LED1_INNERSHADOW_CENTER = new java.awt.geom.Point2D.Double( (0.47368421052631576 * IMAGE_WIDTH), (0.47368421052631576 * IMAGE_HEIGHT) );
        final float[] E_LED1_INNERSHADOW_FRACTIONS =
        {
            0.0f,
            0.86f,
            1.0f
        };
        final java.awt.Color[] E_LED1_INNERSHADOW_COLORS =
        {
            new java.awt.Color(0, 0, 0, 0),
            new java.awt.Color(0, 0, 0, 88),
            new java.awt.Color(0, 0, 0, 102)
        };
        final java.awt.RadialGradientPaint E_LED1_INNERSHADOW_GRADIENT = new java.awt.RadialGradientPaint(E_LED1_INNERSHADOW_CENTER, (float)(0.25 * IMAGE_WIDTH), E_LED1_INNERSHADOW_FRACTIONS, E_LED1_INNERSHADOW_COLORS);
        G2.setPaint(E_LED1_INNERSHADOW_GRADIENT);
        G2.fill(E_LED1_INNERSHADOW);

        // Led highlight
        final java.awt.geom.Ellipse2D E_LED1_HL = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.3947368562221527, IMAGE_HEIGHT * 0.31578946113586426, IMAGE_WIDTH * 0.21052631735801697, IMAGE_HEIGHT * 0.1315789520740509);
        final java.awt.geom.Point2D E_LED1_HL_START = new java.awt.geom.Point2D.Double(0, E_LED1_HL.getBounds2D().getMinY() );
        final java.awt.geom.Point2D E_LED1_HL_STOP = new java.awt.geom.Point2D.Double(0, E_LED1_HL.getBounds2D().getMaxY() );
        final float[] E_LED1_HL_FRACTIONS =
        {
            0.0f,
            1.0f
        };
        final java.awt.Color[] E_LED1_HL_COLORS =
        {
            new java.awt.Color(255, 255, 255, 102),
            new java.awt.Color(255, 255, 255, 0)
        };
        final java.awt.LinearGradientPaint E_LED1_HL_GRADIENT = new java.awt.LinearGradientPaint(E_LED1_HL_START, E_LED1_HL_STOP, E_LED1_HL_FRACTIONS, E_LED1_HL_COLORS);
        G2.setPaint(E_LED1_HL_GRADIENT);
        G2.fill(E_LED1_HL);

        G2.dispose();

        return IMAGE;
    }

    @Override
    public String toString()
    {
        return "DigitalRadial";
    }
}