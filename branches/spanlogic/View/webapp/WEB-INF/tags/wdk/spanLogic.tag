<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="question" value="${requestScope.wdkQuestion}"/>
<c:set var="importStep" value="${requestScope.importStep}"/>
<c:set var="wdkStep" value="${requestScope.wdkStep}"/>
<c:set var="allowChooseOutput" value="${requestScope.allowChooseOutput}"/>


<style>
  #spanLogicParams, #spanLogicGraphics {
    float:left;
    margin:5px;
  }

  #spanLogicParams fieldset {
    float:left;
    border:1px solid gray;
	height:75px;
  }

  #spanLogicGraphics {
	
  }
 
  #spanLogicParams fieldset:first-of-type {
    margin-bottom: 5px;
  }
 
  #spanLogicParams fieldset:nth-of-type(2) {
    margin-top: 5px;
  }

  .invisible {
    visibility: hidden;
  }  

  .roundLabel {
    float:left;
    height: 3em;
    margin: 7px;
    width: 3em;
    text-align:center;
    border: 2px solid black;
    -moz-border-radius: 1.7em; /* Not sure why this doesn't work @ 1.5em */
  }

  .roundLabel span {
    font-size:2em;
    line-height: 1.5;
  }

  ul.horizontal.center {
    text-align: center;
  }
 
  ul.horizontal li {
    display: inline;
  }
  canvas{
	border:1px solid black;
  }
</style>
<c:set var="pMap" value="${question.paramsMap}"/>
<html:form styleId="form_question" method="post" enctype='multipart/form-data' action="/wizard.do"  onsubmit="callWizard('wizard.do?action=${requestScope.action}&step=${wdkStep.stepId}&',this,null,null,'submit')">
  <div id="spanLogicParams">
	<wdk:answerParamInput qp="${pMap['span_a']}"/>
	<wdk:answerParamInput qp="${pMap['span_b']}"/>
	<table>
		<tr>
			<td colspan="2">
    <div class="roundLabel"><span>1</span></div>
				Text for Step 1
			</td>
		</tr>
		<tr>
			<td>
    <fieldset id="setAFields">
      <table id="offsetOptions" cellpadding="2">
        <tr>
		<td><wdk:enumParamInput qp="${pMap['span_begin_a']}"/></td>
		<td><wdk:enumParamInput qp="${pMap['span_begin_direction_a']}"/></td>
		<td align="left" valign="top">
            <html:text styleId="span_begin_offset_a" property="value(span_begin_offset_a)" size="35" />
        </td>
        </tr>
        <tr>
		<td><wdk:enumParamInput qp="${pMap['span_end_a']}"/></td>
		<td><wdk:enumParamInput qp="${pMap['span_end_direction_a']}"/></td>
		<td align="left" valign="top">
            <html:text styleId="span_end_offset_a" property="value(span_end_offset_a)" size="35" />
        </td>
        </tr>
      </table>
    </fieldset>
		</td>
		<td>
		<canvas id="scaleA" width="400" height="75">
				This browser does not support Canvas Elements (probably IE) :(
		</canvas>
		</td>
		</tr>
		<tr>
			<td colspan="2">
    <div class="roundLabel clear"><span>2</span></div>
	Text for Step 2 </td>
		</tr>
		<tr>
			<td>
    <fieldset id="setBFields">
      <table id="offsetOptions" cellpadding="2">
        <tr>
          	<td><wdk:enumParamInput qp="${pMap['span_begin_b']}"/></td>
			<td><wdk:enumParamInput qp="${pMap['span_begin_direction_b']}"/></td>
			<td align="left" valign="top">
	            <html:text styleId="span_begin_offset_b" property="value(span_begin_offset_b)" size="35" />
	        </td>
	        </tr>
	        <tr>
			<td><wdk:enumParamInput qp="${pMap['span_end_b']}"/></td>
			<td><wdk:enumParamInput qp="${pMap['span_end_direction_b']}"/></td>
			<td align="left" valign="top">
	            <html:text styleId="span_end_offset_b" property="value(span_end_offset_b)" size="35" />
	        </td>
        </tr>
      </table>
    </fieldset>
</td>
<td>
	<canvas id="scaleB" width="400" height="75">
			This browser does not support Canvas Elements (probably IE) :(
	</canvas>
	</td>
</tr>
</table>

  <br>
<table width="100%">
	<tr>
		<td>  
<div class="roundLabel"><span>3</span></div> Text for Step 3
		</td>
	</tr>
	<tr>
		<td>
   			<table><tr>
			<td><wdk:enumParamInput qp="${pMap['span_operation']}"/></td>
			</tr></table>
		</td>
	</tr>
	<tr>
		<td>
<div class="roundLabel"><span>4</span></div>Text for Step 4
		</td>
	</tr>
	<tr>
		<td>
<table><tr>
    <td><wdk:enumParamInput qp="${pMap['span_strand']}"/></td>
</tr></table>
		</td>
	</tr>
	<tr>
		<td>
<div class="roundLabel"><span>5</span></div>Text for Step 5
		</td>
	</tr>
	<tr>
		<td>
<table><tr>
    <td style="line-height:1.5">Select Output Set:&nbsp;</td>
    <c:if test="allowBoolean == false">
      <c:set var="disabled" value="DISABLED"/>
      <c:set var="selected" value="CHECKED" />
      You cannot select output because there are steps in the strategy after the current one you are working on.
    </c:if>
    <!--<td><input type="radio" name="output" value="A" ${disabled} ${selected}>Set A</input></td>
    <td><input type="radio" name="output" value="B" ${disabled}>Set B</input></td>-->
	<wdk:enumParamInput qp="${pMap['span_output']}"/>
</tr></table>
		</td>
	</tr>
</table>

  </div>
<div class="filter-button"><html:submit property="questionSubmit" value="Run Step"/></div>
</html:form>
<script>
	initWindow();
</script>
