<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

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
      <th>Filter words by rank: </th>
      <td>
        <input type="text" name="from" size="4" /> to
        <input type="text" name="to" size="4" />
      </td>
      <td style="padding-left:20px;"><div id="amount"> </div></td>
    </tr>
    <tr>
      <td colspan="2">
        <b>Sort by: </b>
        <input type="radio" name="sort" value="count" checked="checked" />Rank
        <input type="radio" name="sort" value="word" />A-Z
      </td>
      <td class="help" style="top:-2px;"><i>Use slider or enter numbers to adjust filter</i></td></tr>
    </tr>
  </table>
  <br />
  <div class="help"><i>Mouse over a word to see its occurence in the column</i></div>
  <div id="layout"> </div>
  </c:otherwise>
</c:choose>
</div>
