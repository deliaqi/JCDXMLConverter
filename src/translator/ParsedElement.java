package translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

/**
 * Represents a unit of information from a Source, stored in a ParsedStructure.
 * This element must be designed in source type support implementations,
 * such that it can be translated into a single shape by a Processor.
 * It can contain child ParsedElements inside.
 */
public class ParsedElement extends ParsedObject<ParsedElement> {
    
    /**
     * The element’s name.
     */
    private String name;
    
    /**
     * The element’s value.
     */
    private String value;
    
    public ParsedElement() {
    }

    /**
     *Returns the value of name attribute.
     */
    public String getName() {
        return name;
    }

    /**
     *This method add a value in the name attribute.
     *@param name represents element name.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     *Returns elements using as criterion the element name.
     *@param name this is the criterion used in the search.
     */
    public List<ParsedElement> getElements(String name){
        List<ParsedElement> elements = new ArrayList();
        
        for(ParsedElement element : getElements()){
            if(element.getName().equals(name)){
                elements.add(element);
            }
        }
        
        return elements;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ParsedElement copy(){
        ParsedElement element = new ParsedElement();
        
        element.id = this.id; 
        element.name = this.name;
        element.value = this.value;
        
        if(elements.size() > 0){
            for(ParsedElement subElement : elements.values()){
                element.addElement(subElement.copy());
            }
        }
        
        element.attributes.putAll(this.attributes);
        element.attributeKeys.addAll(this.attributeKeys);
        
        element.environment = this.environment;
        
        return element;
    }
}
