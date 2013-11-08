package eu.hansolo.steelseries.gauges;


/**
 *
 * @author hansolo
 */
public final class Altimeter extends AbstractRadial
{
    private double value100 = 0;
    private double oldValue = 0;
    private double value1000 = 0;
    private double value10000 = 0;
    private double angleStep100ft;
    private double angleStep1000ft;
    private double angleStep10000ft;
    private double TICKMARK_OFFSET = Math.PI;    
    private float tickLabelPeriod = 1f; // Draw value at every 10th tickmark    
    private java.awt.image.BufferedImage backgroundImage;
    private java.awt.image.BufferedImage tickmarksImage;
    private java.awt.image.BufferedImage pointer10000FtImage;
    private java.awt.image.BufferedImage pointer1000FtImage;
    private java.awt.image.BufferedImage pointer100FtImage;
    private java.awt.image.BufferedImage foregroundImage;    
    private org.pushingpixels.trident.Timeline timeline = new org.pushingpixels.trident.Timeline(this);
    private final org.pushingpixels.trident.callback.TimelineCallback TIMELINE_CALLBACK = new org.pushingpixels.trident.callback.TimelineCallback()
    {
            @Override
            public void onTimelineStateChanged(org.pushingpixels.trident.Timeline.TimelineState oldState, org.pushingpixels.trident.Timeline.TimelineState newState, float oldValue, float newValue)
            {

            }

            @Override
            public void onTimelinePulse(float oldValue, float newValue)
            {                
            }

        };

        
    public Altimeter()
    {
        super();
        setMinValue(0);
        setMaxValue(10);
        calcAngleStep();
        setTitle("ALT");
        setUnitString("ft");
        addComponentListener(this);
        setSize(getPreferredSize());
        init(getWidth(), getWidth());
        timeline.addCallback(TIMELINE_CALLBACK);
    }
   
    @Override
    public AbstractGauge init(final int WIDTH, final int HEIGHT)
    {
        backgroundImage = create_BACKGROUND_Image(WIDTH, getTitle(), getUnitString());
        tickmarksImage = create_TICKMARKS_Image(WIDTH, 0, TICKMARK_OFFSET, 0, 10, angleStep100ft, (int)tickLabelPeriod, 0);
        pointer100FtImage = create_100FT_POINTER_Image(WIDTH);
        pointer1000FtImage = create_1000FT_POINTER_Image(WIDTH);
        pointer10000FtImage = create_10000FT_POINTER_Image(WIDTH);
        foregroundImage = create_FOREGROUND_Image(WIDTH);

        return this;
    }

    @Override
    protected void paintComponent(java.awt.Graphics g)
    {
        final java.awt.Graphics2D G2 = (java.awt.Graphics2D) g;

        final java.awt.geom.Point2D CENTER = new java.awt.geom.Point2D.Double(pointer100FtImage.getWidth() / 2.0, pointer100FtImage.getWidth() / 2.0);

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

        // Draw the tickmarks
        G2.drawImage(tickmarksImage, 0, 0, null);

        // Draw the 10000ft pointer
        final double ANGLE10000FT = (value10000 - getMinValue()) * angleStep10000ft;
        G2.rotate(ANGLE10000FT, CENTER.getX(), CENTER.getY());
        G2.drawImage(pointer10000FtImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);

        // Draw the 1000ft pointer
        final double ANGLE1000FT = (value1000 - getMinValue()) * angleStep1000ft;
        G2.rotate(ANGLE1000FT, CENTER.getX(), CENTER.getY());
        G2.drawImage(pointer1000FtImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);

        // Draw the 100ft pointer
        final double ANGLE100FT = (value100 - getMinValue()) * angleStep100ft;
        G2.rotate(ANGLE100FT, CENTER.getX(), CENTER.getY());
        G2.drawImage(pointer100FtImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);

        // Draw the foreground
        G2.drawImage(foregroundImage, 0, 0, null);
    }

    @Override
    public void setValue(final double VALUE)
    {                
        this.value100 = (VALUE % 1000) / 100;
        this.value1000 = (VALUE % 10000) / 100;
        this.value10000 = (VALUE % 100000) / 100;
        
        firePropertyChange(VALUE_PROPERTY, this.oldValue, VALUE);
        this.oldValue = VALUE;
        repaint();
    }

    @Override
    public void setValueAnimated(final double VALUE)
    {
        if (timeline.getState() == org.pushingpixels.trident.Timeline.TimelineState.PLAYING_FORWARD || timeline.getState() == org.pushingpixels.trident.Timeline.TimelineState.PLAYING_REVERSE)
        {
            timeline.abort();
        }
        timeline.addPropertyToInterpolate("value", this.oldValue, VALUE);
        timeline.setEase(new org.pushingpixels.trident.ease.Spline(0.5f));
        final double RANGE = Math.abs(this.value100 - VALUE);
        final double FRACTION = RANGE / 1000;

        timeline.setDuration((long) (1000 * FRACTION));
        timeline.play();
    }

    @Override
    protected void calcAngleStep()
    {
        this.angleStep100ft = (2 * Math.PI) / (getMaxValue() -  getMinValue());
        this.angleStep1000ft = angleStep100ft / 10.0;
        this.angleStep10000ft = angleStep1000ft / 10.0;
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

    @Override
    protected java.awt.image.BufferedImage create_TICKMARKS_Image(final int WIDTH, final double FREE_AREA_ANGLE, final double OFFSET, final double MIN_VALUE, final double MAX_VALUE, final double ANGLE_STEP, final int TICK_LABEL_PERIOD, final int SCALE_DIVIDER_POWER)
    {
        if (WIDTH <= 0)
        {
            return null;
        }

        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, WIDTH, java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_ENABLE);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final java.awt.Font STD_FONT = new java.awt.Font("Verdana", 0, (int) (0.09 * WIDTH));
        final java.awt.BasicStroke MEDIUM_STROKE = new java.awt.BasicStroke(2.0f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_BEVEL);
        final java.awt.BasicStroke THIN_STROKE = new java.awt.BasicStroke(1.5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_BEVEL);        
        final int TEXT_DISTANCE = (int) (0.17 * WIDTH);        
        final int MED_LENGTH = (int) (0.05 * WIDTH);
        final int MAX_LENGTH = (int) (0.07 * WIDTH);

        final java.awt.Color TEXT_COLOR = super.getBackgroundColor().LABEL_COLOR;
        final java.awt.Color TICK_COLOR = super.getBackgroundColor().LABEL_COLOR;

        // Create the ticks itself
        final float RADIUS = IMAGE_WIDTH * 0.4f;
        final java.awt.geom.Point2D CENTER = new java.awt.geom.Point2D.Double(IMAGE_WIDTH / 2.0f, IMAGE_HEIGHT / 2.0f);

        // Draw ticks
        java.awt.geom.Point2D innerPoint;
        java.awt.geom.Point2D outerPoint;
        java.awt.geom.Point2D textPoint = null;        
        java.awt.geom.Line2D tick;
        int counter = 0;
        int tickCounter = 0;

        G2.setFont(STD_FONT);

        double sinValue = 0;
        double cosValue = 0;

        double alpha; // angle for the tickmarks
        final double ALPHA_START = -OFFSET - (FREE_AREA_ANGLE / 2.0);
        float valueCounter; // value for the tickmarks

        for (alpha = ALPHA_START, valueCounter = 0 ; valueCounter <= 10 ; alpha -= ANGLE_STEP * 0.1, valueCounter += 0.1)
        {
            G2.setStroke(THIN_STROKE);
            sinValue = Math.sin(alpha);
            cosValue = Math.cos(alpha);                        

            // tickmark every 2 units
            if (counter % 2 == 0)
            {
                G2.setStroke(THIN_STROKE);
                innerPoint = new java.awt.geom.Point2D.Double(CENTER.getX() + (RADIUS - MED_LENGTH) * sinValue, CENTER.getY() + (RADIUS - MED_LENGTH) * cosValue);
                outerPoint = new java.awt.geom.Point2D.Double(CENTER.getX() + RADIUS * sinValue, CENTER.getY() + RADIUS * cosValue);
                // Draw ticks
                G2.setColor(TICK_COLOR);
                tick = new java.awt.geom.Line2D.Double(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                G2.draw(tick);
            }
            
            // Different tickmark every 10 units
            if (counter == 10 || counter == 0)
            {
                G2.setColor(TEXT_COLOR);
                G2.setStroke(MEDIUM_STROKE);
                innerPoint = new java.awt.geom.Point2D.Double(CENTER.getX() + (RADIUS - MAX_LENGTH) * sinValue, CENTER.getY() + (RADIUS - MAX_LENGTH) * cosValue);
                outerPoint = new java.awt.geom.Point2D.Double(CENTER.getX() + RADIUS * sinValue, CENTER.getY() + RADIUS * cosValue);
                textPoint = new java.awt.geom.Point2D.Double(CENTER.getX() + (RADIUS - TEXT_DISTANCE + STD_FONT.getSize() / 2f) * sinValue, CENTER.getY() + (RADIUS - TEXT_DISTANCE + STD_FONT.getSize() / 2f) * cosValue + TEXT_DISTANCE / 2.5f);

                // Draw text
                final java.awt.font.FontRenderContext RENDER_CONTEXT = new java.awt.font.FontRenderContext(null, true, true);
                final java.awt.font.TextLayout TEXT_LAYOUT = new java.awt.font.TextLayout(String.valueOf(Math.round(valueCounter)), G2.getFont(), RENDER_CONTEXT);

                final java.awt.geom.Rectangle2D TEXT_BOUNDARY = TEXT_LAYOUT.getBounds();

                // if gauge is full circle, avoid painting maxValue over minValue
                if (FREE_AREA_ANGLE == 0)
                {
                    if (Float.compare(valueCounter, 10) != 0)
                    {
                        G2.drawString(String.valueOf(Math.round(valueCounter)), (int) (textPoint.getX() - TEXT_BOUNDARY.getWidth() / 2.0), (int) ((textPoint.getY() - TEXT_BOUNDARY.getHeight() / 2.0)));                        
                    }
                }
                else
                {
                    G2.drawString(String.valueOf(Math.round(valueCounter)), (int) (textPoint.getX() - TEXT_BOUNDARY.getWidth() / 2.0), (int) ((textPoint.getY() - TEXT_BOUNDARY.getHeight() / 2.0)));                    
                }
                counter = 0;
                tickCounter++;

                // Draw ticks
                G2.setColor(TICK_COLOR);
                tick = new java.awt.geom.Line2D.Double(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                G2.draw(tick);
            }

            counter++;
        }

        G2.dispose();

        return IMAGE;
    }

    private java.awt.image.BufferedImage create_100FT_POINTER_Image(final int WIDTH)
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
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final java.awt.geom.GeneralPath POINTER100FT = new java.awt.geom.GeneralPath();
        POINTER100FT.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        POINTER100FT.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.4719626168224299, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.4672897196261682, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.4672897196261682);
        POINTER100FT.lineTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.20093457943925233);
        POINTER100FT.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.16822429906542055);
        POINTER100FT.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.20093457943925233);
        POINTER100FT.lineTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.4672897196261682);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.4672897196261682, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4719626168224299, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.514018691588785, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.5280373831775701, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5327102803738317);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5794392523364486, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5887850467289719);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.5934579439252337, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.5981308411214953, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.6074766355140186);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.616822429906542, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.6261682242990654, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.6261682242990654);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.6261682242990654, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.616822429906542, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.6074766355140186);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5981308411214953, IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.5934579439252337, IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.5887850467289719);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.5794392523364486, IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.5327102803738317);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.5280373831775701, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.514018691588785, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5);
        POINTER100FT.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER100FT.closePath();
        final java.awt.geom.Point2D POINTER100FT_START = new java.awt.geom.Point2D.Double(0, POINTER100FT.getBounds2D().getMinY() );
        final java.awt.geom.Point2D POINTER100FT_STOP = new java.awt.geom.Point2D.Double(0, POINTER100FT.getBounds2D().getMaxY() );
        final float[] POINTER100FT_FRACTIONS =
        {
            0.0f,
            0.31f,
            0.3101f,
            0.32f,
            1.0f
        };
        final java.awt.Color[] POINTER100FT_COLORS =
        {
            new java.awt.Color(255, 255, 255, 255),
            new java.awt.Color(255, 255, 255, 255),
            new java.awt.Color(255, 255, 255, 255),
            new java.awt.Color(32, 32, 32, 255),
            new java.awt.Color(32, 32, 32, 255)
        };        
        final java.awt.LinearGradientPaint POINTER100FT_GRADIENT = new java.awt.LinearGradientPaint(POINTER100FT_START, POINTER100FT_STOP, POINTER100FT_FRACTIONS, POINTER100FT_COLORS);
        G2.setPaint(POINTER100FT_GRADIENT);
        G2.fill(POINTER100FT);

        G2.dispose();

        return IMAGE;
    }

    private java.awt.image.BufferedImage create_1000FT_POINTER_Image(final int WIDTH)
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
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final java.awt.geom.GeneralPath POINTER1000FT = new java.awt.geom.GeneralPath();
        POINTER1000FT.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        POINTER1000FT.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.46261682242990654, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.40186915887850466, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.40186915887850466);
        POINTER1000FT.lineTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.3317757009345794);
        POINTER1000FT.lineTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.40186915887850466);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.40186915887850466, IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.46261682242990654, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.514018691588785, IMAGE_WIDTH * 0.4766355140186916, IMAGE_HEIGHT * 0.5280373831775701, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5327102803738317);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.46261682242990654, IMAGE_HEIGHT * 0.5747663551401869, IMAGE_WIDTH * 0.46261682242990654, IMAGE_HEIGHT * 0.5934579439252337);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.616822429906542, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.6121495327102804, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.6121495327102804);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.6121495327102804, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.616822429906542, IMAGE_WIDTH * 0.5373831775700935, IMAGE_HEIGHT * 0.5934579439252337);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.5373831775700935, IMAGE_HEIGHT * 0.5747663551401869, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.5327102803738317);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.5233644859813084, IMAGE_HEIGHT * 0.5280373831775701, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.514018691588785, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5);
        POINTER1000FT.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER1000FT.closePath();
        final java.awt.geom.Point2D POINTER1000FT_START = new java.awt.geom.Point2D.Double(0, POINTER1000FT.getBounds2D().getMinY() );
        final java.awt.geom.Point2D POINTER1000FT_STOP = new java.awt.geom.Point2D.Double(0, POINTER1000FT.getBounds2D().getMaxY() );
        final float[] POINTER1000FT_FRACTIONS =
        {
            0.0f,
            0.51f,
            0.52f,
            0.5201f,
            0.53f,
            1.0f
        };
        final java.awt.Color[] POINTER1000FT_COLORS =
        {
            new java.awt.Color(255, 255, 255, 255),
            new java.awt.Color(255, 255, 255, 255),
            new java.awt.Color(255, 255, 255, 255),
            new java.awt.Color(32, 32, 32, 255),
            new java.awt.Color(32, 32, 32, 255),
            new java.awt.Color(32, 32, 32, 255)
        };
        final java.awt.LinearGradientPaint POINTER1000FT_GRADIENT = new java.awt.LinearGradientPaint(POINTER1000FT_START, POINTER1000FT_STOP, POINTER1000FT_FRACTIONS, POINTER1000FT_COLORS);
        G2.setPaint(POINTER1000FT_GRADIENT);
        G2.fill(POINTER1000FT);

        G2.dispose();

        return IMAGE;
    }

    private java.awt.image.BufferedImage create_10000FT_POINTER_Image(final int WIDTH)
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
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        final java.awt.geom.GeneralPath POINTER10000FT = new java.awt.geom.GeneralPath();
        POINTER10000FT.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        POINTER10000FT.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER10000FT.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.4719626168224299, IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.4672897196261682, IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.4672897196261682);
        POINTER10000FT.lineTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.3177570093457944);
        POINTER10000FT.lineTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.3037383177570093);
        POINTER10000FT.lineTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.1822429906542056);
        POINTER10000FT.lineTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.11682242990654206);
        POINTER10000FT.lineTo(IMAGE_WIDTH * 0.46261682242990654, IMAGE_HEIGHT * 0.11682242990654206);
        POINTER10000FT.lineTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.1822429906542056);
        POINTER10000FT.lineTo(IMAGE_WIDTH * 0.4953271028037383, IMAGE_HEIGHT * 0.29906542056074764);
        POINTER10000FT.lineTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.3177570093457944);
        POINTER10000FT.lineTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.4672897196261682);
        POINTER10000FT.curveTo(IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.4672897196261682, IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.4719626168224299, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER10000FT.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5);
        POINTER10000FT.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5327102803738317);
        POINTER10000FT.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5);
        POINTER10000FT.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER10000FT.closePath();        
        G2.setColor(java.awt.Color.WHITE);
        G2.fill(POINTER10000FT);

        G2.dispose();

        return IMAGE;
    }

    @Override
    public String toString()
    {
        return "Altimeter";
    }
}
