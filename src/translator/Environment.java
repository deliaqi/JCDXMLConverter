
package translator;

import java.util.Hashtable;
import translator.utils.JoinPoint;
import translator.domains.XMLDomainConfiguration;

public abstract class Environment {
    
    protected static Environment instance;
    
    protected Hashtable<String, JoinPoint> joinPoints = new Hashtable();
    
    protected int shadowRatio = 0;
    
    protected Environment(){
        
    }
    
    public static Environment getInstance(){
        return instance;
    }
    
    public int getShadowRatio() {
        return shadowRatio;
    }
          
    public abstract void cleanUpEnvironment();
    
    /* 
     * This method sets preferences of the document.
     */
    public void SetPreferences(XMLDomainConfiguration domainConfiguration) {
        shadowRatio = Integer.parseInt(domainConfiguration.getConfiguration().getDocumentPreferences().getShadowRatio());
    }
}
