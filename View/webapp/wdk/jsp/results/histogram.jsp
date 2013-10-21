<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<c:set var="attribute" value="${requestScope.attribute}" />
<c:set var="plugin" value="${requestScope.plugin}" />
<c:set var="data" value="${requestScope.data}" />
<c:set var="histogram" value="${requestScope.histogram}" />

<div id="${plugin.name}" class="histogram"
     data-controller="wdk.result.histogram.init">
  <h2 align="center">${plugin.display}</h2>

  <ul>
    <li><a href="#graph">Graph</li>
    <li><a href="#data">Table</li>
  </ul>
  
  <div id="graph">
    <div class="plot" style="width:600px; height:400px;"> </div>
  </div>
  
  <div id="data">
    <table class="datatable">
      <thead>
        <tr>
          <th>${attribute.displayName}</th>
          <th>#Records</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach items="${data}" var="item">
          <tr class="data">
            <td class="bin">${item.key}</td>
            <td class="size">${summary[item.key]}</td>
          </tr>
        </c:forEach>
      </tbody>
      <c:if test="${fn:length(data) > 10}">
        <tfoot>
          <tr>
            <th>${attribute.displayName}</th>
            <th>#Records</th>
          </tr>
        </tfoot>
      </c:if>
    </table>
  </div>
  
</div>
