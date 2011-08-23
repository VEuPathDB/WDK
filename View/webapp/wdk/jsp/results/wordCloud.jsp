<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>

<c:set var="plugin" value="${requestScope.plugin}" />
<c:set var="tags" value="${requestScope.tags}" />

<script type="text/javascript" src="<c:url value='/wdk/js/wordCloud.js' />"></script>
<div id="word-cloud">
<c:choose>
  <c:when test="${fn:length(tags) == 0}">
    No text available.
  </c:when>
  <c:otherwise>
  <div id="tags" total="${fn:length(tags)}">
    <%-- the tags are sorted by count --%>
    <c:forEach items="${tags}" var="tag">
      <span class="word" count="${tag.count}" title="Occurrence: ${tag.count}">${tag.word}</span>
    </c:forEach>
  </div>
  <table>
    <tr>
      <th>Display words by rank: </th>
      <td>
        <input type="text" name="from" size="4" /> -
        <input type="text" name="to" size="4" />
      </td>
      <td><div id="amount"> </div></td>
    </tr>
    <tr>
      <th>Sort displayed words by: </th>
      <td colspan="2">
        <input type="radio" name="sort" value="word" checked />Alphabetic
        <input type="radio" name="sort" value="count" />Weight
      </td>
    </tr>
  </table>
  <div id="layout"> </div>
  </c:otherwise>
</c:choose>
</div>
