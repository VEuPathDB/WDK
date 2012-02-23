<%-- this tag file is only used to import static resources --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- JS libraries --%>
<%-- comment out the production code. need to revert this on check in --%>
<!--
<script type="text/javascript" src='<c:url value="/wdk/js/lib/jquery-1.6.4.min.js"/>'></script>
-->
<script type="text/javascript" src="http://code.jquery.com/jquery-1.6.4.js"></script>
<script type="text/javascript" src='<c:url value="/wdk/js/lib/jquery-ui-1.8.14.custom.min.js"/>'></script>
<script type="text/javascript" src="<c:url value='/wdk/js/lib/jquery.blockUI.js'/>"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/lib/jquery.cookie.js'/>"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/lib/jquery.dataTables-1.8.1.min.js'/>"></script>
<script type="text/javascript" src="wdk/js/lib/jstree/jquery.jstree.js"></script>

<%-- styles for JS libraries --%>
<link rel="stylesheet" type="text/css" href="<c:url value='/wdk/css/ui-custom/custom-theme/jquery-ui-1.8.16.custom.css' />"/>
<link rel="StyleSheet" type="text/css" href="<c:url value='/wdk/css/jquery.multiSelect.css' />"/>
<link rel="StyleSheet" type="text/css" href="<c:url value='/wdk/css/datatables.css' />"/>

<%-- WDK based js files --%>
<script type="text/javascript" src='<c:url value="/wdk/js/wdkCommon.js"/>'></script>
<script type="text/javascript" src="<c:url value='/wdk/js/stratTabCookie.js'/>"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/htmltooltip.js'/>"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/js-utils.js'/>"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/checkboxTree.js'/>"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/basket.js'/>"></script>
<script type="text/javascript" src="<c:url value='/wdk/js/favorite.js'/>"></script>

<%-- WDL based styles --%>
<link rel="stylesheet" type="text/css" href="<c:url value='/wdk/css/wdkCommon.css' />">
<link rel="stylesheet" type="text/css" href="<c:url value='/wdk/css/wdkFilter.css' />">
<link rel="stylesheet" type="text/css" href='<c:url value="/wdk/css/dyk.css"/>'/>
<link rel="stylesheet" type="text/css" href='<c:url value="/wdk/css/Strategy.css"/>'/>
<link rel="stylesheet" type="text/css" href='<c:url value="/wdk/css/flexigrid.css"/>'/>
