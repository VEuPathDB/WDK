package org.gusdb.wdk.model;

/** Defaults.java
* Created for Custom specifications
* @author Nivedita Kaluskar
**/

public class Defaults {

   private static String questionType="";
   private static String flatVocabFormat="";
   private static String nonFlatVocabFormat="";
   private static String paramAlign="";  

   public void setQuestionType(String newQuestionType) {
         questionType = newQuestionType;
       //  System.out.println("Question Type :" +questionType);
   }

   public static String getQuestionType() {
    //System.out.println("Question Type :" +questionType);
     return questionType;
   }

   public void setFlatVocabFormat(String newFlatVocabFormat) {
        flatVocabFormat = newFlatVocabFormat;
  }

  public static String getFlatVocabFormat() {
        return flatVocabFormat;
  }

  public void setNonFlatVocabFormat(String newNonFlatVocabFormat) {
         nonFlatVocabFormat = newNonFlatVocabFormat;
   }

 public static String getNonFlatVocabFormat() {
        return nonFlatVocabFormat;
  }
  
  public static String getParamAlign() {
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
