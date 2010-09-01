<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" --%>
<%--@ taglib prefix="wdk" uri="/WEB-INF/tags/wdk" --%>
<%--@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" --%>

<span class="dragHandle">
	<div class="modal_name">
		<h1 style="font-size:130%;margin-top:4px;" id="query_form_title"></h1>
	</div>
	<a class="back" href="javascript:back()">
		<img src="<c:url value='/wdk/images/backbutton.png'/>" alt='Close'/>
	</a>
	<a class='close_window' href='javascript:closeAll()'>
		<img src="<c:url value='/wdk/images/closebutton.png'/>" alt='Close'/>
	</a>
</span>