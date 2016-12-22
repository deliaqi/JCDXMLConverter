package translator.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import svgrenderer.CDXMLConverter;
import svgrenderer.CDXMLReader;
import svgrenderer.SVGWriter;
import svgrenderer.SVGWriterException;
import translator.TranslatorException;
import translator.domains.XMLDomainConfiguration;

public class myTest {
	
	private static final String USER_HOME = "user.home";
    private static final String ARG_CONFIGURATION = "-c";
    private static final String ARG_DTD = "-d";
    private static final String ARG_SOURCE = "-s";
    
    private static final String FILE_CONFIGURATION = "configuration.xml";
    private static final String FILE_DTD = "CDXML.dtd";
    private static final String SOURCE = "CDXML";
    private static List<File> cdxmlList = new ArrayList<File>();

	public static void main(String[] args) throws TranslatorException, SVGWriterException, IOException {
		// TODO Auto-generated method stub
		CDXMLReader reader = new CDXMLReader();
		SVGWriter writer = new SVGWriter();
		/*
		byte[] encoded = Files.readAllBytes(Paths.get("C:\\Users\\LIUJF\\Desktop\\new\\text.cdxml"));
		String cdxml = new String(encoded);
		reader.read(cdxml);
		writer.createDocument(reader.getDocument());
		writer.write(new File("C:\\Users\\LIUJF\\Desktop\\new\\javatext.svg"));*/
		
		String configurationPath = "C:\\Users\\LIUJF\\Desktop\\new\\configuration.xml";
        String dtdPath = "http://cdxml.azurewebsites.net/CDXML.DTD";
        String sourcePath = "C:\\Users\\LIUJF\\Desktop\\new\\Demo.cdxml";
        String targetPath = "C:\\Users\\LIUJF\\Desktop\\new\\javaDemo.svg";
        
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
        
        // convert directly
		//CDXMLConverter converter = new CDXMLConverter();
        //converter.convert("C:\\Users\\LIUJF\\Desktop\\CDXMLTest\\CDD2.0 Test Data");
        convert("C:\\Users\\LIUJF\\Desktop\\CDXMLTest\\cdxml2svg_Result\\ValidResult\\Brackets_Testing Data\\Corss bond data file\\cross_bond_1.cdxml");
        //convert("C:\\Users\\LIUJF\\Desktop\\CDXMLTest\\cdxml2svg_Result\\Error_WrongResult\\4.Error_Null_BoundingBox\\Record1.cdxml");
        

	}
	
	public static void ReadAllFile(File folder){
		for(File fileEntry : folder.listFiles()){
			if(fileEntry.isDirectory()){
				ReadAllFile(fileEntry);
			}else if(fileEntry.isFile() && fileEntry.getName().toUpperCase().endsWith(".CDXML")){
				cdxmlList.add(fileEntry);
			}
		}
		
	}
	
	public static void convert(String path){
		CDXMLReader reader = new CDXMLReader();
		SVGWriter writer = new SVGWriter();
        try {
            File file = new File(path);
            
            if(file.isDirectory()){
                for(String newPath : file.list()){
                    convert(path + File.separator + newPath);
                }
            } else{
                if(path.endsWith(".cdxml") || path.endsWith(".CDXML")){
                    System.out.println("Reading: " + path);
                    
                    try {                        
                        reader.read(path);
                        
                        writer.createDocument(reader.getDocument());
                        writer.write(new File(path.substring(0,
                                path.indexOf(".cdxml")) + ".svg"));
                        
                        System.out.println("The file has been generated: " +
                                path.substring(0,
                                path.indexOf(".cdxml")) + ".svg");
                    } catch (TranslatorException ex) {
                        System.out.println("Translation fail \"" + path + "\":\n");
                        ex.printStackTrace();
                    }
                }
            }
        } catch (SVGWriterException ex) {
            ex.printStackTrace();
        }
    }
	

}
