<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<%--
Provides form input element for a given DatasetParam.
--%>

<%@ attribute name="qp"
              type="org.gusdb.wdk.model.jspwrap.DatasetParamBean"
              required="true"
              description="parameter name"
%>

<c:set var="qP" value="${qp}"/>
<c:set var="pNam" value="${qP.name}"/>
<c:set var="opt" value="0"/>
<c:set var="dsName" value="${pNam}_dataset"/>
<c:set var="wdkUser" value="${sessionScope.wdkUser}"/>
<c:set var="dataset" value="${requestScope[dsName]}" />  
<c:set var="recordClass" value="${qp.recordClass}" />
<c:set var="defaultType" value="${qp.defaultType}" />
<c:choose>
  <c:when test="${defaultType == 'file'}">    <c:set var="fileChecked">checked="checked"</c:set></c:when>
  <c:when test="${defaultType == 'basket'}">  <c:set var="basketChecked">checked="checked"</c:set></c:when>
  <c:when test="${defaultType == 'strategy'}"><c:set var="strategyChecked">checked="checked"</c:set></c:when>
  <c:otherwise><c:set var="dataChecked">checked="checked"</c:set></c:otherwise>
</c:choose>
<c:set var="noAction" value="${requestScope.action == null || requestScope.action == ''}" />

<div id="${qp.name}" class="param datasetParam"
     data-controller="wdk.component.datasetParam.init">

dataset: ${dataset}
<table id="${qp.name}">

  <c:if test="${dataset != null || defaultType != 'basket'}"> 
    <!-- display an input box for user to enter data -->
    <tr>
        <td align="left" valign="top" nowrap>
            <input type="radio" name="${qp.typeSubParam}" class="type" value="data" ${dataChecked} />
            Enter list:&nbsp;
        </td>
        <c:set var="datasetValues">
            <c:choose>
                <c:when test="${dataset != null}">
                    ${dataset.value}
                </c:when>
                <c:otherwise>
                    ${qP.default}
                </c:otherwise>
            </c:choose>
        </c:set>
        <td align="left">
            <textarea name="${qp.dataSubParam}" class="data" rows="5" cols="30">${datasetValues}</textarea>
        </td>
    </tr>
  </c:if>

  
    <c:if test="${defaultType ne 'basket' and noAction}">
        <!-- display an input box and upload file button -->
        <tr class="dataset-file">
            <td align="left" valign="top">
                <input type="radio" name="${qp.typeSubParam}" class="type" value="file" ${fileChecked} />
               Upload from a <i>text</i> file:&nbsp;
            </td>
            <td align="left">
                <html:file styleId="${qa.fileSubParam}" styleClass="file" property="value(${qa.fileSubParam})"/>
		<div class="type-ahead-help">Maximum size: 10MB.</div>
            </td>
        </tr>
    </c:if>
    
    
    <!-- display options for the parser -->
    <tr class="dataset-parsers">
      <td>Choose a format for the input list, or uplodaed file:</td>
      <td>
       <c:forEach items="${qP.parsers}" var="parser">
          <c:set var="checked">
            <c:if test="${(dataset != null && parser.name eq dataset.parserName) || (dataset eq null && parser.name eq 'list')}">checked="checked"</c:if>
          </c:set>
          <span title="${parser.description}">
            <input type="radio" name="${qp.parserSubParam}" class="parser" value="${parser.name}" ${checked} />
            ${parser.display}
          </span>
        </c:forEach>
      </td>
    </tr>

    <c:if test="${recordClass !=  null}">
      <!-- display option to use basket snapshot -->
      <c:if test="${recordClass.useBasket}">
        <c:set var="basketCount" value="${qp.basketCount}" />
        <c:set var="disabled"><c:if test="${basketCount == 0}">disabled="disabled"</c:if></c:set>
        <tr>
          <td colspan="2" align="left" valign="top" nowrap>
            <input type="radio" name="${qp.typeSubParam}" class="type" value="basket" ${basketChecked} ${disabled}/>
            Copy ${recordClass.displayNamePlural} from My Basket (${basketCount} ${recordClass.displayNamePlural})&nbsp;
          </td>
        </tr>
        
        <!-- display option to use strategy snapshot -->
        <c:set var="strategies" value="${qp.strategies}" />
        <c:if test="${fn:length(strategies) gt 0}">
          <tr>
              <td colspan="2" align="left" valign="top" nowrap>
                <input type="radio" name="${qp.typeSubParam}" class="type" value="strategy" ${strategyChecked}
                       onclick="chooseType('${pNam}', 'strategy');" />
                Copy from ${recordClass.displayName} strategy:
                <select name="${qp.strategySubParam}" class="strategy">
                  <c:forEach items="${strategies}" var="strategy">
                    <option value="${strategy.strategyId}">${strategy.name} (${strategy.estimateSize} ${recordClass.displayNamePlural})</option>
                  </c:forEach>
                </select>
              </td>
          </tr>
        </c:if>
      </c:if>
    
    </c:if>
    
    <!-- display an existing info -->
    <c:if test="${dataset ne null and fn:length(dataset.uploadFile) gt 0}">
        <tr>
            <td colspan="2" align="right">
                <i>Data was uploaded from: ${dataset.uploadFile}</i>
            </td>
        </tr>
    </c:if>
</table>

</div>
