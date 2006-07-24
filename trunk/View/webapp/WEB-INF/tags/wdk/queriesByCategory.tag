<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!-- show all questions in model -->

<%-- first pass to get all record types --%>
<c:set value="${wdkModel.questionsByCategory}" var="qByRecType"/>
<c:set value="${wdkModel.recordClassTypes}" var="recTypes"/>

<table width="100%" cellpadding="4">

<c:forEach items="${qByRecType}" var="recTypeEntry">
  <c:set var="recType" value="${recTypeEntry.key}"/>
  <c:set var="qByCat" value="${recTypeEntry.value}"/>
  <c:set var="isMultiCat" value="${fn:length(qByCat) > 1}"/>

  <tr class="headerRow"><td colspan="4" align="center"><b>${recTypes[recTypeEntry.key]} Queries</b></td></tr>

  <c:set var="i" value="0"/>
  <c:forEach items="${qByCat}" var="catEntry">
    <c:set var="cat" value="${catEntry.key}"/>
    <c:set var="questions" value="${catEntry.value}"/>
    
    <c:if test="${isMultiCat}">
      <c:if test="${cat == '' || cat == ' ' || cat == '_'}">
        <c:set var="cat" value="miscellaneous"/>
      </c:if>
      <tr><td colspan="4">&nbsp;</td></tr>
      <tr class="rowDark" width="100%"><td colspan="4" align="center"><b>${cat}</b></td></tr>
    </c:if>

    <c:forEach items="${questions}" var="q">
      <c:set var="i" value="${i+1}"/>
      <c:choose>
        <c:when test="${i % 2 == 1}"><tr class="rowLight"></c:when>
        <c:otherwise><tr class="rowMedium"></c:otherwise>
      </c:choose>

      <td><b>${q.displayName}</b></td>
      <td><a href="<c:url value="/showQuestion.do?questionFullName=${q.fullName}"/>"><img src="<c:url value="/images/go.gif"/>" border="0"></td>
      <td>&nbsp;&nbsp;</td>
      <td><c:set var="desc" value="${q.description}"/>
          <c:if test="${q.summary != null}">
            <c:set var="desc" value="${q.summary}"/>
          </c:if>
          <c:if test="${fn:length(desc) > 163}">
            <c:set var="desc" value="${fn:substring(desc, 0, 160)}..."/>
          </c:if>
          ${desc}
      </td>
      </tr>
    </c:forEach>
  </c:forEach>

  <tr><td colspan="4">&nbsp;</td></tr>

</c:forEach>
</table>
