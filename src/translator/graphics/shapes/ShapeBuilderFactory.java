package translator.graphics.shapes;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import translator.BuilderConfiguration;
import translator.graphics.shapes.builders.ArcBuilder;
import translator.graphics.shapes.builders.CircleBuilder;
import translator.graphics.shapes.builders.CompositeShapeBuilder;
import translator.graphics.shapes.builders.CubicCurveBuilder;
import translator.graphics.shapes.builders.EllipseBuilder;
import translator.graphics.shapes.builders.ImageBuilder;
import translator.graphics.shapes.builders.QuadraticCurveBuilder;
import translator.graphics.shapes.builders.RectangleBuilder;
import translator.graphics.shapes.builders.SegmentBuilder;
import translator.graphics.shapes.builders.ShapeBuilder;
import translator.graphics.shapes.builders.SplineBuilder;
import translator.graphics.shapes.builders.TextBuilder;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;

public final class ShapeBuilderFactory {
    
    private static ShapeBuilderFactory instance;

    private Hashtable<String, String> builders = new Hashtable();
    
    static{
        instance = new ShapeBuilderFactory();
        
        instance.addBuilder(
                BuilderConfiguration.CIRCLE_BUILDER_ID, CircleBuilder.class);
        instance.addBuilder(
                BuilderConfiguration.ELLIPSE_BUILDER_ID, EllipseBuilder.class);
        instance.addBuilder(
                BuilderConfiguration.SEGMENT_BUILDER_ID, SegmentBuilder.class);
        instance.addBuilder(
                BuilderConfiguration.ARC_BUILDER_ID, ArcBuilder.class);
        instance.addBuilder(
                BuilderConfiguration.QUADRATIC_CURVE_BUILDER_ID, QuadraticCurveBuilder.class);
        instance.addBuilder(
                BuilderConfiguration.CUBIC_CURVE_BUILDER_ID, CubicCurveBuilder.class);
        instance.addBuilder(
                BuilderConfiguration.COMPOSITE_SHAPE_BUILDER_ID, CompositeShapeBuilder.class);
        instance.addBuilder(
                BuilderConfiguration.SPLINE_BUILDER_ID, SplineBuilder.class);
        instance.addBuilder(
                BuilderConfiguration.RECTANGLE_BUILDER_ID, RectangleBuilder.class);
        instance.addBuilder(
                BuilderConfiguration.TEXT_BUILDER_ID, TextBuilder.class);
        instance.addBuilder(
                BuilderConfiguration.IMAGE_BUILDER_ID, ImageBuilder.class);
    }
    
    private ShapeBuilderFactory() {
    }
    
    public static ShapeBuilderFactory getInstance() {
        return instance;
    }
    
    public ShapeBuilder getBuilder(ShapeBuilderConfiguration configuration) {
        ShapeBuilder suitableBuilder = null;
        try {
            Class builderClass = Class.forName(builders.get(configuration.getBuilderId()));

            Constructor builderConstructor = builderClass.getConstructor(ShapeBuilderConfiguration.class);
            
            suitableBuilder = (ShapeBuilder) builderConstructor.newInstance(configuration);
        } catch (SecurityException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        }
        
        return suitableBuilder;
    }
    
    public void addBuilder(String id, Class builder){
        builders.put(id, builder.getName());
    }
}
