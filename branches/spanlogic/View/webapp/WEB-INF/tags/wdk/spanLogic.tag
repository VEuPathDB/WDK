<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

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

<form>
  <div id="spanLogicParams">
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
          <td>begin</td>
          <td align="left">
            <select name="upstreamAnchor" onchange="redraw(true,'A')">
              <option value="Start" selected>Start</option>
              <option value="CodeStart">translation start (ATG)</option>
              <option value="CodeEnd">translation stop codon</option>
              <option value="End">Stop</option>
            </select>
          </td>
          <td align="left">
            <select name="upstreamSign" onchange="redraw(true,'A')">
              <option value="plus" selected>+</option>
              <option value="minus">-</option>
            </select>
	  </td>
          <td align="left">
            <input id="upstreamOffset" name="upstreamOffset" value="0" size="6" onchange="redraw(true,'A')"/> nucleotides
          </td>
        </tr>
        <tr>
          <td>end</td>
          <td align="left">
            <select name="downstreamAnchor" onchange="redraw(true,'A')">
              <option value="Start">Start</option>
              <option value="CodeStart">translation start (ATG)</option>
              <option value="CodeEnd">translation stop codon</option>
              <option value="End" selected>Stop</option>
            </select>
          </td>
          <td align="left">
            <select name="downstreamSign" onchange="redraw(true,'A')">
              <option value="plus" selected>+</option>
              <option value="minus">-</option>
            </select>
          </td>
          <td align="left">
            <input id="downstreamOffset" name="downstreamOffset" value="0" size="6" onchange="redraw(true,'A')"> nucleotides
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
          <td>begin</td>
          <td align="left">
            <select name="upstreamAnchor" onchange="redraw(true,'B')">
              <option value="Start" selected>Start</option>
              <option value="CodeStart">translation start (ATG)</option>
              <option value="CodeEnd">translation stop codon</option>
              <option value="End">Stop</option>
            </select>
          </td>
          <td align="left">
            <select name="upstreamSign" onchange="redraw(true,'B')">
              <option value="plus" selected>+</option>
              <option value="minus">-</option>
            </select>
	  </td>
          <td align="left">
            <input id="upstreamOffset" name="upstreamOffset" value="0" size="6" onchange="redraw(true,'B')"/> nucleotides
          </td>
        </tr>
        <tr>
          <td>end</td>
          <td align="left">
            <select name="downstreamAnchor" onchange="redraw(true,'B')">
              <option value="Start">Start</option>
              <option value="CodeStart">translation start (ATG)</option>
              <option value="CodeEnd">translation stop codon</option>
              <option value="End" selected>Stop</option>
            </select>
          </td>
          <td align="left">
            <select name="downstreamSign" onchange="redraw(true,'B')">
              <option value="plus" selected>+</option>
              <option value="minus">-</option>
            </select>
          </td>
          <td align="left">
            <input id="downstreamOffset" name="downstreamOffset" value="0" size="6" onchange="redraw(true,'B')"/> nucleotides
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
      <td style="margin-bottom:5px;"><input type="radio" name="relationship" value="overlaps">Overlaps with</input></td>
      <td style=""><input type="radio" name="relationship" value="contains">Containing</input></td>
      <td style=""><input type="radio" name="relationship" value="contained">Contained within</input></td>
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
    <td style="line-height:1.5">Select Strand:&nbsp;</td>
    <td><input type="radio" name="strand" value="either">Either</input></td>
    <td><input type="radio" name="strand" value="both">Both</input></td>
    <td><input type="radio" name="strand" value="same">Same</input></td>
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
    <td><input type="radio" name="output" value="A" ${disabled} ${selected}>Set A</input></td>
    <td><input type="radio" name="output" value="B" ${disabled}>Set B</input></td>
</tr></table>
		</td>
	</tr>
</table>
  </div>
</form>
<script>
	initWindow();
</script>