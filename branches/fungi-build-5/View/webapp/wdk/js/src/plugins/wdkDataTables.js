wdk.util.namespace("window.wdk.plugin", function(ns, $) {
  "use strict";

  $.fn.wdkDataTable = function(opts) {
    return this.each(function() {
      var $this = $(this),
          sorting = $this.data("sorting"),
          aoColumns = [],
          dataTableOpts = {
            sScrollY: "600px",
            bScrollCollapse: true,
            bPaginate: false,
            bJQueryUI: true,
            oLanguage: {
              sSearch: "Filter:"
            }
          };

      if ($this.length === 0) return;

      if (sorting) {
        aoColumns = $.map(sorting, function(s) {
          var types = ['string', 'numeric', 'data', 'html'];
          return {
            "bSortable" : Boolean(s),
            "sType" : types.join("@~@").indexOf(s) > -1 ? s : [null]
          };
        });
      }

      // allow options to be passed like in the default dataTable function
      $this.dataTable($.extend(dataTableOpts, opts));
    });
  };

});
