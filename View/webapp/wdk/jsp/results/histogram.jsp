<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<c:set var="attribute" value="${requestScope.attribute}" />
<c:set var="plugin" value="${requestScope.plugin}" />
<c:set var="summary" value="${requestScope.summary}" />
<c:set var="histogram" value="${requestScope.histogram}" />

<script type="text/javascript">
$(document).ready(function() {
  var histogram = $('#${plugin.name}');
  var summary = histogram.children("summary");
  
  // register the tab
  histogram.tabs();
  
  // draw the histogram
  var data = [];
  summary.find("td.data").each(function() {
    var bin = $(this).children(".bin").text();
    var size = $(this).children(".size").text();
    data.push(bin, size);
  });
  var options = {
    series: {
        bars: { show: true, fill: true, horizontal:false, align: "center" },
        points: { show: true },
    }
  };
  var plot = histogram.find(".graph .plot").plot(data, options);

  $.each(plot.getData()[0].data, function(i, el){
    var o = plot.pointOffset({x: el[0], y: el[1]});
    $('<div class="data-point-label">' + el[1] + '</div>').css( {
        position: 'absolute',
        left: o.left,
        top: o.top - 15,
        display: 'none'
    }).appendTo(plot.getPlaceholder()).fadeIn('slow');
  });

  // register the datatable on summary
  summary.children("datatable").dataTable( {
        "bJQueryUI": true,
        "bPaginate": false,
        "aoColumns": [ null, null, { "bSortable": false } ],
        "aaSorting": [[ 0, "asc" ]],
        "sDom": 'lrti'
    } );
} );
</script>

<div id="${plugin.name}" class="histogram">
  <h2 align="center">${plugin.display}</h2>

  <ul>
    <li><a href="#graph">Graph</li>
    <li><a href="#summary">Summary</li>
  </ul>
  <div id="graph">
    <div class="plot"> </div>
  </div>
  <div id="summary">
    <table class="datatable">
      <thead>
        <tr>
          <th>${attribute.displayName}</th>
          <th>#Records</th>
        </tr>
      </thead>
      <tbody>
      <c:forEach items="${histogram}" var="item">
        <tr class="data">
          <td class="bin">${item.key}</td>
          <td class="size">${summary[item.key]}</td>
        </tr>
      </c:forEach>
      </tbody>
    <c:if test="${fn:length(histogram) > 10}">
      <tfoot>
        <tr>
          <th>${attribute.displayName}</th>
          <th>#Records</th>
        </tr>
      </tfoot>
    </c:if>
    </table>
  </div>
  
</div>
