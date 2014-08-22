wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  var Field = wdk.models.filter.Field;

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
      Handlebars.registerHelper('property', function(key, context) {
        return context[key];
      });
      wdk.views.View.apply(this, arguments);
    },

    initialize: function(options) {
      this.defaultColumns = options.defaultColumns;
      this.initTableOnce = _.once(this._initTable.bind(this));

      this.queueRender();

      this.listenTo(this.model.filteredData, 'reset', this.queueRender);
    },

    render: function() {
      this.initTableOnce();
      this.dataTable.fnClearTable(false);

      if (this.model.filteredData.length) {
        this.dataTable.fnAddData(this.model.filteredData.toJSON(), false);
      }

      this.dataTable.fnDraw();
      this.dataTable.fnAdjustColumnSizing(false);

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
    },

    queueRender: function() {
      if (this.$el.is(':visible')) {
        this.render();
      }
      else {
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
        this.render();
        this._doRender = false;
        this._doResizeTable = false;
      }
    },

    generateTableConfig: function() {
      var _this = this;
      var columns = [];
      var defaultColumns = this.defaultColumns;

      columns.push({
        sClass: 'display',
        sTitle: 'Name',
        mData: function(row) {
          var html = _this.displayTemplate(row);
          return html;
        }
      });

      // allow all columns to be sortable
      this.model.fields
        .where({ leaf: 'true' })
        .forEach(function(field) {
          columns.push({
            sClass: field.get('term').trim().replace(/\s+/g, '-'),
            sTitle: field.get('display'),
            mData: 'metadata.' + field.get('term'),
            defaultContent: Field.UNKNOWN_VALUE,
            bVisible: defaultColumns
                      ? defaultColumns.indexOf(field.get('term')) > -1
                      : true
          });
        });

      return {
        sDom: 'C<"clear">lfrtip',
        bFilter: false,
        bDeferRender: true,
        aoColumns: columns,
        fnRowCallback: function(tr, d) {
          $(tr).toggleClass('muted', d.ignored);
        },
        oColVis: {
          buttonText: 'Change columns',
          sAlign: 'right',
          aiExclude: [ 0 ] // exclude the name column
        }
      };
    }

  });

});
