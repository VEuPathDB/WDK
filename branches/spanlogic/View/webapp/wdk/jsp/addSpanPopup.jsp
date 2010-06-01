<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="dType"><%= request.getParameter("dataType") %></c:set>
<c:set var="stepNum"><%= request.getParameter("prevStepNum") %></c:set>
<c:set var="add"><%= request.getParameter("isAdd") %></c:set>

<%--<wdk:addStepPopup_new model="${applicationScope.wdkModel}" rcName="${dType}" prevStepNum="${stepNum}" isAdd="${add}"/>--%>

<div id="span_popup" width="100%" align="center">
	<input type="hidden" name="myProp(span_a)" value="${stepNum}" id="spanA"/>
	<input type="hidden" value="${dType}" id="typeA"/>
	<input type="hidden" value="" id="typeB"/>
	<input type="hidden" name="myProp(span_b)" value="" id="spanB"/>
	<table>
		<tr>
			<td id="left">
				<div class="controls">
					<select name="myProp(span_begin_a)"><option value="start">start</option><option value="stop">stop</option></select>
					<select name="myProp(span_begin_direction_a)"><option value="+">+</option><option value="-">-</option></select>
					<input type="text" value="0" name="myProp(span_begin_offset_a)"/><br><br>
					<select name="myProp(span_end_a)"><option value="start">start</option><option value="stop" SELECTED>stop</option></select>
					<select name="myProp(span_end_direction_a)"><option value="+">+</option><option value="-" SELECTED>-</option></select>
					<input type="text" value="0" name="myProp(span_end_offset_a)"/><br><br>
					<input type="radio" name="myProp(span_output)" value="type of input A" id="left_output"/><label>Select Set A as output</label>
				</div>
			</td>
			<td id="middle">
				<table class="operations" width="100%">
					<tr><td><label>Overlap</label></td><td><input type="radio" value="overlap" name="myProp(span_operation)" id="span-operation-overlap"></td></tr>
					<tr><td><label>Containing</label></td><td><input type="radio" value="a_contain_b" name="myProp(span_operation)" id="span-operation-containing"></td></tr>
					<tr><td><label>Contained In</label></td><td><input type="radio" value="b_contain_a" name="myProp(span_operation)" id="span-operation-contained"></td></tr>
				</table>
				</div>
			</td>
			<td id="right">
				<div class="controls">
					<select name="myProp(span_begin_b)"><option value="start">start</option><option value="stop">stop</option></select>
					<select name="myProp(span_begin_direction_b)"><option value="+">+</option><option value="-">-</option></select>
					<input type="text" value="0" name="myProp(span_begin_offset_b)"/><br><br>
					<select name="myProp(span_end_b)"><option value="start">start</option><option value="stop" SELECTED>stop</option></select>
					<select name="myProp(span_end_direction_b)"><option value="+">+</option><option value="-" SELECTED>-</option></select>
					<input type="text" value="0" name="myProp(span_end_offset_b)"/><br><br>
					<input type="radio" name="myProp(span_output)" value="type of intput B" id="right_output"/><label>Select Set B as output</label>
				</div>
			</td>
		</tr>
		<tr>
			<td><div style="text-align:center;vertical-align:middle;">GRAPHICS</div></td>
			<td>
				<table class="operations" width="100%">
					<tr><td><label>Both Strands</label></td><td><input type="radio" value="Both strands" name="myProp(span_strand)" id="strand-operation-both"></td></tr>
					<tr><td><label>Same Strand</label></td><td><input type="radio" value="Same strand" name="myProp(span_strand)" id="strand-operation-same"></td></tr>
					<tr><td><label>Opposite Strand</label></td><td><input type="radio" value="Opposite strand" name="myProp(span_strand)" id="strand-operation-opposite"></td></tr>
				</table>
			</td>
			<td><div style="text-align:center;vertical-align:middle;">GRAPHICS</div></td>
		</tr>
	</table><br><br>
	<input type="submit" value="Get Result"/>
</div>
