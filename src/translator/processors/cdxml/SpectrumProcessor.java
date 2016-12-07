package translator.processors.cdxml;

import java.awt.font.TextLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.graphics.Color;
import translator.graphics.Font;
import translator.graphics.StyleElement;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.CompositeTextConfiguration;
import translator.graphics.shapes.builders.configurations.RectangleConfiguration;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.graphics.shapes.builders.configurations.TextConfiguration;
import translator.utils.Point;

public class SpectrumProcessor extends CDXMLProcessor {
    
    //Taken from C++ code
    private static final double BOUNDING_RECTANGLE_PERCENT = 0.05;
    
    private static final double GRAPHIC_STROKE_WIDTH = 0.5;
    
    private List<Double> yCoordinates = new ArrayList();
    
    private Point beginPoint;
    private Point endPoint;
    
    private double xPhysicalLow;
    private double xPhysicalHigh;
    private double yPhysicalLow;
    private double yPhysicalHigh;
    
    private double xLow;
    private double xHigh;
    private double yLow;
    private double yHigh;
    
    private double xLength;
    private double yLength;
    
    private double xSpacing;
    private double xRatio;
    private double yRatio;
    
    private double deltaY;
    
    //Taken from C++ code
    private static final double MIN_TICK_SPACING = 1500;
    private static final int MIN_TICK_COUNT = 2;
    
    private double first;
    private double delta;
    private int decimals;
    private double deltaLong;
    
    private String xLabel;
    private String yLabel;
    
    private String xType;
    private String yType;
    
    private Font labelFont;
    private double fontHeight;
    
    private String spectrumClass;
    
    public SpectrumProcessor() {
    }
    
    protected void process() {
        Collection<ShapeBuilderConfiguration> configurations = new ArrayList();
        List<SegmentConfiguration> segments = new ArrayList();
        
        Color color = convertColor(getElement().getElements(ParseElementDefinition.COLOR).get(0));
        
        xLow = Double.parseDouble(getElement().getAttribute(ParseElementDefinition.SPECTRUM_X_LOW));
        
        int numStick = 0;
        int numNonStick = 0;
        for (int i = 1; i < yCoordinates.size() - 1; ++i) {
            if (yCoordinates.get(i) != 0.0 && yCoordinates.get(i - 1) == 0){
                if (yCoordinates.get(i + 1) == 0.0)
                    ++numStick;
                else {
                    while (i < yCoordinates.size() - 1 && yCoordinates.get(i + 1) != 0.0)
                        ++i;
                    ++numNonStick;
                }
            }
        }
        // Allow a non-stick region as long as there are other sticks
        boolean looksLikeStickSpectrum = numStick > 0 && numNonStick <= 1;
        
        //Find the highest and lowest y-coordinate
        double x = xLow;
        for(double y : yCoordinates){
            if(y < yLow){
                yLow = y;
            }
            
            if(y > yHigh){
                yHigh = y;
            }
            
            x += xSpacing;
        }
        
        xHigh = x;
        
        //Taken from C++ code
        if(xHigh - xLow != 0){
            xRatio = (xPhysicalHigh  - xPhysicalLow) / (xHigh - xLow);
        }
        
        if(yHigh - yLow != 0){
            yRatio = (yPhysicalHigh - yPhysicalLow) / (yHigh - yLow);
        }
        
        double controlOffset = 0;
        if(yLow < 0){
            controlOffset = yLow;
        }
        
        Point lastPoint = null;
        x = xLow;
        double yPhysical = yLogicalToyPhysical(yHigh);
        for(double y : yCoordinates){
            Point newPoint = new Point(xLogicalToxPhysical(x), yLogicalToyPhysical(yHigh - y + controlOffset));
            
            if(lastPoint != null){
                if(looksLikeStickSpectrum){
                    SegmentConfiguration curveMove = new SegmentConfiguration(
                            new Point(lastPoint.getX(), yPhysical),
                            new Point(newPoint.getX(), yPhysical));
                    
                    segments.add(curveMove);
                    
                    SegmentConfiguration curveSegment = new SegmentConfiguration(
                            new Point(newPoint.getX(), yPhysical), newPoint);
                    
                    segments.add(curveSegment);
                    
                    SegmentConfiguration curveSegmentReturn = new SegmentConfiguration(
                            newPoint, new Point(newPoint.getX(), yPhysical));
                    
                    segments.add(curveSegmentReturn);
                } else {
                    SegmentConfiguration curveSegment = new SegmentConfiguration(
                            lastPoint, newPoint);
                    
                    segments.add(curveSegment);
                }
            } else {
                SegmentConfiguration curveSegment = new SegmentConfiguration(
                        new Point(newPoint.getX(), yPhysical), newPoint);
                
                segments.add(curveSegment);
            }
            
            lastPoint = newPoint;
            x += xSpacing;
        }
        
        SplineConfiguration curve = new SplineConfiguration(segments);
        curve.setFill(false);
        curve.setStrokeWidth(GRAPHIC_STROKE_WIDTH);
        
        configurations.add(curve);
        
        deltaY = Math.abs(yLow - yHigh);
        
        RectangleConfiguration rectangle =
                new RectangleConfiguration(
                new Point(xPhysicalLow, yLogicalToyPhysical(yLow - BOUNDING_RECTANGLE_PERCENT * deltaY)),
                new Point(xPhysicalHigh, yLogicalToyPhysical(yHigh + BOUNDING_RECTANGLE_PERCENT * deltaY)));
        rectangle.setStrokeWidth(1);
        rectangle.setColor(color);
        
        configurations.add(rectangle);
        
        double xTick;
        double last;
        double yTick;
        
        //Draw x axis ticks and labels
        calculateXTicks();
        // Draw and annotate the ticks one by one.
        last = Math.max(xLow, xHigh);
        yTick = yLogicalToyPhysical(yHigh + BOUNDING_RECTANGLE_PERCENT * deltaY);
        for (xTick = first; xTick < last; xTick += delta) {
            // Move to the top of the tick.
            Point beginTickPoint = new Point(xLogicalToxPhysical(xTick), yTick);
            
            // Draw to the bottom and annotate it if it's a long tick.
            Point endTickPoint;
            if (xTick % deltaLong < 0.1 * deltaLong) {
                endTickPoint = new Point(xLogicalToxPhysical(xTick), yTick + getXTickLengthLong() / 4);
                
                CompositeTextConfiguration labelConfigurationText = new CompositeTextConfiguration();
                
                TextConfiguration labelConfiguration =
                        new TextConfiguration(xLogicalToxPhysical(xTick),
                        yLogicalToyPhysical(yHigh + BOUNDING_RECTANGLE_PERCENT * deltaY) + getXTickLengthShort(), 1);
                labelConfiguration.addPart(Integer.toString((int)xTick), labelFont, color);
                
                labelConfigurationText.setJustification(TextConfiguration.CENTER_JUSTIFICATION);
                
                labelConfigurationText.addLine(labelConfiguration);
                
                configurations.add(labelConfigurationText);
            } else {
                endTickPoint = new Point(xLogicalToxPhysical(xTick),
                        yTick + getXTickLengthShort() / 4);
            }
            
            SegmentConfiguration tickConfiguration = new SegmentConfiguration(
                    beginTickPoint, endTickPoint);
            tickConfiguration.setStrokeWidth(1);
            
            configurations.add(tickConfiguration);
        }
        
        if(xLabel != null){
            CompositeTextConfiguration labelConfigurationText = new CompositeTextConfiguration();
            
            TextConfiguration labelConfiguration =
                    new TextConfiguration(xLogicalToxPhysical((xHigh - xLow) / 2 + xLow),
                    yTick + getXTickLengthLong() / 4 + getXTickGap() * 2 + fontHeight, 1);
            labelConfiguration.addPart(xLabel, labelFont, color);
            
            labelConfigurationText.setJustification(TextConfiguration.CENTER_JUSTIFICATION);
            
            labelConfigurationText.addLine(labelConfiguration);
            
            configurations.add(labelConfigurationText);
        }
        
        if(yType != null && !yType.equals(ParseElementDefinition.SPECTRUM_UNKNOWN_TYPE)){
            //Draw y axis ticks and labels
            calculateYTicks();
            
            last = Math.max(yLow, yHigh);
            xTick = xLogicalToxPhysical(xHigh);
            for (yTick = first; yTick <= last; yTick += delta) {
                Point beginTickPoint = new Point(xTick, yLogicalToyPhysical(yTick));
                
                Point endTickPoint;
                if(yTick % deltaLong < 0.1 * deltaLong) {
                    endTickPoint = new Point(xTick - getYTickLengthLong() / 4, yLogicalToyPhysical(yTick));
                    
                    CompositeTextConfiguration labelConfigurationText = new CompositeTextConfiguration();
                    
                    TextConfiguration labelConfiguration =
                            new TextConfiguration(xTick - getYTickLengthLong() / 4,
                            yLogicalToyPhysical(yTick) + fontHeight / 2, 1);
                    labelConfiguration.addPart(Integer.toString((int)(last - yTick)), labelFont, color);
                    
                    labelConfigurationText.setJustification(TextConfiguration.RIGHT_JUSTIFICATION);
                    
                    labelConfigurationText.addLine(labelConfiguration);
                    
                    configurations.add(labelConfigurationText);
                } else {
                    endTickPoint = new Point(xTick - getYTickLengthShort() / 4, yLogicalToyPhysical(yTick));
                }
                
                SegmentConfiguration tickConfiguration = new SegmentConfiguration(
                        beginTickPoint, endTickPoint);
                tickConfiguration.setStrokeWidth(1);
                
                configurations.add(tickConfiguration);
            }
        }
        
        CompositeShapeConfiguration resultinConfiguration =
                new CompositeShapeConfiguration(ParseElementDefinition.SPECTRUM, configurations);
        resultinConfiguration.setColor(color);
        resultinConfiguration.setZOrder(zOrder);
        
        setResultingConfiguration(resultinConfiguration);
    }
    
    private double xLogicalToxPhysical(double xCoordinate){
       
        double result;
        
        if((spectrumClass.equals(ParseElementDefinition.SPECTRUM_NMR_CLASS) 
        && !xLabel.equals(ParseElementDefinition.SPECTRUM_X_TYPE_PARTS_PER_MILLION_LABEL)) 
        || spectrumClass.equals(ParseElementDefinition.SPECTRUM_X_TYPE_UVVIS_LABEL)){
            
            result = xPhysicalLow + ( xHigh - xCoordinate) * xRatio;
            
        }else{            
            
            result = xPhysicalLow + (xCoordinate - xLow) * xRatio;            
            
        }
        
        return result;
        
    }
    
    private double yLogicalToyPhysical(double yCoordinate){
        return yPhysicalLow + (yCoordinate - yLow) * yRatio;
    }
    
    private void calculateYTicks(){
        // Our strategy is to have as many tick labels as possible, as long as they are
        // more than MinTickSpacing apart.  But we will always have at least MinTickCount labels.
        // And always, we'll follow the 1-2-5 rule: all tick positions are of the
        // form (1, 2, or 5) * (some power of ten).
        // Remember that long ticks are labeled, but small ticks aren't.
        
        // Compute the largest possible number of labels.
        double rangePhys = Math.abs(yPhysicalLow - yPhysicalHigh);
        int numLabelsMax = (int) Math.floor(rangePhys/MIN_TICK_SPACING);
        
        if (numLabelsMax < MIN_TICK_COUNT){
            numLabelsMax = MIN_TICK_COUNT;
        }
        
        // Compute the increment between ticks for this number of ticks, and then
        // get the next smaller 125 number than that.
        int presicion;
        double rangeLog = Math.abs(yLow - yHigh);
        if (numLabelsMax < MIN_TICK_COUNT) {
            numLabelsMax = MIN_TICK_COUNT;
            deltaLong = rangeLog / numLabelsMax;
            double[] presicionAndNearest = nearestSmaller125(deltaLong);
            presicion = (int) presicionAndNearest[1];
            deltaLong = presicionAndNearest[0] * 2.0;
            presicionAndNearest = nearestSmaller125(deltaLong);
            presicion = (int) presicionAndNearest[1];
            deltaLong = presicionAndNearest[0];
        } else {
            deltaLong = rangeLog / numLabelsMax;
            double[] presicionAndNearest = nearestSmaller125(deltaLong);
            presicion = (int) presicionAndNearest[1];
            deltaLong = presicionAndNearest[0];
        }
        
        first = deltaLong * Math.ceil(yLow / deltaLong);
        
        // To compute the number of decimal points, we note that the increment between tick is
        // always a single digit (1, 2, or 5) time some power of 10.  So we need just enough
        // decimal places to show that single digit. (If the increment is >1, then we show no
        // decimals or decimal point, and return decimals as 0.)
        // SDR 9/3/98: Never show more than 10 decimal places in any event.  Bogus data was sometimes causing ridiculous labels.
        decimals = (int) Math.floor(Math.log10(deltaLong));
        if (decimals < 0)
            decimals = Math.min(10, -decimals);
        else
            decimals=0;
        
        // The long ticks are those whose index modulo modulusLong equals residualLong.
        // In general, we want one more level of short ticks than long ones.  Like the
        // long ticks, the short ones also follow the 1-2-5 rule, but we also require that
        // they be integer sub-multiples of delta (so that every long tick is also on the short tick grid.)
        switch (get125Number(deltaLong)) {
            case 1:{
                delta = deltaLong / 2.0;
                break;
            }
            case 2:{
                delta = deltaLong / 2.0;
                break;
            }
            case 5:{
                delta = deltaLong / 5.0;
                break;
            }
        }
        
        // Recompute first based on the small tick interval.
        first = delta * Math.ceil(yLow / delta);
    }
    
    private void calculateXTicks(){
        // Our strategy is to have as many tick labels as possible, as long as they are
        // more than MinTickSpacing apart.  But we will always have at least MinTickCount labels.
        // And always, we'll follow the 1-2-5 rule: all tick positions are of the
        // form (1, 2, or 5) * (some power of ten).
        // Remember that long ticks are labeled, but small ticks aren't.
        
        // Compute the largest possible number of labels.
        double delta1 = getYAxisThickness();
        double delta2 = Math.abs(xPhysicalLow * 20 - xPhysicalHigh * 20) - delta1 * 2;
        
        double rangePhys = Math.abs(delta1 - delta2);
        int numLabelsMax = (int) Math.floor(rangePhys/MIN_TICK_SPACING);
        
        if (numLabelsMax < MIN_TICK_COUNT){
            numLabelsMax = MIN_TICK_COUNT;
        }
        
        // Compute the increment between ticks for this number of ticks, and then
        // get the next smaller 125 number than that.
        int presicion;
        double rangeLog = Math.abs(xLow - xHigh);
        if (numLabelsMax < MIN_TICK_COUNT) {
            numLabelsMax = MIN_TICK_COUNT;
            deltaLong = rangeLog / numLabelsMax;
            double[] presicionAndNearest = nearestSmaller125(deltaLong);
            presicion = (int) presicionAndNearest[1];
            deltaLong = presicionAndNearest[0] * 2.0;
            presicionAndNearest = nearestSmaller125(deltaLong);
            presicion = (int) presicionAndNearest[1];
            deltaLong = presicionAndNearest[0];
        } else {
            deltaLong = rangeLog / numLabelsMax;
            double[] presicionAndNearest = nearestSmaller125(deltaLong);
            presicion = (int) presicionAndNearest[1];
            deltaLong = presicionAndNearest[0];
        }
        
        first = deltaLong * Math.ceil(xLow / deltaLong);
        
        // To compute the number of decimal points, we note that the increment between tick is
        // always a single digit (1, 2, or 5) time some power of 10.  So we need just enough
        // decimal places to show that single digit. (If the increment is >1, then we show no
        // decimals or decimal point, and return decimals as 0.)
        // SDR 9/3/98: Never show more than 10 decimal places in any event.  Bogus data was sometimes causing ridiculous labels.
        decimals = (int) Math.floor(Math.log10(deltaLong));
        if (decimals < 0)
            decimals = Math.min(10, -decimals);
        else
            decimals=0;
        
        // The long ticks are those whose index modulo modulusLong equals residualLong.
        // In general, we want one more level of short ticks than long ones.  Like the
        // long ticks, the short ones also follow the 1-2-5 rule, but we also require that
        // they be integer sub-multiples of delta (so that every long tick is also on the short tick grid.)
        switch (get125Number(deltaLong)) {
            case 1:{
                delta = deltaLong / 2.0;
                break;
            }
            case 2:{
                delta = deltaLong / 2.0;
                break;
            }
            case 5:{
                delta = deltaLong / 5.0;
                break;
            }
        }
        
        // Recompute first based on the small tick interval.
        first = delta * Math.ceil(xLow / delta);
    }
    
    //Get nearest 125 number.
    //Given a number, find the nearest 1-2-5 number, return a precision indicator,
    //and copy the number into outputNumber.
    //
    //Precision indicators,
    //
    //		 xx.   =2
    //		  x.   =1
    //		  0.   =0
    //		   .x  =-1
    //		   .0x =-2
    //
    //The nearest (1-2-5) number in first element of result value,
    //and a precision indicator as the-
    //second element of result value.  The precision
    //indicator return (+num digits to the left) (or -num digits to the
    //right of the decimal place) or (0)
    private double[] nearestSmaller125(double inputNumber) {
        double[] result = new double[2];
        double powerOfTen;
        int mag, sign;
        
        
        // We deal henceforth with positive numbers.
        sign = 1;
        if (inputNumber < 0) {
            inputNumber = -inputNumber;
            sign = -1;
        }
        
        // Handle 0 as a special case.
        if (inputNumber == 0.0) {
            result[0] = 0;
            result[1] = 0;
            return result;
        }
        
        // Work out the order of magnitude of the input number.  I.e., inputnumber >= 10**mag
        mag = (int)(0.1 + Math.floor(Math.log10(inputNumber)));
        
        // We now know that the input is somewhere in the range
        // 1*10**mag  --  2*10**mag -- 5*10**mag  -- 10*10**mag
        // Figure out which of those four numbers is just below inputNumber.
        powerOfTen = Math.pow(10.0, mag);
        
        if (inputNumber < 2.0 * powerOfTen)
            result[0] = powerOfTen;
        else if (inputNumber < 5.0 * powerOfTen)
            result[0] = 2.0 * powerOfTen;
        else if (inputNumber < 10.0 * powerOfTen)
            result[0] = 5.0 * powerOfTen;
        else
            result[0] = 10.0 * powerOfTen;
        
        result[1] = mag * sign;
        return result;
    }
    
    private int get125Number(double delta) {
        int num = (int)(0.5 + delta / Math.pow(10.0, Math.floor(Math.log10(delta))));
        
        return num;
    }
    
    //Taken from C++ code
    private double getXTickLengthLong() {
        if (xLength < 400.0) {
            return 25.0;
        } else if (xLength < 4000.0){
            return 15.0 + xLength / 40.0;
        }
        
        return 115.0;
    }
    
    //Taken from C++ code
    private double getXTickLengthShort() {
        return 0.6 * getXTickLengthLong();
    }
    
    //Taken from C++ code
    private double getXTickGap() {
        return 0.2 * getXTickLengthLong();
    }
    
    //Taken from C++ code
    private double getYTickLengthLong() {
        if (yLength < 400.0) {
            return 25.0;
        } else if (yLength < 4000.0){
            return 15.0 + yLength / 40.0;
        }
        
        return 115.0;
    }
    
    //Taken from C++ code
    private double getYTickLengthShort() {
        return 0.6 * getXTickLengthLong();
    }
    
    //Taken from C++ code
    private double getYTickGap() {
        return 0.2 * getXTickLengthLong();
    }
    
    // Compute the amount of space to allow for ticks, tick labels and axis labels
    double getYAxisThickness() {
        double axisFullThickness;
        
        // It's a y-axis
        double windowWidth = xPhysicalHigh * 20 - xPhysicalLow * 20;
        if (windowWidth < 750)
            axisFullThickness = 100;
        else if (windowWidth < 1500)
            axisFullThickness = 200;
        else if (windowWidth < 3000)
            axisFullThickness = 450;
        else
            axisFullThickness = 800;
        
        return axisFullThickness;
    }
    
    protected void configure() {
        super.configure();
        
        labelFont = new Font();
        labelFont.setName(getEnvironment().getLabelFont());
        labelFont.setSize(Double.toString(getEnvironment().getLabelFontSize()));
        labelFont.setStyle(new StyleElement(getEnvironment().getLabelFace()));
        
        TextLayout textLayout = getEnvironment().createTextLayout(
                "A", 0, 0, labelFont.getName(), java.awt.Font.PLAIN, Float.parseFloat(labelFont.getSize()), 0);
        
        fontHeight = textLayout.getBounds().getHeight();
        
        String value = getElement().getValue();
        value = value.replaceAll("\n", " ");
        String[] stringCoordinates = value.split(" ");
        
        for(String coordinate : stringCoordinates){
            if(coordinate.indexOf("e") >= 0){
                coordinate = coordinate.replaceAll("e", "E");
            }
            if(!coordinate.equals("")){
                yCoordinates.add(Double.parseDouble(coordinate));
            }
        }
        
        spectrumClass = getElement().getAttribute(ParseElementDefinition.SPECTRUM_CLASS);
        
        List<Point> boundingBox = parsePoints(
                getElement().getAttribute(ParseElementDefinition.SPECTRUM_BOUNDING_BOX), getElement());
        beginPoint = boundingBox.get(0);
        endPoint = boundingBox.get(1);
        
        xLow = Double.parseDouble(getElement().getAttribute(
                ParseElementDefinition.SPECTRUM_X_LOW));
        xSpacing = Double.parseDouble(getElement().getAttribute(
                ParseElementDefinition.SPECTRUM_X_SPACING));
        
        xType = getElement().getAttribute(ParseElementDefinition.SPECTRUM_X_TYPE);
        yType = getElement().getAttribute(ParseElementDefinition.SPECTRUM_Y_TYPE);
        
        xPhysicalLow = endPoint.getX();
        xPhysicalHigh = beginPoint.getX();
        yPhysicalLow = beginPoint.getY();
        yPhysicalHigh = endPoint.getY();
        
        xLow = Double.MAX_VALUE;
        xHigh = 0;
        yLow = Double.MAX_VALUE;
        yHigh = 0;
        
        xRatio = 0;
        yRatio = 0;
        
        xLength = Math.abs(xPhysicalHigh - xPhysicalLow);
        yLength = Math.abs(yPhysicalHigh - yPhysicalLow) / 20;
        
        if(getElement().hasAttribute(ParseElementDefinition.SPECTRUM_X_AXIS_LABEL)){
            xLabel = getElement().getAttribute(
                    ParseElementDefinition.SPECTRUM_X_AXIS_LABEL);
        }else if(getElement().hasAttribute(ParseElementDefinition.SPECTRUM_X_TYPE)){
            if(getElement().getAttribute(ParseElementDefinition.SPECTRUM_X_TYPE).equals(ParseElementDefinition.SPECTRUM_X_TYPE_PARTS_PER_MILLION)){
                xLabel = ParseElementDefinition.SPECTRUM_X_TYPE_PARTS_PER_MILLION_LABEL;
            }
        }
        
        if(getElement().hasAttribute(ParseElementDefinition.SPECTRUM_Y_AXIS_LABEL)){
            yLabel = getElement().getAttribute(
                    ParseElementDefinition.SPECTRUM_Y_AXIS_LABEL);
        }
        
        double xLowOffset = getYTickGap();
        double xHighOffset = getYTickGap();
        if(yType != null && !yType.equals(ParseElementDefinition.SPECTRUM_UNKNOWN_TYPE)){
            xLowOffset = getYTickGap();
            xHighOffset = getXTickLengthLong() + getXTickGap() + fontHeight;
        }
        
        xPhysicalLow -= xLowOffset;
        xPhysicalHigh += xHighOffset;
        
        double yLowOffet = getXTickLengthShort();
        double yHighOffset = getXTickLengthLong() + getXTickGap() + fontHeight;
        
        yPhysicalLow += yLowOffet;
        yPhysicalHigh -= yHighOffset;
    }
    
    protected void cleanup() {
        xPhysicalLow = 0;
        xPhysicalHigh = 0;
        yPhysicalLow = 0;
        yPhysicalHigh = 0;
        
        xLow = 0;
        xHigh = 0;
        yLow = 0;
        yHigh = 0;
        
        yCoordinates = new ArrayList();
        xLow = 0;
        xSpacing = 0;
        
        deltaY = 0;
        
        xLength = 0;
        yLength = 0;
        
        xLabel = null;
        yLabel = null;
        
        spectrumClass = null;
        
        super.cleanup();
    }
}
