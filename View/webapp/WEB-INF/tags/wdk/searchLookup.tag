<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<c:set var="wdkModel" value="${applicationScope.wdkModel}" />

<style TYPE="text/css"> 

#search-lookup-panel { margin:20px 5px 10px 220px; background-color:#EEDEFF; }
#search-lookup_wrapper #type-filter { display:inline; }
#search-lookup td { border: 1px dotted #CCCCCC; }
#search-lookup td.type { font-weight: bold; text-align: right; padding-right: 5px; }
#search-lookup td > div.content { display: none; }
.content ul { padding: 3px 3px 3px 15px; list-style: disc outside none; }
.content li { padding-bottom: 3px; }
.content h3 { margin: 15px 0px 0px 0px; padding: 10px; background-color: #EEEEEE; }

</style>

<script type="text/javascript">

$(document).ready(function() {
/*
			 * Function: fnGetColumnData
			 * Purpose:  Return an array of table values from a particular column.
			 * Returns:  array string: 1d data array 
			 * Inputs:   object:oSettings - dataTable settings object. This is always the last argument past to the function
			 *           int:iColumn - the id of the column to extract the data from
			 *           bool:bUnique - optional - if set to false duplicated values are not filtered out
			 *           bool:bFiltered - optional - if set to false all the table data is used (not only the filtered)
			 *           bool:bIgnoreEmpty - optional - if set to false empty values are not filtered from the result array
			 * Author:   Benedikt Forchhammer <b.forchhammer /AT\ mind2.de>
			 */
			$.fn.dataTableExt.oApi.fnGetColumnData = function ( oSettings, iColumn, bUnique, bFiltered, bIgnoreEmpty ) {
				// check that we have a column id
				if ( typeof iColumn == "undefined" ) return new Array();
				
				// by default we only wany unique data
				if ( typeof bUnique == "undefined" ) bUnique = true;
				
				// by default we do want to only look at filtered data
				if ( typeof bFiltered == "undefined" ) bFiltered = true;
				
				// by default we do not wany to include empty values
				if ( typeof bIgnoreEmpty == "undefined" ) bIgnoreEmpty = true;
				
				// list of rows which we're going to loop through
				var aiRows;
				
				// use only filtered rows
				if (bFiltered == true) aiRows = oSettings.aiDisplay; 
				// use all rows
				else aiRows = oSettings.aiDisplayMaster; // all row numbers
			
				// set up data array	
				var asResultData = new Array();
				
				for (var i=0,c=aiRows.length; i<c; i++) {
					iRow = aiRows[i];
					var aData = this.fnGetData(iRow);
					var sValue = aData[iColumn];
					
					// ignore empty values?
					if (bIgnoreEmpty == true && sValue.length == 0) continue;
			
					// ignore unique values?
					else if (bUnique == true && jQuery.inArray(sValue, asResultData) > -1) continue;
					
					// else push the value onto the result data array
					else asResultData.push(sValue);
				}
				
				return asResultData;
			};


    $("input[type=button]").button();
    var searchTable = $('#search-lookup').dataTable( {
        "bJQueryUI": true,
        "aaSorting": [ [ 3, 'desc'] ],
        "sDom": '<"H"f<"#type-filter">lr>t<"F"ip>',
        "oLanguage": {
            "sSearch": "Keyword:",
            "sLengthMenu": "Show _MENU_ rows"
        },
    } );

    // get the type info, and create a dropdown list
    var aData = searchTable.fnGetColumnData(0);
    var select=' &nbsp;Limit to <select><option value=""><b>All</b></option>'; 
    for (var i=0 ; i<aData.length ; i++ ) {
        select += '<option value="'+aData[i]+'">'+aData[i]+'</option>';
    }
    select += '</select> searches. &nbsp;';
    var typeFilter = $('#search-lookup_wrapper #type-filter');
    typeFilter.html(select);
    typeFilter.children("select").change( function () {
        searchTable.fnFilter( $(this).val(), 0 );
    } );
} );

function toggleContent(ele) {
    $(ele).siblings("div.content").clone().dialog({width: 600});
}

</script>

<div id="search-lookup-panel">
<h2 align="center">Find a search</h2>
<table id="search-lookup" class="datatables" width="100%">
  <thead>
    <tr>
      <th class="type" align="right">Type</th>
      <th class="display">Search Name</th>
      <th class="category">Category</th>
      <th class="popularity">Popularity</th>
      <th class="description">Description</th>
    </tr>
  </thead>
  <tbody>
  <c:set var="popularParam" value="${wdkModel.params['sharedParams.question_popularity']}" />
  <c:set var="popularMap" value="${popularParam.displayMap}" />
  <c:forEach items="${wdkModel.websiteQuestions}" var="item">
    <c:set var="question" value="${item.key}" />
    <c:set var="category" value="${item.value}" />
    <tr>
      <td class="type">${question.recordClass.type}</td>
      <td><a href="<c:url value='/showQuestion.do?questionFullName=${question.fullName}'/>"
          >${question.displayName}</a>
      </td>
      <td>${category.displayName}</td>
      <td>${popularMap[question.fullName]}</td>
      <td>
        <input type="button" onclick="toggleContent(this)" value="Description" />
        <div class="content" title="${question.displayName}">
          <h3>Summary</h3>
          <div>${question.summary}</div>

          <h3>Parameters</h3>
          <div>
            <ul>
              <c:forEach items="${question.params}" var="qparam">
                <c:if test="${qparam.isVisible}">
                  <li><b>${qparam.prompt}</b><div>${qparam.help}</div></li>
                </c:if>
              </c:forEach>
            </ul>
          </div>

          <h3>Detail</h3>
          <div>${question.description}</div>
        </div>
      </td>
    </tr>
  </c:forEach>
  </tbody>
  <tfoot>
    <tr>
      <th>Type</th>
      <th>Search Name</th>
      <th>Category</th>
      <th>Popularity</th>
      <th>Description</th>
    </tr>
  </tfoot>
</table>
</div>
