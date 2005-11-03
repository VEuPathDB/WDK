package org.gusdb.wdk.model;

/** PageDefaults.java
* Created for Custom specifications 
* @author Nivedita Kaluskar
**/


public class PageDefaults {

   private String questionType="";
   private String flatVocabFormat="";
   private String nonFlatVocabFormat="";
   private String paramAlign="";  

   public void setQuestionType(String newQuestionType) {
         questionType = newQuestionType;
       //  System.out.println("Question Type :" +questionType);
   }

   public String getQuestionType() {
    //System.out.println("Question Type :" +questionType);
     return questionType;
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
  
  public String getParamAlign() {
         return paramAlign;
  }
   
  public String setParamAlign(String newParamAlign) {
         return paramAlign = newParamAlign;
  }

   public String toString() {
       String newline = System.getProperty( "line.separator" );
       StringBuffer buf = new StringBuffer("Question type: questionType='" + questionType + "'" + newline
                                           + "FlatVocabFormat='"  + flatVocabFormat + "'" + newline + "NonFlatVocabFormat='" + nonFlatVocabFormat + "'"  + newline + "ParamAlign='" + paramAlign + "'");

       return buf.toString();
    }

}
