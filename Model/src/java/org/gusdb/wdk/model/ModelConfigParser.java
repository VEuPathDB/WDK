package org.gusdb.gus.wdk.model;

import java.io.File;
import java.net.URL;

import org.apache.commons.digester.Digester;

public class ModelConfigParser {

    public static ModelConfig parseXmlFile(File modelConfigXmlFile) throws java.io.IOException, org.xml.sax.SAXException {
        Digester digester = configureDigester();
        return (ModelConfig)digester.parse(modelConfigXmlFile);
    }
    
    public static ModelConfig parseXmlFile(URL modelConfigXmlURL) throws java.io.IOException, org.xml.sax.SAXException {
        Digester digester = configureDigester();
        return (ModelConfig)digester.parse(modelConfigXmlURL.openStream());
    }
    
    static Digester configureDigester() {

	Digester digester = new Digester();
	digester.setValidating( false );
	
	digester.addObjectCreate( "modelConfig", ModelConfig.class );
	digester.addSetProperties( "modelConfig");
	
	return digester;
    }
    
    public static void main( String[] args ) {
	try {
	    File modelConfigXmlFile = new File(args[0]);
	    ModelConfig modelConfig = parseXmlFile(modelConfigXmlFile);
	    
	    System.out.println(modelConfig.toString() );
	    
	} catch( Exception exc ) {
	    exc.printStackTrace();
	}
    }
}


