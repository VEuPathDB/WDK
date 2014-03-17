<?xml version="1.0" encoding="UTF-8"?>

<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core"
    xmlns:imp="urn:jsptagdir:/WEB-INF/tags/imp">

  <jsp:directive.attribute name="refer" required="false" 
              description="Page calling this tag. The list of WDK recognized refer values are: home, question, summary, record"/>


  <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!-- Scripts and styles that are used on the whole site                    -->
  <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

  <!-- styles for JS libraries -->
  <imp:stylesheet rel="stylesheet" type="text/css" href="/wdk/css/jquery-ui.css"/>
  <imp:stylesheet rel="stylesheet" type="text/css" href="/wdk/css/chosen.min.css"/>
  <!--
  <imp:stylesheet rel="stylesheet" type="text/css" href="/wdk/css/jquery.multiSelect.css"/>
  -->
  <imp:stylesheet rel="stylesheet" type="text/css" href="/wdk/css/datatables.css"/>
  <imp:stylesheet rel="stylesheet" type="text/css" href="/wdk/lib/qtip2/jquery.qtip.css"/>
  <imp:stylesheet rel="stylesheet" type="text/css" href="/wdk/css/wdkCommon.css"/>
  <imp:stylesheet rel="stylesheet" type="text/css" href="/wdk/css/wdk-filter-param.css"/>



  <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!-- scripts and styles used on the SUMMARY page only                      -->
  <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <c:if test="${refer == 'summary'}">

   <!-- Did you know popup -->
    <imp:stylesheet rel="stylesheet" type="text/css" href='/wdk/css/dyk.css'/>

    <imp:stylesheet rel="stylesheet" type="text/css" href='/wdk/css/Strategy.css'/>

    <!-- Results Page  -->
    <imp:stylesheet rel="stylesheet" type="text/css" href="/wdk/css/flexigrid.css"/>
    <imp:stylesheet rel="stylesheet" type="text/css" href="/wdk/css/wdkFilter.css"/>

  </c:if>
</jsp:root>
