wdk.util.namespace("window.wdk.plugin", function(ns, $) {
  "use strict";

  $.fn.wdkDataTable = function(opts) {
    return this.each(function() {
      var $this = $(this),
          sorting = $this.data("sorting"),
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
        dataTableOpts.aoColumns = $.map(sorting, function(o) {
          return o ? [null] : {"bSortable" : false };
        });
      }

      // allow options to be passed like in the default dataTable function
      $this.dataTable($.extend(dataTableOpts, opts));
    });
  };

});
