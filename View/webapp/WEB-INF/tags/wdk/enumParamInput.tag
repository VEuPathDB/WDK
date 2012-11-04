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
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<jsp:useBean id="idgen" class="org.gusdb.wdk.model.jspwrap.NumberUtilBean" scope="application" />

<%@ attribute name="qp"
              type="org.gusdb.wdk.model.jspwrap.EnumParamBean"
              required="true"
              description="parameter name"
%>

<%@ attribute name="layout"
			  required="false"
              description="parameter name"
%>

<c:set var="qP" value="${qp}"/>
<c:set var="pNam" value="${qP.name}"/>
<c:set var="opt" value="0"/>
<c:set var="displayType" value="${qP.displayType}"/>
<c:set var="dependedParamObj" value="${qP.dependedParam}"/>
<c:if test="${dependedParamObj != null}">
  <c:set var="dependedParam" value="${dependedParamObj.name}" />
  <c:set var="dependentClass" value="dependentParam" />
</c:if>
<%-- Setting a variable to display the items in the parameter in a horizontal layout --%>
<c:set var="v" value=""/>
<c:if test="${layout == 'horizontal'}">
	<c:set var="v" value="style='display:inline'"/>
</c:if>
	


<c:choose>
<c:when test="${qP.multiPick}">
  <%-- multiPick is true, use checkboxes or scroll pane --%>
  <c:choose>
    <c:when test="${displayType eq 'checkBox' or (displayType eq null and fn:length(qP.vocab) lt 15)}"><!-- use checkboxes -->
	 <div class="param-multiPick ${dependentClass}" dependson="${dependedParam}" name="${pNam}">
      <c:set var="i" value="0"/>
      <table border="1" cellspacing="0"><tr><td>

      <ul>
      <c:forEach items="${qP.displayMap}" var="entity">
        <c:if test="${i == 0}"><c:set var="checked" value="checked"/></c:if>
        <li>
        <c:choose>
        <%-- test for param labels to italicize --%>
        <c:when test="${pNam == 'organism' or pNam == 'ecorganism'}">
          <html:multibox property="array(${pNam})" value="${entity.key}" styleId="${pNam}" />
          <i>${entity.value}</i>&nbsp;
        </c:when>
        <c:otherwise> <%-- use multiselect menu --%>
          <html:multibox property="array(${pNam})" value="${entity.key}" styleId="${pNam}" />
          ${entity.value}&nbsp;
        </c:otherwise>
        </c:choose> 
        
        <c:set var="i" value="${i+1}"/>
        <c:set var="checked" value=""/>
        </li>
      </c:forEach>
      </ul>
&nbsp;<%@ include file="/WEB-INF/includes/selectAllParamOpt.jsp" %>
      
      </td>
      </tr>
      </table>
    </div>
    </c:when>
    
    <%-- use a tree list --%>
    <c:when test="${displayType eq 'treeBox'}">
    
      <c:if test="${qp.useCheckboxTree}">
        <div class="${dependentClass}" dependson="${dependedParam}" name="${pNam}">
          <imp:checkboxTree id="${pNam}CBT${idgen.nextId}" rootNode="${qP.paramTree}" checkboxName="array(${pNam})" buttonAlignment="left"/>
        </div>
      </c:if>
      
      <c:if test="${not qp.useCheckboxTree}">
				<div class="${dependentClass}" dependson="${dependedParam}" name="${pNam}">
				&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		            <div class="param-controls">
		            <%@ include file="/WEB-INF/includes/selectAllParamOpt.jsp" %>
		            | <a href="javascript:void(0)" onclick="expandCollapseAll(this, true);">expand all</a>
		            | <a href="javascript:void(0)" onclick="expandCollapseAll(this, false);">collapse all</a>
		            </div>
		        <c:set var="recurse_enum_param" value="${qP}" scope="request"/>
		        <c:forEach items="${qP.vocabTreeRoots}" var="root">
		            <c:set var="recurse_term_node" value="${root}" scope="request"/>
		            <c:import url="/WEB-INF/includes/enumParamInputNode.jsp"/>
		        </c:forEach>
		        <c:remove var="recurse_term_node" scope="request"/>
		        <c:remove var="recurse_enum_param" scope="request"/>
		
				&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		            <div class="param-controls">
		            <%@ include file="/WEB-INF/includes/selectAllParamOpt.jsp" %>
		            | <a href="javascript:void(0)" onclick="expandCollapseAll(this, true);">expand all</a>
		            | <a href="javascript:void(0)" onclick="expandCollapseAll(this, false);">collapse all</a>
		            </div><br><br>
		       </div>
		   </c:if>
		   
    </c:when>

    <c:otherwise>
	  <div class="param-multiPick ${dependentClass}" dependson="${dependedParam}" name="${pNam}">
      <html:select  property="array(${pNam})" multiple="1" styleId="${pNam}">
        <c:set var="opt" value="${opt+1}"/>
        <c:set var="sel" value=""/>
        <c:if test="${opt == 1}"><c:set var="sel" value="selected"/></c:if>      
        <html:options property="array(${pNam}-values)" labelProperty="array(${pNam}-labels)" />
      </html:select>
  
      <br><%@ include file="/WEB-INF/includes/selectAllParamOpt.jsp" %>
      </div>
    </c:otherwise>
</c:choose>
</c:when> <%-- end of multipick --%>
<c:otherwise> <%-- pick single item --%>
  <div class="param ${dependentClass}" dependson="${dependedParam}" name="${pNam}">
    <c:choose>
      <c:when test="${displayType eq 'radioBox'}">
         <ul>
         <c:forEach items="${qP.displayMap}" var="entity">
           <div ${v}>
             <html:radio property="array(${pNam})" value="${entity.key}" /> <span>${entity.value}</span>
           </li>
         </c:forEach>
         </ul>
      </c:when>
    
      <%-- use a type ahead --%>
      <c:when test="${displayType eq 'typeAhead'}">
        <input type="text" id="${pNam}_display" size="50" value=""/>
        <html:hidden styleClass="typeAhead" property="value(${pNam})" />
        <div class="type-ahead-help">You can enter wildcards as in *kinase*. <br>Or type three or more characters in the above textbox to get inline suggestions.</div>
      </c:when>

      <c:otherwise>
        <%-- multiPick is false, use pull down menu --%>
        <html:select  property="array(${pNam})" styleId="${pNam}">
          <c:set var="opt" value="${opt+1}"/>
          <c:set var="sel" value=""/>
          <c:if test="${opt == 1}"><c:set var="sel" value="selected"/></c:if>      
          <html:options property="array(${pNam}-values)" labelProperty="array(${pNam}-labels)"/>
        </html:select>
      </c:otherwise>
    </c:choose>
  </div>
</c:otherwise> <%-- end of pick single item --%>
</c:choose>


<%-- display invalid terms, if any. --%>
<c:set var="currentValues" value="${qP.currentValues}" />
<c:set var="invalid" value="${false}" />
<c:forEach items="${currentValues}" var="entry">
  <c:if test="${entry.value == false}">
    <c:set var="invalid" value="${true}" />
  </c:if>
</c:forEach>

<c:if test="${invalid}">
  <div class="invalid-values">
    <p>Some of the option(s) you previously selected are no longer available.</p>
    <p>Here is a list of the values you selected (unavailable options are marked in red):</p>
    <ul>
      <c:forEach items="${currentValues}" var="entry">
        <c:set var="style">
          <c:if test="${entry.value == false}">class="invalid"</c:if>
        </c:set>
        <li ${style}>${entry.key}</li>
      </c:forEach>
    </ul>
  </div>
</c:if>

