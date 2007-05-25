package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

/**
 * A wrapper on a {@link Param} that provides simplified access for consumption
 * by a view
 */
public class ParamBean {
    
    protected Param param;
    protected String paramValue;
    
    public ParamBean( Param param ) {
        this.param = param;
    }
    
    public String getName() {
        return param.getName();
    }
    
    public String getId() {
        return param.getId();
    }
    
    public String getFullName() {
        return param.getFullName();
    }
    
    public String getPrompt() {
        return param.getPrompt();
    }
    
    public String getHelp() {
        return param.getHelp();
    }
    
    public String getDefault() {
        try {
            return param.getDefault();
        } catch ( WdkModelException e ) {
            throw new RuntimeException( e );
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#isReadonly()
     */
    public boolean getIsReadonly() {
        return this.param.isReadonly();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#isVisible()
     */
    public boolean getIsVisible() {
        return this.param.isVisible();
    }
    
    /**
     * @return
     * @see org.gusdb.wdk.model.Param#getGroup()
     */
    public GroupBean getGroup() {
        return new GroupBean( param.getGroup() );
    }
    
    /**
     * for controller
     */
    public String validateValue( Object val ) throws WdkModelException {
        return param.validateValue( val );
    }
    
    /**
     * @param value
     * @return
     * @throws WdkModelException
     * @see org.gusdb.wdk.model.Param#compressValue(java.lang.Object)
     */
    public String compressValue( Object value ) throws WdkModelException {
        return param.compressValue( value );
    }
    
    /**
     * @param value
     * @return
     * @throws WdkModelException
     */
    public Object decompressValue( String value ) throws WdkModelException {
        return param.decompressValue( value );
    }
    
    public void setParamValue( String paramValue ) {
        this.paramValue = paramValue;
    }
    
    public String getDecompressedValue() throws WdkModelException {
        Object object = decompressValue( paramValue );
        if ( object == null ) return null;
        String strValue;
        if ( object instanceof String[ ] ) {
            String[ ] array = ( String[ ] ) object;
            StringBuffer sb = new StringBuffer();
            for ( String value : array ) {
                if ( sb.length() > 0 ) sb.append( ", " );
                sb.append( value );
            }
            strValue = sb.toString();
        } else strValue = object.toString();
        if ( strValue.length() > WdkModel.TRUNCATE_DEFAULT ) {
            strValue = strValue.substring( 0, WdkModel.TRUNCATE_DEFAULT )
                    + "...";
        }
        return strValue;
    }
}
