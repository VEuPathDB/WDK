<%-- this tag file is only used to import static resources --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<%@ attribute name="refer"
              required="false"
              description="Page calling this tag. The list of WDK recognized refer values are: home, question, summary, record"
%>

<%-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<%-- Scripts and styles that are used on the whole site                    --%>
<%-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>

<%-- JS libraries --%>
<%-- comment out the production code. need to revert this on check in --%>

<!--<script type="text/javascript" src="http://code.jquery.com/jquery-1.7.js"></script>-->
<script type="text/javascript" src='<c:url value="/wdk/js/lib/jquery-1.7.min.js"/>'></script>

<script type="text/javascript" src='<c:url value="/wdk/js/lib/jquery-ui-1.8.16.custom.min.js"/>'></script>
<script type="text/javascript" src="<c:url value='/wdk/js/lib/jquery.blockUI.js'/>"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/lib/jquery.cookie.js'/>"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/lib/jquery.dataTables-1.9.0.min.js'/>"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/lib/FixedColumns.min.js'/>"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/lib/FixedHeader.min.js'/>"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/wdk/js/lib/jstree/jquery.jstree.js"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/lib/qtip2/jquery.qtip.js'/>"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/lib/json.js'/>"></script>
<script type="text/javascript" src='<c:url value="/wdk/js/lib/handlebars-1.0.0.beta.6.js"/>'></script>

<%-- styles for JS libraries --%>
<link rel="Stylesheet" type="text/css" href="<c:url value='/wdk/css/ui-custom/custom-theme/jquery-ui-1.8.16.custom.css' />"/>
<link rel="StyleSheet" type="text/css" href="<c:url value='/wdk/css/jquery.multiSelect.css' />"/>
<link rel="StyleSheet" type="text/css" href="<c:url value='/wdk/css/datatables.css' />"/>
<link rel="StyleSheet" type="text/css" href="<c:url value='/wdk/js/lib/qtip2/jquery.qtip.mod.css'/>" />

<%-- WDK ja and css --%>
<script type="text/javascript" src='<c:url value="/wdk/js/api.js"/>'></script>
<script type="text/javascript" src="<c:url value='/wdk/js/stratTabCookie.js'/>"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/js-utils.js'/>"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/checkboxTree.js'/>"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/basket.js'/>"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/favorite.js'/>"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/tooltips.js'/>"></script>
<script type="text/javascript" src='<c:url value="/wdk/js/Utilities.js"/>'></script>
<script type="text/javascript" src='<c:url value="/wdk/js/User.js"/>'></script>
<script type="text/javascript" src='<c:url value="/wdk/js/wdkEvent.js"/>'></script>

<script type="text/javascript" src='<c:url value="/wdk/js/wdkCommon.js"/>'></script>
<link rel="stylesheet" type="text/css" href="<c:url value='/wdk/css/wdkCommon.css' />">



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
  <imp:parameterScript />
</c:if>


<%-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<%-- scripts and styles used on the SUMMARY page only                      --%>
<%-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<c:if test="${refer == 'summary'}">

 <!-- Did you know popup -->
  <script type="text/javascript" src="<c:url value='wdk/js/dyk.js'/>"></script>
  <link rel="stylesheet" type="text/css" href='<c:url value="/wdk/css/dyk.css"/>'/>

  <!-- JQuery Drag And Drop Plugin -->
  <script type="text/javascript" src="<c:url value='wdk/js/lib/jquery.multiSelect.js'/>"></script>
  <script type="text/javascript" src="<c:url value='wdk/js/lib/jquery.form.js'/>"></script>

  <!-- Add Step menu  -->
  <script type="text/javascript" src="<c:url value='wdk/js/addStepPopup.js'/>"></script>

  <!-- History page (All tab) -->
  <script type="text/javascript" src="<c:url value='wdk/js/history.js'/>"></script>

  <!-- Strategy Interaction  -->
  <script type="text/javascript" src="<c:url value='wdk/js/model-JSON.js'/>"></script>
  <script type="text/javascript" src="<c:url value='wdk/js/view-JSON.js'/>"></script>
  <script type="text/javascript" src="<c:url value='wdk/js/controller-JSON.js'/>"></script>
  <script type="text/javascript" src="<c:url value='wdk/js/error-JSON.js'/>"></script>
  <script type="text/javascript" src="<c:url value='wdk/js/step.js'/>"></script>

  <link rel="stylesheet" type="text/css" href='<c:url value="/wdk/css/Strategy.css"/>'/>

  <!-- Results Page  -->
  <script type="text/javascript" src="<c:url value='wdk/js/lib/flexigrid.js'/>"></script>
  <link rel="stylesheet" type="text/css" href='<c:url value="/wdk/css/flexigrid.css"/>'/>

  <script type="text/javascript" src="<c:url value='wdk/js/resultsPage.js'/>"></script>
  <script type="text/javascript" src="<c:url value='wdk/js/wdkFilter.js'/>"></script>
  <link rel="stylesheet" type="text/css" href="<c:url value='/wdk/css/wdkFilter.css' />">

  <!--[if lt IE 7]>
  <script type="text/javascript">
        $(document).ready(function(){
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

