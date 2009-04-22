<%-- 
display the parameter values for an non-boolean answer.
--%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>

<%@ attribute name="history"
              type="org.gusdb.wdk.model.jspwrap.HistoryBean"
              required="true"
              description="History Bean Object"
%>

<div ${paddingStyle}>

<!-- simple question -->
<c:set value="${history.params}" var="params"/>
<c:set value="${history.question}" var="question"/>
<c:set value="${question.paramsMap}" var="qParamsMap"/>
<c:set value="${question.displayName}" var="wdkQuestionName"/>

<c:choose>
  <c:when test="${history.boolean}">
    <b><i>Boolean expression</i></b>: ${history.booleanExpression}
  </c:when>
  <c:otherwise>
    <table border="0" cellspacing="0" cellpadding="0">
        <tr>
            <td align="right" valign="top" class="medium"><b>Query</b></td>
            <td valign="top" class="medium">&nbsp;:&nbsp;</td>
            <td class="medium">${wdkQuestionName}</td>
        </tr>
        <tr>
            <td align="right" valign="top" class="medium"><b>Parameter</b></td>
            <td valign="top" class="medium">&nbsp;:&nbsp;</td>
            <td>
                <table border="0" cellspacing="0" cellpadding="0">
                    <c:forEach items="${qParamsMap}" var="p">
                       <c:set var="pNam" value="${p.key}"/>
                       <c:set var="qP" value="${p.value}"/>
                       <c:set var="aP" value="${params[pNam]}"/>
                       <c:if test="${qP.isVisible}">
                          <tr>
                             <td align="right" valign="top" nowrap class="medium"><b><i>${qP.prompt}</i><b></td>
                             <td valign="top" class="medium">&nbsp;:&nbsp;</td>
                             <td class="medium">
                                <c:choose>
                                   <c:when test="${qP.class.name eq 'org.gusdb.wdk.model.jspwrap.DatasetParamBean'}">
                                      <jsp:setProperty name="qP" property="combinedId" value="${aP}" />
                                      <c:set var="dataset" value="${qP.dataset}" />  
                                      "${dataset.summary}"
                                      <c:if test='${fn:length(dataset.uploadFile) > 0}'>
                                         from file &lt;${dataset.uploadFile}&gt;
                                      </c:if>
                                   </c:when>
                                   <c:when test="${qP.class.name eq 'org.gusdb.wdk.model.jspwrap.AnswerParamBean'}">
                                      <jsp:setProperty name="qP" property="historyKey" value="${aP}" />
                                      <%-- wdk:showParams wdkAnswer="${qP.history}" /--%>
                                      <c:set var="childHistory" value="${qP.history}" />
                                      Query#${childHistory.historyId} - ${childHistory.customName} (${childHistory.estimateSize} records)
                                   </c:when>
                                   <c:otherwise>
                                      <jsp:setProperty name="qP" property="paramValue" value="${aP}" />
                                      <jsp:setProperty name="qP" property="truncateLength" value="1000" />
                                      ${qP.decompressedValue}
                                   </c:otherwise>
                                </c:choose>
                             </td>
                          </tr>
                       </c:if>
                    </c:forEach>
                </table>
            </td>
        </tr>
        <!-- display filter info -->
        <c:set var="filter" value="${wdkAnswer.filter}" />
        <c:if test="${filter != null}">
            <tr>
                <td align="right" valign="top" class="medium"><b>Filter</b></td>
                <td valign="top" class="medium">&nbsp;:&nbsp;</td>
                <td class="medium">${filter.displayName}</td>
            </tr>
        </c:if>
    </table>
  </c:otherwise>
</c:choose>
</div>
