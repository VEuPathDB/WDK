<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>

<c:set var="plugin" value="${requestScope.plugin}" />
<c:set var="content" value="${requestScope.content}" />

<applet width="100%" height="600" 
        archive="<c:url value='/wdk/applet/wordle-v1354.jar' />"
        codebase="http://jerric.toxodb.org"
        code="wordle.WordleApplet.class" mayscript="mayscript" name="wordle">
<param value="${content}" name="text">
<param value="-Xmx1024m -Xms256m" name="java_arguments">
</applet>

