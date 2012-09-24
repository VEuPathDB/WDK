<%-- this tag file is only used to import static resources --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ attribute name="refer"
              required="false"
              description="Page calling this tag. The list of WDK recognized refer values are: home, question, summary, record"
%>

<%-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<%-- Scripts and styles that are used on the whole site                    --%>
<%-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>

<%-- JS libraries --%>
<%-- comment out the production code. need to revert this on check in --%>

<script type="text/javascript" src='<c:url value="/wdk/js/lib/jquery-1.7.min.js"/>'></script>
<!--
<script type="text/javascript" src="http://code.jquery.com/jquery-1.7.js"></script>
-->
<script type="text/javascript" src='<c:url value="/wdk/js/lib/jquery-ui-1.8.16.custom.min.js"/>'></script>
<script type="text/javascript" src="<c:url value='/wdk/js/lib/jquery.blockUI.js'/>"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/lib/jquery.cookie.js'/>"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/lib/jquery.dataTables-1.9.0.min.js'/>"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/lib/FixedColumns.min.js'/>"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/lib/FixedHeader.min.js'/>"></script>
<!--  <script type="text/javascript" src="<c:url value='/wdk/js/lib/jstree/jquery.jstree.js'/>"></script>  -->
<script type="text/javascript" src="wdk/js/lib/jstree/jquery.jstree.js"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/lib/qtip2/jquery.qtip.js'/>"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/lib/json.js'/>"></script>

<%-- styles for JS libraries --%>
<link rel="Stylesheet" type="text/css" href="<c:url value='/wdk/css/ui-custom/custom-theme/jquery-ui-1.8.16.custom.css' />"/>
<link rel="StyleSheet" type="text/css" href="<c:url value='/wdk/css/jquery.multiSelect.css' />"/>
<link rel="StyleSheet" type="text/css" href="<c:url value='/wdk/css/datatables.css' />"/>
<link rel="StyleSheet" type="text/css" href="<c:url value='/wdk/js/lib/qtip2/jquery.qtip.mod.css'/>" />

<%-- WDK based js files --%>
<script type="text/javascript" src='<c:url value="/wdk/js/api.js"/>'></script>
<script type="text/javascript" src='<c:url value="/wdk/js/wdkCommon.js"/>'></script>
<script type="text/javascript" src="<c:url value='/wdk/js/stratTabCookie.js'/>"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/js-utils.js'/>"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/checkboxTree.js'/>"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/basket.js'/>"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/favorite.js'/>"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/tooltips.js'/>"></script>

<%-- WDL based styles --%>
<link rel="stylesheet" type="text/css" href="<c:url value='/wdk/css/wdkCommon.css' />">
<link rel="stylesheet" type="text/css" href='<c:url value="/wdk/css/flexigrid.css"/>'/>


<%-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<%-- scripts and styles used on the home page only                         --%>
<%-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<c:if test="${refer == 'home'}">

</c:if>


<%-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<%-- scripts and styles used on the question page. all question content are 
     also included in summary page to support the addStep popup            --%>
<%-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<c:if test="${refer == 'question' || refer == 'summary'}">
  <script type="text/javascript" src="<c:url value='/wdk/js/parameterHandlers.js'/>"></script>
</c:if>


<%-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<%-- scripts and styles used on the summary page only                      --%>
<%-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<c:if test="${refer == 'summary'}">
  <script type="text/javascript" src="<c:url value='wdk/js/dyk.js'/>"></script>

  <!-- JQuery Drag And Drop Plugin -->
  <script type="text/javascript" src="<c:url value='wdk/js/lib/json.js'/>"></script>
  <script type="text/javascript" src="<c:url value='wdk/js/lib/jquery.multiSelect.js'/>"></script>
  <script type="text/javascript" src="<c:url value='wdk/js/lib/jquery.form.js'/>"></script>


  <!-- filter menu javascript -->
  <script type="text/javascript" src="<c:url value='wdk/js/addStepPopup.js'/>"></script>

  <!-- history page code -->
  <script type="text/javascript" src="<c:url value='wdk/js/history.js'/>"></script>

  <!-- Strategy Interaction javascript -->
  <script type="text/javascript" src="<c:url value='wdk/js/model-JSON.js'/>"></script>
  <script type="text/javascript" src="<c:url value='wdk/js/view-JSON.js'/>"></script>
  <script type="text/javascript" src="<c:url value='wdk/js/controller-JSON.js'/>"></script>
  <script type="text/javascript" src="<c:url value='wdk/js/error-JSON.js'/>"></script>
  <script type="text/javascript" src="<c:url value='wdk/js/step.js'/>"></script>
  <script type="text/javascript" src="<c:url value='wdk/js/lib/flexigrid.js'/>"></script>

  <!-- Results Page AJAX Javascript code -->
  <script type="text/javascript" src="<c:url value='wdk/js/resultsPage.js'/>"></script>
  <script type="text/javascript" src="<c:url value='wdk/js/basket.js'/>"></script>
  
  <script type="text/javascript" src="<c:url value='wdk/js/wdkFilter.js'/>"></script>
  <script type="text/javascript" src="<c:url value='wdk/js/favorite.js'/>"></script>

  <!--[if lt IE 7]>
  <script type="text/javascript">
        $(document).ready(function(){
                $("#Strategies").prepend("<div style='height:124px;'>&nbsp;</div>");
        });
  </script>
  <![endif]-->



  <link rel="stylesheet" type="text/css" href="<c:url value='/wdk/css/wdkFilter.css' />">
  <link rel="stylesheet" type="text/css" href='<c:url value="/wdk/css/dyk.css"/>'/>
  <link rel="stylesheet" type="text/css" href='<c:url value="/wdk/css/Strategy.css"/>'/>
</c:if>


<%-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<%-- scripts and styles used on the record page only                       --%>
<%-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ --%>
<c:if test="${refer == 'record'}">
  <script type="text/javascript" src="<c:url value='wdk/js/lib/json.js'/>"></script>
  <script type="text/javascript" src="<c:url value='wdk/js/basket.js'/>"></script>
  <script type="text/javascript" src="<c:url value='wdk/js/favorite.js'/>"></script>
</c:if>

