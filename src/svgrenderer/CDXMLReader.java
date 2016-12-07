package svgrenderer;

import java.io.InputStream;
import translator.Parser;
import translator.ParserFactory;
import translator.Translator;
import translator.TranslatorException;
import translator.cddom.Document;
import translator.cdxml.CDXMLEnvironment;
import translator.domains.XMLDomainConfiguration;
import translator.sources.Source;
import translator.sources.Stream;
import translator.sources.impl.FileImpl;
import translator.sources.impl.StreamImpl;

public class CDXMLReader {
    
    private Translator translator;
    
    public CDXMLReader() {
        translator = new Translator();
    }
       
    public void read(InputStream in) throws TranslatorException {
        Stream source = new StreamImpl(in);
        read(source);
    }
    
    public void read(String path) throws TranslatorException {
        FileImpl source = new FileImpl(path);
        read(source);
    }
    
    private void read(Source source) throws TranslatorException {
        ParserFactory factory = ParserFactory.getInstance();
        CDXMLEnvironment.getInstance().SetPreferences((XMLDomainConfiguration)factory.getDomainConfiguration());
        Parser parser = factory.getParser(source);

        translator.setParser(parser);
        translator.setSource(source);
        
        translator.translate();
    }
    

    public Document getDocument() throws TranslatorException{
        return translator.getDocument();
    }

}
