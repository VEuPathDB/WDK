package org.gusdb.wdk.model;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

public class ModelConfigParser {
    
    public static ModelConfig parseXmlFile( File modelConfigXmlFile )
            throws WdkModelException {
        Digester digester = configureDigester();
        try {
            return ( ModelConfig ) digester.parse( modelConfigXmlFile );
        } catch ( IOException ex ) {
            throw new WdkModelException( ex );
        } catch ( SAXException ex ) {
            throw new WdkModelException( ex );
        }
    }
    
    public static ModelConfig parseXmlFile( URL modelConfigXmlURL )
            throws WdkModelException {
        Digester digester = configureDigester();
        try {
            return ( ModelConfig ) digester.parse( modelConfigXmlURL.openStream() );
        } catch ( IOException ex ) {
            throw new WdkModelException( ex );
        } catch ( SAXException ex ) {
            throw new WdkModelException( ex );
        }
    }
    
    static Digester configureDigester() {
        
        Digester digester = new Digester();
        digester.setValidating( false );
        
        digester.addObjectCreate( "modelConfig", ModelConfig.class );
        digester.addSetProperties( "modelConfig" );
        digester.addBeanPropertySetter( "modelConfig/emailSubject" );
        digester.addBeanPropertySetter( "modelConfig/emailContent" );
        
        return digester;
    }
    
    public static void main( String[ ] args ) {
        try {
            File modelConfigXmlFile = new File( args[ 0 ] );
            ModelConfig modelConfig = parseXmlFile( modelConfigXmlFile );
            
            System.out.println( modelConfig.toString() );
            
        } catch ( Exception exc ) {
            exc.printStackTrace();
        }
    }
}
