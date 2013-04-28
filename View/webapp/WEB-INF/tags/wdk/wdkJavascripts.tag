<?xml version="1.0" encoding="UTF-8"?>

<jsp:root version="2.0"
  xmlns:jsp="http://java.sun.com/JSP/Page"
  xmlns:c="http://java.sun.com/jsp/jstl/core"
  xmlns:imp="urn:jsptagdir:/WEB-INF/tags/imp">

  <jsp:directive.attribute name="refer" required="false" 
              description="Page calling this tag. The list of WDK recognized refer values are: home, question, summary, record"/>

  <jsp:directive.attribute name="min"
      type="java.lang.Boolean"
      required="false"/>

  <c:set var="urlBase" value="${pageContext.request.contextPath}"/>

  <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!-- Scripts and styles that are used on the whole site                    -->
  <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

  <!-- JS libraries -->
  <!-- comment out the production code. need to revert this on check in -->

  <!--<script type="text/javascript" src="http://code.jquery.com/jquery-1.7.js"><jsp:text/></script>-->
  <script type="text/javascript" src='${urlBase}/wdk/js/lib/jquery-1.7.min.js'><jsp:text/></script>

  <script type="text/javascript" src="${urlBase}/wdk/js/lib/jquery-ui-1.8.16.custom.min.js"><jsp:text/></script>
  <script type="text/javascript" src="${urlBase}/wdk/js/lib/underscore-min.js"><jsp:text/></script>
  <script type="text/javascript" src="${urlBase}/wdk/js/lib/jquery.blockUI.js"><jsp:text/></script>
  <script type="text/javascript" src="${urlBase}/wdk/js/lib/jquery.cookie.js"><jsp:text/></script>
  <script type="text/javascript" src="${urlBase}/wdk/js/lib/jquery.dataTables-1.9.0.min.js"><jsp:text/></script>
  <script type="text/javascript" src="${urlBase}/wdk/js/lib/FixedColumns.min.js"><jsp:text/></script>
  <script type="text/javascript" src="${urlBase}/wdk/js/lib/FixedHeader.min.js"><jsp:text/></script>
  <script type="text/javascript" src="${urlBase}/wdk/js/lib/jstree/jquery.jstree.js"><jsp:text/></script>
  <script type="text/javascript" src="${urlBase}/wdk/js/lib/qtip2/jquery.qtip.js"><jsp:text/></script>
  <script type="text/javascript" src="${urlBase}/wdk/js/lib/json.js"><jsp:text/></script>
  <script type="text/javascript" src="${urlBase}/wdk/js/lib/handlebars-1.0.0.beta.6.js"><jsp:text/></script>

  <!-- WDK js and css -->

  <c:choose>
    <c:when test="${param._js eq 'min' or min}">
      <!-- minified files -->
      <script src="${urlBase}/wdk/js/wdk-min.js"><jsp:text/></script>
    </c:when>
    <c:when test="${param._js eq 'concat'}">
      <!-- concatenated files -->
      <script src="${urlBase}/wdk/js/wdk.js"><jsp:text/></script>
    </c:when>
    <c:otherwise>
      <!-- individual files -->
      <script src="${urlBase}/wdk/js/src/util/namespace.js"><jsp:text/></script>
      <script src="${urlBase}/wdk/js/src/addStepPopup.js"><jsp:text/></script>
      <script src="${urlBase}/wdk/js/src/api.js"><jsp:text/></script>
      <script src="${urlBase}/wdk/js/src/basket.js"><jsp:text/></script>
      <script src="${urlBase}/wdk/js/src/checkboxTree.js"><jsp:text/></script>
      <script src="${urlBase}/wdk/js/src/dyk.js"><jsp:text/></script>
      <script src="${urlBase}/wdk/js/src/event.js"><jsp:text/></script>
      <script src="${urlBase}/wdk/js/src/favorite.js"><jsp:text/></script>
      <script src="${urlBase}/wdk/js/src/filter.js"><jsp:text/></script>
      <script src="${urlBase}/wdk/js/src/history.js"><jsp:text/></script>
      <script src="${urlBase}/wdk/js/src/parameterHandlers.js"><jsp:text/></script>
      <script src="${urlBase}/wdk/js/src/question.js"><jsp:text/></script>
      <script src="${urlBase}/wdk/js/src/resultsPage.js"><jsp:text/></script>
      <script src="${urlBase}/wdk/js/src/step.js"><jsp:text/></script>
      <script src="${urlBase}/wdk/js/src/stratTabCookie.js"><jsp:text/></script>
      <script src="${urlBase}/wdk/js/src/tooltips.js"><jsp:text/></script>
      <script src="${urlBase}/wdk/js/src/user.js"><jsp:text/></script>
      <script src="${urlBase}/wdk/js/src/util.js"><jsp:text/></script>
      <script src="${urlBase}/wdk/js/src/reporter.js"><jsp:text/></script>
      <script src="${urlBase}/wdk/js/src/wdk.js"><jsp:text/></script>
      <script src="${urlBase}/wdk/js/src/wordCloud.js"><jsp:text/></script>
      <script src="${urlBase}/wdk/js/src/plugins/wdkDataTables.js"><jsp:text/></script>
      <script src="${urlBase}/wdk/js/src/strategy/controller.js"><jsp:text/></script>
      <script src="${urlBase}/wdk/js/src/strategy/error.js"><jsp:text/></script>
      <script src="${urlBase}/wdk/js/src/strategy/model.js"><jsp:text/></script>
      <script src="${urlBase}/wdk/js/src/strategy/view.js"><jsp:text/></script>
    </c:otherwise>
  </c:choose>

  <!-- All pages -->
  <script>
    jQuery(wdk.init);
    jQuery(wdk.user.init);
    jQuery(wdk.favorite.init);
  </script>

  <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!-- scripts and styles used on the QUESTION page. all question content are 
       also included in summary page to support the addStep popup            -->
  <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <c:if test="${refer == 'question' || refer == 'summary'}">
  <!--
    <script>
      jQuery(wdk.question.init);
      jQuery(wdk.parameterHandlers.init);
    </script>
  -->
  </c:if>


  <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!-- scripts and styles used on the SUMMARY page only                      -->
  <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <c:if test="${refer == 'summary'}">

    <!-- JQuery Drag And Drop Plugin -->
    <script type="text/javascript" src="${urlBase}/wdk/js/lib/jquery.multiSelect.js"><jsp:text/></script>
    <script type="text/javascript" src="${urlBase}/wdk/js/lib/jquery.form.js"><jsp:text/></script>

    <!-- Results Page  -->
    <script type="text/javascript" src="${urlBase}/wdk/js/lib/flexigrid.js"><jsp:text/></script>

    <script>
      jQuery(wdk.step.init);
      jQuery(wdk.strategy.controller.init);
      jQuery(wdk.wordCloud.init);
    </script>

    <jsp:text><![CDATA[
    <!--[if lt IE 7]>
    <script type="text/javascript">
          jQuery(document).ready(function(){
                  $("#Strategies").prepend("<div style='height:124px;'>&nbsp;</div>");
          });
    </script>
    <![endif]-->
    ]]></jsp:text>

  </c:if>
</jsp:root>
