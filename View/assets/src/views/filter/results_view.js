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
      'change input:checkbox': 'handleCheckboxChange'
    },

    initialize: function(options) {
      this.fields = options.fields;
    },

    render: function() {
      this.$el.html(this.template({
        item: this.model.toJSON(),
        fields: this.fields.toJSON()
      }));
      this.$el.toggleClass('muted', this.model.get('ignored'));
    },

    handleCheckboxChange: function(e) {
      this.model.set('ignored', !e.currentTarget.checked);
      this.$el.toggleClass('muted', !e.currentTarget.checked);
    }

  });


  ns.ResultsView = wdk.views.View.extend({

    events: {
      'mouseover td': 'setTitle'
    },

    template: wdk.templates['filter/results.handlebars'],

    className: 'results context ui-helper-clearfix',

    dataTable: null,

    constructor: function() {
      Handlebars.registerHelper('property', function(key, context, options) {
        return context[key];
      });
      wdk.views.View.apply(this, arguments);
    },

    initialize: function() {
      this.listenTo(this.model.filteredData, 'reset', this.render);
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
      this.dataTable = this.$('.results-table').wdkDataTable({ bFilter: false }).dataTable();

      $(window).on('resize', _.debounce(this.resizeTable.bind(this), 100));

      return this;
    },

    setTitle: function(e) {
      var td = e.currentTarget;
      if (td.scrollWidth > td.clientWidth) {
        td.setAttribute('title', $(td).text());
      }
    },

    didShow: function() {
      this.dataTable.fnDraw();
    },

    resizeTable: function() {
      this.dataTable.fnDraw();
    }

  });

});
