<%-- this tag file is only used to import static resources --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<%@ attribute name="refer"
              required="false"
              description="Page calling this tag. The list of WDK recognized refer values are: home, question, summary, record"
%>

<c:set var="urlBase" value="${pageContext.request.contextPath}"/>

<%-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<%-- Scripts and styles that are used on the whole site                    --%>
<%-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>

<%-- JS libraries --%>
<%-- comment out the production code. need to revert this on check in --%>

<!--<script type="text/javascript" src="http://code.jquery.com/jquery-1.7.js"></script>-->
<script type="text/javascript" src='${urlBase}/wdk/js/lib/jquery-1.7.min.js'></script>

<script type="text/javascript" src="${urlBase}/wdk/js/lib/jquery-ui-1.8.16.custom.min.js"></script>
<script type="text/javascript" src="${urlBase}/wdk/js/lib/jquery.blockUI.js"></script>
<script type="text/javascript" src="${urlBase}/wdk/js/lib/jquery.cookie.js"></script>
<script type="text/javascript" src="${urlBase}/wdk/js/lib/jquery.dataTables-1.9.0.min.js"></script>
<script type="text/javascript" src="${urlBase}/wdk/js/lib/FixedColumns.min.js"></script>
<script type="text/javascript" src="${urlBase}/wdk/js/lib/FixedHeader.min.js"></script>
<script type="text/javascript" src="${urlBase}/wdk/js/lib/jstree/jquery.jstree.js"></script>
<script type="text/javascript" src="${urlBase}/wdk/js/lib/qtip2/jquery.qtip.js"></script>
<script type="text/javascript" src="${urlBase}/wdk/js/lib/json.js"></script>
<script type="text/javascript" src="${urlBase}/wdk/js/lib/handlebars-1.0.0.beta.6.js"></script>

<%-- styles for JS libraries --%>
<link rel="stylesheet" type="text/css" href="${urlBase}/wdk/css/ui-custom/custom-theme/jquery-ui-1.8.16.custom.css"/>
<link rel="stylesheet" type="text/css" href="${urlBase}/wdk/css/jquery.multiSelect.css"/>
<link rel="stylesheet" type="text/css" href="${urlBase}/wdk/css/datatables.css"/>
<link rel="stylesheet" type="text/css" href="${urlBase}/wdk/js/lib/qtip2/jquery.qtip.mod.css"/>

<%-- WDK js and css --%>

<c:choose>
  <c:when test="${param._js eq 'min'}">
    <%-- minified files --%>
    <script src="${urlBase}/wdk/js/wdk-min.js"></script>
  </c:when>
  <c:when test="${param._js eq 'concat'}">
    <%-- concatenated files --%>
    <script src="${urlBase}/wdk/js/wdk.js"></script>
  </c:when>
  <c:otherwise>
    <%-- individual files --%>
    <script src="${urlBase}/wdk/js/src/util/namespace.js"></script>
    <script src="${urlBase}/wdk/js/src/addStepPopup.js"></script>
    <script src="${urlBase}/wdk/js/src/api.js"></script>
    <script src="${urlBase}/wdk/js/src/basket.js"></script>
    <script src="${urlBase}/wdk/js/src/checkboxTree.js"></script>
    <script src="${urlBase}/wdk/js/src/dyk.js"></script>
    <script src="${urlBase}/wdk/js/src/event.js"></script>
    <script src="${urlBase}/wdk/js/src/favorite.js"></script>
    <script src="${urlBase}/wdk/js/src/filter.js"></script>
    <script src="${urlBase}/wdk/js/src/history.js"></script>
    <script src="${urlBase}/wdk/js/src/parameterHandlers.js"></script>
    <script src="${urlBase}/wdk/js/src/question.js"></script>
    <script src="${urlBase}/wdk/js/src/resultsPage.js"></script>
    <script src="${urlBase}/wdk/js/src/step.js"></script>
    <script src="${urlBase}/wdk/js/src/stratTabCookie.js"></script>
    <script src="${urlBase}/wdk/js/src/tooltips.js"></script>
    <script src="${urlBase}/wdk/js/src/user.js"></script>
    <script src="${urlBase}/wdk/js/src/util.js"></script>
    <script src="${urlBase}/wdk/js/src/wdk.js"></script>
    <script src="${urlBase}/wdk/js/src/wordCloud.js"></script>
    <script src="${urlBase}/wdk/js/src/plugins/wdkDataTables.js"></script>
    <script src="${urlBase}/wdk/js/src/strategy/controller.js"></script>
    <script src="${urlBase}/wdk/js/src/strategy/error.js"></script>
    <script src="${urlBase}/wdk/js/src/strategy/model.js"></script>
    <script src="${urlBase}/wdk/js/src/strategy/view.js"></script>
  </c:otherwise>
</c:choose>

<link rel="stylesheet" type="text/css" href="${urlBase}/wdk/css/wdkCommon.css"/>


<%-- All pages --%>
<script>
  jQuery(wdk.init);
  jQuery(wdk.user.init);
  jQuery(wdk.favorite.init);
</script>

<%-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<%-- scripts and styles used on the HOME page only                         --%>
<%-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<c:if test="${refer == 'home'}">

</c:if>


<%-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<%-- scripts and styles used on the QUESTION page. all question content are 
     also included in summary page to support the addStep popup            --%>
<%-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<c:if test="${refer == 'question' || refer == 'summary'}">
  <script>
    jQuery(wdk.question.init);
    jQuery(wdk.parameterHandlers.init);
  </script>
</c:if>


<%-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<%-- scripts and styles used on the SUMMARY page only                      --%>
<%-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<c:if test="${refer == 'summary'}">

 <!-- Did you know popup -->
  <link rel="stylesheet" type="text/css" href='${urlBase}/wdk/css/dyk.css'/>

  <!-- JQuery Drag And Drop Plugin -->
  <script type="text/javascript" src="${urlBase}/wdk/js/lib/jquery.multiSelect.js"></script>
  <script type="text/javascript" src="${urlBase}/wdk/js/lib/jquery.form.js"></script>

  <link rel="stylesheet" type="text/css" href='${urlBase}/wdk/css/Strategy.css'/>

  <!-- Results Page  -->
  <script type="text/javascript" src="${urlBase}/wdk/js/lib/flexigrid.js"></script>
  <link rel="stylesheet" type="text/css" href="${urlBase}/wdk/css/flexigrid.css"/>
  <link rel="stylesheet" type="text/css" href="${urlBase}/wdk/css/wdkFilter.css">

  <script>
    jQuery(wdk.step.init);
    jQuery(wdk.strategy.controller.init);
    jQuery(wdk.wordCloud.init);
  </script>

  <!--[if lt IE 7]>
  <script type="text/javascript">
        jQuery(document).ready(function(){
                $("#Strategies").prepend("<div style='height:124px;'>&nbsp;</div>");
        });
  </script>
  <![endif]-->

</c:if>


<%-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<%-- scripts and styles used on the RECORD page only                       --%>
<%-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<c:if test="${refer == 'record'}">

</c:if>

