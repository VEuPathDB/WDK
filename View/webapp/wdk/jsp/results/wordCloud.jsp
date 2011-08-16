<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>

<c:set var="plugin" value="${requestScope.plugin}" />
<c:set var="frequency" value="${requestScope.frequency}" />

<div id="word-cloud">
  <c:forEach items="${frequency}" var="item">
    <c:set var="word" value="${item.key}" />
    <c:set var="fontSize" value="${item.value / 255}" />
    <c:set var="fontColor" value="${item.value % 255}" />
    <c:if test="${fontColor < 10}"><c:set var="fontColor" value="0${fontColor}" /></c:if>
    <span class="word" style="font-size: ${fontSize}pt; color: #0000${fontColor}">${word}</span> 
  </c:forEach>
</div>
