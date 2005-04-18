<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="pg" uri="http://jsptags.com/tags/navigation/pager" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="nested" uri="http://jakarta.apache.org/struts/tags-nested" %>

<!-- get wdkAnswer from requestScope -->
<c:set value="${requestScope.wdkAnswer}" var="wdkAnswer"/>

<!-- display page header with wdkAnswer's recordClass's type as banner -->
<c:set value="${wdkAnswer.recordClass.type}" var="wdkAnswerType"/>
<site:header banner="${wdkAnswerType} Results" />

<!-- display question and param values and result size for wdkAnswer -->
<c:choose>
  <c:when test="${wdkAnswer.isBoolean}">
    <!-- boolean question -->

    <table><tr><td valign="top" align="left"><b>Expanded Question:</b></td>
               <td valign="top" align="left">
                 <nested:root name="wdkAnswer">
                   <jsp:include page="/WEB-INF/includes/bqShowNode.jsp"/>
                 </nested:root>
               </td></tr>
           <tr><td valign="top" align="left"><b>Results:</b></td>
               <td valign="top" align="left">
                   ${wdkAnswer.resultSize}
                   <c:if test="${wdkAnswer.resultSize > 0}">
                    (showing ${wdk_paging_start} to ${wdk_paging_end})
                   </c:if></td></tr>
    </table>
  </c:when>
  <c:otherwise>
    <!-- simple question -->
    <c:set value="${wdkAnswer.params}" var="params"/>
    <c:set value="${wdkAnswer.question.displayName}" var="wdkQuestionName"/>
    <table><tr><td valign="top" align="left"><b>Query:</b></td>
               <td valign="top" align="left">${wdkQuestionName}</td></tr>
           <tr><td valign="top" align="left"><b>Parameters:</b></td>
               <td valign="top" align="left">
                 <table>
                   <c:forEach items="${params}" var="p">
                     <tr><td align="right">${p.key}:</td><td><i>${p.value}</i></td></tr> 
                   </c:forEach>
                 </table></td></tr>
           <tr><td valign="top" align="left"><b>Results:</b></td>
               <td valign="top" align="left">
                   ${wdkAnswer.resultSize}
                   <c:if test="${wdkAnswer.resultSize > 0}">
                    (showing ${wdk_paging_start} to ${wdk_paging_end})
                   </c:if></td></tr>
    </table>
  </c:otherwise>
</c:choose>

<hr>

<!-- handle empty result set situation -->
<c:choose>
  <c:when test='${wdkAnswer.resultSize == 0}'>
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

<c:forEach items="${wdkAnswer.summaryAttributeNames}" var="recAttrName">
  <c:set value="${wdkAnswer.question.recordClass.attributeFields[recAttrName]}" var="recAttr"/>
  <c:if test="${!recAttr.isInternal}">
    <th align="left">${recAttr.displayName}</th>
  </c:if>
</c:forEach>

<c:set var="i" value="0"/>
<c:forEach items="${wdkAnswer.records}" var="record">

<c:choose>
  <c:when test="${i % 2 == 0}"><tr class="rowLight"></c:when>
  <c:otherwise><tr class="rowDark"></c:otherwise>
</c:choose>

  <c:set var="j" value="0"/>

  <c:forEach items="${record.summaryAttributeNames}" var="recAttrName">
  <c:set value="${record.attributes[recAttrName]}" var="recAttr"/>
  <c:if test="${!recAttr.isInternal}">
 
    <td>
    <c:set var="recNam" value="${record.recordClass.fullName}"/>
    <c:set var="fieldVal" value="${recAttr.briefValue}"/>
    <c:choose>
      <c:when test="${j == 0}">

        <a href="showRecord.do?name=${recNam}&id=${record.primaryKey}">${fieldVal}</a>

      </c:when>
      <c:otherwise>

        <!-- need to know if fieldVal should be hot linked -->
        <c:choose>
          <c:when test="${fieldVal.class.name eq 'org.gusdb.wdk.model.LinkValue'}">
            <a href="${fieldVal.url}">${fieldVal.visible}</a>
          </c:when>
          <c:otherwise>
            ${fieldVal}
          </c:otherwise>
        </c:choose>

      </c:otherwise>
    </c:choose>
    </td>
    <c:set var="j" value="${j+1}"/>

  </c:if>
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
