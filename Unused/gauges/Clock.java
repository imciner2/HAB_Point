package eu.hansolo.steelseries.gauges;


/**
 *
 * @author hansolo
 */
public final class Clock extends AbstractGauge implements java.awt.event.ActionListener
{   
    protected static final eu.hansolo.steelseries.tools.Util UTIL = eu.hansolo.steelseries.tools.Util.INSTANCE;
    private static final double ANGLE_STEP = 6;
    private final javax.swing.Timer CLOCK_TIMER = new javax.swing.Timer(1000, this);
    private double minutePointerAngle = java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE) * ANGLE_STEP;
    private double hourPointerAngle = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR) * ANGLE_STEP * 5 + 0.5 * java.util.Calendar.getInstance().get( java.util.Calendar.MINUTE);
    private double secondPointerAngle = java.util.Calendar.getInstance().get(java.util.Calendar.SECOND) * ANGLE_STEP;    
    // Background
    private boolean backgroundColorFromTheme = true;
    private java.awt.Paint customBackground = java.awt.Color.BLACK;
    private eu.hansolo.steelseries.tools.BackgroundColor backgroundColor = eu.hansolo.steelseries.tools.BackgroundColor.DARK_GRAY;
    private eu.hansolo.steelseries.tools.FrameDesign frameDesign = eu.hansolo.steelseries.tools.FrameDesign.METAL;
    private java.awt.image.BufferedImage backgroundImage;
    private java.awt.image.BufferedImage hourImage;
    private java.awt.image.BufferedImage hourShadowImage;
    private java.awt.image.BufferedImage minuteImage;
    private java.awt.image.BufferedImage minuteShadowImage;
    private java.awt.image.BufferedImage knobImage;
    private java.awt.image.BufferedImage secondImage;
    private java.awt.image.BufferedImage secondShadowImage;
    private java.awt.image.BufferedImage foregroundImage;
    private int hour;
    private int minute;
    private int timeZoneOffsetHour = 0;
    private int timeZoneOffsetMinute = 0;
    private final java.awt.Color SHADOW_COLOR = new java.awt.Color(0.0f, 0.0f, 0.0f, 0.65f);

    
    public Clock()
    {
        super();
        addComponentListener(this);
        setPreferredSize(new java.awt.Dimension(200, 200));
        setSize(getPreferredSize());
        init(getWidth(), getWidth());
        CLOCK_TIMER.start(); 
    }

    @Override
    public AbstractGauge init(final int WIDTH, final int HEIGHT)
    {
        backgroundImage = create_BACKGROUND_Image(WIDTH);
        hourImage = create_HOUR_Image(WIDTH);
        hourShadowImage = create_HOUR_SHADOW_Image(WIDTH);
        minuteImage = create_MINUTE_Image(WIDTH);
        minuteShadowImage = create_MINUTE_SHADOW_Image(WIDTH);
        knobImage = create_KNOB_Image(WIDTH);
        secondImage = create_SECOND_Image(WIDTH);
        secondShadowImage = create_SECOND_SHADOW_Image(WIDTH);
        foregroundImage = create_FOREGROUND_Image(WIDTH);

        return this;
    }

    @Override
    protected void paintComponent(java.awt.Graphics g)
    {
        final java.awt.Graphics2D G2 = (java.awt.Graphics2D) g;

        final java.awt.geom.Point2D CENTER = new java.awt.geom.Point2D.Double(backgroundImage.getWidth() / 2.0, backgroundImage.getWidth() / 2.0);

        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        final java.awt.geom.AffineTransform OLD_TRANSFORM = G2.getTransform();

        // Draw the background
        G2.drawImage(backgroundImage, 0, 0, null);

        // Draw the hour pointer
        G2.rotate(Math.toRadians(hourPointerAngle + (2 * Math.sin(Math.toRadians(hourPointerAngle)))), CENTER.getX(), CENTER.getY());
        G2.drawImage(hourShadowImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);
        G2.rotate(Math.toRadians(hourPointerAngle), CENTER.getX(), CENTER.getY());
        G2.drawImage(hourImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);

        // Draw the minute pointer
        G2.rotate(Math.toRadians(minutePointerAngle + (2 * Math.sin(Math.toRadians(minutePointerAngle)))), CENTER.getX(), CENTER.getY());
        G2.drawImage(minuteShadowImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);
        G2.rotate(Math.toRadians(minutePointerAngle), CENTER.getX(), CENTER.getY());
        G2.drawImage(minuteImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);

        // Draw knob image
        G2.drawImage(knobImage, 0, 0, null);

        // Draw the second pointer
        G2.rotate(Math.toRadians(secondPointerAngle + (2 * Math.sin(Math.toRadians(secondPointerAngle)))), CENTER.getX(), CENTER.getY());
        G2.drawImage(secondShadowImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);
        G2.rotate(Math.toRadians(secondPointerAngle), CENTER.getX(), CENTER.getY());
        G2.drawImage(secondImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);

        // Draw the foreground
        G2.drawImage(foregroundImage, 0, 0, null);
    }

    public int getTimeZoneOffsetHour()
    {
        return this.timeZoneOffsetHour;
    }

    public void setTimeZoneOffsetHour(final int TIMEZONE_OFFSET_HOUR)
    {
        this.timeZoneOffsetHour = TIMEZONE_OFFSET_HOUR;
    }

    public int getTimeZoneOffsetMinute()
    {
        return this.timeZoneOffsetMinute;
    }

    public void setTimeZoneOffsetMinute(final int TIMEZONE_OFFSET_MINUTE)
    {
        this.timeZoneOffsetMinute = TIMEZONE_OFFSET_MINUTE;
    }

    public eu.hansolo.steelseries.tools.BackgroundColor getBackgroundColor()
    {
        return this.backgroundColor;
    }

    public void setBackgroundColor(final eu.hansolo.steelseries.tools.BackgroundColor BACKGROUND_COLOR)
    {
        this.backgroundColor = BACKGROUND_COLOR;
        init(getWidth(), getWidth());
        repaint();
    }

    public eu.hansolo.steelseries.tools.FrameDesign getFrameDesign()
    {
        return this.frameDesign;
    }

    public void setFrameDesign(final eu.hansolo.steelseries.tools.FrameDesign FRAME_DESIGN)
    {
        this.frameDesign = FRAME_DESIGN;
        init(getWidth(), getWidth());
        repaint();
    }

     /**
     * Returns true if the background paint will be taken from
     * the variable customBackground.
     * @return true if background color from "theme" is used e.g. DARK_GRAY
     */
    public boolean useBackgroundColorFromTheme()
    {
        return this.backgroundColorFromTheme;
    }

    /**
     * Enables/disables the usage of a custom paint as
     * replacement for the predefined background colors.
     * @param BACKGROUND_COLOR_FROM_THEME
     */
    public void setBackgroundColorFromTheme(final boolean BACKGROUND_COLOR_FROM_THEME)
    {
        this.backgroundColorFromTheme = BACKGROUND_COLOR_FROM_THEME;
        init(getWidth(), getHeight());
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
        init(getWidth(), getWidth());
        repaint();
    }

    public java.awt.geom.Point2D getCenter()
    {
        return new java.awt.geom.Point2D.Double(backgroundImage.getWidth() / 2.0, backgroundImage.getHeight() / 2.0);
    }

    public java.awt.geom.Rectangle2D getBounds2D()
    {
        return new java.awt.geom.Rectangle2D.Double(backgroundImage.getMinX(), backgroundImage.getMinY(), backgroundImage.getWidth(), backgroundImage.getHeight());
    }

    private java.awt.image.BufferedImage create_BACKGROUND_Image(final int WIDTH)
    {
        if (WIDTH <= 0)
        {
            return null;
        }

        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, (int) (1.0 * WIDTH), java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final java.awt.geom.Ellipse2D E_FRAME_OUTERFRAME = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.0, IMAGE_HEIGHT * 0.0, IMAGE_WIDTH * 1.0, IMAGE_HEIGHT * 1.0);
        final java.awt.Color FILL_COLOR_E_FRAME_OUTERFRAME = new java.awt.Color(0x848484);
        G2.setColor(FILL_COLOR_E_FRAME_OUTERFRAME);
        G2.fill(E_FRAME_OUTERFRAME);

        final java.awt.geom.Ellipse2D E_FRAME_MAIN = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.004672897048294544, IMAGE_HEIGHT * 0.004672897048294544, IMAGE_WIDTH * 0.9906542301177979, IMAGE_HEIGHT * 0.9906542301177979);
        final java.awt.geom.Point2D E_FRAME_MAIN_START = new java.awt.geom.Point2D.Double(0, E_FRAME_MAIN.getBounds2D().getMinY() );
        final java.awt.geom.Point2D E_FRAME_MAIN_STOP = new java.awt.geom.Point2D.Double(0, E_FRAME_MAIN.getBounds2D().getMaxY() );
        final java.awt.geom.Point2D E_FRAME_MAIN_CENTER = new java.awt.geom.Point2D.Double(E_FRAME_MAIN.getCenterX(), E_FRAME_MAIN.getCenterY());

        final float[] E_FRAME_MAIN_FRACTIONS;
        final java.awt.Color[] E_FRAME_MAIN_COLORS;
        final java.awt.Paint E_FRAME_MAIN_GRADIENT;

        switch(this.frameDesign)
        {
            case BLACK_METAL:
                E_FRAME_MAIN_FRACTIONS = new float[]
                {
                    0.0f,
                    45.0f,
                    125.0f,
                    180.0f,
                    245.0f,
                    315.0f,
                    360.0f
                };

                E_FRAME_MAIN_COLORS = new java.awt.Color[]
                {
                    new java.awt.Color(254, 254, 254, 255),
                    new java.awt.Color(0, 0, 0, 255),
                    new java.awt.Color(153, 153, 153, 255),
                    new java.awt.Color(0, 0, 0, 255),
                    new java.awt.Color(153, 153, 153, 255),
                    new java.awt.Color(0, 0, 0, 255),
                    new java.awt.Color(254, 254, 254, 255)
                };

                E_FRAME_MAIN_GRADIENT = new eu.hansolo.steelseries.tools.ConicalGradientPaint(true, E_FRAME_MAIN_CENTER, 0, E_FRAME_MAIN_FRACTIONS, E_FRAME_MAIN_COLORS);
                break;

            case METAL:
                E_FRAME_MAIN_FRACTIONS = new float[]
                {
                    0.0f,
                    0.07f,
                    0.12f,
                    1.0f
                };

                E_FRAME_MAIN_COLORS = new java.awt.Color[]
                {
                    new java.awt.Color(254, 254, 254, 255),
                    new java.awt.Color(210, 210, 210, 255),
                    new java.awt.Color(179, 179, 179, 255),
                    new java.awt.Color(213, 213, 213, 255)
                };

                E_FRAME_MAIN_GRADIENT = new java.awt.LinearGradientPaint(E_FRAME_MAIN_START, E_FRAME_MAIN_STOP, E_FRAME_MAIN_FRACTIONS, E_FRAME_MAIN_COLORS);
                break;

            case SHINY_METAL:
                E_FRAME_MAIN_FRACTIONS = new float[]
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

                E_FRAME_MAIN_COLORS = new java.awt.Color[]
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

                E_FRAME_MAIN_GRADIENT = new eu.hansolo.steelseries.tools.ConicalGradientPaint(true, E_FRAME_MAIN_CENTER, 0, E_FRAME_MAIN_FRACTIONS, E_FRAME_MAIN_COLORS);
                break;

            default:
                E_FRAME_MAIN_FRACTIONS = new float[]
                {
                    0.0f,
                    0.07f,
                    0.12f,
                    1.0f
                };

                E_FRAME_MAIN_COLORS = new java.awt.Color[]
                {
                    new java.awt.Color(254, 254, 254, 255),
                    new java.awt.Color(210, 210, 210, 255),
                    new java.awt.Color(179, 179, 179, 255),
                    new java.awt.Color(213, 213, 213, 255)
                };

                E_FRAME_MAIN_GRADIENT = new java.awt.LinearGradientPaint(E_FRAME_MAIN_START, E_FRAME_MAIN_STOP, E_FRAME_MAIN_FRACTIONS, E_FRAME_MAIN_COLORS);
                break;
        }

        G2.setPaint(E_FRAME_MAIN_GRADIENT);
        G2.fill(E_FRAME_MAIN);

        final java.awt.geom.Ellipse2D E_FRAME_INNERFRAME = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.07943925261497498, IMAGE_HEIGHT * 0.07943925261497498, IMAGE_WIDTH * 0.8411215543746948, IMAGE_HEIGHT * 0.8411215543746948);        
        G2.setColor(java.awt.Color.WHITE);
        G2.fill(E_FRAME_INNERFRAME);

        final java.awt.geom.Ellipse2D GAUGE_BACKGROUND = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.08411215245723724, IMAGE_HEIGHT * 0.08411215245723724, IMAGE_WIDTH * 0.8317756652832031, IMAGE_HEIGHT * 0.8317756652832031);
        final java.awt.geom.Point2D GAUGE_BACKGROUND_START = new java.awt.geom.Point2D.Double(0, GAUGE_BACKGROUND.getBounds2D().getMinY() );
        final java.awt.geom.Point2D GAUGE_BACKGROUND_STOP = new java.awt.geom.Point2D.Double(0, GAUGE_BACKGROUND.getBounds2D().getMaxY() );
        final float[] GAUGE_BACKGROUND_FRACTIONS =
        {
            0.0f,            
            0.4f,
            1.0f
        };
        final java.awt.Color[] GAUGE_BACKGROUND_COLORS =
        {
            backgroundColor.GRADIENT_START_COLOR,
            backgroundColor.GRADIENT_FRACTION_COLOR,
            backgroundColor.GRADIENT_STOP_COLOR
        };
        final java.awt.Paint GAUGE_BACKGROUND_GRADIENT;
        if (getBackgroundColor() == eu.hansolo.steelseries.tools.BackgroundColor.BRUSHED_METAL)
        {
            GAUGE_BACKGROUND_GRADIENT = new java.awt.TexturePaint(UTIL.createBrushMetalTexture(null, GAUGE_BACKGROUND.getBounds().width, GAUGE_BACKGROUND.getBounds().height), GAUGE_BACKGROUND.getBounds());
        }
        else
        {
            GAUGE_BACKGROUND_GRADIENT = new java.awt.LinearGradientPaint(GAUGE_BACKGROUND_START, GAUGE_BACKGROUND_STOP, GAUGE_BACKGROUND_FRACTIONS, GAUGE_BACKGROUND_COLORS);
        }

        // Set custom background paint if selected
        if (useBackgroundColorFromTheme())
        {
            G2.setPaint(GAUGE_BACKGROUND_GRADIENT);
        }
        else
        {
            G2.setPaint(getCustomBackground());
        }
        G2.fill(GAUGE_BACKGROUND);


        final java.awt.geom.Ellipse2D E_GAUGE_INNERSHADOW = new java.awt.geom.Ellipse2D.Double(IMAGE_WIDTH * 0.08411215245723724, IMAGE_HEIGHT * 0.08411215245723724, IMAGE_WIDTH * 0.8317756652832031, IMAGE_HEIGHT * 0.8317756652832031);
        final java.awt.geom.Point2D E_GAUGE_INNERSHADOW_CENTER = new java.awt.geom.Point2D.Double( (0.5 * IMAGE_WIDTH), (0.5 * IMAGE_HEIGHT) );
        final float[] E_GAUGE_INNERSHADOW_FRACTIONS =
        {
            0.0f,
            0.7f,
            0.71f,
            1.0f
        };
        final java.awt.Color[] E_GAUGE_INNERSHADOW_COLORS =
        {
            new java.awt.Color(0, 0, 0, 0),
            new java.awt.Color(0, 0, 0, 0),
            new java.awt.Color(0, 0, 0, 0),
            new java.awt.Color(0, 0, 0, 76)
        };
        final java.awt.RadialGradientPaint E_GAUGE_INNERSHADOW_GRADIENT = new java.awt.RadialGradientPaint(E_GAUGE_INNERSHADOW_CENTER, (float)(0.4158878504672897 * IMAGE_WIDTH), E_GAUGE_INNERSHADOW_FRACTIONS, E_GAUGE_INNERSHADOW_COLORS);
        G2.setPaint(E_GAUGE_INNERSHADOW_GRADIENT);
        G2.fill(E_GAUGE_INNERSHADOW);

        final java.awt.Color TICKMARK_COLOR = backgroundColor.LABEL_COLOR;

        final java.awt.geom.GeneralPath ELEVENSHADOW = new java.awt.geom.GeneralPath();
        ELEVENSHADOW.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        ELEVENSHADOW.moveTo(IMAGE_WIDTH * 0.3130841121495327, IMAGE_HEIGHT * 0.17757009345794392);
        ELEVENSHADOW.lineTo(IMAGE_WIDTH * 0.35046728971962615, IMAGE_HEIGHT * 0.2383177570093458);
        ELEVENSHADOW.lineTo(IMAGE_WIDTH * 0.34579439252336447, IMAGE_HEIGHT * 0.2383177570093458);
        ELEVENSHADOW.lineTo(IMAGE_WIDTH * 0.308411214953271, IMAGE_HEIGHT * 0.17757009345794392);
        ELEVENSHADOW.lineTo(IMAGE_WIDTH * 0.3130841121495327, IMAGE_HEIGHT * 0.17757009345794392);
        ELEVENSHADOW.closePath();
        final java.awt.Color FILL_COLOR_ELEVENSHADOW = new java.awt.Color(0x353534);
        G2.setColor(FILL_COLOR_ELEVENSHADOW);
        G2.fill(ELEVENSHADOW);

        final java.awt.geom.GeneralPath ELEVEN = new java.awt.geom.GeneralPath();
        ELEVEN.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        ELEVEN.moveTo(IMAGE_WIDTH * 0.32242990654205606, IMAGE_HEIGHT * 0.17289719626168223);
        ELEVEN.lineTo(IMAGE_WIDTH * 0.35514018691588783, IMAGE_HEIGHT * 0.2336448598130841);
        ELEVEN.lineTo(IMAGE_WIDTH * 0.35046728971962615, IMAGE_HEIGHT * 0.2383177570093458);
        ELEVEN.lineTo(IMAGE_WIDTH * 0.3130841121495327, IMAGE_HEIGHT * 0.17757009345794392);
        ELEVEN.lineTo(IMAGE_WIDTH * 0.32242990654205606, IMAGE_HEIGHT * 0.17289719626168223);
        ELEVEN.closePath();        
        G2.setColor(TICKMARK_COLOR);
        G2.fill(ELEVEN);

        final java.awt.geom.GeneralPath TENSHADOW = new java.awt.geom.GeneralPath();
        TENSHADOW.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        TENSHADOW.moveTo(IMAGE_WIDTH * 0.17757009345794392, IMAGE_HEIGHT * 0.3177570093457944);
        TENSHADOW.lineTo(IMAGE_WIDTH * 0.2383177570093458, IMAGE_HEIGHT * 0.35514018691588783);
        TENSHADOW.lineTo(IMAGE_WIDTH * 0.2383177570093458, IMAGE_HEIGHT * 0.3598130841121495);
        TENSHADOW.lineTo(IMAGE_WIDTH * 0.17757009345794392, IMAGE_HEIGHT * 0.32242990654205606);
        TENSHADOW.lineTo(IMAGE_WIDTH * 0.17757009345794392, IMAGE_HEIGHT * 0.3177570093457944);
        TENSHADOW.closePath();
        final java.awt.Color FILL_COLOR_TENSHADOW = new java.awt.Color(0x353534);
        G2.setColor(FILL_COLOR_TENSHADOW);
        G2.fill(TENSHADOW);

        final java.awt.geom.GeneralPath TEN = new java.awt.geom.GeneralPath();
        TEN.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        TEN.moveTo(IMAGE_WIDTH * 0.1822429906542056, IMAGE_HEIGHT * 0.3130841121495327);
        TEN.lineTo(IMAGE_WIDTH * 0.24299065420560748, IMAGE_HEIGHT * 0.34579439252336447);
        TEN.lineTo(IMAGE_WIDTH * 0.2383177570093458, IMAGE_HEIGHT * 0.35514018691588783);
        TEN.lineTo(IMAGE_WIDTH * 0.17757009345794392, IMAGE_HEIGHT * 0.3177570093457944);
        TEN.lineTo(IMAGE_WIDTH * 0.1822429906542056, IMAGE_HEIGHT * 0.3130841121495327);
        TEN.closePath();
        G2.setColor(TICKMARK_COLOR);
        G2.fill(TEN);

        final java.awt.geom.GeneralPath NINESHADOW = new java.awt.geom.GeneralPath();
        NINESHADOW.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        NINESHADOW.moveTo(IMAGE_WIDTH * 0.10747663551401869, IMAGE_HEIGHT * 0.48130841121495327);
        NINESHADOW.lineTo(IMAGE_WIDTH * 0.205607476635514, IMAGE_HEIGHT * 0.48130841121495327);
        NINESHADOW.lineTo(IMAGE_WIDTH * 0.205607476635514, IMAGE_HEIGHT * 0.5233644859813084);
        NINESHADOW.lineTo(IMAGE_WIDTH * 0.10747663551401869, IMAGE_HEIGHT * 0.5233644859813084);
        NINESHADOW.lineTo(IMAGE_WIDTH * 0.10747663551401869, IMAGE_HEIGHT * 0.48130841121495327);
        NINESHADOW.closePath();
        final java.awt.Color FILL_COLOR_NINESHADOW = new java.awt.Color(0x353534);
        G2.setColor(FILL_COLOR_NINESHADOW);
        G2.fill(NINESHADOW);

        final java.awt.geom.GeneralPath NINE = new java.awt.geom.GeneralPath();
        NINE.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        NINE.moveTo(IMAGE_WIDTH * 0.11682242990654206, IMAGE_HEIGHT * 0.48598130841121495);
        NINE.lineTo(IMAGE_WIDTH * 0.20093457943925233, IMAGE_HEIGHT * 0.48598130841121495);
        NINE.lineTo(IMAGE_WIDTH * 0.20093457943925233, IMAGE_HEIGHT * 0.5093457943925234);
        NINE.lineTo(IMAGE_WIDTH * 0.11682242990654206, IMAGE_HEIGHT * 0.5093457943925234);
        NINE.lineTo(IMAGE_WIDTH * 0.11682242990654206, IMAGE_HEIGHT * 0.48598130841121495);
        NINE.closePath();        
        G2.setColor(TICKMARK_COLOR);
        G2.fill(NINE);        

        final java.awt.geom.GeneralPath EIGHTSHADOW = new java.awt.geom.GeneralPath();
        EIGHTSHADOW.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        EIGHTSHADOW.moveTo(IMAGE_WIDTH * 0.17757009345794392, IMAGE_HEIGHT * 0.6869158878504673);
        EIGHTSHADOW.lineTo(IMAGE_WIDTH * 0.2383177570093458, IMAGE_HEIGHT * 0.6542056074766355);
        EIGHTSHADOW.lineTo(IMAGE_WIDTH * 0.24299065420560748, IMAGE_HEIGHT * 0.6588785046728972);
        EIGHTSHADOW.lineTo(IMAGE_WIDTH * 0.1822429906542056, IMAGE_HEIGHT * 0.6962616822429907);
        EIGHTSHADOW.lineTo(IMAGE_WIDTH * 0.17757009345794392, IMAGE_HEIGHT * 0.6869158878504673);
        EIGHTSHADOW.closePath();
        final java.awt.Color FILL_COLOR_EIGHTSHADOW = new java.awt.Color(0x353534);
        G2.setColor(FILL_COLOR_EIGHTSHADOW);
        G2.fill(EIGHTSHADOW);

        final java.awt.geom.GeneralPath EIGHT = new java.awt.geom.GeneralPath();
        EIGHT.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        EIGHT.moveTo(IMAGE_WIDTH * 0.17757009345794392, IMAGE_HEIGHT * 0.6822429906542056);
        EIGHT.lineTo(IMAGE_WIDTH * 0.2383177570093458, IMAGE_HEIGHT * 0.6495327102803738);
        EIGHT.lineTo(IMAGE_WIDTH * 0.2383177570093458, IMAGE_HEIGHT * 0.6542056074766355);
        EIGHT.lineTo(IMAGE_WIDTH * 0.17757009345794392, IMAGE_HEIGHT * 0.6869158878504673);
        EIGHT.lineTo(IMAGE_WIDTH * 0.17757009345794392, IMAGE_HEIGHT * 0.6822429906542056);
        EIGHT.closePath();
        G2.setColor(TICKMARK_COLOR);
        G2.fill(EIGHT);

        final java.awt.geom.GeneralPath SEVENSHADOW = new java.awt.geom.GeneralPath();
        SEVENSHADOW.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        SEVENSHADOW.moveTo(IMAGE_WIDTH * 0.3130841121495327, IMAGE_HEIGHT * 0.822429906542056);
        SEVENSHADOW.lineTo(IMAGE_WIDTH * 0.35046728971962615, IMAGE_HEIGHT * 0.7616822429906542);
        SEVENSHADOW.lineTo(IMAGE_WIDTH * 0.35514018691588783, IMAGE_HEIGHT * 0.7663551401869159);
        SEVENSHADOW.lineTo(IMAGE_WIDTH * 0.32242990654205606, IMAGE_HEIGHT * 0.8271028037383178);
        SEVENSHADOW.lineTo(IMAGE_WIDTH * 0.3130841121495327, IMAGE_HEIGHT * 0.822429906542056);
        SEVENSHADOW.closePath();
        final java.awt.Color FILL_COLOR_SEVENSHADOW = new java.awt.Color(0x353534);
        G2.setColor(FILL_COLOR_SEVENSHADOW);
        G2.fill(SEVENSHADOW);

        final java.awt.geom.GeneralPath SEVEN = new java.awt.geom.GeneralPath();
        SEVEN.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        SEVEN.moveTo(IMAGE_WIDTH * 0.308411214953271, IMAGE_HEIGHT * 0.822429906542056);
        SEVEN.lineTo(IMAGE_WIDTH * 0.34579439252336447, IMAGE_HEIGHT * 0.7616822429906542);
        SEVEN.lineTo(IMAGE_WIDTH * 0.35046728971962615, IMAGE_HEIGHT * 0.7616822429906542);
        SEVEN.lineTo(IMAGE_WIDTH * 0.3130841121495327, IMAGE_HEIGHT * 0.822429906542056);
        SEVEN.lineTo(IMAGE_WIDTH * 0.308411214953271, IMAGE_HEIGHT * 0.822429906542056);
        SEVEN.closePath();
        G2.setColor(TICKMARK_COLOR);
        G2.fill(SEVEN);

        final java.awt.geom.GeneralPath SIXSHADOW = new java.awt.geom.GeneralPath();
        SIXSHADOW.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        SIXSHADOW.moveTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.8084112149532711);
        SIXSHADOW.lineTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.8084112149532711);
        SIXSHADOW.lineTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.8925233644859814);
        SIXSHADOW.lineTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.8925233644859814);
        SIXSHADOW.lineTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.8084112149532711);
        SIXSHADOW.closePath();
        final java.awt.Color FILL_COLOR_SIXSHADOW = new java.awt.Color(0x2B2B2B);
        G2.setColor(FILL_COLOR_SIXSHADOW);
        G2.fill(SIXSHADOW);

        final java.awt.geom.GeneralPath SIX = new java.awt.geom.GeneralPath();
        SIX.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        SIX.moveTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.794392523364486);
        SIX.lineTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.794392523364486);
        SIX.lineTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.8878504672897196);
        SIX.lineTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.8878504672897196);
        SIX.lineTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.794392523364486);
        SIX.closePath();
        G2.setColor(TICKMARK_COLOR);
        G2.fill(SIX);        

        final java.awt.geom.GeneralPath FIVESHADOW = new java.awt.geom.GeneralPath();
        FIVESHADOW.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        FIVESHADOW.moveTo(IMAGE_WIDTH * 0.6869158878504673, IMAGE_HEIGHT * 0.822429906542056);
        FIVESHADOW.lineTo(IMAGE_WIDTH * 0.6495327102803738, IMAGE_HEIGHT * 0.7616822429906542);
        FIVESHADOW.lineTo(IMAGE_WIDTH * 0.6542056074766355, IMAGE_HEIGHT * 0.7616822429906542);
        FIVESHADOW.lineTo(IMAGE_WIDTH * 0.6915887850467289, IMAGE_HEIGHT * 0.822429906542056);
        FIVESHADOW.lineTo(IMAGE_WIDTH * 0.6869158878504673, IMAGE_HEIGHT * 0.822429906542056);
        FIVESHADOW.closePath();
        final java.awt.Color FILL_COLOR_FIVESHADOW = new java.awt.Color(0x353534);
        G2.setColor(FILL_COLOR_FIVESHADOW);
        G2.fill(FIVESHADOW);

        final java.awt.geom.GeneralPath FIVE = new java.awt.geom.GeneralPath();
        FIVE.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        FIVE.moveTo(IMAGE_WIDTH * 0.677570093457944, IMAGE_HEIGHT * 0.8271028037383178);
        FIVE.lineTo(IMAGE_WIDTH * 0.6448598130841121, IMAGE_HEIGHT * 0.7663551401869159);
        FIVE.lineTo(IMAGE_WIDTH * 0.6495327102803738, IMAGE_HEIGHT * 0.7616822429906542);
        FIVE.lineTo(IMAGE_WIDTH * 0.6869158878504673, IMAGE_HEIGHT * 0.822429906542056);
        FIVE.lineTo(IMAGE_WIDTH * 0.677570093457944, IMAGE_HEIGHT * 0.8271028037383178);
        FIVE.closePath();
        G2.setColor(TICKMARK_COLOR);
        G2.fill(FIVE);

        final java.awt.geom.GeneralPath FOURSHADOW = new java.awt.geom.GeneralPath();
        FOURSHADOW.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        FOURSHADOW.moveTo(IMAGE_WIDTH * 0.822429906542056, IMAGE_HEIGHT * 0.6869158878504673);
        FOURSHADOW.lineTo(IMAGE_WIDTH * 0.7616822429906542, IMAGE_HEIGHT * 0.6542056074766355);
        FOURSHADOW.lineTo(IMAGE_WIDTH * 0.7616822429906542, IMAGE_HEIGHT * 0.6495327102803738);
        FOURSHADOW.lineTo(IMAGE_WIDTH * 0.822429906542056, IMAGE_HEIGHT * 0.6822429906542056);
        FOURSHADOW.lineTo(IMAGE_WIDTH * 0.822429906542056, IMAGE_HEIGHT * 0.6869158878504673);
        FOURSHADOW.closePath();
        final java.awt.Color FILL_COLOR_FOURSHADOW = new java.awt.Color(0x353534);
        G2.setColor(FILL_COLOR_FOURSHADOW);
        G2.fill(FOURSHADOW);

        final java.awt.geom.GeneralPath FOUR = new java.awt.geom.GeneralPath();
        FOUR.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        FOUR.moveTo(IMAGE_WIDTH * 0.8177570093457944, IMAGE_HEIGHT * 0.6962616822429907);
        FOUR.lineTo(IMAGE_WIDTH * 0.7570093457943925, IMAGE_HEIGHT * 0.6588785046728972);
        FOUR.lineTo(IMAGE_WIDTH * 0.7616822429906542, IMAGE_HEIGHT * 0.6542056074766355);
        FOUR.lineTo(IMAGE_WIDTH * 0.822429906542056, IMAGE_HEIGHT * 0.6869158878504673);
        FOUR.lineTo(IMAGE_WIDTH * 0.8177570093457944, IMAGE_HEIGHT * 0.6962616822429907);
        FOUR.closePath();
        G2.setColor(TICKMARK_COLOR);
        G2.fill(FOUR);

        final java.awt.geom.GeneralPath THREESHADOW = new java.awt.geom.GeneralPath();
        THREESHADOW.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        THREESHADOW.moveTo(IMAGE_WIDTH * 0.7990654205607477, IMAGE_HEIGHT * 0.48130841121495327);
        THREESHADOW.lineTo(IMAGE_WIDTH * 0.897196261682243, IMAGE_HEIGHT * 0.48130841121495327);
        THREESHADOW.lineTo(IMAGE_WIDTH * 0.897196261682243, IMAGE_HEIGHT * 0.5233644859813084);
        THREESHADOW.lineTo(IMAGE_WIDTH * 0.7990654205607477, IMAGE_HEIGHT * 0.5233644859813084);
        THREESHADOW.lineTo(IMAGE_WIDTH * 0.7990654205607477, IMAGE_HEIGHT * 0.48130841121495327);
        THREESHADOW.closePath();
        final java.awt.Color FILL_COLOR_THREESHADOW = new java.awt.Color(0x353534);
        G2.setColor(FILL_COLOR_THREESHADOW);
        G2.fill(THREESHADOW);

        final java.awt.geom.GeneralPath THREE = new java.awt.geom.GeneralPath();
        THREE.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        THREE.moveTo(IMAGE_WIDTH * 0.8084112149532711, IMAGE_HEIGHT * 0.48598130841121495);
        THREE.lineTo(IMAGE_WIDTH * 0.8925233644859814, IMAGE_HEIGHT * 0.48598130841121495);
        THREE.lineTo(IMAGE_WIDTH * 0.8925233644859814, IMAGE_HEIGHT * 0.5093457943925234);
        THREE.lineTo(IMAGE_WIDTH * 0.8084112149532711, IMAGE_HEIGHT * 0.5093457943925234);
        THREE.lineTo(IMAGE_WIDTH * 0.8084112149532711, IMAGE_HEIGHT * 0.48598130841121495);
        THREE.closePath();
        G2.setColor(TICKMARK_COLOR);
        G2.fill(THREE);        

        final java.awt.geom.GeneralPath TWOSHADOW = new java.awt.geom.GeneralPath();
        TWOSHADOW.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        TWOSHADOW.moveTo(IMAGE_WIDTH * 0.822429906542056, IMAGE_HEIGHT * 0.3177570093457944);
        TWOSHADOW.lineTo(IMAGE_WIDTH * 0.7616822429906542, IMAGE_HEIGHT * 0.35514018691588783);
        TWOSHADOW.lineTo(IMAGE_WIDTH * 0.7570093457943925, IMAGE_HEIGHT * 0.34579439252336447);
        TWOSHADOW.lineTo(IMAGE_WIDTH * 0.8177570093457944, IMAGE_HEIGHT * 0.3130841121495327);
        TWOSHADOW.lineTo(IMAGE_WIDTH * 0.822429906542056, IMAGE_HEIGHT * 0.3177570093457944);
        TWOSHADOW.closePath();
        final java.awt.Color FILL_COLOR_TWOSHADOW = new java.awt.Color(0x353534);
        G2.setColor(FILL_COLOR_TWOSHADOW);
        G2.fill(TWOSHADOW);

        final java.awt.geom.GeneralPath TWO = new java.awt.geom.GeneralPath();
        TWO.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        TWO.moveTo(IMAGE_WIDTH * 0.822429906542056, IMAGE_HEIGHT * 0.32242990654205606);
        TWO.lineTo(IMAGE_WIDTH * 0.7616822429906542, IMAGE_HEIGHT * 0.3598130841121495);
        TWO.lineTo(IMAGE_WIDTH * 0.7616822429906542, IMAGE_HEIGHT * 0.35514018691588783);
        TWO.lineTo(IMAGE_WIDTH * 0.822429906542056, IMAGE_HEIGHT * 0.3177570093457944);
        TWO.lineTo(IMAGE_WIDTH * 0.822429906542056, IMAGE_HEIGHT * 0.32242990654205606);
        TWO.closePath();
        G2.setColor(TICKMARK_COLOR);
        G2.fill(TWO);

        final java.awt.geom.GeneralPath ONESHADOW = new java.awt.geom.GeneralPath();
        ONESHADOW.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        ONESHADOW.moveTo(IMAGE_WIDTH * 0.6915887850467289, IMAGE_HEIGHT * 0.17757009345794392);
        ONESHADOW.lineTo(IMAGE_WIDTH * 0.6542056074766355, IMAGE_HEIGHT * 0.2383177570093458);
        ONESHADOW.lineTo(IMAGE_WIDTH * 0.6495327102803738, IMAGE_HEIGHT * 0.2336448598130841);
        ONESHADOW.lineTo(IMAGE_WIDTH * 0.6869158878504673, IMAGE_HEIGHT * 0.17289719626168223);
        ONESHADOW.lineTo(IMAGE_WIDTH * 0.6915887850467289, IMAGE_HEIGHT * 0.17757009345794392);
        ONESHADOW.closePath();
        final java.awt.Color FILL_COLOR_ONESHADOW = new java.awt.Color(0x353534);
        G2.setColor(FILL_COLOR_ONESHADOW);
        G2.fill(ONESHADOW);

        final java.awt.geom.GeneralPath ONE = new java.awt.geom.GeneralPath();
        ONE.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        ONE.moveTo(IMAGE_WIDTH * 0.6962616822429907, IMAGE_HEIGHT * 0.17757009345794392);
        ONE.lineTo(IMAGE_WIDTH * 0.6635514018691588, IMAGE_HEIGHT * 0.2383177570093458);
        ONE.lineTo(IMAGE_WIDTH * 0.6542056074766355, IMAGE_HEIGHT * 0.2383177570093458);
        ONE.lineTo(IMAGE_WIDTH * 0.6915887850467289, IMAGE_HEIGHT * 0.17757009345794392);
        ONE.lineTo(IMAGE_WIDTH * 0.6962616822429907, IMAGE_HEIGHT * 0.17757009345794392);
        ONE.closePath();
        G2.setColor(TICKMARK_COLOR);
        G2.fill(ONE);

        final java.awt.geom.GeneralPath TWELVESHADOW = new java.awt.geom.GeneralPath();
        TWELVESHADOW.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        TWELVESHADOW.moveTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.10747663551401869);
        TWELVESHADOW.lineTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.10747663551401869);
        TWELVESHADOW.lineTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.205607476635514);
        TWELVESHADOW.lineTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.205607476635514);
        TWELVESHADOW.lineTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.10747663551401869);
        TWELVESHADOW.closePath();
        final java.awt.Color FILL_COLOR_TWELVESHADOW = new java.awt.Color(0x2B2B2B);
        G2.setColor(FILL_COLOR_TWELVESHADOW);
        G2.fill(TWELVESHADOW);

        final java.awt.geom.GeneralPath TWELVE = new java.awt.geom.GeneralPath();
        TWELVE.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        TWELVE.moveTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.10747663551401869);
        TWELVE.lineTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.10747663551401869);
        TWELVE.lineTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.20093457943925233);
        TWELVE.lineTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.20093457943925233);
        TWELVE.lineTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.10747663551401869);
        TWELVE.closePath();
        G2.setColor(TICKMARK_COLOR);
        G2.fill(TWELVE);        

        G2.dispose();

        return IMAGE;
    }

    private java.awt.image.BufferedImage create_HOUR_Image(final int WIDTH)
    {
        if (WIDTH <= 0)
        {
            return null;
        }

        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, (int) (1.0 * WIDTH), java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final java.awt.geom.GeneralPath HOURPOINTER = new java.awt.geom.GeneralPath();
        HOURPOINTER.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        HOURPOINTER.moveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.5607476635514018);
        HOURPOINTER.lineTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.21495327102803738);
        HOURPOINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1822429906542056);
        HOURPOINTER.lineTo(IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.21495327102803738);
        HOURPOINTER.lineTo(IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.5607476635514018);
        HOURPOINTER.lineTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.5607476635514018);
        HOURPOINTER.closePath();
        final java.awt.geom.Point2D HOURPOINTER_START = new java.awt.geom.Point2D.Double(0, HOURPOINTER.getBounds2D().getMaxY() );
        final java.awt.geom.Point2D HOURPOINTER_STOP = new java.awt.geom.Point2D.Double(0, HOURPOINTER.getBounds2D().getMinY() );
        final float[] HOURPOINTER_FRACTIONS =
        {
            0.0f,
            1.0f
        };
        final java.awt.Color[] HOURPOINTER_COLORS =
        {
            new java.awt.Color(245, 246, 248, 255),
            new java.awt.Color(176, 181, 188, 255)
        };
        final java.awt.LinearGradientPaint HOURPOINTER_GRADIENT = new java.awt.LinearGradientPaint(HOURPOINTER_START, HOURPOINTER_STOP, HOURPOINTER_FRACTIONS, HOURPOINTER_COLORS);
        G2.setPaint(HOURPOINTER_GRADIENT);
        G2.fill(HOURPOINTER);
        final java.awt.Color STROKE_COLOR_HOURPOINTER = new java.awt.Color(0xDADDE1);
        G2.setColor(STROKE_COLOR_HOURPOINTER);
        G2.setStroke(new java.awt.BasicStroke(1.0f, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_MITER));
        G2.draw(HOURPOINTER);

        G2.dispose();

        return IMAGE;
    }

    private java.awt.image.BufferedImage create_HOUR_SHADOW_Image(final int WIDTH)
    {
        if (WIDTH <= 0)
        {
            return null;
        }

        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, (int) (1.0 * WIDTH), java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final java.awt.geom.GeneralPath HOURPOINTER = new java.awt.geom.GeneralPath();
        HOURPOINTER.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        HOURPOINTER.moveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.5607476635514018);
        HOURPOINTER.lineTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.21495327102803738);
        HOURPOINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1822429906542056);
        HOURPOINTER.lineTo(IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.21495327102803738);
        HOURPOINTER.lineTo(IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.5607476635514018);
        HOURPOINTER.lineTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.5607476635514018);
        HOURPOINTER.closePath();

        G2.setColor(SHADOW_COLOR);
        G2.fill(HOURPOINTER);

        G2.dispose();

        return IMAGE;
    }

    private java.awt.image.BufferedImage create_MINUTE_Image(final int WIDTH)
    {
        if (WIDTH <= 0)
        {
            return null;
        }

        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, (int) (1.0 * WIDTH), java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final java.awt.geom.GeneralPath MINUTEPOINTER = new java.awt.geom.GeneralPath();
        MINUTEPOINTER.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        MINUTEPOINTER.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5747663551401869);
        MINUTEPOINTER.lineTo(IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.13551401869158877);
        MINUTEPOINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.10747663551401869);
        MINUTEPOINTER.lineTo(IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.14018691588785046);
        MINUTEPOINTER.lineTo(IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.5747663551401869);
        MINUTEPOINTER.lineTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5747663551401869);
        MINUTEPOINTER.closePath();
        final java.awt.geom.Point2D MINUTEPOINTER_START = new java.awt.geom.Point2D.Double(0, MINUTEPOINTER.getBounds2D().getMinY() );
        final java.awt.geom.Point2D MINUTEPOINTER_STOP = new java.awt.geom.Point2D.Double(0, MINUTEPOINTER.getBounds2D().getMaxY() );
        final float[] MINUTEPOINTER_FRACTIONS =
        {
            0.0f,
            1.0f
        };
        final java.awt.Color[] MINUTEPOINTER_COLORS =
        {
            new java.awt.Color(245, 246, 248, 255),
            new java.awt.Color(176, 181, 188, 255)
        };
        final java.awt.LinearGradientPaint MINUTEPOINTER_GRADIENT = new java.awt.LinearGradientPaint(MINUTEPOINTER_START, MINUTEPOINTER_STOP, MINUTEPOINTER_FRACTIONS, MINUTEPOINTER_COLORS);
        G2.setPaint(MINUTEPOINTER_GRADIENT);
        G2.fill(MINUTEPOINTER);
        final java.awt.Color STROKE_COLOR_MINUTEPOINTER = new java.awt.Color(0xDADDE1);
        G2.setColor(STROKE_COLOR_MINUTEPOINTER);
        G2.setStroke(new java.awt.BasicStroke(1.0f, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_MITER));
        G2.draw(MINUTEPOINTER);

        G2.dispose();

        return IMAGE;
    }

    private java.awt.image.BufferedImage create_MINUTE_SHADOW_Image(final int WIDTH)
    {
        if (WIDTH <= 0)
        {
            return null;
        }

        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, (int) (1.0 * WIDTH), java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final java.awt.geom.GeneralPath MINUTEPOINTER = new java.awt.geom.GeneralPath();
        MINUTEPOINTER.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        MINUTEPOINTER.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5747663551401869);
        MINUTEPOINTER.lineTo(IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.13551401869158877);
        MINUTEPOINTER.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.10747663551401869);
        MINUTEPOINTER.lineTo(IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.14018691588785046);
        MINUTEPOINTER.lineTo(IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.5747663551401869);
        MINUTEPOINTER.lineTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5747663551401869);
        MINUTEPOINTER.closePath();
        
        G2.setColor(SHADOW_COLOR);
        G2.fill(MINUTEPOINTER);

        G2.dispose();

        return IMAGE;
    }

    private java.awt.image.BufferedImage create_KNOB_Image(final int WIDTH)
    {
        if (WIDTH <= 0)
        {
            return null;
        }

        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, (int) (1.0 * WIDTH), java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final java.awt.geom.GeneralPath KNOBSHADOW = new java.awt.geom.GeneralPath();
        KNOBSHADOW.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        KNOBSHADOW.moveTo(IMAGE_WIDTH * 0.4532710280373832, IMAGE_HEIGHT * 0.5046728971962616);
        KNOBSHADOW.curveTo(IMAGE_WIDTH * 0.4532710280373832, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.45794392523364486);
        KNOBSHADOW.curveTo(IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.5467289719626168, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.5467289719626168, IMAGE_HEIGHT * 0.5046728971962616);
        KNOBSHADOW.curveTo(IMAGE_WIDTH * 0.5467289719626168, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.5560747663551402, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5560747663551402);
        KNOBSHADOW.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.5560747663551402, IMAGE_WIDTH * 0.4532710280373832, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.4532710280373832, IMAGE_HEIGHT * 0.5046728971962616);
        KNOBSHADOW.closePath();
        final java.awt.geom.Point2D KNOBSHADOW_START = new java.awt.geom.Point2D.Double(0, KNOBSHADOW.getBounds2D().getMinY() );
        final java.awt.geom.Point2D KNOBSHADOW_STOP = new java.awt.geom.Point2D.Double(0, KNOBSHADOW.getBounds2D().getMaxY() );
        final float[] KNOBSHADOW_FRACTIONS =
        {
            0.0f,
            1.0f
        };
        final java.awt.Color[] KNOBSHADOW_COLORS =
        {
            new java.awt.Color(40, 40, 41, 255),
            new java.awt.Color(13, 13, 13, 255)
        };
        final java.awt.LinearGradientPaint KNOBSHADOW_GRADIENT = new java.awt.LinearGradientPaint(KNOBSHADOW_START, KNOBSHADOW_STOP, KNOBSHADOW_FRACTIONS, KNOBSHADOW_COLORS);
        G2.setPaint(KNOBSHADOW_GRADIENT);
        G2.fill(KNOBSHADOW);

        final java.awt.geom.GeneralPath KNOB = new java.awt.geom.GeneralPath();
        KNOB.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        KNOB.moveTo(IMAGE_WIDTH * 0.45794392523364486, IMAGE_HEIGHT * 0.5);
        KNOB.curveTo(IMAGE_WIDTH * 0.45794392523364486, IMAGE_HEIGHT * 0.4766355140186916, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.45794392523364486);
        KNOB.curveTo(IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.5420560747663551, IMAGE_HEIGHT * 0.4766355140186916, IMAGE_WIDTH * 0.5420560747663551, IMAGE_HEIGHT * 0.5);
        KNOB.curveTo(IMAGE_WIDTH * 0.5420560747663551, IMAGE_HEIGHT * 0.5233644859813084, IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.5420560747663551, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5420560747663551);
        KNOB.curveTo(IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.5420560747663551, IMAGE_WIDTH * 0.45794392523364486, IMAGE_HEIGHT * 0.5233644859813084, IMAGE_WIDTH * 0.45794392523364486, IMAGE_HEIGHT * 0.5);
        KNOB.closePath();
        final java.awt.geom.Point2D KNOB_START = new java.awt.geom.Point2D.Double(0, KNOB.getBounds2D().getMinY() );
        final java.awt.geom.Point2D KNOB_STOP = new java.awt.geom.Point2D.Double(0, KNOB.getBounds2D().getMaxY() );
        final float[] KNOB_FRACTIONS =
        {
            0.0f,
            1.0f
        };
        final java.awt.Color[] KNOB_COLORS =
        {
            new java.awt.Color(238, 240, 242, 255),
            new java.awt.Color(101, 105, 109, 255)
        };
        final java.awt.LinearGradientPaint KNOB_GRADIENT = new java.awt.LinearGradientPaint(KNOB_START, KNOB_STOP, KNOB_FRACTIONS, KNOB_COLORS);
        G2.setPaint(KNOB_GRADIENT);
        G2.fill(KNOB);

        G2.dispose();

        return IMAGE;
    }

    private java.awt.image.BufferedImage create_SECOND_Image(final int WIDTH)
    {
        if (WIDTH <= 0)
        {
            return null;
        }

        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, (int) (1.0 * WIDTH), java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final java.awt.geom.GeneralPath SECONDPOINTER = new java.awt.geom.GeneralPath();
        SECONDPOINTER.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        SECONDPOINTER.moveTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.11682242990654206);
        SECONDPOINTER.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.5747663551401869);
        SECONDPOINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5747663551401869);
        SECONDPOINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.11682242990654206);
        SECONDPOINTER.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.11682242990654206);
        SECONDPOINTER.closePath();
        final java.awt.geom.Point2D SECONDPOINTER_START = new java.awt.geom.Point2D.Double(SECONDPOINTER.getBounds2D().getMaxX(), 0);
        final java.awt.geom.Point2D SECONDPOINTER_STOP = new java.awt.geom.Point2D.Double(SECONDPOINTER.getBounds2D().getMinX(), 0);
        final float[] SECONDPOINTER_FRACTIONS =
        {
            0.0f,
            0.47f,
            1.0f
        };
        final java.awt.Color[] SECONDPOINTER_COLORS =
        {
            new java.awt.Color(236, 123, 125, 255),
            new java.awt.Color(231, 27, 33, 255),
            new java.awt.Color(166, 40, 46, 255)
        };
        final java.awt.LinearGradientPaint SECONDPOINTER_GRADIENT = new java.awt.LinearGradientPaint(SECONDPOINTER_START, SECONDPOINTER_STOP, SECONDPOINTER_FRACTIONS, SECONDPOINTER_COLORS);
        G2.setPaint(SECONDPOINTER_GRADIENT);
        G2.fill(SECONDPOINTER);

        G2.dispose();

        return IMAGE;
    }

    private java.awt.image.BufferedImage create_SECOND_SHADOW_Image(final int WIDTH)
    {
        if (WIDTH <= 0)
        {
            return null;
        }

        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, (int) (1.0 * WIDTH), java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final java.awt.geom.GeneralPath SECONDPOINTER = new java.awt.geom.GeneralPath();
        SECONDPOINTER.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        SECONDPOINTER.moveTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.11682242990654206);
        SECONDPOINTER.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.5747663551401869);
        SECONDPOINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5747663551401869);
        SECONDPOINTER.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.11682242990654206);
        SECONDPOINTER.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.11682242990654206);
        SECONDPOINTER.closePath();

        G2.setPaint(SHADOW_COLOR);
        G2.fill(SECONDPOINTER);
        
        G2.dispose();

        return IMAGE;
    }

    private java.awt.image.BufferedImage create_FOREGROUND_Image(final int WIDTH)
    {
        if (WIDTH <= 0)
        {
            return null;
        }

        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, (int) (1.0 * WIDTH), java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final java.awt.geom.GeneralPath TOPKNOBSHADOW = new java.awt.geom.GeneralPath();
        TOPKNOBSHADOW.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        TOPKNOBSHADOW.moveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.5);
        TOPKNOBSHADOW.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.48598130841121495, IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.4719626168224299, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.4719626168224299);
        TOPKNOBSHADOW.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.4719626168224299, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.48598130841121495, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.5);
        TOPKNOBSHADOW.curveTo(IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.514018691588785, IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.5280373831775701, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5280373831775701);
        TOPKNOBSHADOW.curveTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.5280373831775701, IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.514018691588785, IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.5);
        TOPKNOBSHADOW.closePath();
        final java.awt.geom.Point2D TOPKNOBSHADOW_START = new java.awt.geom.Point2D.Double(0, TOPKNOBSHADOW.getBounds2D().getMinY() );
        final java.awt.geom.Point2D TOPKNOBSHADOW_STOP = new java.awt.geom.Point2D.Double(0, TOPKNOBSHADOW.getBounds2D().getMaxY() );
        final float[] TOPKNOBSHADOW_FRACTIONS =
        {
            0.0f,
            1.0f
        };
        final java.awt.Color[] TOPKNOBSHADOW_COLORS =
        {
            new java.awt.Color(221, 223, 223, 255),
            new java.awt.Color(38, 40, 41, 255)
        };
        final java.awt.LinearGradientPaint TOPKNOBSHADOW_GRADIENT = new java.awt.LinearGradientPaint(TOPKNOBSHADOW_START, TOPKNOBSHADOW_STOP, TOPKNOBSHADOW_FRACTIONS, TOPKNOBSHADOW_COLORS);
        G2.setPaint(TOPKNOBSHADOW_GRADIENT);
        G2.fill(TOPKNOBSHADOW);

        final java.awt.geom.GeneralPath TOPKNOB = new java.awt.geom.GeneralPath();
        TOPKNOB.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        TOPKNOB.moveTo(IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.5);
        TOPKNOB.curveTo(IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.48598130841121495, IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.4766355140186916, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.4766355140186916);
        TOPKNOB.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.4766355140186916, IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.48598130841121495, IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.5);
        TOPKNOB.curveTo(IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.514018691588785, IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.5233644859813084, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5233644859813084);
        TOPKNOB.curveTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.5233644859813084, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.514018691588785, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.5);
        TOPKNOB.closePath();
        final java.awt.geom.Point2D TOPKNOB_START = new java.awt.geom.Point2D.Double(0, TOPKNOB.getBounds2D().getMinY() );
        final java.awt.geom.Point2D TOPKNOB_STOP = new java.awt.geom.Point2D.Double(0, TOPKNOB.getBounds2D().getMaxY() );
        final float[] TOPKNOB_FRACTIONS =
        {
            0.0f,
            0.11f,
            0.12f,
            0.2f,
            0.2001f,
            1.0f
        };
        final java.awt.Color[] TOPKNOB_COLORS =
        {
            new java.awt.Color(234, 235, 238, 255),
            new java.awt.Color(234, 236, 238, 255),
            new java.awt.Color(232, 234, 236, 255),
            new java.awt.Color(192, 197, 203, 255),
            new java.awt.Color(190, 195, 201, 255),
            new java.awt.Color(169, 174, 181, 255)
        };
        final java.awt.LinearGradientPaint TOPKNOB_GRADIENT = new java.awt.LinearGradientPaint(TOPKNOB_START, TOPKNOB_STOP, TOPKNOB_FRACTIONS, TOPKNOB_COLORS);
        G2.setPaint(TOPKNOB_GRADIENT);
        G2.fill(TOPKNOB);

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
        final java.awt.geom.Point2D HIGHLIGHT_START = new java.awt.geom.Point2D.Double(0, HIGHLIGHT.getBounds2D().getMinY() );
        final java.awt.geom.Point2D HIGHLIGHT_STOP = new java.awt.geom.Point2D.Double(0, HIGHLIGHT.getBounds2D().getMaxY() );
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

    @Override
    public void actionPerformed(java.awt.event.ActionEvent event)
    {
        if (event.getSource().equals(CLOCK_TIMER))
        {
            // Seconds
            secondPointerAngle = java.util.Calendar.getInstance().get(java.util.Calendar.SECOND) * ANGLE_STEP + java.util.Calendar.getInstance().get(java.util.Calendar.MILLISECOND) * ANGLE_STEP / 1000;

            // Hours
            hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR) - this.timeZoneOffsetHour;
            if (hour > 12)
            {
                hour -= 12;
            }
            if (hour < 0)
            {
                hour += 12;
            }

            // Minutes
            minute = java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE) + this.timeZoneOffsetMinute;
            if (minute > 60)
            {
                minute -= 60;
                hour++;
            }
            if (minute < 0)
            {
                minute += 60;
                hour--;
            }

            // Calculate angles from current hour and minute values
            hourPointerAngle = hour * ANGLE_STEP * 5 + (0.5) * minute;
            minutePointerAngle = minute * ANGLE_STEP;


            repaint(0, 0, getWidth(), getHeight());
        }
    }

    @Override
    public String toString()
    {
        return "Clock";
    }

}