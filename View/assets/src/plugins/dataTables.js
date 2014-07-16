wdk.util.namespace("window.wdk.plugin", function(ns, $) {
  "use strict";

  // custom types

  // Example: 1.04e-3
  $.extend( $.fn.dataTableExt.oSort, {
    "scientific-pre": function ( a ) {
      return Number(a);
    },

    "scientific-asc": function ( a, b ) {
      return ((a < b) ? -1 : ((a > b) ? 1 : 0));
    },

    "scientific-desc": function ( a, b ) {
      return ((a < b) ? 1 : ((a > b) ? -1 : 0));
    }
  } );

  $.fn.wdkDataTable = function(opts) {
    return this.each(function() {
      var $this = $(this),
          sorting = $this.data("sorting"),
          dataTableOpts = {
            aoColumns: null,
            sScrollX: "100%",
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
          }
          return column;
        });
      }

      // allow options to be passed like in the default dataTable function
      return $this.dataTable($.extend(dataTableOpts, opts));
    });
  };

});
