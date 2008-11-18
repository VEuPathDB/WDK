<%-- 
Provides form input element for a given EnumParam.

For a multi-selectable parameter a form element is provided as either a 
series of checkboxes or a multiselect menu depending on number of 
parameter options. Also, if number of options is over a threshhold, this tag
includes a checkAll button to select all options for the parameter.

Otherwise a standard select menu is used.
--%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<%@ attribute name="qp"
              type="org.gusdb.wdk.model.jspwrap.EnumParamBean"
              required="true"
              description="parameter name"
%>

<c:set var="qP" value="${qp}"/>
<c:set var="pNam" value="${qP.name}"/>
<c:set var="opt" value="0"/>
<c:set var="displayType" value="${qP.displayType}"/>

<!--<div class="param">-->

<c:choose>
<c:when test="${qP.multiPick}">
  <%-- multiPick is true, use checkboxes or scroll pane --%>
  <c:choose>
    <c:when test="${displayType eq 'checkBox' || (displayType == null && fn:length(qP.vocab) < 15)}"><%-- use checkboxes --%>
	 <div class="param multiPick">
      <c:set var="i" value="0"/>
      <table border="1" cellspacing="0"><tr><td>
      <c:forEach items="${qP.displayMap}" var="entity">
        <c:if test="${i == 0}"><c:set var="checked" value="checked"/></c:if>
        <c:if test="${i > 0}"><br></c:if>
        
        <c:choose>
        <%-- test for param labels to italicize --%>
        <c:when test="${pNam == 'organism' or pNam == 'ecorganism'}">
          <html:multibox property="myMultiProp(${pNam})" value="${entity.key}" styleId="${pNam}" />
          <i>${entity.value}</i>&nbsp;
        </c:when>
        <c:otherwise> <%-- use multiselect menu --%>
          <html:multibox property="myMultiProp(${pNam})" value="${entity.key}" styleId="${pNam}" />
          ${entity.value}&nbsp;
        </c:otherwise>
        </c:choose> 
        
        <c:set var="i" value="${i+1}"/>
        <c:set var="checked" value=""/>
      </c:forEach>
  
      <%@ include file="/WEB-INF/includes/selectAllParamOpt.jsp" %>
      
      </td>
      </tr>
      </table>
    </c:when>
    
    <%-- use a tree list --%>
    <c:when test="${displayType eq 'treeBox'}">
		<div class="param tree">
        <c:set var="recurse_enum_param" value="${qP}" scope="request"/>
        <c:forEach items="${qP.vocabTreeRoots}" var="root">
            <c:set var="recurse_term_node" value="${root}" scope="request"/>
            <c:import url="/WEB-INF/includes/enumParamInputNode.jsp"/>
        </c:forEach>
        <c:remove var="recurse_term_node" scope="request"/>
        <c:remove var="recurse_enum_param" scope="request"/>
    </c:when>

    <c:otherwise>
	  <div class="param multiPick">
      <html:select  property="myMultiProp(${pNam})" multiple="1" styleId="${pNam}">
        <c:set var="opt" value="${opt+1}"/>
        <c:set var="sel" value=""/>
        <c:if test="${opt == 1}"><c:set var="sel" value="selected"/></c:if>      
        <html:options property="values(${pNam})" labelProperty="labels(${pNam})" />
      </html:select>
  
      <%@ include file="/WEB-INF/includes/selectAllParamOpt.jsp" %>
  
    </c:otherwise>
</c:choose>
</div>
</c:when>
<c:otherwise>
  <div class="param">
  <%-- multiPick is false, use pull down menu --%>
  <html:select  property="myMultiProp(${pNam})" styleId="${pNam}">
    <c:set var="opt" value="${opt+1}"/>
    <c:set var="sel" value=""/>
    <c:if test="${opt == 1}"><c:set var="sel" value="selected"/></c:if>      
    <html:options property="values(${pNam})" labelProperty="labels(${pNam})"/>
  </html:select>
  </div>
</c:otherwise>
</c:choose>

<!--</div>-->
