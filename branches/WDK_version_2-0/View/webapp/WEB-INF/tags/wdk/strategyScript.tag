<%-- this tag file is only used to import script for 
     supporting strategy page functionality  --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<script type="text/javascript" src="<c:url value='wdk/js/lib/jquery-ui-1.8.12.custom.min.js'/>"></script>
<script type="text/javascript" src="<c:url value='wdk/js/dyk.js'/>"></script>

<!-- JQuery Drag And Drop Plugin -->
<script type="text/javascript" src="<c:url value='wdk/js/lib/json.js'/>"></script>
<script type="text/javascript" src="<c:url value='wdk/js/lib/jquery.multiSelect.js'/>"></script>

<!-- filter menu javascript -->
<script type="text/javascript" src="<c:url value='wdk/js/addStepPopup.js'/>"></script>
<script type="text/javascript" src="<c:url value='wdk/js/spanlogic.js'/>"></script>
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
