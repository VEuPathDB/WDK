<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="pg" uri="http://jsptags.com/tags/navigation/pager" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<c:set value="${sessionScope.wdkSummary}" var="wdkSummary"/>

<c:set value="${wdkSummary.question.displayName}" var="wdkQuestionName"/>
<site:header banner="${wdkQuestionName}" />

<c:set value="${wdkSummary.params}" var="params"/>

<p><b>
Summary result for query "${wdkQuestionName}" with parameters:
<c:forEach items="${params}" var="p">
   ${p.key} = "${p.value}"; 
</c:forEach>
<br>Number of results returned:
${wdkSummary.totalSize}<c:if test="${wdkSummary.totalSize > 0}">,
showing ${wdk_paging_start} to ${wdk_paging_end} </c:if>
</b></p>

<hr>


<c:choose>
  <c:when test='${wdkSummary.totalSize == 0}'>
    No results for your query
  </c:when>
  <c:otherwise>

<!-- pager -->
<pg:pager isOffset="true"
          scope="request"
          items="${wdk_paging_total}"
          maxItems="${wdk_paging_total}"
          url="${wdk_paging_url}"
          maxPageItems="${wdk_paging_pageSize}"
          export="currentPageNumber=pageNumber">
  <c:forEach var="paramName" items="${wdk_paging_params}">
    <pg:param name="${paramName}" id="pager" />
  </c:forEach>
  <!-- pager on top -->
  <wdk:pager /> 

<!-- content of current page -->
<table border="2">
<tr>
<c:forEach items="${wdkSummary.attributes}" var="attr">
<th>${attr.value.displayName}</th>
</c:forEach>

<c:forEach items="${wdkSummary.records}" var="record">
<tr>
  <c:set var="i" value="0"/>
  <c:forEach items="${record.attributes}" var="recAttr">
    <td>
    <c:choose>
      <c:when test="${i == 0}">

        <a href="showRecord.do?name=${record.recordClass.fullName}&id=${record.primaryKey}">${recAttr.value.value}</a>

      </c:when>
      <c:otherwise>
        ${recAttr.value.value}
      </c:otherwise>
    </c:choose>
    </td>
    <c:set var="i" value="${i+1}"/>
  </c:forEach>
</tr>
</c:forEach>

</tr>
</table>

  <!-- pager at bottom -->
  <wdk:pager />
</pg:pager>

  </c:otherwise>
</c:choose>

<site:footer/>
