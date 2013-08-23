<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="title"
			  required="false"
			  description="Title to appear in the header"
%>
<div id="query_form_overlay" class="ui-widget-overlay"></div>
<div id="query_form" style="min-height:140px;">
<span class="dragHandle">
	<div class="modal_name">
		<h1 style="font-size:130%;margin-top:4px;" id="query_form_title">${title}</h1>
	</div>
	<a class="back" href="javascript:wdk.addStepPopup.backStage()">
		<img src="<c:url value='/wdk/images/backbutton.png'/>" alt='Close'/>
	</a>
	<a class='close_window' href='javascript:wdk.addStepPopup.closeAll()'>
		<img src="<c:url value='/wdk/images/closebutton.png'/>" alt='Close'/>
	</a>
</span>
<div id="errors"></div>
<div id="qf_content">
