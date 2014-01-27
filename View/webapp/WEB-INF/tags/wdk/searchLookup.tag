<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<c:set var="wdkModel" value="${applicationScope.wdkModel}" />

<script type="text/javascript">

$(document).ready(function() {

$("table#search-lookup").css("display","table");

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
				
	// by default we only want unique data
	if ( typeof bUnique == "undefined" ) bUnique = true;
				
	// by default we do want to only look at filtered data
	if ( typeof bFiltered == "undefined" ) bFiltered = true;
				
	// by default we do not want to include empty values
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

var searchTable = $('#search-lookup').dataTable( {
        "bJQueryUI": true,
				"bPaginate": false,
				"sScrollY": "120px",
				"bScrollCollapse": true,
        "aaSorting": [[ 1, 'asc']],
        "sDom": '<"H"f<"#type-filter">lr>t<"F"ip>',
        "oLanguage": {
	    	"sSearch": "<b style='font-size:120%'>Do you need help finding a <i>Search</i>?</b>...</>",
	   		"sInfo":"(Found _TOTAL_ searches)",
	   		"sZeroRecords": "There are no searches that include your keyword in the search Title, Category or Description",
 				"sInfoFiltered": "(out of _MAX_ searches)"
        },
} );

//adding default value to input box
//$('table#search-lookup').dataTable().fnFilter('Type keyword(s)');

// get the type info, and create a dropdown list
var aData = searchTable.fnGetColumnData(0);

$("div.dataTables_scroll").css("display","none");
$("div.dataTables_scroll").css("border","1px solid grey");
$("div.ui-corner-bl").css("display","none");
$("img#close-table").css("display","none");
$("div.dataTables_scrollHeadInner th").css("border-width","0 0 1px 0");
$("div.dataTables_scrollHead").css("border-width","0 0 1px 0");
$("div.dataTables_scrollHead").css("border-style","solid");
$("div.dataTables_scrollHead").css("border-color","grey");
$("table#search-lookup td").css("padding","4px");


} ); //document ready function

function toggleContent(ele) {
    var mydiv= $(ele).siblings("div.mycontent").clone().dialog({width: 600});
    mydiv.css("display","block");
}

//add event on input box
$("div#search-lookup_filter input").live("click", function () {
    $("div.dataTables_scroll").css("display","block");
    $("div.ui-corner-bl").css("display","block");
    $("img#close-table").css("display","block");
});

//add event on close link
$("img#close-table").live("click", function () {
    $("div.dataTables_scroll").css("display","none");
    $("div.ui-corner-bl").css("display","none");
    $("img#close-table").css("display","none");
});



</script>

<%-----------------------------------------------------------------------%>
<center style="position:relative">
 
<div  id="search-lookup-panel" style="width:90%"  
	title='Enter a keyword; searches that contain the keyword in its name, category or description will be listed'>

<a style="position:absolute;top:8px;right:50px;" href='javascript:void(0)'>
	<imp:image id="close-table" style='vertical-align:middle' src='/wdk/images/close.gif'/>
</a>

  <table id="search-lookup" class="datatables" width="100%" style="display:none">
  <thead>
    <tr>

      <th class="type" width="30%">Search Title</th>
      <th class="type" width="20%">Search Category</th>
      <th class="description" width="50%">Click for a detailed description</th>
    </tr>
  </thead>

  <tbody>
  <c:forEach items="${wdkModel.allQuestions}" var="item">
    <c:set var="question" value="${item.key}" />
    <c:set var="category" value="${item.value}" />
    <tr>
    
      <td >
	<a title="Click to go to search page" href="<c:url value='/showQuestion.do?questionFullName=${question.fullName}'/>">
	 	Find ${question.recordClass.displayNamePlural} by ${question.displayName}</a>
      </td>
  <td >
	  ${category.displayName}
      </td>
      <td>
	 <a title="Click to open a popup with a detailed description" href="javascript:void(0)" onclick="toggleContent(this)">${fn:substring(question.description,0,50)}...</a>

         <div class="mycontent" title="${question.displayName}" style="display:none">
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
          <h3>Description</h3>
          <div>${question.description}</div>
        </div>

      </td>
    </tr>
  </c:forEach>
  </tbody>

  </table>


</div>


</center>
