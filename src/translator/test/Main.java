package translator.test;

import java.io.File;
import svgrenderer.CDXMLConverter;
import svgrenderer.SVGWriter;
import translator.domains.XMLDomainConfiguration;

public class Main {
    
    private static final String USER_HOME = "user.home";
    private static final String ARG_CONFIGURATION = "-c";
    private static final String ARG_DTD = "-d";
    private static final String ARG_SOURCE = "-s";
    
    private static final String FILE_CONFIGURATION = "configuration.xml";
    private static final String FILE_DTD = "CDXML.dtd";
    private static final String SOURCE = "CDXML";
    
    
    public Main() {
    }
    
    public static void main(String[] args) throws Exception {
        String configurationPath = "C:\\Users\\LIUJF\\Desktop\\new\\configuration.xml";
        String dtdPath = null;
        String sourcePath = "C:\\Users\\LIUJF\\Desktop\\new\\text.cdxml";
        
        String userHomePath = System.getProperty(USER_HOME);
        
        for(String arg : args){
            if(arg.startsWith(ARG_CONFIGURATION)){
                configurationPath = arg.substring(2).trim();
            } else if(arg.startsWith(ARG_DTD)){
                dtdPath = arg.substring(2).trim();
            } else if(arg.startsWith(ARG_SOURCE)){
                sourcePath = arg.substring(2).trim();
            }
        }
        
        if(configurationPath == null){
            configurationPath =  userHomePath + File.separator + FILE_CONFIGURATION;
        }
        if(dtdPath == null){
            dtdPath = userHomePath + File.separator + FILE_DTD;
        }
        if(sourcePath == null){
            sourcePath = userHomePath + File.separator + SOURCE;
        }
        
        System.setProperty(
                translator.domains.XMLDomainConfiguration.DOMAIN_CONFIGURATION_PROPERTY_KEY,
                XMLDomainConfiguration.class.getName());
        
        System.setProperty(
                translator.domains.XMLDomainConfiguration.FILE_CONFIGURATION_PATH,
                configurationPath);
        
        System.setProperty(
                translator.domains.XMLDomainConfiguration.DTD_FILE_PATH,
                dtdPath);
        
        CDXMLConverter converter = new CDXMLConverter();
        converter.convert(sourcePath);
        
        //File f = new File("C:\\Users\\LIUJF\\Desktop\\new\\javatext.svg");
        //SVGWriter writer = new SVGWriter();
        //writer.write(f);
        
    }
    
}
