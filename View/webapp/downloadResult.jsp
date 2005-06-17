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
<pre>${requestScope.downloadResult}</pre>

  </c:otherwise>
</c:choose>

<site:footer/>
