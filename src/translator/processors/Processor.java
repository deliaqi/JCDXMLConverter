package translator.processors;

import translator.Environment;
import translator.ParsedElement;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;

public abstract class Processor <E extends Environment> {
    
    private ParsedElement element;
    protected ShapeBuilderConfiguration resultingConfiguration;
    protected E environment;
    
    public Processor() {
    }
        
    protected abstract void configure();
    protected abstract void validate();
    protected abstract void process();
    protected abstract void cleanup();
    
    public ParsedElement getElement() {
        return element;
    }

    public void setElement(ParsedElement element) {
        this.element = element;
        environment = (E) element.getEnvironment();
    }

    public ShapeBuilderConfiguration getResultingConfiguration() {
        configure();
        validate();
        process();
        ShapeBuilderConfiguration result = resultingConfiguration;
        resultingConfiguration = null;
        cleanup();
        environment = null;
        return result;
    }
    
    protected void setResultingConfiguration(ShapeBuilderConfiguration resultingConfiguration) {
        this.resultingConfiguration = resultingConfiguration;
    }

    protected E getEnvironment() {
        return environment;
    }
    
}
