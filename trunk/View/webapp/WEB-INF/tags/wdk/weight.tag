<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<%@ attribute name="wdkModel"
              required="true"
              type="org.gusdb.wdk.model.jspwrap.WdkModelBean"
%>

<%@ attribute name="wdkQuestion"
              required="true"
              type="org.gusdb.wdk.model.jspwrap.QuestionBean"
%>

<%-- set the weight --%>
<c:if test="${wdkModel.useWeights && !wdkQuestion.isTransform}">
  <div name="All_weighting" class="param-group" type="ShowHide">
    <c:set var="display" value="none"/>
    <c:set var="image" value="plus.gif"/>
    <div class="group-title">
      <img class="group-handle" src='<c:url value="wdk/images/${image}" />'/>
      <span title="This is an optional number that will be assigned to all the results of this search; this 'weight' might later be used for sorting when doing unions in a strategy."> Give this search a weight</span>
    </div>
    <div class="group-detail" style="display:${display};text-align:center">
      <div class="group-description">
        <p><input type="text" name="weight" maxlength="9" value="${param.weight}" />  </p>
        <p>Optionally give this search a "weight" (for example 10, 200, -50, integer only).<br>In a search strategy, unions and intersects will sum the weights, giving higher scores to items found in multiple searches.</p>
        
      </div>
      <br>
    </div>
  </div>
</c:if>
