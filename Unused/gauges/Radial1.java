package eu.hansolo.steelseries.gauges;


/**
 *
 * @author hansolo
 */
public class Radial1 extends AbstractRadial
{    
    private final double FREE_AREA_ANGLE = Math.toRadians(0); // area where no tickmarks will be painted
    private double angleStep;
    private static final double TICKMARK_OFFSET = 0.5 * Math.PI;
    private final double ROTATION_OFFSET = (1.5 * Math.PI) + (FREE_AREA_ANGLE / 2.0); // Offset for the pointer
    private int tickLabelPeriod = 20; // Draw value at every nth tickmark
    private java.awt.image.BufferedImage backgroundImage;
    private java.awt.image.BufferedImage postsImage;
    private java.awt.image.BufferedImage trackImage;
    private java.awt.image.BufferedImage tickmarksImage;
    private java.awt.image.BufferedImage titleImage;
    private java.awt.image.BufferedImage pointerImage;
    private java.awt.image.BufferedImage pointerShadowImage;
    private java.awt.image.BufferedImage foregroundImage;
    private java.awt.image.BufferedImage thresholdImage;
    private java.awt.image.BufferedImage minMeasuredImage;
    private java.awt.image.BufferedImage maxMeasuredImage;
    private final java.awt.geom.Arc2D AREA = new java.awt.geom.Arc2D.Double(java.awt.geom.Arc2D.PIE);
    private final java.util.ArrayList<eu.hansolo.steelseries.tools.Section> SECTIONS = new java.util.ArrayList<eu.hansolo.steelseries.tools.Section>();
    

    public Radial1()
    {
        super();
        calcAngleStep();                
        addComponentListener(this);
        setSize(getPreferredSize());
        init(getWidth(), getWidth());
    }

    @Override
    public final AbstractGauge init(final int WIDTH, final int HEIGHT)
    {
        backgroundImage = create_BACKGROUND_Image(WIDTH);        
        postsImage = create_POSTS_Image(WIDTH, eu.hansolo.steelseries.tools.PostPosition.MAX_CENTER_TOP, eu.hansolo.steelseries.tools.PostPosition.MIN_LEFT);
        create_AREA();
        create_SECTIONS();
        trackImage = create_TRACK_Image(WIDTH, getMinValue(), getMaxValue(), angleStep, getTrackStart(), getTrackSection(), getTrackStop(), getTrackStartColor(), getTrackSectionColor(), getTrackStopColor(), ROTATION_OFFSET);
        tickmarksImage = create_TICKMARKS_Image(WIDTH, FREE_AREA_ANGLE, TICKMARK_OFFSET, getMinValue(), getMaxValue(), angleStep, tickLabelPeriod, getScaleDividerPower(), isDrawTicks(), isDrawTickLabels(), getTickmarkSections());
        thresholdImage = create_THRESHOLD_Image(WIDTH);
        minMeasuredImage = create_MEASURED_VALUE_Image(WIDTH, new java.awt.Color(0, 23, 252, 255));
        maxMeasuredImage = create_MEASURED_VALUE_Image(WIDTH, new java.awt.Color(252, 29, 0, 255));
        titleImage = create_TITLE_Image(WIDTH, getTitle(), getUnitString());
        pointerImage = create_POINTER_Image(WIDTH, getPointerType());
        pointerShadowImage = create_POINTER_SHADOW_Image(WIDTH, getPointerType());
        foregroundImage = create_FOREGROUND_Image(WIDTH);
        setCurrentLedImage(getLedImageOff());

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
        //G2.setRenderingHint(java.awt.RenderingHints.KEY_FRACTIONALMETRICS, java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        G2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        final java.awt.geom.AffineTransform OLD_TRANSFORM = G2.getTransform();

        // Draw the background
        G2.drawImage(backgroundImage, 0, 0, null);

        // Draw posts
        G2.drawImage(postsImage, 0, 0, null);
        
        // Draw the area
        if (isAreaVisible())
        {
            G2.setColor(getAreaColor());
            G2.fill(AREA);
        }

        // Draw the sections
        if (isSectionsVisible())
        {
            for (eu.hansolo.steelseries.tools.Section section : SECTIONS)
            {                
                G2.setColor(section.getColor());
                G2.fill(section.getArea());
            }
        }

        // Draw the track
        if (isTrackVisible())
        {
            G2.drawImage(trackImage, 0, 0, null);
        }
        
        // Draw the tickmarks
        G2.drawImage(tickmarksImage, 0, 0, null);

        // Draw threshold indicator
        if (isThresholdVisible())
        {
            G2.rotate(ROTATION_OFFSET + (getThreshold() - getMinValue()) * angleStep, CENTER.getX(), CENTER.getY());            
            G2.drawImage(thresholdImage, (int) (backgroundImage.getWidth() * 0.480369999), (int) (backgroundImage.getHeight() * 0.13), null);
            G2.setTransform(OLD_TRANSFORM);
        }

        // Draw min measured value indicator
        if (isMinMeasuredValueVisible())
        {
            G2.rotate(ROTATION_OFFSET + (getMinMeasuredValue() - getMinValue()) * angleStep, CENTER.getX(), CENTER.getY());
            G2.drawImage(minMeasuredImage, (int) (backgroundImage.getWidth() * 0.4865), (int) (backgroundImage.getHeight() * 0.105), null);
            G2.setTransform(OLD_TRANSFORM);
        }

        // Draw max measured value indicator
        if (isMaxMeasuredValueVisible())
        {
            G2.rotate(ROTATION_OFFSET + (getMaxMeasuredValue() - getMinValue()) * angleStep, CENTER.getX(), CENTER.getY());
            G2.drawImage(maxMeasuredImage, (int) (backgroundImage.getWidth() * 0.4865), (int) (backgroundImage.getHeight() * 0.105), null);
            G2.setTransform(OLD_TRANSFORM);
        }

        // Draw title and unit
        G2.drawImage(titleImage, 0, 0, null);

        // Draw LED if enabled
        if (isLedVisible())
        {
            G2.drawImage(getCurrentLedImage(), (int) (backgroundImage.getWidth() * getLedPositionX()), (int) (backgroundImage.getHeight() * getLedPositionY()), null);
        }

        // Draw the pointer
        final double ANGLE = ROTATION_OFFSET + (getValue() - getMinValue()) * angleStep;
        G2.rotate(ANGLE + (Math.cos(Math.toRadians(ANGLE - ROTATION_OFFSET - 91.5))), CENTER.getX(), CENTER.getY());
        G2.drawImage(pointerShadowImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);
        G2.rotate(ANGLE, CENTER.getX(), CENTER.getY());
        G2.drawImage(pointerImage, 0, 0, null);
        G2.setTransform(OLD_TRANSFORM);

        // Draw the foreground
        G2.drawImage(foregroundImage, 0, 0, null);
    }

    public int getTickLabelPeriod()
    {
        return this.tickLabelPeriod;
    }

    public void setTickLabelPeriod(final int TICK_LABEL_PERIOD)
    {
        this.tickLabelPeriod = TICK_LABEL_PERIOD;
        tickmarksImage = create_TICKMARKS_Image(getWidth(), FREE_AREA_ANGLE, TICKMARK_OFFSET, getMinValue(), getMaxValue(), angleStep, tickLabelPeriod, getScaleDividerPower());
        repaint();
    }

    @Override
    public void setScaleDividerPower(final int SCALE_DIVIDER_POWER)
    {
        super.setScaleDividerPower(SCALE_DIVIDER_POWER);
        tickmarksImage = create_TICKMARKS_Image(getWidth(), FREE_AREA_ANGLE, TICKMARK_OFFSET, getMinValue(), getMaxValue(), angleStep, tickLabelPeriod, getScaleDividerPower());
        repaint();
    }
    
    @Override
    protected final void calcAngleStep()
    {
        angleStep = (Math.PI / 2.0 - FREE_AREA_ANGLE) / (getMaxValue() - getMinValue());
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
       
    private void create_AREA()
    {
        final double ORIGIN_CORRECTION = 180;
        final double ANGLE_STEP = 90 / (getMaxValue() - getMinValue());
        final double ANGLE_START = ORIGIN_CORRECTION - (getAreaStart() * ANGLE_STEP);
        final double ANGLE_EXTEND = -(getAreaStop() - getAreaStart()) * ANGLE_STEP;

        if (backgroundImage != null)
        {
            final double OUTER_RADIUS = backgroundImage.getWidth() * 0.38f;            
            final double FREE_AREA = backgroundImage.getWidth() / 2.0 - OUTER_RADIUS;
            AREA.setFrame(backgroundImage.getMinX() + FREE_AREA, backgroundImage.getMinY() + FREE_AREA, 2 * OUTER_RADIUS, 2 * OUTER_RADIUS);
            AREA.setAngleStart(ANGLE_START);
            AREA.setAngleExtent(ANGLE_EXTEND);
        }
    }

    private void create_SECTIONS()
    {                
        if (!getSections().isEmpty() && backgroundImage != null)
        {
            SECTIONS.clear();
            SECTIONS.addAll(getSections());

            final double ORIGIN_CORRECTION = 180.0;
            final double ANGLE_STEP = 90.0 / (getMaxValue() - getMinValue());
            final double OUTER_RADIUS = backgroundImage.getWidth() * 0.26f;
            final double INNER_RADIUS = backgroundImage.getWidth() * 0.23f;
            final double FREE_AREA_OUTER_RADIUS = backgroundImage.getWidth() / 2.0 - OUTER_RADIUS;
            final double FREE_AREA_INNER_RADIUS = backgroundImage.getWidth() / 2.0 - INNER_RADIUS;
            final java.awt.geom.Ellipse2D INNER = new java.awt.geom.Ellipse2D.Double(backgroundImage.getMinX() + FREE_AREA_INNER_RADIUS, backgroundImage.getMinY() + FREE_AREA_INNER_RADIUS, 2 * INNER_RADIUS, 2 * INNER_RADIUS);

            for (eu.hansolo.steelseries.tools.Section section : SECTIONS)
            {                            
                final double ANGLE_START = ORIGIN_CORRECTION - (section.getStart() * ANGLE_STEP);
                final double ANGLE_EXTEND = -(section.getStop() - section.getStart()) * ANGLE_STEP;                
                                                                                                                
                final java.awt.geom.Arc2D OUTER_ARC = new java.awt.geom.Arc2D.Double(java.awt.geom.Arc2D.PIE);
                OUTER_ARC.setFrame(backgroundImage.getMinX() + FREE_AREA_OUTER_RADIUS, backgroundImage.getMinY() + FREE_AREA_OUTER_RADIUS, 2 * OUTER_RADIUS, 2 * OUTER_RADIUS);
                OUTER_ARC.setAngleStart(ANGLE_START);
                OUTER_ARC.setAngleExtent(ANGLE_EXTEND);
                final java.awt.geom.Area SECTION = new java.awt.geom.Area(OUTER_ARC);
                
                SECTION.subtract(new java.awt.geom.Area(INNER));
                
                section.setArea(SECTION);
            }
        }
    }
    
    @Override
    public String toString()
    {
        return "Radial1";
    }    
}