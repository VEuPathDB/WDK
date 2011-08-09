<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>

<c:set var="plugin" value="${requestScope.plugin}" />
<c:set var="summary" value="${requestScope.summary}" />
<c:set var="histogram" value="${requestScope.histogram}" />

<h2 align="center">${plugin.display}</h2>
<table class="histogram">
  <tr>
    <th>#Exons</th>
    <th colspan="2" align="left">#Records</th>
  </tr>
  <c:forEach items="${histogram}" var="item">
    <tr>
      <td>${item.key}</td>
      <td>${summary[item.key]}</td>
      <td><div class="bar" style="width:${item.value}px"> </div>
    <tr>
  </c:forEach>
</table>
