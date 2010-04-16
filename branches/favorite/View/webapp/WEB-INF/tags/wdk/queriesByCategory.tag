<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!-- show all questions in model -->

<%-- first pass to get all record types --%>
<c:set value="${wdkModel.rootCategoryMap}" var="rootCatMap"/>
<c:set value="${wdkModel.recordClassTypes}" var="recTypes"/>

<table width="100%" cellpadding="4">
<c:forEach items="${rootCatMap}" var="rootCatEntry">
  <c:set var="recType" value="${rootCatEntry.key}"/>
  <c:set var="rootCat" value="${rootCatEntry.value}"/>
  <c:set var="isMultiCat" value="${rootCat.multiCategory}"/>

  <c:if test="${param.type == null 
             or param.type eq recTypes[recType] 
             or param.type == ''}" >

  <tr class="headerRow"><td colspan="4" align="center">
    <b>${recTypes[recType]} Queries</b>
    
    <br>
    <c:if test="${param.cat != null}">
      <a href="${pageContext.request.requestURI}?type=${recTypes[recType]}" id='allQueriesAndTools'>
         More ${recTypes[recType]} Queries
      </a> | 
    </c:if>
    <c:if test="${param.type != null}">
      <a href="${pageContext.request.requestURI}" id='allQueriesAndTools'>
          All Queries and Tools
       </a>
     </c:if>
   </td></tr>

  <c:set var="i" value="0"/>
  <c:forEach items="${rootCat.children}" var="cat">
    <c:set var="catName" value="${cat.name}" />
    <c:set var="questions" value="${cat.questions}"/>

    <c:if test="${isMultiCat}">
      <c:if test="${catName == '' || catName == ' ' || catName == '_'}">
        <c:set var="catName" value="miscellaneous"/>
      </c:if>
    </c:if>

    <c:if test="${param.cat == null or param.cat eq cat.name or param.cat == ''}">
      <tr><td colspan="4">&nbsp;</td></tr>
      <tr class="rowDark" width="100%"><td colspan="4" align="center"><b><a name='${catName}'>${cat.displayName}</a></b></td></tr>

     <%-- Cristina reset per category, first rowMedium, then rowLight --%>    
     <c:set var="i" value="0"/>
      <c:forEach items="${questions}" var="q">
        <c:set var="i" value="${i+1}"/>
        <c:choose>
          <c:when test="${i % 2 == 1}"><tr class="rowMedium"></c:when>
          <c:otherwise><tr class="rowLight"></c:otherwise>
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
  </c:forEach>     <%-- forEach items=rootCat --%>
  <tr><td colspan="4">&nbsp;</td></tr>
  
  </c:if>          <%-- if param.type --%>
</c:forEach>       <%-- items=rootCatMap --%>

</table>
