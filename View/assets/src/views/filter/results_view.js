wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  var Field = wdk.models.filter.Field;

  Handlebars.registerHelper('property', function(key, context) {
    return context[key];
  });

  var ColumnView = wdk.views.View.extend({
    events: {
      'change input': 'handleChange'
    },

    initialize: function() {
      this.render();
    },

    render: function() {
      var checked = this.model.get('visible') ? 'checked' : '';
      this.$el.html('<input type="checkbox" id="' + this.model.cid + '" ' +
                    checked + '> ' +
                    '<label for="' + this.model.cid + '">' +
                    this.model.get('display') + '</label>');
      return this;
    },

    handleChange: function(event) {
      event.stopPropagation();
      this.model.set('visible', event.currentTarget.checked);
    }
  });

  var ColumnsView = wdk.views.View.extend({

    initialize: function() {
      this.render();
    },

    render: function() {
      var $el = this.$el;
      this.collection.models.forEach(function(model) {
        var view = new ColumnView({ model: model });
        view.render();
        $el.append(view.el);
      });
      return this;
    }
  });

  ns.ResultsView = wdk.views.View.extend({

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
      this.columns = new Backbone.Collection(this.controller.fields.where({ leaf: 'true' }));
      this.columnsDialog = new ColumnsView({ collection: this.columns }).$el
        .dialog({
          autoOpen: false,
          modal: true,
          title: 'Choose columns to show or hide'
        });

      this.initTableOnce = _.once(this._initTable.bind(this));

      this.queueRender();

      this.listenTo(this.model, 'change:filteredData', this.queueRender);
      this.listenTo(this.columns, 'change:visible', this.fetchMetadata);
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

    fetchMetadata: function(column) {
      this.controller.getMetadata(column).then(this.queueRender.bind(this));
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
      }].concat(this.columns
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
