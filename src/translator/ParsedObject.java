package translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;

public abstract class ParsedObject<P extends ParsedObject> implements Comparable<ParsedObject> {

    /**
     * The element’s id.
     */
    protected String id;
    
    /**
     * The element’s child ParsedElements.
     */
    
    protected LinkedHashMap<String, P> elements = new LinkedHashMap();
        
    /**
     * The element’s attributes, in a table mapping name to value.
     */
    protected Hashtable<String, String> attributes = new Hashtable();
    protected List<String> attributeKeys = new ArrayList();
    
    protected Environment environment;
    
    private int zOrder;
    private ParsedObject<P> owner; //if this parsedObject has an owner this must be set here (ej. the objectTag has an owner)
    public ParsedObject(){
        
    }
    
    /**
     *This method add an element (ParsedElement class) in the object, this work include two tasks,
     *add the element in the table and add an index for this element. If already exist the index,
     *the linked value will be replaced. The key used is the element name.
     *@param element this is a instance of the ParsedElement that represent the element to add in the object.
     */
    public void addElement(P element){
        elements.put(element.getId(), element);
    }
    
    /**
     * Adds all elements in the collection to this ParsedObject.
     */
    public void addAllElements(Collection<P> elementsToAdd) {
        for (P element : elementsToAdd) {
            addElement(element);
        }
    }
    
    /**
     * This method forces the element to be at the top of the list.
     * This is neccesary when we have Bond Truncation cases. We always need to parse texts first.
     */
    public void insertElement(P element){
        Collection<P> oldValues = elements.values();
        elements = new LinkedHashMap();
                
        addElement(element);
        addAllElements(oldValues);
    }
    
    /**
     *Returns a collection with all elements (ParsedElement class).
     */
    public Collection<P> getElements(){
        return elements.values();
    }
    
    /**
     * Removes the element from this ParsedObject.
     */
    public void removeElement(P element) {
        elements.remove(element.getId());
    }
    
    /**
     * Removes all elements in the collection from this ParsedObject.
     */
    public void removeAllElements(Collection<P> elementsToRemove) {
        for (P element : elementsToRemove) {
            removeElement(element);
        }
    }
    
    /**
     *This method add an attribute in the object, this work include two tasks,
     *add the attribute in the table and add an index for this attribute. If already exist the index,
     *the linked value will be replaced. The key used is the attribute name.
     *@param name represents the attribute name.
     *@param value represents the attribute value.
     */
    public void addAttribute(String name, String value){
        if(attributes.get(name) == null){
            attributeKeys.add(name);
        }
        attributes.put(name, value);
    }
    
    /**
     *Returns an attribute using as criterion the element name. If attribute is not found,
     *this method throws an TranslatorException.
     *@param name this is the criterion used in the search.
     */
    public String getAttribute(String name) {
        return attributes.get(name);
    }
    
    public boolean hasAttribute(String name) {
        return attributeKeys.contains(name);
    }
    
    /**
     *Returns a collection with all attributes.
     */
    public Collection<String> getAttributes(){
        return attributes.values();
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
    
    public int compareTo(ParsedObject o) {
        int result = this.zOrder - o.zOrder;
        
        if(result == 0){
            result = 1;
        }
        
        return result;
    }

    public int getZOrder() {
        return zOrder;
    }

    public void setZOrder(int zOrder) {
        this.zOrder = zOrder;
    }
    
    public ParsedObject<P> getOwner(){
        return owner;
    }
    
    public void setOwner(ParsedObject<P> owner){
        this.owner = owner;
    }
}
