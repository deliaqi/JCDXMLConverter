package translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

/**
 *Represents a Source's contents, in the form of a collection of
 *ParsedElements.
 */
public class ParsedStructure {
    
    private Hashtable<String, ParsedPage> elements = new Hashtable();
    private List<String> keys = new ArrayList();
    
    public ParsedStructure() {
    }
    
    /**
     *This method add an element (ParsedElement class) in the object, this work include two tasks,
     *add the element in the table and add an index for this element. If index already exists,
     *the linked value will be replaced. The key used is the element name.
     *@param element this is a instance of the ParsedElement that represents the element to add in the object.
     */
    public void addElement(ParsedPage element){
        if(elements.get(element.getId()) == null){
            keys.add(element.getId());
        }
        elements.put(element.getId(), element);
    }
    
    /**
     *Returns an element using as criterion the element name. If element is not found,
     *this method throws a TranslatorException.
     *@param name this is the criterion used in the search.
     */
    public ParsedObject getElement(String id) throws TranslatorException {
        ParsedObject element = elements.get(id);
        
        if(element == null){
            throw new TranslatorException("Element not found: \""+ id +"\"");
        }
        
        return element;
    }
    
    /**
     *Returns a collection with all the elements (ParsedElement class).
     */
    public Collection<ParsedPage> getElements(){
        return elements.values();
    }
}
