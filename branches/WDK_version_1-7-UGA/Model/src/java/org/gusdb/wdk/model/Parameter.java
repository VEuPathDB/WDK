package org.gusdb.wdk.model;

import org.gusdb.wdk.model.Defaults;
import org.gusdb.wdk.model.PageDefaults;

/** Parameter.java
* Created for Custom specifications 
* @author Nivedita Kaluskar
**/

public class Parameter {

   private String name;
  // private Defaults defaults;
   private String format="";
   private String flatVocabFormat="";
   private String nonFlatVocabFormat="";
   private String align="";
  
   public Parameter() {
         // flatVocabFormat = Defaults.getFlatVocabFormat();

         //  nonFlatVocabFormat=Defaults.getNonFlatVocabFormat();

          // align=Defaults.getParamAlign();
   
    }

 /*  public void setPageDefaults(PageDefaults pageDefaults) 
  {
         if (pageDefaults.getFlatVocabFormat() != "")
            flatVocabFormat = pageDefaults.getFlatVocabFormat();

           if (pageDefaults.getNonFlatVocabFormat() != "")
              nonFlatVocabFormat=pageDefaults.getNonFlatVocabFormat();

           if (pageDefaults.getParamAlign() != "")
               align=pageDefaults.getParamAlign();

  }
*/
/*  public void setDefaults(Defaults defaults)
  {
         if (defaults.getFlatVocabFormat() != "")
            flatVocabFormat = defaults.getFlatVocabFormat();

           if (defaults.getNonFlatVocabFormat() != "")
              nonFlatVocabFormat=defaults.getNonFlatVocabFormat();

           if (defaults.getParamAlign() != "")
               align=defaults.getParamAlign();

  }
*/

   public void setName(String newName) {
         name = newName;
   }

   public String getName() {
     return name;
   }

   public void setFormat(String newFormat) {
      format = newFormat;
   }

   public String getFormat() {
       return format;
   }

   public void setFlatVocabFormat(String newFlatVocabFormat) {
        flatVocabFormat = newFlatVocabFormat;
  }

  public String getFlatVocabFormat() {
        return flatVocabFormat;
  }

   public void setNonFlatVocabFormat(String newNonFlatVocabFormat) {
        nonFlatVocabFormat = newNonFlatVocabFormat;
  }

  public String getNonFlatVocabFormat() {
        return nonFlatVocabFormat;
  }

  public void setAlign(String newAlign) {
         align = newAlign;
   }
  
  public String getAlign() {
         return align;
  }

   public String toString() {
       String newline = System.getProperty( "line.separator" );
       StringBuffer buf = new StringBuffer("Parameter: name='" + name + "'" + newline
                                           + "Format='"  + format + "'" + newline + "Align='" + align + "'");

       return buf.toString();
    }

}
