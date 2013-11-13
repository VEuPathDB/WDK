<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<c:set var="attribute" value="${requestScope.attribute}" />
<c:set var="plugin" value="${requestScope.plugin}" />
<c:set var="data" value="${requestScope.histogramData}" />
<c:set var="type" value="${requestScope.histogramType}" />
<c:set var="binSize" value="${requestScope.histogramBinSize}" />
<c:set var="min" value="${requestScope.histogramMin}" />
<c:set var="max" value="${requestScope.histogramMax}" />

<div id="${attribute.name}-${plugin.name}" class="histogram"
     data-controller="wdk.result.histogram.init"
     data-type="${type}" data-min="${min}" data-max="${max}">
  <h2 align="center">${plugin.display}</h2>

  <ul>
    <li><a href="#graph">Graph</a></li>
    <li><a href="#data">Data</a></li>
  </ul>
  
  <div id="graph">
    <div class="plot"> </div>

    <div class="bin-control control-panel">
      <span>Set bin size:</span>
      <input class="bin-size" type="input" value="${binSize}" />
      <div class="bin-slider"></div>
    </div>

    <div class="value-control control-panel">
      Choose column display:
      <input name="display" class="normal" type="radio" value="Normal" checked="checked" />Normal
      <input name="display" class="logarithm" type="radio" value="Logarithm" />Logarithm
    </div>

    <div class="update">
      <input class="button" type="button" value="Update" />
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
          <th class="count"># of ${attribute.recordClass.displayNamePlural}</th>

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
