package translator.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import svgrenderer.CDXMLReader;
import svgrenderer.SVGWriter;
import svgrenderer.SVGWriterException;
import translator.TranslatorException;

public class Test {

	public static void main(String[] args) throws TranslatorException, SVGWriterException, IOException {
		// TODO Auto-generated method stub
		CDXMLReader reader = new CDXMLReader();
		SVGWriter writer = new SVGWriter();
		
		byte[] encoded = Files.readAllBytes(Paths.get("C:\\Users\\LIUJF\\Desktop\\new\\text.cdxml"));
		String cdxml = new String(encoded);
		reader.read(cdxml);
		writer.createDocument(reader.getDocument());
		writer.write(new File("C:\\Users\\LIUJF\\Desktop\\new\\javatext.svg"));

	}

}
