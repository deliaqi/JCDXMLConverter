package translator.domains;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import translator.domains.xml.*;
import translator.domains.xml.DomainConfiguration;

/**
 * Implementation of DomainConfiguration using an XML file to store the
 * information.
 * The tags allow definition of Parser classes and their association with
 * DomainVerifier classes, which are not limited to a single parser.
 * The stored values are the fully qualified class names of both Parser and
 * DomainVerifier classes.
 */
public class XMLDomainConfiguration implements translator.domains.DomainConfiguration {
    
    private static String XML_CONTEXT_NAME = "translator.domains.xml";
    
    public static final String DOMAIN_CONFIGURATION_PROPERTY_KEY = "translator.domain.DomainConfiguration";
    public static final String FILE_CONFIGURATION_PATH = "file.configuration.path";
    public static final String DTD_FILE_PATH = "dtd.file.path";
    
    private File xmlFile;
    private DomainConfiguration configuration;
    
    private static JAXBContext xmlContext;
    
    public XMLDomainConfiguration(){
        String filePath = System.getProperty(FILE_CONFIGURATION_PATH);
        
        if(filePath != null){
            xmlFile = new File(filePath);
        }
    }
    
    public DomainConfiguration getConfiguration() {
        return configuration;
    }
    
    public void loadConfiguration() throws DomainConfigurationException {
        if(xmlFile == null){
            throw new DomainConfigurationException("File configuration not found");
        }
        
        try {
            if (xmlContext == null) {
                xmlContext = JAXBContext.newInstance(XML_CONTEXT_NAME);
            }
            Unmarshaller unmarshaller = xmlContext.createUnmarshaller();
            
            JAXBElement rootElement = (JAXBElement) unmarshaller.unmarshal(xmlFile);
            
            configuration = (translator.domains.xml.DomainConfiguration) rootElement.getValue();
        } catch (JAXBException ex) {
            ex.printStackTrace();
            throw new DomainConfigurationException(ex.getMessage());
        }
    }
    
    public Collection<String> getParsers(){
        Collection<String> parsers = new ArrayList();
        
        for(Parser installedParser : configuration.getParser()){
            parsers.add(installedParser.getName());
        }
        
        return parsers;
    }
    
    public Collection<translator.domains.DomainVerifier> getVerifiers(String parserName) throws DomainConfigurationException {
        try {
            Collection<translator.domains.DomainVerifier>
                    verifiers = new ArrayList();
            
            for(Parser installedParser : configuration.getParser()){
                
                if(parserName.equals(installedParser.getName())){
                    
                    for(translator.domains.xml.DomainVerifier xmlVerifier : installedParser.getDomainVerifier()){
                        DomainVerifier installedVerifier =
                                (DomainVerifier)
                                Class.forName(xmlVerifier.getName()).newInstance();
                        
                        verifiers.add(installedVerifier);
                    }
                    
                    break;
                }
                
            }
            
            return verifiers;
        } catch (InstantiationException ex) {
            throw new DomainConfigurationException(ex.getMessage());
        } catch (IllegalAccessException ex) {
            throw new DomainConfigurationException(ex.getMessage());
        } catch (ClassNotFoundException ex) {
            throw new DomainConfigurationException(ex.getMessage());
        }
        
    }
}
