package svgrenderer;

import java.io.File;
import translator.TranslatorException;

public class CDXMLConverter {
    
    private CDXMLReader reader = new CDXMLReader();
    private SVGWriter writer = new SVGWriter();
    
    public CDXMLConverter() {
    }
    
    public void convert(String path){
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
                                path.indexOf(".", path.lastIndexOf(File.pathSeparator))) + ".svg"));
                        
                        System.out.println("The file has been generated: " +
                                path.substring(0,
                                path.indexOf(".", path.lastIndexOf(File.pathSeparator))) + ".svg");
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
