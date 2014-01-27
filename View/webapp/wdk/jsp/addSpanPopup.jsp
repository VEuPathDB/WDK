<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="dType"><%= request.getParameter("dataType") %></c:set>
<c:set var="stepNum"><%= request.getParameter("prevStepNum") %></c:set>
<c:set var="add"><%= request.getParameter("isAdd") %></c:set>

<%--<imp:addStepPopup_new model="${applicationScope.wdkModel}" rcName="${dType}" prevStepNum="${stepNum}" isAdd="${add}"/>--%>
<c:set var="displayType" value=""/>
<c:choose>
<c:when test="${dType == 'OrfRecordClasses.OrfRecordClass'}"><c:set var="displayType" value="ORF"/></c:when>
<c:when test="${dType == 'EstRecordClasses.EstRecordClass'}"><c:set var="displayType" value="EST"/></c:when>
<c:when test="${dType == 'SnpRecordClasses.SnpRecordClass'}"><c:set var="displayType" value="SNP"/></c:when>
<c:when test="${dType == 'AssemblyRecordClasses.AssemblyRecordClass'}"><c:set var="displayType" value="Assembly"/></c:when>
<c:when test="${dType == 'SequenceRecordClasses.SequenceRecordClass'}"><c:set var="displayType" value="Genomic Sequence"/></c:when>
<c:when test="${dType == 'dynSpanRecordClasses.DynSpanRecordClass'}"><c:set var="displayType" value="Dynamic Span"/></c:when>
<c:when test="${dType == 'IsolateRecordClasses.IsolateReocrdClass'}"><c:set var="displayType" value="Isolate"/></c:when>
<c:when test="${dType == 'SageTageRecordClasses.SageTagRecordClass'}"><c:set var="displayType" value="SAGE Tag"/></c:when>
<c:otherwise>
	<c:set var="displayType" value="Gene"/>
</c:otherwise>
</c:choose>
<table><tr><td style="vertical-align:top">
	<!--	<span class="heading">Instructions</span>-->
		<ul id="directions">
      <li><imp:image title="The default region is the genomic location of the feature ID in the result" src="/wdk/images/step1.png" width="20"/> Define a region for the strategy results</li>
      <li><imp:image title="The default region is the genomic location of the feature ID in the result" src="/wdk/images/step2.png" width="20"/> Define a region for results in the new step</li>
      <li><imp:image src="/wdk/images/step3.png" width="20"/> Choose the operation to relate these two sets of regions</li>
      <li><imp:image src="/wdk/images/step4.png" width="20"/> Choose the strands to which the operation should apply</li>
      <li><imp:image title="Think of what you are looking for" src="/wdk/images/step5.png" width="20"/> Decide data type of the Result</li>
		</ul>
	</td>
	<td style="vertical-align:top">
<div id="span_popup" width="100%" align="center">
	
	<input type="hidden" name="value(span_a)" value="${stepNum}" id="spanA"/>
	<input type="hidden" value="${dType}" id="typeA"/>
	<input type="hidden" value="" id="typeB"/>
	<input type="hidden" name="value(span_b)" value="" id="spanB"/>
	<table>
		<tr><th><span class="heading">${displayType}&nbsp;Results</span></th><th></th><th><span class="heading" id="fromAjax">&nbsp;Results</span></th></tr>
		<tr>
			<td id="left">
				<div class="controls">
					<select name="value(span_begin_a)"><option value="start">start</option><option value="stop">stop</option></select>
					<select name="value(span_begin_direction_a)"><option value="+">+</option><option value="-">-</option></select>
					<input type="text" value="0" name="value(span_begin_offset_a)"/><br><br>
					<select name="value(span_end_a)"><option value="start">start</option><option value="stop" SELECTED>stop</option></select>
					<select name="value(span_end_direction_a)"><option value="+">+</option><option value="-" SELECTED>-</option></select>
					<input type="text" value="0" name="value(span_end_offset_a)"/><br><br>
				</div>
			</td>
			<td id="middle" style="border-bottom:1px solid #BBBBBB">
					<input checked type="radio" value="overlap" name="value(span_operation)" id="span-operation-overlap">&nbsp;&nbsp;<label>Overlap</label><br/>
					<input type="radio" value="a_contain_b" name="value(span_operation)" id="span-operation-containing">&nbsp;&nbsp;<label>Containing</label><br/>
					<input type="radio" value="b_contain_a" name="value(span_operation)" id="span-operation-contained">&nbsp;&nbsp;<label>Contained In</label><br/>
				</div>
			</td>
			<td id="right">
				<div class="controls">
					<select name="value(span_begin_b)"><option value="start">start</option><option value="stop">stop</option></select>
					<select name="value(span_begin_direction_b)"><option value="+">+</option><option value="-">-</option></select>
					<input type="text" value="0" name="value(span_begin_offset_b)"/><br><br>
					<select name="value(span_end_b)"><option value="start">start</option><option value="stop" SELECTED>stop</option></select>
					<select name="value(span_end_direction_b)"><option value="+">+</option><option value="-" SELECTED>-</option></select>
					<input type="text" value="0" name="value(span_end_offset_b)"/><br><br>
					
				</div>
			</td>
		</tr>
		<tr>
			<td id="left"><div style="text-align:center;vertical-align:middle;">GRAPHICS</div></td>
			<td id="middle">
				<input checked type="radio" value="Both strands" name="value(span_strand)" id="strand-operation-both">&nbsp;&nbsp;<label>Both Strands</label><br/>
				<input type="radio" value="Same strand" name="value(span_strand)" id="strand-operation-same">&nbsp;&nbsp;<label>Same Strand</label><br/>
				<input type="radio" value="Opposite strands" name="value(span_strand)" id="strand-operation-opposite">&nbsp;&nbsp;<label>Opposite Strand</label><br/>
			</td>
			<td id="right"><div style="text-align:center;vertical-align:middle;">GRAPHICS</div></td>
		</tr>
		<tr>
			<td id="left" style="text-align:center">
				<input type="radio" name="value(span_output)" value="type of input A" id="left_output"/>&nbsp;<label>Select this datatype for Result</label></td>
				<td id="middle"></td>
				<td id="right" style="text-align:center"><input type="radio" name="value(span_output)" value="type of input B" id="left_output"/>&nbsp;<label>Select this datatype for Result</label></td>
		</tr>
	</table>
	<input type="submit" value="Get Result" style="margin-top:5px"/>
	<div id="loading_data_gif" style="display:none" >
    <imp:image src="/wdk/images/getting-span-data.gif"/>
		<span>Processing...</span>
	</div>
  <imp:image class="step_img" id="step_1" src="/wdk/images/step1.png"/>
  <imp:image class="step_img" id="step_3" src="/wdk/images/step3.png"/>
  <imp:image class="step_img" id="step_2" src="/wdk/images/step2.png"/>
  <imp:image class="step_img" id="step_4" src="/wdk/images/step4.png"/>
  <imp:image class="step_img" id="step_5" src="/wdk/images/step5.png"/>
</div>
	</td></tr></table>
