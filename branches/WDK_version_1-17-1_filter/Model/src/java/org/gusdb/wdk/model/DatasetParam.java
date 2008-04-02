/**
 * 
 */
package org.gusdb.wdk.model;

import org.gusdb.wdk.model.user.Dataset;
import org.gusdb.wdk.model.user.DatasetFactory;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;

/**
 * @author xingao
 * 
 */
public class DatasetParam extends Param {
    
    private UserFactory userFactory;
    private DatasetFactory datasetFactory;
    
    public DatasetParam() { }
    
    public DatasetParam(DatasetParam param) {
        super(param);
        this.userFactory = param.userFactory;
        this.datasetFactory = param.datasetFactory;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#validateValue(java.lang.Object)
     */
    @Override
    public String validateValue( Object value ) throws WdkModelException {
        String errmsg = null;
        
        // cannot use the validation for dataset, since the validation happens
        // before the data transformation
        // if ( value == null ) {
        // errmsg = "The value to the param " + this.name + " is missing.";
        // } else {
        // // validate datasetParam by getting the dataset object
        // try {
        // getDataset( ( String ) value );
        // } catch ( WdkUserException ex ) {
        // errmsg = ex.getMessage();
        // }
        // }
        return errmsg;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    protected void resolveReferences( WdkModel model ) throws WdkModelException {}
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#setResources(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    protected void setResources( WdkModel model ) throws WdkModelException {
        super.setResources( model );
        try {
            userFactory = model.getUserFactory();
            datasetFactory = model.getDatasetFactory();
        } catch ( WdkUserException ex ) {
            throw new WdkModelException( ex );
        }
    }
    
    /*
     * (non-Javadoc) The internal value for the DatasetParam will be the
     * dataset_id
     * 
     * @see org.gusdb.wdk.model.Param#getInternalValue(java.lang.String)
     */
    @Override
    protected String getInternalValue( String value ) throws WdkModelException {
        try {
            Dataset dataset = getDataset( value );
            return Integer.toString( dataset.getDatasetId() );
        } catch ( WdkUserException ex ) {
            throw new WdkModelException( ex );
        }
    }
    
    public Dataset getDataset( String combinedId ) throws WdkModelException,
            WdkUserException {
        combinedId = ( String ) decompressValue( combinedId );
        // at this point, the input value should be formatted as
        // signature:dataset_id
        String[ ] parts = combinedId.split( ":" );
        if ( parts.length != 2 )
            throw new WdkModelException( "Invalid value for DatasetParam "
                    + name + ": '" + combinedId + "'" );
        
        String signature = parts[ 0 ].trim();
        String datasetChecksum = parts[ 1 ].trim();
        
        // make sure the dataset belongs to this user
        User user = userFactory.loadUserBySignature( signature );
        return user.getDataset( datasetChecksum );
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.Param#clone()
     */
    @Override
    public Param clone() {
        return new DatasetParam(this);
    }
}
