<?xml version="1.0" encoding="UTF-8"?>

<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core"
    xmlns:imp="urn:jsptagdir:/WEB-INF/tags/imp">

  <c:set var="urlBase" value="${pageContext.request.contextPath}"/>

  <jsp:directive.attribute name="refer" required="false" 
              description="Page calling this tag. The list of WDK recognized refer values are: home, question, summary, record"/>


  <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!-- Scripts and styles that are used on the whole site                    -->
  <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

  <!-- styles for JS libraries -->
  <link rel="stylesheet" type="text/css" href="http://code.jquery.com/ui/1.10.2/themes/smoothness/jquery-ui.css"/>
  <!--
  <link rel="stylesheet" type="text/css" href="${urlBase}/wdk/css/jquery.multiSelect.css"/>
  -->
  <link rel="stylesheet" type="text/css" href="${urlBase}/wdk/css/datatables.css"/>
  <link rel="stylesheet" type="text/css" href="${urlBase}/wdk/js/lib/qtip2/jquery.qtip.css"/>
  <link rel="stylesheet" type="text/css" href="${urlBase}/wdk/css/wdkCommon.css"/>


  <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!-- scripts and styles used on the SUMMARY page only                      -->
  <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <c:if test="${refer == 'summary'}">

   <!-- Did you know popup -->
    <link rel="stylesheet" type="text/css" href='${urlBase}/wdk/css/dyk.css'/>

    <link rel="stylesheet" type="text/css" href='${urlBase}/wdk/css/Strategy.css'/>

    <!-- Results Page  -->
    <link rel="stylesheet" type="text/css" href="${urlBase}/wdk/css/flexigrid.css"/>
    <link rel="stylesheet" type="text/css" href="${urlBase}/wdk/css/wdkFilter.css"/>

  </c:if>
</jsp:root>
