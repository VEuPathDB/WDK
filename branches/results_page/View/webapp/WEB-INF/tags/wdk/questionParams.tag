<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
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
  $(function() { wdk.tooltips.assignParamTooltips('.help-link'); });
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

<!-- =================== ALL PARAM GROUPS ====================================
        I think there is only one or  two groups in most of our queries;
        the first includes all the visible ones, the second is the advanced group
======================================================================================= -->
<c:forEach items="${paramGroups}" var="paramGroupItem">
    <c:set var="group" value="${paramGroupItem.key}" />
    <c:set var="paramGroup" value="${paramGroupItem.value}" />
  
    <%-- detemine starting display style by displayType of the group --%>
    <c:set var="groupName" value="${group.displayName}" />
    <c:set var="displayType" value="${group.displayType}" />

    <c:choose>
      <c:when test="${group.name eq 'advancedParams'}">
        <c:set var="advancedGroup" value="${group}"/>
        <c:set var="advancedParamGroup" value="${paramGroup}"/>
      </c:when>

      <c:otherwise>  <!-- REGULAR SET OF PARAMS -->
        <%-- if displayType is empty, then don't make collapsible --%>
        <c:set var="paramClass" value=""/>
        <c:set var="groupDisplay" value=""/>

        <c:choose>
          <%-- hide group if not visible -- based on visible attribute of params --%>
          <c:when test="${displayType eq 'hidden'}">
            <div name="${wdkQuestion.name}_${group.name}" class="param-group ${displayType}">
          </c:when>
          <c:when test="${displayType eq 'empty'}">
            <div name="${wdkQuestion.name}_${group.name}" class="param-group ${displayType} content-pane">
              <!-- <div class="group-title h4left">Parameters</div> -->
          </c:when>
          <c:otherwise>  <-- REGULAR SET OF PARAMS -->
            <c:if test="${group.visible ne true}">
              <c:set var="groupDisplay" value="hidden"/>
            </c:if>
            <div name="${wdkQuestion.name}_${group.name}" class="param-group ${displayType} collapsible content-pane">
              <div class="group-title h4left" title="Click to expand or collapse">${groupName}</div>
          </c:otherwise>
        </c:choose>

          <div class="group-detail ${groupDisplay}">
        
          <c:set var="paramCount" value="${fn:length(paramGroup)}"/>
          <%-- display parameter list --%>
          <imp:questionParamGroup paramGroup="${paramGroup}" />
        
          </div> <%-- end of group-detail div --%>

        </div> <%-- end of param-group div --%>
      </c:otherwise>
    </c:choose>

</c:forEach> <%-- end of foreach on paramGroups --%>



<!-- =================== ADVANCED PARAM SECTION, ====================================
        the advanced param group in loop above (if it exists, it is the second one) 
        is included in this div after the weight    imp:questionParamGroup          
======================================================================================= -->
<!-- use h4left and remove font-family style to go back to new search page style -->

<div name="${wdkQuestion.name}_${advancedGroup.name}" class="param-group ${advancedGroup.displayName} collapsible content-pane">
  <div title="Click to open/close this section" class="group-title h4center" style="font-family:Arial,Helvetica,sans-serif">
		Advanced Parameters</div>

  <div class="group-detail" style="display:none">
      <%-- weight param --%>
      <c:set var="weight" value="${param.weight}" />
      <c:if test="${weight == null || weight == ''}">
        <c:set var="weight" value="${10}" />
      </c:if>

      <div class="param-item">
        <label>
          <span style="font-weight:bold">Weight</span>
          <img class="help-link" style="cursor:pointer"
            title="Optionally give this search a &quot;weight&quot; (for example 10, 200, -50, integer only). In a search strategy, unions and intersects will sum the weights, giving higher scores to items found in multiple searches."
            src="${pageContext.request.contextPath}/wdk/images/question.png" />
        </label>
        <div class="param-control">
          <input type="text" name="weight" maxlength="9" value="${weight}"/>
        </div>
      </div>

      <%-- rest of Advanced params --%>
      <imp:questionParamGroup paramGroup="${advancedParamGroup}" />

  </div>
</div>
