<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="wdkModel" value="${applicationScope.wdkModel}" />
<c:set var="wdkUser" value="${sessionScope.wdkUser}" />

<%-- This should go in history.js and be invoked within updateHistory --%>
<script type="text/javascript">
$(document).ready(function() {
    $("#search_history table.datatables").dataTable( {
        "bAutoWidth": false,
        "bJQueryUI": true,
        "bScrollCollapse": true,
        "aoColumns": [ { "bSortable": false }, 
                       null, 
                       null, 
                       { "bSortable": false },
                       { "bSortable": false },
                       null, 
                       null, 
                       null, 
                       null  ],
        "aaSorting": [[ 6, "desc" ]]
    } );
} );
</script>

<imp:strategyHistory model="${wdkModel}" user="${wdkUser}" />
