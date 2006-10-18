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

  <c:if test="${param.type == null 
             or param.type eq recTypes[recTypeEntry.key] 
             or param.type == ''}" >

  <tr class="headerRow"><td colspan="4" align="center">
    <b>${recTypes[recTypeEntry.key]} Queries</b>
    
    <br>
    <c:if test="${param.cat != null}">
      <a href="${pageContext.request.requestURI}?type=${recTypes[recTypeEntry.key]}" id='allQueriesAndTools'>
         More ${recTypes[recTypeEntry.key]} Queries
      </a> | 
    </c:if>
    <c:if test="${param.type != null}">
      <a href="${pageContext.request.requestURI}" id='allQueriesAndTools'>
          All Queries and Tools
       </a>
     </c:if>
   </td></tr>

  <c:set var="i" value="0"/>
  <c:forEach items="${qByCat}" var="catEntry">
    <c:set var="cat" value="${catEntry.key}"/>

    <c:set var="questions" value="${catEntry.value}"/>

    <c:if test="${isMultiCat}">
      <c:if test="${cat == '' || cat == ' ' || cat == '_'}">
        <c:set var="cat" value="miscellaneous"/>
      </c:if>
    </c:if>

    <c:if test="${param.cat == null or param.cat eq cat or param.cat == ''}">
      <tr><td colspan="4">&nbsp;</td></tr>
      <tr class="rowDark" width="100%"><td colspan="4" align="center"><b><a name='${cat}'>${cat}</a></b></td></tr>

      <c:forEach items="${questions}" var="q">
        <c:set var="i" value="${i+1}"/>
        <c:choose>
          <c:when test="${i % 2 == 1}"><tr class="rowLight"></c:when>
          <c:otherwise><tr class="rowMedium"></c:otherwise>
        </c:choose>
  
        <td colspan="3">
            <a href="<c:url value="/showQuestion.do?questionFullName=${q.fullName}"/>">
            <font color="#000066"><b>${q.displayName}</b></font></a>
        </td>
        <td>
            <c:choose>
              <c:when test="${q.summary != null}">
                <c:set var="desc" value="${q.summary}"/>
              </c:when>
              <c:otherwise>
                <c:set var="desc" value="${q.description}"/>
                <c:if test="${fn:length(desc) > 163}">
                  <c:set var="desc" value="${fn:substring(desc, 0, 160)}..."/>
                </c:if>
              </c:otherwise>
            </c:choose>
            ${desc}
        </td>
        </tr>
      </c:forEach> <%-- forEach items=questions --%>
    </c:if>        <%-- if test=param.cat --%>
  </c:forEach>     <%-- forEach items=qByCat --%>
  <tr><td colspan="4">&nbsp;</td></tr>
  
  </c:if>          <%-- if param.type --%>
</c:forEach>       <%-- items=qByRecType --%>

</table>
