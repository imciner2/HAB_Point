package eu.hansolo.steelseries.gauges;


/**
 *
 * @author hansolo
 */
public final class Compass extends AbstractRadial
{    
    private static final double MIN_VALUE = 0;
    private static final double MAX_VALUE = 359.9;
    private double value = 0;
    private double angleStep = (2 * Math.PI) / (MAX_VALUE - MIN_VALUE);    
    private java.awt.image.BufferedImage backgroundImage;
    private java.awt.image.BufferedImage compassRoseImage;
    private java.awt.image.BufferedImage pointerShadowImage;
    private java.awt.image.BufferedImage pointerImage;
    private java.awt.image.BufferedImage foregroundImage;
    private final org.pushingpixels.trident.Timeline TIMELINE = new org.pushingpixels.trident.Timeline(this);
    private final org.pushingpixels.trident.ease.Spline EASE = new org.pushingpixels.trident.ease.Spline(0.5f);
    
    public Compass()
    {
        super();
        addComponentListener(this);
        setSize(getPreferredSize());
        init(getWidth(), getWidth());
    }

    @Override
    public AbstractGauge init(final int WIDTH, final int HEIGHT)
    {
        backgroundImage = create_BACKGROUND_Image(WIDTH);
        compassRoseImage = create_COMPASS_ROSE_Image(WIDTH);
        pointerShadowImage = create_POINTER_SHADOW_Image(WIDTH);
        pointerImage = create_POINTER_Image(WIDTH);
        foregroundImage = create_FOREGROUND_Image(WIDTH);

        return this;
    }

    @Override
    protected void paintComponent(java.awt.Graphics g)
    {
        final java.awt.Graphics2D G2 = (java.awt.Graphics2D) g;

        final java.awt.geom.Point2D CENTER = new java.awt.geom.Point2D.Double(pointerImage.getWidth() / 2.0, pointerImage.getWidth() / 2.0);

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

        // Draw compass rose
        G2.drawImage(compassRoseImage, 0, 0, null);

        // Draw the pointer
        final double ANGLE = (value - MIN_VALUE) * angleStep;        
        G2.rotate(ANGLE, CENTER.getX(), CENTER.getY() + 2);
        G2.drawImage(pointerShadowImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);
        G2.rotate(ANGLE, CENTER.getX(), CENTER.getY());
        G2.drawImage(pointerImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);

        // Draw the foreground
        G2.drawImage(foregroundImage, 0, 0, null);
    }

    @Override
    public void setValue(double value)
    {
        value = value > MAX_VALUE ? (value - 360) : value;
        value = value < -360 ? (value + 360) : value;
                               
        final double OLD_VALUE = this.value;
        this.value = value;

        firePropertyChange(VALUE_PROPERTY, OLD_VALUE, value);
        repaint();
    }

    @Override
    public void setValueAnimated(double value)
    {
        // Needle should always take the shortest way to it's new position
        if (360 - value + this.value < value - this.value)
        {
            value = 360 - value;
        }

        if (TIMELINE.getState() == org.pushingpixels.trident.Timeline.TimelineState.PLAYING_FORWARD || TIMELINE.getState() == org.pushingpixels.trident.Timeline.TimelineState.PLAYING_REVERSE)
        {
            TIMELINE.abort();
        }
        TIMELINE.addPropertyToInterpolate("value", this.value, value);
        TIMELINE.setEase(EASE);

        TIMELINE.setDuration((long) 3000);
        TIMELINE.play();
    }

    @Override
    protected void calcAngleStep()
    {
        angleStep = (2 * Math.PI) / (MAX_VALUE - MIN_VALUE);
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

    private java.awt.image.BufferedImage create_BIG_ROSE_POINTER_Image(final int WIDTH)
    {
        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage((int) (WIDTH * 0.0546875f), (int)(WIDTH * 0.2f), java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();

        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        G2.setStroke(new java.awt.BasicStroke(0.75f));

        // Define arrow shape of pointer
        final java.awt.geom.GeneralPath POINTER_WHITE_LEFT = new java.awt.geom.GeneralPath();
        final java.awt.geom.GeneralPath POINTER_WHITE_RIGHT = new java.awt.geom.GeneralPath();

        POINTER_WHITE_LEFT.moveTo(IMAGE_WIDTH - IMAGE_WIDTH * 0.95f, IMAGE_HEIGHT);
        POINTER_WHITE_LEFT.lineTo(IMAGE_WIDTH / 2.0f, 0);
        POINTER_WHITE_LEFT.lineTo(IMAGE_WIDTH / 2.0f, IMAGE_HEIGHT);
        POINTER_WHITE_LEFT.closePath();

        POINTER_WHITE_RIGHT.moveTo(IMAGE_WIDTH * 0.95f, IMAGE_HEIGHT);
        POINTER_WHITE_RIGHT.lineTo(IMAGE_WIDTH / 2.0f, 0);
        POINTER_WHITE_RIGHT.lineTo(IMAGE_WIDTH / 2.0f, IMAGE_HEIGHT);
        POINTER_WHITE_RIGHT.closePath();

        final java.awt.geom.Area POINTER_FRAME_WHITE = new java.awt.geom.Area(POINTER_WHITE_LEFT);
        POINTER_FRAME_WHITE.add(new java.awt.geom.Area(POINTER_WHITE_RIGHT));

        final java.awt.Color POINTER_DARK_WHITE_COLOR = new java.awt.Color(0x848380);
        final java.awt.Color POINTER_LIGHT_WHITE_COLOR = new java.awt.Color(0xE6E7E2);

        G2.setColor(POINTER_DARK_WHITE_COLOR);
        G2.fill(POINTER_WHITE_RIGHT);
        G2.setColor(POINTER_LIGHT_WHITE_COLOR);
        G2.fill(POINTER_WHITE_LEFT);
        G2.setColor(POINTER_DARK_WHITE_COLOR);
        G2.draw(POINTER_FRAME_WHITE);

        G2.dispose();

        return IMAGE;
    }

    private java.awt.image.BufferedImage create_SMALL_ROSE_POINTER_Image(final int WIDTH)
    {
        final java.awt.GraphicsConfiguration GFX_CONF = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final java.awt.image.BufferedImage IMAGE = GFX_CONF.createCompatibleImage((int) (WIDTH * 0.0546875f), (int)(WIDTH * 0.2f), java.awt.Transparency.TRANSLUCENT);
        final java.awt.Graphics2D G2 = IMAGE.createGraphics();


        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();

        G2.setStroke(new java.awt.BasicStroke(0.75f));

        // Define arrow shape of pointer
        final java.awt.geom.GeneralPath POINTER_WHITE_LEFT = new java.awt.geom.GeneralPath();
        final java.awt.geom.GeneralPath POINTER_WHITE_RIGHT = new java.awt.geom.GeneralPath();

        POINTER_WHITE_LEFT.moveTo(IMAGE_WIDTH - IMAGE_WIDTH * 0.75f, IMAGE_HEIGHT);
        POINTER_WHITE_LEFT.lineTo(IMAGE_WIDTH / 2.0f, IMAGE_HEIGHT / 2.0f);
        POINTER_WHITE_LEFT.lineTo(IMAGE_WIDTH / 2.0f, IMAGE_HEIGHT);
        POINTER_WHITE_LEFT.closePath();

        POINTER_WHITE_RIGHT.moveTo(IMAGE_WIDTH * 0.75f, IMAGE_HEIGHT);
        POINTER_WHITE_RIGHT.lineTo(IMAGE_WIDTH / 2.0f, IMAGE_HEIGHT / 2.0f);
        POINTER_WHITE_RIGHT.lineTo(IMAGE_WIDTH / 2.0f, IMAGE_HEIGHT);
        POINTER_WHITE_RIGHT.closePath();

        final java.awt.geom.Area POINTER_FRAME_WHITE = new java.awt.geom.Area(POINTER_WHITE_LEFT);
        POINTER_FRAME_WHITE.add(new java.awt.geom.Area(POINTER_WHITE_RIGHT));

        final java.awt.Color POINTER_DARK_WHITE_COLOR = new java.awt.Color(0x848380);
        final java.awt.Color POINTER_LIGHT_WHITE_COLOR = new java.awt.Color(0xE6E7E2);

        G2.setColor(POINTER_LIGHT_WHITE_COLOR);
        G2.fill(POINTER_FRAME_WHITE);
        G2.setColor(POINTER_DARK_WHITE_COLOR);
        G2.draw(POINTER_FRAME_WHITE);

        G2.dispose();

        return IMAGE;
    }

    private java.awt.image.BufferedImage create_COMPASS_ROSE_Image(final int WIDTH)
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
        //final int IMAGE_HEIGHT = IMAGE.getHeight();

        // ******************* COMPASS ROSE *************************************************
        final java.awt.geom.Point2D CENTER = new java.awt.geom.Point2D.Double(IMAGE_WIDTH / 2.0f, IMAGE_WIDTH / 2.0f);
        java.awt.geom.AffineTransform transform = G2.getTransform();
        G2.setStroke(new java.awt.BasicStroke(IMAGE_WIDTH * 0.01953125f, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_BEVEL));
        for (int i = 0 ; i <= 360 ; i+= 30)
        {            
            G2.setColor(new java.awt.Color(0.75f, 0.75f, 0.75f));
            G2.draw(new java.awt.geom.Arc2D.Double(CENTER.getX() - IMAGE_WIDTH * 0.263671875f, CENTER.getY() - IMAGE_WIDTH * 0.263671875f, IMAGE_WIDTH * 0.52734375f, IMAGE_WIDTH * 0.52734375f, i, 15, java.awt.geom.Arc2D.OPEN));
        }

        G2.setStroke(new java.awt.BasicStroke(0.5f));
        java.awt.Shape outerCircle = new java.awt.geom.Ellipse2D.Double(CENTER.getX() - IMAGE_WIDTH * 0.2734375f, CENTER.getY() - IMAGE_WIDTH * 0.2734375f, IMAGE_WIDTH * 0.546875f, IMAGE_WIDTH * 0.546875f);
        G2.draw(outerCircle);
        java.awt.Shape innerCircle = new java.awt.geom.Ellipse2D.Double(CENTER.getX() - IMAGE_WIDTH * 0.25390625f, CENTER.getY() - IMAGE_WIDTH * 0.25390625f, IMAGE_WIDTH * 0.5078125f, IMAGE_WIDTH * 0.5078125f);
        G2.draw(innerCircle);

        final float LINE_LENGTH = IMAGE_WIDTH * 0.76f;
        final java.awt.geom.Line2D LINE = new java.awt.geom.Line2D.Double((IMAGE_WIDTH - LINE_LENGTH) / 2.0f, CENTER.getY(), (IMAGE_WIDTH - LINE_LENGTH) / 2.0f + LINE_LENGTH, CENTER.getY());        
        G2.setColor(new java.awt.Color(0x848380));

        G2.setStroke(new java.awt.BasicStroke(0.5f));
        G2.draw(LINE);
        G2.rotate(Math.PI / 12, CENTER.getX(), CENTER.getY());
        G2.draw(LINE);
        G2.rotate(Math.PI / 6, CENTER.getX(), CENTER.getY());
        G2.draw(LINE);
        G2.rotate(Math.PI / 6, CENTER.getX(), CENTER.getY());
        G2.draw(LINE);
        G2.rotate(Math.PI / 12, CENTER.getX(), CENTER.getY());
        G2.draw(LINE);
        G2.rotate(Math.PI / 12, CENTER.getX(), CENTER.getY());
        G2.draw(LINE);
        G2.rotate(Math.PI / 6, CENTER.getX(), CENTER.getY());
        G2.draw(LINE);
        G2.rotate(Math.PI / 6, CENTER.getX(), CENTER.getY());
        G2.draw(LINE);

        G2.setTransform(transform);
        final java.awt.image.BufferedImage BIG_ROSE_POINTER = create_BIG_ROSE_POINTER_Image(IMAGE_WIDTH);
        final java.awt.image.BufferedImage SMALL_ROSE_POINTER = create_SMALL_ROSE_POINTER_Image(IMAGE_WIDTH);
        final java.awt.geom.Point2D OFFSET = new java.awt.geom.Point2D.Double(IMAGE_WIDTH * 0.475f, IMAGE_WIDTH * 0.20f);

        G2.translate(OFFSET.getX(), OFFSET.getY());

        // N
        G2.drawImage(BIG_ROSE_POINTER, 0, 0, this);

        // NE
        G2.rotate(Math.PI / 4f, CENTER.getX() - OFFSET.getX(), CENTER.getY() - OFFSET.getY());
        G2.drawImage(SMALL_ROSE_POINTER, 0, 0, this);

        // E
        G2.rotate(Math.PI / 4f, CENTER.getX() - OFFSET.getX(), CENTER.getY() - OFFSET.getY());
        G2.drawImage(BIG_ROSE_POINTER, 0, 0, this);

        // SE
        G2.rotate(Math.PI / 4f, CENTER.getX() - OFFSET.getX(), CENTER.getY() - OFFSET.getY());
        G2.drawImage(SMALL_ROSE_POINTER, 0, 0, this);

        // S
        G2.rotate(Math.PI / 4f, CENTER.getX() - OFFSET.getX(), CENTER.getY() - OFFSET.getY());
        G2.drawImage(BIG_ROSE_POINTER, 0, 0, this);

        // SW
        G2.rotate(Math.PI / 4f, CENTER.getX() - OFFSET.getX(), CENTER.getY() - OFFSET.getY());
        G2.drawImage(SMALL_ROSE_POINTER, 0, 0, this);


        // W
        G2.rotate(Math.PI / 4f, CENTER.getX() - OFFSET.getX(), CENTER.getY() - OFFSET.getY());
        G2.drawImage(BIG_ROSE_POINTER, 0, 0, this);

        // NW
        G2.rotate(Math.PI / 4f, CENTER.getX() - OFFSET.getX(), CENTER.getY() - OFFSET.getY());
        G2.drawImage(SMALL_ROSE_POINTER, 0, 0, this);

        G2.setTransform(transform);
        
        G2.setColor(new java.awt.Color(0xEFEFEF));
        G2.setStroke(new java.awt.BasicStroke(IMAGE_WIDTH * 0.00953125f, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_BEVEL));
        G2.draw(new java.awt.geom.Ellipse2D.Double(CENTER.getX() - (IMAGE_WIDTH * 0.1025f), CENTER.getY() - (IMAGE_WIDTH * 0.1025f), IMAGE_WIDTH * 0.205f, IMAGE_WIDTH * 0.205f));

        G2.setStroke(new java.awt.BasicStroke(0.5f));        
        G2.setColor(new java.awt.Color(0x9FA19C));
        final java.awt.Shape OUTER_ROSE_ELLIPSE = new java.awt.geom.Ellipse2D.Double(CENTER.getX() - (IMAGE_WIDTH * 0.11f), CENTER.getY() - (IMAGE_WIDTH * 0.11f), IMAGE_WIDTH * 0.22f, IMAGE_WIDTH * 0.22f);
        G2.draw(OUTER_ROSE_ELLIPSE);
        final java.awt.Shape INNER_ROSE_ELLIPSE = new java.awt.geom.Ellipse2D.Double(CENTER.getX() - (IMAGE_WIDTH * 0.095f), CENTER.getY() - (IMAGE_WIDTH * 0.095f), IMAGE_WIDTH * 0.19f, IMAGE_WIDTH * 0.19f);
        G2.draw(INNER_ROSE_ELLIPSE);


        // ******************* TICKMARKS ****************************************************
        create_TICKMARKS(G2, IMAGE_WIDTH);

        G2.dispose();

        return IMAGE;
    }

    private void create_TICKMARKS(final java.awt.Graphics2D G2, final int IMAGE_WIDTH)
    {
        // Store former transformation
        final java.awt.geom.AffineTransform FORMER_TRANSFORM = G2.getTransform();

        final java.awt.BasicStroke MEDIUM_STROKE = new java.awt.BasicStroke(0.005859375f * IMAGE_WIDTH, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_BEVEL);
        final java.awt.BasicStroke THIN_STROKE = new java.awt.BasicStroke(0.00390625f * IMAGE_WIDTH, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_BEVEL);
        final java.awt.Font BIG_FONT = new java.awt.Font("Verdana", 1, (int) (0.08f * IMAGE_WIDTH));
        final java.awt.Font SMALL_FONT = new java.awt.Font("Verdana", 0, (int) (0.04f * IMAGE_WIDTH));
        final float TEXT_DISTANCE = 0.0750f * IMAGE_WIDTH;
        final float MIN_LENGTH = 0.015625f * IMAGE_WIDTH;
        final float MED_LENGTH = 0.0234375f * IMAGE_WIDTH;
        final float MAX_LENGTH = 0.03125f * IMAGE_WIDTH;

        final java.awt.Color TEXT_COLOR = new java.awt.Color(1.0f, 1.0f, 1.0f, 1.0f);
        final java.awt.Color TICK_COLOR = new java.awt.Color(1.0f, 1.0f, 1.0f, 1.0f);

        // Create the watch itself
        final float RADIUS = IMAGE_WIDTH * 0.38f;
        final java.awt.geom.Point2D CENTER = new java.awt.geom.Point2D.Double(IMAGE_WIDTH / 2.0f, IMAGE_WIDTH / 2.0f);

        G2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_FRACTIONALMETRICS, java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        // Draw ticks
        java.awt.geom.Point2D innerPoint;
        java.awt.geom.Point2D outerPoint;
        java.awt.geom.Point2D textPoint = null;
        java.awt.geom.Line2D tick;
        int tickCounter90 = 0;
        int tickCounter15 = 0;
        int tickCounter5 = 0;
        int counter = 0;

        double sinValue = 0;
        double cosValue = 0;

        final double STEP = (2.0d * Math.PI) / (360.0d);

        for (double alpha = 2 * Math.PI; alpha >= 0; alpha -= STEP)
        {
            G2.setStroke(THIN_STROKE);
            sinValue = Math.sin(alpha);
            cosValue = Math.cos(alpha);

            G2.setColor(TICK_COLOR);

            if (tickCounter5 == 5)
            {
                G2.setStroke(THIN_STROKE);
                innerPoint = new java.awt.geom.Point2D.Double(CENTER.getX() + (RADIUS - MIN_LENGTH) * sinValue, CENTER.getY() + (RADIUS - MIN_LENGTH) * cosValue);
                outerPoint = new java.awt.geom.Point2D.Double(CENTER.getX() + RADIUS * sinValue, CENTER.getY() + RADIUS * cosValue);
                // Draw ticks
                tick = new java.awt.geom.Line2D.Double(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                G2.draw(tick);

                tickCounter5 = 0;
            }

            // Different tickmark every 15 units
            if (tickCounter15 == 15)
            {
                G2.setStroke(THIN_STROKE);
                innerPoint = new java.awt.geom.Point2D.Double(CENTER.getX() + (RADIUS - MED_LENGTH) * sinValue, CENTER.getY() + (RADIUS - MED_LENGTH) * cosValue);
                outerPoint = new java.awt.geom.Point2D.Double(CENTER.getX() + RADIUS * sinValue, CENTER.getY() + RADIUS * cosValue);

                // Draw ticks
                tick = new java.awt.geom.Line2D.Double(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                G2.draw(tick);

                tickCounter15 = 0;
                tickCounter90 += 15;
            }

            // Different tickmark every 90 units plus text
            if (tickCounter90 == 90)
            {
                G2.setStroke(MEDIUM_STROKE);
                innerPoint = new java.awt.geom.Point2D.Double(CENTER.getX() + (RADIUS - MAX_LENGTH) * sinValue, CENTER.getY() + (RADIUS - MAX_LENGTH) * cosValue);
                outerPoint = new java.awt.geom.Point2D.Double(CENTER.getX() + RADIUS * sinValue, CENTER.getY() + RADIUS * cosValue);

                // Draw ticks
                tick = new java.awt.geom.Line2D.Double(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                G2.draw(tick);

                tickCounter90 = 0;
            }

            // Draw text
            G2.setFont(BIG_FONT);
            G2.setColor(TEXT_COLOR);

            textPoint = new java.awt.geom.Point2D.Double(CENTER.getX() + (RADIUS - TEXT_DISTANCE) * sinValue, CENTER.getY() + (RADIUS - TEXT_DISTANCE) * cosValue);
            switch(counter)
            {
                case 360:
                    G2.setFont(BIG_FONT);
                    G2.fill(UTIL.rotateTextAroundCenter(G2, "S", (int) textPoint.getX(), (int) textPoint.getY(), Math.toDegrees(Math.PI - alpha)));
                    break;
                case 45:
                    G2.setFont(SMALL_FONT);
                    G2.fill(UTIL.rotateTextAroundCenter(G2, "SW", (int) textPoint.getX(), (int) textPoint.getY(), Math.toDegrees(Math.PI - alpha)));
                    break;
                case 90:
                    G2.setFont(BIG_FONT);
                    G2.fill(UTIL.rotateTextAroundCenter(G2, "W", (int) textPoint.getX(), (int) textPoint.getY(), Math.toDegrees(Math.PI - alpha)));
                    break;
                case 135:
                    G2.setFont(SMALL_FONT);
                    G2.fill(UTIL.rotateTextAroundCenter(G2, "NW", (int) textPoint.getX(), (int) textPoint.getY(), Math.toDegrees(Math.PI - alpha)));
                    break;
                case 180:                    
                    G2.setFont(BIG_FONT);
                    G2.fill(UTIL.rotateTextAroundCenter(G2, "N", (int) textPoint.getX(), (int) textPoint.getY(), Math.toDegrees(Math.PI - alpha)));
                    break;
                case 225:
                    G2.setFont(SMALL_FONT);
                    G2.fill(UTIL.rotateTextAroundCenter(G2, "NE", (int) textPoint.getX(), (int) textPoint.getY(), Math.toDegrees(Math.PI - alpha)));
                    break;
                case 270:
                    G2.setFont(BIG_FONT);
                    G2.fill(UTIL.rotateTextAroundCenter(G2, "E", (int) textPoint.getX(), (int) textPoint.getY(), Math.toDegrees(Math.PI - alpha)));
                    break;
                case 315:
                    G2.setFont(SMALL_FONT);
                    G2.fill(UTIL.rotateTextAroundCenter(G2, "SE", (int) textPoint.getX(), (int) textPoint.getY(), Math.toDegrees(Math.PI - alpha)));
                    break;
            }
            G2.setTransform(FORMER_TRANSFORM);
            
            tickCounter5++;
            tickCounter15++;

            counter ++;
        }

        // Restore former transformation
        G2.setTransform(FORMER_TRANSFORM);
    }

    @Override
    protected java.awt.image.BufferedImage create_POINTER_Image(final int WIDTH)
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

        final java.awt.geom.GeneralPath POINTER_RED = new java.awt.geom.GeneralPath();
        POINTER_RED.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        POINTER_RED.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER_RED.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.4158878504672897, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.40186915887850466);
        POINTER_RED.curveTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.38317757009345793, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1308411214953271, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1308411214953271);
        POINTER_RED.curveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1308411214953271, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.38317757009345793, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.397196261682243);
        POINTER_RED.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.4158878504672897, IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER_RED.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5);
        POINTER_RED.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5327102803738317);
        POINTER_RED.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5);
        POINTER_RED.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER_RED.closePath();
        final java.awt.geom.Point2D POINTER_RED_START = new java.awt.geom.Point2D.Double(0, POINTER_RED.getBounds2D().getMinY() );
        final java.awt.geom.Point2D POINTER_RED_STOP = new java.awt.geom.Point2D.Double(0, POINTER_RED.getBounds2D().getMaxY() );
        final float[] POINTER_RED_FRACTIONS =
        {
            0.0f,
            0.3f,
            0.59f,
            1.0f
        };
        final java.awt.Color[] POINTER_RED_COLORS =
        {
            new java.awt.Color(82, 0, 0, 255),
            new java.awt.Color(252, 29, 0, 255),
            new java.awt.Color(252, 29, 0, 255),
            new java.awt.Color(82, 0, 0, 255)
        };
        final java.awt.LinearGradientPaint POINTER_RED_GRADIENT = new java.awt.LinearGradientPaint(POINTER_RED_START, POINTER_RED_STOP, POINTER_RED_FRACTIONS, POINTER_RED_COLORS);
        G2.setPaint(POINTER_RED_GRADIENT);
        G2.fill(POINTER_RED);

        final java.awt.Color STROKE_COLOR_POINTER_RED = new java.awt.Color(0xFC1D00);
        G2.setColor(STROKE_COLOR_POINTER_RED);
        G2.setStroke(new java.awt.BasicStroke(1.0f, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_MITER));
        G2.draw(POINTER_RED);

        final java.awt.geom.GeneralPath POINTER_WHITE = new java.awt.geom.GeneralPath();
        POINTER_WHITE.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        POINTER_WHITE.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5280373831775701);
        POINTER_WHITE.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.5420560747663551, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.5841121495327103, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.602803738317757);
        POINTER_WHITE.curveTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.616822429906542, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.8691588785046729, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.8691588785046729);
        POINTER_WHITE.curveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.8691588785046729, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.616822429906542, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.602803738317757);
        POINTER_WHITE.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5841121495327103, IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.5420560747663551, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.5280373831775701);
        POINTER_WHITE.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5093457943925234, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5);
        POINTER_WHITE.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4672897196261682, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.4672897196261682);
        POINTER_WHITE.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4672897196261682, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5);
        POINTER_WHITE.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5093457943925234, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5280373831775701);
        POINTER_WHITE.closePath();
        final java.awt.geom.Point2D POINTER_WHITE_START = new java.awt.geom.Point2D.Double(0, POINTER_WHITE.getBounds2D().getMaxY() );
        final java.awt.geom.Point2D POINTER_WHITE_STOP = new java.awt.geom.Point2D.Double(0, POINTER_WHITE.getBounds2D().getMinY() );
        final float[] POINTER_WHITE_FRACTIONS =
        {
            0.0f,
            0.3f,
            0.59f,
            1.0f
        };
        final java.awt.Color[] POINTER_WHITE_COLORS =
        {
            new java.awt.Color(82, 82, 82, 255),
            new java.awt.Color(255, 255, 255, 255),
            new java.awt.Color(255, 255, 255, 255),
            new java.awt.Color(82, 82, 82, 255)
        };        
        final java.awt.LinearGradientPaint POINTER_WHITE_GRADIENT = new java.awt.LinearGradientPaint(POINTER_WHITE_START, POINTER_WHITE_STOP, POINTER_WHITE_FRACTIONS, POINTER_WHITE_COLORS);
        G2.setPaint(POINTER_WHITE_GRADIENT);
        G2.fill(POINTER_WHITE);
                
        G2.setColor(java.awt.Color.WHITE);
        G2.setStroke(new java.awt.BasicStroke(1.0f, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_MITER));
        G2.draw(POINTER_WHITE);

        G2.dispose();

        return IMAGE;
    }

    @Override
    protected java.awt.image.BufferedImage create_POINTER_SHADOW_Image(final int WIDTH)
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

        final java.awt.geom.GeneralPath POINTER_RED = new java.awt.geom.GeneralPath();
        POINTER_RED.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        POINTER_RED.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER_RED.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.4158878504672897, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.40186915887850466);
        POINTER_RED.curveTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.38317757009345793, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1308411214953271, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1308411214953271);
        POINTER_RED.curveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.1308411214953271, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.38317757009345793, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.397196261682243);
        POINTER_RED.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.4158878504672897, IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.45794392523364486, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER_RED.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5);
        POINTER_RED.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.5327102803738317);
        POINTER_RED.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5327102803738317, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5);
        POINTER_RED.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.49065420560747663, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4719626168224299);
        POINTER_RED.closePath();

        final java.awt.geom.GeneralPath POINTER_WHITE = new java.awt.geom.GeneralPath();
        POINTER_WHITE.setWindingRule(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        POINTER_WHITE.moveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5280373831775701);
        POINTER_WHITE.curveTo(IMAGE_WIDTH * 0.514018691588785, IMAGE_HEIGHT * 0.5420560747663551, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.5841121495327103, IMAGE_WIDTH * 0.5093457943925234, IMAGE_HEIGHT * 0.602803738317757);
        POINTER_WHITE.curveTo(IMAGE_WIDTH * 0.5046728971962616, IMAGE_HEIGHT * 0.616822429906542, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.8691588785046729, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.8691588785046729);
        POINTER_WHITE.curveTo(IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.8691588785046729, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.616822429906542, IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.602803738317757);
        POINTER_WHITE.curveTo(IMAGE_WIDTH * 0.49065420560747663, IMAGE_HEIGHT * 0.5841121495327103, IMAGE_WIDTH * 0.48598130841121495, IMAGE_HEIGHT * 0.5420560747663551, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.5280373831775701);
        POINTER_WHITE.curveTo(IMAGE_WIDTH * 0.4719626168224299, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5093457943925234, IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.5);
        POINTER_WHITE.curveTo(IMAGE_WIDTH * 0.4672897196261682, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.48130841121495327, IMAGE_HEIGHT * 0.4672897196261682, IMAGE_WIDTH * 0.5, IMAGE_HEIGHT * 0.4672897196261682);
        POINTER_WHITE.curveTo(IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.4672897196261682, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.48130841121495327, IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5);
        POINTER_WHITE.curveTo(IMAGE_WIDTH * 0.5327102803738317, IMAGE_HEIGHT * 0.5093457943925234, IMAGE_WIDTH * 0.5280373831775701, IMAGE_HEIGHT * 0.5186915887850467, IMAGE_WIDTH * 0.5186915887850467, IMAGE_HEIGHT * 0.5280373831775701);
        POINTER_WHITE.closePath();

        final java.awt.Color SHADOW_COLOR = new java.awt.Color(0.0f, 0.0f, 0.0f, 0.65f);
        G2.setColor(SHADOW_COLOR);
        G2.fill(POINTER_RED);
        G2.fill(POINTER_WHITE);

        G2.dispose();

        return IMAGE;
    }
    
    @Override
    public String toString()
    {
        return "Compass";
    }
}
