<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="pg" uri="http://jsptags.com/tags/navigation/pager" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<c:set value="${requestScope.wdkAnswer}" var="wdkAnswer"/>

<c:set value="${wdkAnswer.question.displayName}" var="wdkQuestionName"/>
<site:header banner="${wdkQuestionName}" />

<c:set value="${wdkAnswer.params}" var="params"/>

<p><b>
Summary result for query "${wdkQuestionName}" with parameters:
<c:forEach items="${params}" var="p">
   ${p.key} = "${p.value}"; 
</c:forEach>
<br>Number of results returned:
${wdkAnswer.totalSize}<c:if test="${wdkAnswer.totalSize > 0}">,
showing ${wdk_paging_start} to ${wdk_paging_end} </c:if>
</b></p>

<hr>


<c:choose>
  <c:when test='${wdkAnswer.totalSize == 0}'>
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
<table border="0" cellpadding="2" cellspacing="0">
<tr class="headerRow">
<c:forEach items="${wdkAnswer.attributes}" var="attr">
<th>${attr.value.displayName}</th>
</c:forEach>

<c:set var="i" value="0"/>
<c:forEach items="${wdkAnswer.records}" var="record">

<c:choose>
  <c:when test="${i % 2 == 0}"><tr class="rowLight"></c:when>
  <c:otherwise><tr class="rowDark"></c:otherwise>
</c:choose>

  <c:set var="j" value="0"/>
  <c:forEach items="${record.attributes}" var="recAttr">
    <td>
    <c:choose>
      <c:when test="${j == 0}">

        <a href="showRecord.do?name=${record.recordClass.fullName}&id=${record.primaryKey}">${recAttr.value.value}</a>

      </c:when>
      <c:otherwise>
        ${recAttr.value.value}
      </c:otherwise>
    </c:choose>
    </td>
    <c:set var="j" value="${j+1}"/>
  </c:forEach>
</tr>
<c:set var="i" value="${i+1}"/>
</c:forEach>

</tr>
</table>

  <!-- pager at bottom -->
  <wdk:pager />
</pg:pager>

  </c:otherwise>
</c:choose>

<site:footer/>
