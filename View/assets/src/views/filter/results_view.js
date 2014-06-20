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

    dataTable: null,

    constructor: function() {
      Handlebars.registerHelper('property', function(key, context, options) {
        return context[key];
      });
      this.initTableOnce = _.once(this._initTable.bind(this));
      wdk.views.View.apply(this, arguments);
    },

    initialize: function() {
      this.listenTo(this.model.filteredData, 'reset', this.queueRender);
    },

    render: function() {
      this.initTableOnce();
      this.dataTable.fnClearTable(false);
      this.dataTable.fnAddData(this.model.filteredData.toJSON(), false);
      this.dataTable.fnDraw();

      return this;
    },

    _initTable: function() {
      var tableConfig = this.generateTableConfig();

      this.$el.html(this.template({
        fields: this.model.fields.toJSON()
      }));

      this.dataTable = this.$('.results-table')
        .wdkDataTable(tableConfig)
        .dataTable();

      $(window).on('resize', _.debounce(this.queueResizeTable.bind(this), 100));
    },

    resizeTable: function() {
      this.dataTable.fnAdjustColumnSizing(false);
    },

    queueRender: function() {
      if (this.$el.is(':visible')) {
        this.render();
      }
      else {
        this._doRender = true;
      }
    },

    queueResizeTable: function() {
      if (this.$el.is(':visible')) {
        this.resizeTable();
      }
      else {
        this._doResizeTable = true;
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
        this.render();
        this._doRender = false;
        this._doResizeTable = false;
      }
      if (this._doResizeTable) {
        this.resizeTable();
        this._doResizeTable = false;
      }
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
      this.model.fields
        .where({ filterable: true })
        .forEach(function(field) {
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
