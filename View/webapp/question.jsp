<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<c:set value="${sessionScope.wdkQuestion}" var="wdkQuestion"/>
<jsp:useBean scope="request" id="helps" class="java.util.HashMap"/>

<site:header banner="${wdkQuestion.displayName} question page" />

<p><b><jsp:getProperty name="wdkQuestion" property="description"/></b></p>

<hr>

<!-- all params of question -->
<c:set value="Help for question: ${wdkQuestion.displayName}" var="fromAnchorQ"/>
<jsp:useBean id="helpQ" class="java.util.HashMap"/>

<!-- put an anchor here for linking back from help sections -->
<A name="${fromAnchorQ}"></A>
<html:form method="post" action="/showSummary.do">
<table>

<c:set value="${wdkQuestion.params}" var="qParams"/>
<c:forEach items="${qParams}" var="qP">

  <!-- an individual param -->
  <c:set value="${qP.name}" var="pNam"/>
  <tr><td align="right"><b><jsp:getProperty name="qP" property="prompt"/></b></td>

  <!-- choose between flatVocabParam and straight text or number param -->
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

      <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
      <td>
          <c:set var="anchorQp" value="HELP_${fromAnchorQ}_${pNam}"/>
          <c:set target="${helpQ}" property="${anchorQp}" value="${qP}"/>
          <a href="#${anchorQp}">
          <img src='<c:url value="/images/toHelp.jpg"/>' border="0" alt="Help!"></a>
      </td>
  </tr>

  <c:set target="${helps}" property="${fromAnchorQ}" value="${helpQ}"/>
</c:forEach>

  <tr><td></td>
      <td><html:submit value="Get Answer"/></td>
      <td></td>
</table>
</html:form>

<site:footer/>
