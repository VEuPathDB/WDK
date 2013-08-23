<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core"
    xmlns:apifn="http://apidb.org/apicommon/functions"
    xmlns:imp="urn:jsptagdir:/WEB-INF/tags/imp">

  <jsp:directive.attribute name="refer" required="false" 
              description="Page calling this tag"/>
              
  <jsp:directive.attribute name="title" required="false"
              description="Value to appear in page's title"/>

  <jsp:directive.attribute name="banner" required="false"
              description="Value to appear at top of page if there is no title provided"/>

  
    <!-- StyleSheets provided by WDK -->
    <imp:wdkStylesheets refer="${refer}" /> 

    <!-- JavaScript provided by WDK -->
    <imp:wdkJavascripts refer="${refer}" />

</jsp:root>
