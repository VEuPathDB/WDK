<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<jsp:useBean id="wdkModel" scope="application" class="org.gusdb.wdk.model.WdkModel"/>

<c:set value="${wdkModel.name}" var="wdkModelName"/>
<site:header banner="${wdkModelName} main page" />

<p><b><jsp:getProperty name="wdkModel" property="introduction"/></b></p>

<hr>

<!-- all questionSets in model -->
<table>
<c:set value="${wdkModel.allSummarySets}" var="questionSets"/>
<c:forEach items="${questionSets}" var="qSet">
  <tr><td bgcolor="lightblue"><jsp:getProperty name="qSet" property="description"/></td></tr>
  <tr><td><!-- list of questions in a questionSet -->
          <html:form method="post" action="/showQuestion.do">
          <html:select property="questionFullName">
            <c:set value="${qSet.summaries}" var="questions"/>
            <c:forEach items="${questions}" var="q">
            <c:set value="${q.fullName}" var="qFullName"/>
            <c:set value="${q.query.displayName}" var="qName"/>
              <html:option value="${qFullName}">${qName}</html:option>
            </c:forEach>
          </html:select>
          <html:submit value="Show Question"/>
          </html:form>
       </td>
</c:forEach>
</table>

<site:footer/>
