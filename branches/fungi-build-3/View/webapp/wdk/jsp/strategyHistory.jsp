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
                       { "bSortable": false },
                       // { "bSortable": false },
                       // { "bSortable": false },
                       { "bSortable": false },
                       null, 
                       null, 
                       null, 
                       null, 
                       { "bSortable": false } ],
        "aaSorting": [[ 5, "desc" ]],
        "fnDrawCallback": function(oSettings) {
            $(".strategy_description .full", this).qtip({
              content: {
                text: function(api) {
                  return $(this).html()
                      .replace(/\n/g, "<br/>")
                      .replace(/(https?:\/\/\S+)/g, "<a href='$1' target='_blank'>$1</a>")
                },
                title: {
                  text: "<a href='#' class='open-dialog-update-strat'>Edit description</a>",
                  button: "Close"
                }
              },
              position: {
                my: "top left",
                at: "bottom left"
              },
              show: {
                solo: true
              },
              hide: false,
              style: {
                classes: "strategy-description"
              },
              events: {
                show: function(event, api) {
                  // disable all other qtips
                  $(".strategy_description .full", "#search_history").each(function(idx, el) {
                    // don't disable this qtip
                    if (el == api.elements.target.get(0)) return;
                    $(el).qtip("disable");
                  });
                },
                hide: function(event, api) {
                  // disable all other qtips
                  $(".strategy_description .full", "#search_history").each(function(idx, el) {
                    // don't disable this qtip
                    if (el == api.elements.target.get(0)) return;
                    $(el).qtip("enable");
                  });
                }
              }
            });
        }
    } );
} );
</script>

<imp:strategyHistory model="${wdkModel}" user="${wdkUser}" />
