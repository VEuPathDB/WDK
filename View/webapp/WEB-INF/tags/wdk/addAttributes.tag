<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>

<%@ attribute name="wdkAnswer"
              required="true"
              type="org.gusdb.wdk.model.jspwrap.AnswerValueBean"
              description="the AnswerValueBean for this attribute list"
%>

<%@ attribute name="commandUrl"
              required="true"
              type="java.lang.String"
%>

<input type="button" onclick="openAttributeList(this);" class="addAttributesButton" value="Select Columns" />

<div class="attributesList formPopup">
  <span class="dragHandle">
  	<div class="modal_name">
		<h1 style="font-size:130%;margin-top:4px;" id="query_form_title">Select Columns</h1>
  	</div>
  	<a class='close_selcolwindow' href='javascript:void(0);' onclick='closeAttributeList(this);'>
		<img src="<c:url value='/wdk/images/Close-X-box.png'/>" alt='Close'/>
  	</a>
  </span>

  <div class="attributesFormWrapper">
  <form action="javascript:void(0);" onSubmit="updateAttrs(this,'${commandUrl}')">
    <div class="formButtonPanel">
      <input type="submit" value="Update Columns"/>
    </div>
    <c:if test="${wdkAnswer.useAttributeTree}">
      <input type="checkbox" name="selectedFields" value="${wdkAnswer.recordClass.primaryKeyAttribute.name}" checked="checked" style="display:none;"/>
      <wdk:checkboxTree rootNode="${wdkAnswer.displayableAttributeTree}" checkboxName="selectedFields" showSelectAll="false"/>
    </c:if>
    <c:if test="${not wdkAnswer.useAttributeTree}">
	    <div class="formButtonPanel">
	      <wdk:selectClearAll groupName="selectedFields" />
	    </div>
		  <c:set var="allAttributes" value="${wdkAnswer.displayableAttributes}" />
    	<c:set var="i" value="0"/>
    	<fmt:formatNumber var="columnSize" value="${(fn:length(allAttributes) + 1)/3}" pattern="0"/>
    	<table>
        <tr>
	        <c:forEach items="${allAttributes}" var="attribute">
	          <c:if test="${i == 0}">
	            <td>
	              <ul>
	          </c:if>
	          <c:set var="inputProps" value=""/>
	          <c:set var="j" value="0"/>
	          <c:forEach items="${wdkAnswer.summaryAttributes}" var="summary">
	            <c:if test="${attribute.name eq summary.name}">
	              <c:set var="inputProps" value="checked" />
	              <c:if test="${attribute.removable == false}">
	                <c:set var="inputProps" value="${inputProps} disabled" />
	              </c:if>
	            </c:if>
	            <c:set var="j" value="${j + 1}"/>
	          </c:forEach>
	          <li>
	            <input id="${attribute.name}" type="checkbox" name="selectedFields" value="${attribute.name}" title="${attribute.help}" ${inputProps} />
	            <label for="${attribute.name}" title="${attribute.help}">${attribute.displayName}</label>
	          </li>
	          <c:set var="i" value="${i + 1}"/>
	          <c:if test="${i == columnSize}">
	              </ul>
	            </td>
	            <c:set var="i" value="0"/>
	          </c:if>
	        </c:forEach>
        </tr>
      </table>
      <div class="formButtonPanel">
        <wdk:selectClearAll groupName="selectedFields" />
      </div>
    </c:if>
    <div class="formButtonPanel">
      <input type="submit" value="Update Columns"/>
    </div>
  </form>
  </div>

</div>  <%--   class="attributesList formPopup"  --%>
