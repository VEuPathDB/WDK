<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<c:set value="${sessionScope.wdkQuestion}" var="wdkQuestion"/>

<site:header banner="${wdkQuestion.displayName} question page" />

<p><b><jsp:getProperty name="wdkQuestion" property="description"/></b></p>

<hr>

<!-- all params of question -->

<html:form method="post" action="/showSummary.do">
<table>
<c:set value="${wdkQuestion.params}" var="qParams"/>
<c:forEach items="${qParams}" var="qP">
  <c:set value="${qP.name}" var="pNam"/>
  <tr><td align="right"><b><jsp:getProperty name="qP" property="prompt"/></b></td>
  <c:choose>
    <c:when test="${qP.class.name eq 'org.gusdb.wdk.model.FlatVocabParam'}">
      <td>
        <html:select  property="myProp(${pNam})">
          <html:options property="values(${pNam})" labelProperty="labels(${pNam})"/>
        </html:select>
      </td>
    </c:when>
    <c:otherwise>
      <td>
        <html:text property="myProp(${pNam})"/>
      </td>
    </c:otherwise>
  </c:choose>
      <td><i><jsp:getProperty name="qP" property="help"/></i></td>
  </tr>
</c:forEach>
  <tr><td></td>
      <td><html:submit value="Get Answer"/></td>
      <td></td>
</table>
</html:form>

<site:footer/>
