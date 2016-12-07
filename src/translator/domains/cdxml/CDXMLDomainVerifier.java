package translator.domains.cdxml;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import translator.domains.DomainConfiguration;
import translator.domains.DomainVerifier;
import translator.sources.File;
import translator.sources.Source;
import translator.sources.impl.FileImpl;
import translator.sources.impl.StreamImpl;

public class CDXMLDomainVerifier implements DomainVerifier {
    
    public CDXMLDomainVerifier() {
    }

    public boolean isDomainSupported(Source sourceToParse) {
        boolean result = false;
        if(sourceToParse instanceof FileImpl){
            FileImpl fileToParse = (FileImpl) sourceToParse;
            if(fileToParse.getName().endsWith("cdxml") || fileToParse.getName().endsWith("CDXML")){
                result = true;
            }
            else{
                result = false;
            }
        }
        else if(sourceToParse instanceof StreamImpl) {
            result =  true;
        }
        
        return result;
        /*try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException,IOException {
                    return new InputSource(new FileReader(
                            System.getProperty(DomainConfiguration.DTD_FILE_PATH)));
                }
            });
            builder.setErrorHandler(new ErrorHandler() {
                public void error(SAXParseException exception) throws SAXException {
                    throw exception;
                }
                public void fatalError(SAXParseException exception) throws SAXException {
                    throw exception;
                }
                public void warning(SAXParseException exception) throws SAXException {
                }
            });
            
            if(source instanceof FileImpl){
                FileImpl file = (FileImpl) source;
                builder.parse(file);
            }
            else if(source instanceof StreamImpl) {
                StreamImpl stream = (StreamImpl) source;
                builder.parse(stream.getIn());
            }
            
            return true;
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getClass().getSimpleName());
            return false;
        } catch (IOException ex) {
            System.out.println(ex.getClass().getSimpleName());
            return false;
        } catch (SAXException ex) {
            System.out.println(ex.getClass().getSimpleName());
            return true;
        } catch (ParserConfigurationException ex) {
            System.out.println(ex.getClass().getSimpleName());
            return false;
        }*/
    }
}
