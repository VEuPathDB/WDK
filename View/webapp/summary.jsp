<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<c:set value="${sessionScope.wdkSummary}" var="wdkSummary"/>

<c:set value="${wdkSummary.question.displayName}" var="wdkQuestionName"/>
<site:header banner="${wdkQuestionName}" />

<c:set value="${wdkSummary.params}" var="params"/>
<!--c:set value="${sessionScope.questionForm.myProps}" var="params"/-->
<p><b>
Summary result for query "${wdkQuestionName}" with parameters:
<c:forEach items="${params}" var="p">
   ${p.key} = "${p.value}"; 
</c:forEach>
<br>Number of results returned: ${wdkSummary.totalSize}
</b></p>

<hr>

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

        <a href="showRecord.do?name=${record.record.fullName}&id=${recAttr.value.value}">${recAttr.value.value}</a>

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

<site:footer/>
