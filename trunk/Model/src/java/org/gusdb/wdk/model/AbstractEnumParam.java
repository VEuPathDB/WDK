package org.gusdb.wdk.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public abstract class AbstractEnumParam extends Param {
    
    protected boolean multiPick = false;
    protected Map vocabMap;
    protected Vector orderedKeySet = new Vector();
    protected boolean quoteInternalValue;
    
    // ///////////////////////////////////////////////////////////////////
    // /////////// Public properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////
    
    public void setMultiPick( Boolean multiPick ) {
        this.multiPick = multiPick.booleanValue();
    }
    
    public Boolean getMultiPick() {
        return new Boolean( multiPick );
    }
    
    public void setQuoteInternalValue( Boolean quote ) {
        this.quoteInternalValue = quote.booleanValue();
    }
    
    public Boolean getQuoteInternalValue() {
        return new Boolean( quoteInternalValue );
    }
    
    public String validateValue( Object value ) throws WdkModelException {
        // check if the value is string, if yes, try decompres it
        if ( value instanceof String ) {
            value = decompressValue( ( String ) value );
        }
        String err = null;
        String[ ] values = ( String[ ] ) value;
        for ( String val : values ) {
            err = validateSingleValue( val );
            if ( err != null ) break;
        }
        return err;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#getInternalValue(java.lang.String)
     */
    public String getInternalValue( String value ) throws WdkModelException {
        String[ ] values = ( String[ ] ) decompressValue( value );
        initVocabMap();
        StringBuffer buf = new StringBuffer();
        
        for ( int i = 0; i < values.length; i++ ) {
            String v = vocabMap.get( values[ i ] ).toString();
            if ( i > 0 ) buf.append( "," );
            if ( quoteInternalValue ) buf.append( "'" + v + "'" );
            else buf.append( v );
        }
        return buf.toString();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    protected void resolveReferences( WdkModel model ) throws WdkModelException {
    // TODO Auto-generated method stub
    
    }
    
    public String[ ] getVocab() throws WdkModelException {
        
        initVocabMap();
        int keySize = orderedKeySet.size();
        String[ ] a = new String[ keySize ];
        for ( int i = 0; i < keySize; i++ ) {
            a[ i ] = orderedKeySet.elementAt( i ).toString();
        }
        return a;
    }
    
    public String[ ] getVocabInternal() throws WdkModelException {
        initVocabMap();
        int keySize = orderedKeySet.size();
        String[ ] a = new String[ keySize ];
        for ( int i = 0; i < keySize; i++ ) {
            Object nextKey = orderedKeySet.elementAt( i );
            a[ i ] = vocabMap.get( nextKey ).toString();
        }
        return a;
    }
    
    public Map getVocabMap() throws WdkModelException {
        initVocabMap();
        return vocabMap;
    }
    
    public String getDefault() throws WdkModelException {
        if ( defaultValue != null ) return defaultValue;
        String[ ] vocab = getVocab();
        if ( vocab.length == 0 ) return null;
        return vocab[ 0 ];
    }
    
    // ///////////////////////////////////////////////////////////////////
    // /////////// Protected properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#clone()
     */
    @Override
    public Param clone() {
        // TODO Auto-generated method stub
        return null;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#compressValue(java.lang.Object)
     */
    @Override
    public String compressValue( Object value ) throws WdkModelException {
        if ( value instanceof String[ ] ) {
            String[ ] values = ( String[ ] ) value;
            value = Utilities.fromArray( values );
        }
        return super.compressValue( value );
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#decompressValue(java.lang.String)
     */
    @Override
    public Object decompressValue( String value ) throws WdkModelException {
        logger.info( "decompressing: '" + value + "'" );
        
        // check if the value is compressed; that is, if it has a compression
        // prefix
        if ( value.startsWith( Utilities.COMPRESSED_VALUE_PREFIX ) ) {
            
            // decompress the value
            String checksum = value.substring(
                    Utilities.COMPRESSED_VALUE_PREFIX.length() ).trim();
            value = queryFactory.getClobValue( checksum );
        }
        if ( multiPick ) return value.split( "," );
        else return new String[ ] { value };
    }
    
    protected abstract void initVocabMap() throws WdkModelException;
    
    protected String validateSingleValue( Object value )
            throws WdkModelException {
        initVocabMap();
        
        if ( vocabMap.containsKey( value ) ) {
            return null;
        }
        if ( value == null || value.toString().trim().length() == 0 ) {
            return " - Please choose value(s) for parameter '" + name + "'";
        } else {
            return " - Invalid value '" + value + "' for parameter '" + name
                    + "'";
        }
    }
    
    protected void clone( AbstractEnumParam param ) {
        super.clone( param );
        param.multiPick = multiPick;
        if ( vocabMap != null ) {
            if ( param.vocabMap == null ) param.vocabMap = new LinkedHashMap();
            param.vocabMap.putAll( vocabMap );
        }
        param.orderedKeySet.addAll( orderedKeySet );
        param.quoteInternalValue = quoteInternalValue;
    }
}
