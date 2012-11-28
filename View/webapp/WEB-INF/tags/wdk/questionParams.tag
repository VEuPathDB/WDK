<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<%-- get wdkQuestion; setup requestScope HashMap to collect help info for footer --%>
<c:set var="wdkQuestion" value="${requestScope.wdkQuestion}"/>

<c:set var="qForm" value="${requestScope.questionForm}"/>
<c:set var="wdkModel" value="${applicationScope.wdkModel}"/>

<%-- show all params of question, collect help info along the way --%>
<c:set value="Help for question: ${wdkQuestion.displayName}" var="fromAnchorQ"/>
<jsp:useBean id="helpQ" class="java.util.LinkedHashMap"/>

<c:set value="${wdkQuestion.paramMapByGroups}" var="paramGroups"/>

<c:if test="${not empty wdkQuestion.customJavascript}">
  <script type="text/javascript" src="${pageContext.request.contextPath}/wdkCustomization/js/questions/${wdkQuestion.customJavascript}"></script>
</c:if>

<script type="text/javascript">
  $(function() { assignParamTooltips('.help-link'); });
</script>

<c:set var="invalidParams" value="${requestScope.invalidParams}" />
<c:if test="${fn:length(invalidParams) != 0}">
  <div class="invalid-params">
    <p>Some of the parameters are no longer used. Here are the values you selected previously:</p>
    <ul>
      <c:forEach items="${invalidParams}" var="entry">
        <li>${entry.key} : ${entry.value}</li>
      </c:forEach>
    </ul>
  </div>
</c:if>

<c:forEach items="${paramGroups}" var="paramGroupItem">
    <c:set var="group" value="${paramGroupItem.key}" />
    <c:set var="paramGroup" value="${paramGroupItem.value}" />
  
    <%-- detemine starting display style by displayType of the group --%>
    <c:set var="groupName" value="${group.displayName}" />
    <c:set var="displayType" value="${group.displayType}" />
    <div name="${wdkQuestion.name}_${group.name}"
         class="param-group" 
         type="${displayType}">
    <c:choose>
        <c:when test="${displayType eq 'empty'}">
            <%-- output nothing else --%> 
            <div class="group-detail">
        </c:when>
        <c:when test="${displayType eq 'ShowHide'}">
            <c:set var="display">
                <c:choose>
                    <c:when test="${group.visible}">block</c:when>
                    <c:otherwise>none</c:otherwise>
                </c:choose>
            </c:set>
            <c:set var="image">
                <c:choose>
                    <c:when test="${group.visible}">minus.gif</c:when>
                    <c:otherwise>plus.gif</c:otherwise>
                </c:choose>
            </c:set>
            <div class="group-title">
                <img class="group-handle" src='wdk/images/${image}' />
                ${groupName}
            </div>
            <div class="group-detail" style="display:${display};">
                <div class="group-description">${group.description}</div>
        </c:when>
        <c:otherwise>
            <div class="group-title">${groupName}</div>
            <div class="group-detail">
                <div class="group-description">${group.description}</div>
        </c:otherwise>
    </c:choose>
    
    <table border="0" width="100%">
    
    <c:set var="paramCount" value="${fn:length(paramGroup)}"/>
    <%-- display parameter list --%>
    <c:forEach items="${paramGroup}" var="paramItem">
        <c:set var="pNam" value="${paramItem.key}" />
        <c:set var="qP" value="${paramItem.value}" />
        
        <c:set var="isHidden" value="${qP.isVisible == false}"/>
        <c:set var="isReadonly" value="${qP.isReadonly == true}"/>
        <c:set var="paramType" value="${qP.type}" />

        <%-- hide invisible params --%>
        <c:choose>
            <c:when test="${paramType eq 'TimestampParam'}">
                <imp:timestampParamInput qp="${qP}" />
            </c:when>
            <c:when test="${isHidden}">
               <html:hidden property="value(${pNam})"/>
            </c:when>
            <c:otherwise> <%-- visible param --%>
                <%-- an individual param (can not use fullName, w/ '.', for mapped props) --%>
                <tr>
                    <td width="30%" align="right" style="vertical-align:top">
                        <span style="font-weight:bold">${qP.prompt}</span> <img class="help-link" style="cursor:pointer" title="${fn:escapeXml(qP.help)}" src="${pageContext.request.contextPath}/wdk/images/question.png" />
                    </td>
                    <c:choose>
                        <c:when test="${paramType eq 'EnumParam' || paramType eq 'FlatVocabParam'}">
                            <td align="left" style="vertical-align:bottom" id="${qP.name}aaa">
                                <imp:enumParamInput qp="${qP}" />
                            </td>
                        </c:when>
                        <c:when test="${paramType eq 'AnswerParam'}">
                            <td align="left" valign="top">
                                <imp:answerParamInput qp="${qP}" />
                            </td>
                        </c:when>
                        <c:when test="${paramType eq 'DatasetParam'}">
                            <td align="left" valign="top">
                                <imp:datasetParamInput qp="${qP}" />
                            </td>
                        </c:when>
                        <c:when test="${paramType eq 'StringParam'}">
                            <td align="left" valign="top">
                                <imp:stringParamInput qp="${qP}" />
                            </td>
                        </c:when>
                        <c:otherwise>  <%-- not enumParam --%>
                            <c:choose>
                                <c:when test="${isReadonly}">
                                    <td align="left" valign="top">
                                        <bean:write name="qForm" property="value(${pNam})"/>
                                        <html:hidden property="value(${pNam})"/>
                                    </td>
                                </c:when>
                                <c:when test="${qP.class.name eq 'org.gusdb.wdk.model.jspwrap.StringParamBean' and qP.multiLine}">
                                    <td align="left" valing="top">
                                        <html:textarea styleId="${pNam}" property="value(${pNam})" rows="4" cols="50"/>
                                    </td>
                                </c:when>
                                <c:otherwise>
                                    <td align="left" valign="top">
                                      <div class="ui-state-error ui-corner-all">
                                        Unknown param type
                                      </div>
                                    </td>
                                </c:otherwise>
                            </c:choose>
                        </c:otherwise>
                    </c:choose>
                </tr>
            </c:otherwise> <%-- end visible param --%>
        </c:choose>
        
        </c:forEach> <%-- end of forEach params --%>

        <%-- weight param --%>
        <c:set var="weight" value="${param.weight}" />
        <c:if test="${weight == null || weight == ''}">
          <c:set var="weight" value="${10}" />
        </c:if>

        <tr>
          <td width="30%" align="right" style="vertical-align:top">
              <span style="font-weight:bold">Weight</span>
              <img class="help-link" style="cursor:pointer"
              title="Optionally give this search a &quot;weight&quot; (for example 10, 200, -50, integer only). In a search strategy, unions and intersects will sum the weights, giving higher scores to items found in multiple searches."
              src="${pageContext.request.contextPath}/wdk/images/question.png" />
          </td>
          <td align="left" valign="top">
            <input type="text" name="weight" maxlength="9" value="${weight}"/>
          </td>
        </tr>

        <%-- custom name param --%>
        <tr>
          <td width="30%" align="right" style="vertical-align:top">
              <span style="font-weight:bold">Search name</span>
              <img class="help-link" style="cursor:pointer"
              title="Give this search a descriptive name."
              src="${pageContext.request.contextPath}/wdk/images/question.png" />
          </td>
          <td align="left" valign="top">
            <input type="text" name="customName" maxlength="9" value="${customName}"/>
          </td>
        </tr>

        </table>
    
        </div> <%-- end of group-detail div --%>
    </div> <%-- end of param-group div --%>

</c:forEach> <%-- end of foreach on paramGroups --%>

<%-- <imp:weight wdkModel="${wdkModel}" wdkQuestion="${wdkQuestion}" /> --%>


<%-- set the custom name --%>
<%--
<div name="All_weighting" class="param-group" type="ShowHide">
	  <c:set var="display" value="none"/>
  <c:set var="image" value="plus.gif"/>
  <div class="group-title">
	 <img class="group-handle" src='<c:url value="wdk/images/${image}" />'/>
   	 <span title="This name will be the name of the step.">Give this search a name</span>
  </div>
  <div class="group-detail" style="display:${display};text-align:center">
    <div class="group-description">
      <p><html:text property="customName" maxlength="15" value="${customName}" />  </p>
    </div><br>
  </div>
</div>

--%>
