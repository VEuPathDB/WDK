wdk.util.namespace("window.wdk.plugin", function(ns, $) {
  "use strict";

  $.fn.wdkDataTable = function(opts) {
    return this.each(function() {
      var $this = $(this),
          sorting = $this.data("sorting"),
          dataTableOpts = {
            aoColumns: null,
            sScrollX: "",
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
        dataTableOpts.aoColumns = $.map(sorting, function(s) {
          var column = {},
              types = ['string', 'numeric', 'data', 'html'];
          if (s === true) {
            // if true, use defaults -- map wants [null]
            column = [null];
          } else {
            // bSortable must be a Boolean
            column.bSortable = Boolean(s);
            // only set sType if a valid type
            if (types.join("@~@").indexOf(s) > -1) column.sType = s;
          };
          return column;
        });
      }

      // allow options to be passed like in the default dataTable function
      $this.dataTable($.extend(dataTableOpts, opts));
    });
  };

});
