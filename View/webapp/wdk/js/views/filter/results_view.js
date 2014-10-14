wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  var Field = wdk.models.filter.Field;

  var FieldListView = ns.FieldListView;

  Handlebars.registerHelper('property', function(key, context) {
    return context[key];
  });

  ns.ResultsView = wdk.views.core.View.extend({

    events: {
      'mouseover td'                      : 'setTitle',
      'change input'                      : 'handleChange',
      'click [data-action="showColumns"]' : 'showColumns'
    },

    template: wdk.templates['filter/results.handlebars'],

    displayTemplate: wdk.templates['filter/results_item_display.handlebars'],

    className: 'results',

    dataTable: null,

    initialize: function() {
      this._metadataFetchCount = 0;

      this.columnsDialog = new FieldListView({
        className: 'filter-results-columns',
        collection: this.controller.fields,
        controller: this.controller,
        fieldTemplate: function(field) {
          var checked = field.visible ? 'checked' : '';
          return [
            '<label>',
            '<input type="checkbox"' + checked + ' value="' + field.term + '"/>',
            field.display,
            '</label>'
          ].join(' ');
        },
        events: {
          'change input': function(event) {
             event.stopPropagation();
             var field = this.controller.fields.get(event.target.value);
             field.set('visible', event.currentTarget.checked);
          }.bind(this)
        }
      }).$el.dialog({
        autoOpen: false,
        modal: true,
        title: 'Choose columns to show or hide'
      });

      this.initTableOnce = _.once(this._initTable.bind(this));

      this.queueRender();

      this.listenTo(this.model, 'change:filteredData', this.queueRender);
      this.listenTo(this.controller.fields, 'change:visible', this.handleColumnVisibility);
    },

    render: function() {
      console.time('render results');
      var data = this.model.get('filteredData');
      // this.initTableOnce();
      this._initTable();
      this.dataTable.fnClearTable(false);

      if (data.length) {
        this.dataTable.fnAddData(data, false);
      }

      this.dataTable.fnDraw();
      this.dataTable.fnAdjustColumnSizing(false);
      console.timeEnd('render results');

      return this;
    },

    _initTable: function() {
      var tableConfig = this.generateTableConfig();

      this.$el.html(this.template({
        fields: this.controller.fields.toJSON()
      }));

      this.$("button").button();

      this.dataTable = this.$('.results-table')
        .wdkDataTable(tableConfig)
        .dataTable();
    },

    // TODO controller should listen for the column visibility change and do
    // this. This means the controller will be responsible for calling render.
    //
    // As it is, this view has to be concerned with how the controller is
    // getting metadata.
    handleColumnVisibility: function(column, visible) {
      if (!visible) {
        this.controller.abortMetadataRequest(column);
        if (--this._metadataFetchCount === 0) this.queueRender();
        return this;
      }

      this._metadataFetchCount++;
      this.controller.getMetadata(column)
        .then(function() {
          if (--this._metadataFetchCount === 0) this.queueRender();
        }.bind(this),
        function(err) {
          if (err.statusText != 'abort') {
            throw err;
          }
        });
      return this;
    },

    queueRender: function() {
      if (this.$el.is(':visible')) {
        this.render();
      }
      else {
        this._doRender = true;
      }
    },

    showColumns: function(event) {
      event.preventDefault();
      this.columnsDialog.dialog('open');
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
      this.controller.toggleIgnored(target.value, !target.checked);
      $(target).closest('tr').toggleClass('muted', !target.checked);
    },

    didShow: function() {
      if (this._doRender) {
        this.render();
        this._doRender = false;
        this._doResizeTable = false;
      }
    },

    generateTableConfig: function() {
      var controller = this.controller;
      var displayTemplate = this.displayTemplate;

      var columns = [{
        className: 'display',
        title: 'Name',
        data: function(row) {
          var html = displayTemplate(_.extend({
            isIgnored: controller.isIgnored(row)
          }, row));
          return html;
        }
      }].concat(this.controller.fields
        .where({ visible: true })
        .map(function(column) {
          var term = column.get('term');
          return {
            className: term.trim().replace(/\s+/g, '-'),
            title: column.get('display'),
            data: function(row) {
              return controller.metadata[term][row.term];
            },
            defaultContent: Field.UNKNOWN_VALUE
          };
        }));

      return {
        dom: '<"H"ip>t<"F"ip>',
        searching: false,
        deferRender: true,
        paging: true,
        pageLength: 20,
        pagingType: 'full',
        columns: columns,
        rowCallback: function(tr, d) {
          $(tr).toggleClass('muted', controller.isIgnored(d));
        }
      };
    }

  });

});
