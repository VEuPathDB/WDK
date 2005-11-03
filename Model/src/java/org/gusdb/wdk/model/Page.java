package org.gusdb.wdk.model;
import java.util.ArrayList;

/** Page.java
* Created for Custom specifications 
* @author Nivedita Kaluskar
**/


 public class Page
    {
        private String name;
        private ArrayList questionSetList;
        //private PageDefaults pageDefaults;
        private ArrayList pageDefaultsList; 
        
        public Page() {
              questionSetList = new ArrayList();
              pageDefaultsList = new ArrayList();
        }

        public void setName(String newName)
        {
            name = newName;
           // System.out.println("In setName");
        }
        public String getName()
        {
            return name;
        }
     
      public void addQuestionSetName(QuestionSetName questionSetName)
        {
            //questionSetName.setPage(this);
            questionSetList.add(questionSetName);
        }
       
        public ArrayList getQuestionSetList()
        {
             return questionSetList;
        }
       
     /*  public void setPageDefaults(PageDefaults newPageDefaults)
        {
           pageDefaults=newPageDefaults; 
        }

       public PageDefaults getPageDefaults()
        {
            return pageDefaults;
        }
       */

       public void addPageDefaults(PageDefaults pageDefaults)
        {
             pageDefaultsList.add(pageDefaults);
        }
    
       public ArrayList getPageDefaults()
        {
           return pageDefaultsList;
        }

       public String toString() {
       String newline = System.getProperty( "line.separator" );
       StringBuffer buf = new StringBuffer("Page: name='" + name + "'");
       
       return buf.toString();
    }


 }
  



