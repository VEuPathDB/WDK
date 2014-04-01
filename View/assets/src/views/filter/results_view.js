wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';


  var ResultsItemView = wdk.views.View.extend({

    /**
     * Fields to display
     */
    fields: null,

    template: wdk.templates['filter/results_item.handlebars'],

    tagName: 'tr',

    events: {
      'change [data-action="toggleIgnored"]': 'handleToggleIgnore'
    },

    initialize: function(options) {
      this.fields = options.fields;
    },

    render: function() {
      this.$el.html(this.template({
        item: this.model.toJSON(),
        fields: this.fields.toJSON()
      }));
      this.$('.button').button();
      this.$el.toggleClass('muted', this.model.get('ignored'));
    },

    handleToggleIgnore: function(e) {
      e.preventDefault();
      var ignored = !this.model.get('ignored');
      this.model.set('ignored', ignored);
      this.render();
    }

  });


  ns.ResultsView = wdk.views.View.extend({

    events: {
      'mouseover td': 'setTitle'
    },

    template: wdk.templates['filter/results.handlebars'],

    className: 'results context ui-helper-clearfix',

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
      var view = this;
      this.$el.html(this.template({
        fields: this.model.fields.toJSON()
      }));
      this.model.filteredData.forEach(function(item) {
        var itemView = new ResultsItemView({ model: item, fields: view.model.fields });
        itemView.render();
        view.$('tbody').append(itemView.el);
      });

      var tableConfig = this.generateTableConfig();
      this.dataTable = this.$('.results-table').wdkDataTable(tableConfig).dataTable();

      $(window).on('resize', _.debounce(this.resizeTable.bind(this), 100));

      return this;
    },

    queueRender: function() {
      this._doRender = true;
    },

    setTitle: function(e) {
      var td = e.currentTarget;
      if (td.scrollWidth > td.clientWidth) {
        td.setAttribute('title', $(td).text());
      }
    },

    didShow: function() {
      if (this._doRender) {
        this.render();
        this._doRender = false;
      } else {
        this.dataTable.fnDraw();
      }
    },

    resizeTable: function() {
      this.dataTable.fnDraw();
    },

    generateTableConfig: function() {
      // allow all columns to be sortable, except last column
      var columns = _.range(this.model.fields.length + 1).map(function() {
        return null;
      });
      // columns.push({ bSortable: false });
      // columns.unshift(null);

      return {
        bFilter: false,
        aoColumns: columns
      };
    }

  });

});
