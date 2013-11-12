<?xml version="1.0" encoding="UTF-8"?>

<jsp:root version="2.0"
  xmlns:jsp="http://java.sun.com/JSP/Page"
  xmlns:c="http://java.sun.com/jsp/jstl/core"
  xmlns:imp="urn:jsptagdir:/WEB-INF/tags/imp">

  <jsp:directive.attribute name="refer" required="false" 
              description="Page calling this tag. The list of WDK recognized refer values are: home, question, summary, record"/>

  <!-- WDK libraries and source files -->
  <!-- see WDK/View/assets/wdkFiles.js for details about these files -->
  <imp:script src="/wdk/wdk.libs.js"/>
  <imp:script src="/wdk/wdk.js"/>

</jsp:root>
