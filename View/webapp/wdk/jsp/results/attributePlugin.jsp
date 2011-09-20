<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>

<c:set var="plugin" value="${requestScope.plugin}" />

<script>
$(function() {
	$( "#attribute-plugin" ).tabs();
});
</script>
<div id="attribute-plugin">
    <ul>
        <li><a href="#tab-view">Graph</a></li>
        <li><a href="#tab-download">Data (text)</a></li>
	</ul>
	<div id="tab-view">
		<c:import url="${plugin.view}"/>
	</div>
	<div id="tab-download">
		<pre>${plugin.downloadContent}</pre>
	</div>
<div>
