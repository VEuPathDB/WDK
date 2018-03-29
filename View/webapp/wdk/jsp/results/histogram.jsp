<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<c:set var="attribute" value="${requestScope.attribute}" />
<c:set var="plugin" value="${requestScope.plugin}" />
<c:set var="question" value="${requestScope.question}" />
<c:set var="data" value="${requestScope.histogramData}" />
<c:set var="type" value="${requestScope.histogramType}" />
<c:set var="binSize" value="${requestScope.histogramBinSize}" />
<c:set var="binCount" value="${requestScope.histogramBinCount}" />
<c:set var="maxBinCount" value="${requestScope.histogramMaxBinCount}"/>
<c:set var="min" value="${requestScope.histogramMin}" />
<c:set var="max" value="${requestScope.histogramMax}" />
<c:set var="avg" value="${requestScope.histogramAvg}" />

<div id="${attribute.name}-${plugin.name}" class="histogram"
     data-controller="wdk.result.histogram.init"
     data-type="${type}"
     data-min="${min}"
     data-max="${max}"
     data-count="${binCount}"
     >
     <!-- <h2 align="center">${plugin.display}</h2> -->

  <ul>
    <li><a href="#graph">Graph</a></li>
    <li><a href="#data">Data</a></li>
  </ul>
  
  <div id="graph">
    <div class="plot-container">
      <div class="plot"> </div>
      <div class="y-axis-label"># of ${question.recordClass.displayNamePlural}</div>
      <div class="x-axis-label">${attribute.displayName}</div>
      <c:if test="${type ne 'category'}">
        <dl class="distribution">
          <dt>Avg</dt>
          <dd>${avg}</dd>
          <dt>Min</dt>
          <dd>${min}</dd>
          <dt>Max</dt>
          <dd>${max}</dd>
        </dl>
      </c:if>
    </div>

    <c:choose>
      <c:when test="${type != 'category'}">
        <div class="bin-control control-panel">
          <div class="control-panel-section">
            <label>
              <span class="input-label">Number of bins: </span>
              <input class="bin-count" type="number" min="1" max="${maxBinCount}" step="1" value="${binCount}" />
            </label>
            <input class="bin-count-slider" type="range" min="1" max="${maxBinCount}" step="1" value="${binCount}" />
          </div>
          <div class="control-panel-section">
            <label>
              <span class="input-label">Size of bins: </span>
              <input class="bin-size" type="number" min="${min}" max="${max}" step="${type eq 'float' ? 'any' : '1'}" value="${binSize}" />
            </label>
            <input class="bin-size-slider" type="range" min="${min}" max="${max}" step="${type eq 'float' ? 'any' : '1'}" value="${binSize}" />
          </div>
        </div>
      </c:when>
      <c:otherwise>
        <div class="bin-control">
          <input class="bin-size" type="hidden" value="${binSize}" />
        </div>
      </c:otherwise>
    </c:choose>  
    

    <div class="value-control control-panel">
      Choose column display:
      <label>
        <input name="display" class="normal" type="radio" value="Normal" checked="checked" /> Normal
      </label>
      <label>
        <input name="display" class="logarithm" type="radio" value="Logarithm" /> Logarithm
      </label>
    </div>
  </div>
  
  <div id="data">
    <div class="data">
      <c:forEach items="${data}" var="item">
        <span data-count="${data[item.key]}">${item.key}</span>
      </c:forEach>
    </div>

    <table class="datatable">
      <thead>
        <tr>
          <th class="label">${attribute.displayName}</th>
          <th class="count"># of ${question.recordClass.displayNamePlural}</th>

        </tr>
      </thead>
      <tbody>
        <c:forEach items="${data}" var="item">
          <tr>
            <td class="label">${item.key}</td>
            <td class="count">${data[item.key]}</td>
          </tr>
        </c:forEach>
      </tbody>
      <c:if test="${fn:length(data) > 10}">
        <tfoot>
          <tr>
            <th>${attribute.displayName}</th>
            <th>#Records</th>
          </tr>
        </tfoot>
      </c:if>
    </table>

    <div class="plot-data">
      <c:forEach items="${histogram}" var="item">
        <div class="data">
          <span class="bin">${item.key}</span>
          <span class="size">${histogram[item.key]}</span>
        </div>
      </c:forEach>
    </div>
  </div>
  
</div>
