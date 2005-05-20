<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="pg" uri="http://jsptags.com/tags/navigation/pager" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="nested" uri="http://jakarta.apache.org/struts/tags-nested" %>

<!-- get wdkAnswer from requestScope -->
<c:set value="${sessionScope.wdkAnswer}" var="wdkAnswer"/>

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
                    <!-- allow download -->
                    <html:form method="get" action="/processDownload.do">
                      <html:submit value="Download"/>
                      <html:checkbox property="chooseFields" value="off"/>
                    </html:form>
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

<!-- content of current page -->
<table border="0" cellpadding="2" cellspacing="0">
<tr class="headerRow">

<c:forEach items="${wdkAnswer.summaryAttributeNames}" var="recAttrName">
  <c:set value="${wdkAnswer.question.recordClass.attributeFields[recAttrName]}" var="recAttr"/>
  <c:if test="${!recAttr.isInternal}">
    <th align="left">${recAttr.displayName}</th>
  </c:if>
</c:forEach>

  </c:otherwise>
</c:choose>

<site:footer/>
