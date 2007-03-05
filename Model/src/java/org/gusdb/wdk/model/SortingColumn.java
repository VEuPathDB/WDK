/**
 * 
 */
package org.gusdb.wdk.model;

/**
 * @author: xingao
 * @created: Mar 1, 2007
 * @updated: Mar 1, 2007
 */
class SortingColumn {
    
    private String tableName;
    private String columnName;
    private boolean ascending;
    private boolean lowerCase;
    
    public SortingColumn( String tableName, String columnName, boolean ascending, boolean lowerCase ) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.ascending = ascending;
        this.lowerCase = lowerCase;
    }
    
    /**
     * @return the ascending
     */
    public boolean isAscending() {
        return ascending;
    }
    
    /**
     * @return the columnName
     */
    public String getColumnName() {
        return columnName;
    }
    
    /**
     * @return the tableName
     */
    public String getTableName() {
        return tableName;
    }

    
    /**
     * @return the lowerCase
     */
    public boolean isLowerCase() {
        return lowerCase;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object obj ) {
        if (obj instanceof SortingColumn) {
            SortingColumn column = (SortingColumn) obj;
            return column.tableName.equals( tableName ) && column.columnName.equals( columnName );
        } else 
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return tableName.hashCode() ^ columnName.hashCode();
    }
}
