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

  <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!-- Scripts and styles that are used on the whole site                    -->
  <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

  <!-- JS libraries -->
  <!-- comment out the production code. need to revert this on check in -->

  <imp:script src="/wdk/js/lib/jquery.js"/>
  <imp:script src="/wdk/js/lib/jquery-ui.js"/>
  <imp:script src="/wdk/js/lib/es5-shim.min.js"/>
  <imp:script src="/wdk/js/lib/chosen.jquery.min.js"/>
  <imp:script src="/wdk/js/lib/jquery.blockUI.js"/>
  <imp:script src="/wdk/js/lib/jquery.cookie.js"/>
  <imp:script src="/wdk/js/lib/jquery.dataTables-1.9.0.min.js"/>
  <imp:script src="/wdk/js/lib/jstree/jquery.jstree.js"/>
  <imp:script src="/wdk/js/lib/qtip2/jquery.qtip.js"/>
  <imp:script src="/wdk/js/lib/chosen.jquery.min.js"/>
  <imp:script src="/wdk/js/lib/json.js"/>
  <imp:script src="/wdk/js/lib/handlebars.js"/>
  <imp:script src="/wdk/js/lib/jquery.flot-0.8.1.min.js"/>
  <imp:script src="/wdk/js/lib/jquery.flot.categories-0.8.1.min.js"/>

  <!-- WDK js and css -->

  <c:choose>
    <c:when test="${param._js eq 'min' or min}">
      <!-- minified files -->
      <imp:script src="/wdk/js/wdk-min.js"/>
    </c:when>
    <c:when test="${param._js eq 'concat'}">
      <!-- concatenated files -->
      <imp:script src="/wdk/js/wdk.js"/>
    </c:when>
    <c:otherwise>
      <!-- individual files -->
      <imp:script src="/wdk/js/src/util/namespace.js"/>
      <imp:script src="/wdk/js/src/addStepPopup.js"/>
      <imp:script src="/wdk/js/src/api.js"/>
      <imp:script src="/wdk/js/src/basket.js"/>
      <imp:script src="/wdk/js/src/checkboxTree.js"/>
      <imp:script src="/wdk/js/src/dyk.js"/>
      <imp:script src="/wdk/js/src/event.js"/>
      <imp:script src="/wdk/js/src/favorite.js"/>
      <imp:script src="/wdk/js/src/filter.js"/>
      <imp:script src="/wdk/js/src/history.js"/>
      <imp:script src="/wdk/js/src/publicStrats.js"/>
      <imp:script src="/wdk/js/src/parameterHandlers.js"/>
      <imp:script src="/wdk/js/src/question.js"/>
      <imp:script src="/wdk/js/src/resultsPage.js"/>
      <imp:script src="/wdk/js/src/step.js"/>
      <imp:script src="/wdk/js/src/stratTabCookie.js"/>
      <imp:script src="/wdk/js/src/tooltips.js"/>
      <imp:script src="/wdk/js/src/user.js"/>
      <imp:script src="/wdk/js/src/util.js"/>
      <imp:script src="/wdk/js/src/reporter.js"/>
      <imp:script src="/wdk/js/src/wdk.js"/>
      <imp:script src="/wdk/js/src/wordCloud.js"/>
      <imp:script src="/wdk/js/src/histogram.js"/>
      <imp:script src="/wdk/js/src/plugins/wdkDataTables.js"/>
      <imp:script src="/wdk/js/src/plugins/wdkEditable.js"/>
      <imp:script src="/wdk/js/src/plugins/wdkSimpleToggle.js"/>
      <imp:script src="/wdk/js/src/plugins/wdkTooltip.js"/>
      <imp:script src="/wdk/js/src/strategy/controller.js"/>
      <imp:script src="/wdk/js/src/strategy/error.js"/>
      <imp:script src="/wdk/js/src/strategy/model.js"/>
      <imp:script src="/wdk/js/src/strategy/view.js"/>
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
    <!--
    <imp:script src="/wdk/js/lib/jquery.multiSelect.js"/>
    <imp:script src="/wdk/js/lib/jquery.form.js"/>
    -->

    <!-- Results Page  -->
    <imp:script src="/wdk/js/lib/flexigrid.js"/>

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
