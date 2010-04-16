<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!-- show all questions in model -->

<%-- first pass to get all record types --%>
<jsp:useBean scope="request" id="qTypeMap" class="java.util.LinkedHashMap"/>
<c:set value="${wdkModel.questionSets}" var="questionSets"/>
<c:forEach items="${questionSets}" var="qSet">
  <c:if test="${qSet.internal == false}">
    <c:set value="${qSet.questions}" var="questions"/>
    <c:forEach items="${questions}" var="q">
      <c:set var="recType" value="${q.recordClass.type}"/>
      <c:set target="${qTypeMap}" property="${recType}" value=""/>
    </c:forEach>
  </c:if>
</c:forEach>

<table width="100%" cellpadding="4">

<c:forEach items="${qTypeMap}" var="typeEntry">
  <c:set var="recType" value="${typeEntry.key}"/>

  <tr bgcolor="#bbaacc"><td colspan="4" align="center"><b>${typeEntry.key} Queries</b></td></tr>

  <c:set var="i" value="0"/>
  <c:forEach items="${questionSets}" var="qSet">
    <c:if test="${qSet.internal == false}">
      <c:set value="${qSet.questions}" var="questions"/>
      <c:forEach items="${questions}" var="q">
        <c:set var="recType1" value="${q.recordClass.type}"/>
        <c:if test="${recType1 == recType}">
          <c:set var="i" value="${i+1}"/>
          <c:choose>
            <c:when test="${i % 2 == 1}"><tr class="rowLight"></c:when>
            <c:otherwise><tr class="rowMedium"></c:otherwise>
          </c:choose>

          <td><b>${q.displayName}</b></td>
          <td><a href="<c:url value="/showQuestion.do?questionFullName=${q.fullName}"/>"><img src="<c:url value="/images/go.gif"/>" border="0"></td>
          <td>&nbsp;&nbsp;</td>
          <td><c:set var="desc" value="${q.description}"/>
              <c:if test="${fn:length(desc) > 163}">
                <c:set var="desc" value="${fn:substring(desc, 0, 160)}..."/>
              </c:if>
              ${desc}
          </td>
          </tr>
        </c:if>
      </c:forEach>
    </c:if>
  </c:forEach>

  <tr><td colspan="4">&nbsp;</td></tr>

</c:forEach>
</table>
