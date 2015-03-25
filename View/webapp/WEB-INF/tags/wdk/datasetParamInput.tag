<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>

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
<c:set var="dsName" value="${pNam}_raw"/>
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

<div id="${qp.name}" class="param datasetParam" name="${pNam}"
     data-controller="wdk.components.datasetParam.init">

  <table id="${qp.name}">

    <c:if test="${dataset != null || defaultType != 'basket'}"> 
      <!-- display an input box for user to enter data -->
      <tr class="subparam">
        <td align="left" style="vertical-align:top" nowrap>
          <input type="radio" name="${qp.typeSubParam}" class="type" value="data" ${dataChecked} />
          Enter a list of IDs or text:&nbsp;
        </td>
        <c:set var="datasetValues">
          <c:choose>
            <c:when test="${dataset != null}">
              ${dataset.content}
            </c:when>
            <c:when test="${requestScope[qp.dataSubParam] != null}">
              ${requestScope[qp.dataSubParam]}
            </c:when>
            <c:otherwise>
              ${qP.default}
            </c:otherwise>
          </c:choose>
        </c:set>
        <td>
          <textarea name="${qp.dataSubParam}" class="data" rows="5" cols="30">${datasetValues}</textarea>
        </td>
      </tr>
    </c:if>

		<c:set var="parsers" value="${qP.parsers}" />
		
    <c:if test="${defaultType ne 'basket' and noAction}">
      <!-- display an input box and upload file button -->
      <tr class="subparam">
        <td>
          <input type="radio" name="${qp.typeSubParam}" class="type" value="file" ${fileChecked} />
          Upload a text file:&nbsp;
        </td>
        <td>
          <html:file styleId="${qa.fileSubParam}" styleClass="file" property="value(${qp.fileSubParam})"/>
					<div class="type-ahead-help">Maximum size: 10MB. The file should contain the list of IDs. 
						<c:if test="${fn:length(parsers) gt 1}"> Alternatively, please use other file formats:</c:if>
					</div>
        </td>
      </tr>
    </c:if>
    
    
    <!-- display options for the parser -->
		<c:choose>
			<c:when test="${fn:length(parsers) gt 1}">
				<tr class="parsers">
					<td>
						&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
						<!--  Choose file format: -->
					</td>
					<td>
						<c:forEach items="${parsers}" var="parser">
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
			</c:when>
			<c:otherwise>
				<c:forEach items="${parsers}" var="parser">
					<input type="hidden" name="${qp.parserSubParam}" class="parser" value="${parser.name}" />
				</c:forEach>
			</c:otherwise>
		</c:choose>

    <c:if test="${recordClass !=  null}">
      <!-- display option to use basket snapshot -->
      <c:if test="${recordClass.useBasket}">
        <c:set var="basketCount" value="${qp.basketCount}" />
        <c:set var="disabled"><c:if test="${basketCount == 0}">disabled="disabled"</c:if></c:set>
        <tr class="subparam">
          <td nowrap>
            <input type="radio" name="${qp.typeSubParam}" class="type" value="basket" ${basketChecked} ${disabled}/>
            Copy from My Basket:&nbsp;
          </td>
          <td>
            ${basketCount} ${recordClass.displayNamePlural} will be copied from your Basket.
          </td>
        </tr>
        
        <!-- display option to use strategy snapshot -->
        <c:set var="strategies" value="${qp.strategies}" />
        <c:set var="stratCount" value="${fn:length(strategies)}" />
        <c:set var="currentStrategy" value="${requestScope.strategy}" />
        <c:set var="disabled">
          <c:if test="${(currentStrategy == null && stratCount lt 1) || (currentStrategy != null && stratCount lt 2)}">
            disabled="disabled"
          </c:if>
        </c:set>
        <tr class="subparam">
          <td nowrap>
            <input type="radio" name="${qp.typeSubParam}" class="type" value="strategy" ${strategyChecked} ${disabled} />
            Copy from My Strategy:&nbsp;
          </td>
          <td>
            <c:set var="currentStrategy" value="${requestScope.strategy}" />
            Choose a ${recordClass.displayName} strategy:
            <select name="${qp.strategySubParam}" class="strategy">
              <c:forEach items="${strategies}" var="strategy">
                <c:if test="${strategy.strategyId != currentStrategy}" >
                  <option value="${strategy.strategyId}">${strategy.name} (${strategy.estimateSize} ${recordClass.displayNamePlural})</option>
                </c:if>
              </c:forEach>
            </select>
          </td>
        </tr>
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
