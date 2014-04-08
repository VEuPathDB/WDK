wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  ns.ResultsView = wdk.views.View.extend({

    events: {
      'mouseover td' : 'setTitle',
      'change input' : 'handleChange'
    },

    template: wdk.templates['filter/results.handlebars'],

    displayTemplate: wdk.templates['filter/results_item_display.handlebars'],

    className: 'results',

    _doRender: null,

    dataTable: null,

    constructor: function() {
      Handlebars.registerHelper('property', function(key, context, options) {
        return context[key];
      });

      wdk.views.View.apply(this, arguments);
    },

    initialize: function() {
      this.listenTo(this.model.filteredData, 'reset', this.queueRender);
      this.render();
    },

    render: function() {
      var tableConfig = this.generateTableConfig();

      this.$el.html(this.template({
        fields: this.model.fields.toJSON()
      }));

      this.dataTable = this.$('.results-table').wdkDataTable(tableConfig).dataTable();

      this.renderTableBody();

      $(window).on('resize', _.debounce(this.resizeTable.bind(this), 100));

      return this;
    },

    renderTableBody: function() {
      this.dataTable.fnClearTable();
      this.dataTable.fnAddData(this.model.filteredData.toJSON());
    },

    queueRender: function() {
      if (this.$el.is(':visible')) {
        this.renderTableBody();
      } else {
        this._doRender = true;
      }
    },

    // Add title attribute for mouseover when the text content overflows
    setTitle: function(e) {
      var td = e.currentTarget;
      if (td.scrollWidth > td.clientWidth) {
        td.setAttribute('title', $(td).text());
      }
    },

    handleChange: function(e) {
      var target = e.currentTarget;
      var item = this.model.filteredData.get(target.value);
      var ignored = !target.checked;
      item.set('ignored', ignored);
      $(target).closest('tr').toggleClass('muted', ignored);
    },

    didShow: function() {
      if (this._doRender) {
        this.renderTableBody();
        this._doRender = false;
      } else {
        this.resizeTable();
      }
    },

    resizeTable: function() {
      this.dataTable.fnDraw();
    },

    generateTableConfig: function() {
      var _this = this;
      var columns = [];

      columns.push({
        sClass: 'display',
        sTitle: 'Name',
        mData: function(row, type, val) {
          var html = _this.displayTemplate(row);
          return html;
        }
      });

      // allow all columns to be sortable
      this.model.fields.forEach(function(field) {
        columns.push({
          sClass: field.get('term'),
          sTitle: field.get('display'),
          mData: 'metadata.' + field.get('term')
        });
      });

      return {
        sDom: 'C<"clear">lfrtip',
        bFilter: false,
        bDeferRender: true,
        aoColumns: columns,
        fnRowCallback: function(tr, d) {
          $(tr).toggleClass('muted', d.ignored);
        }
        // oColVis: {
        //   buttonText: 'Change columns',
        //   sAlign: 'right',
        //   aiExclude: [ 0 ]
        // }
      };
    }

  });

});
