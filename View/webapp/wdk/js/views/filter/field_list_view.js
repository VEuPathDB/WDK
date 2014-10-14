wdk.namespace('wdk.views.filter', function(ns, $) {
  'use strict';

  // Renders a list of links
  //
  // When a link is clicked, a 'select' event is triggered by the view.
  // Event handlers will recieve the field model as an argument.

  ns.FieldListView = wdk.views.core.View.extend({

    events: {
      'click a[href="#expand"]'   : 'expand',
      'click a[href="#collapse"]' : 'collapse',
      'click h4'                  : 'toggleNext',
      'keyup input'               : 'filter'
    },

    template: wdk.templates['filter/field_list.handlebars'],

    constructor: function(options) {
      _.extend(this.events, options.events);
      wdk.views.core.View.apply(this, arguments);
    },

    initialize: function(options) {
      this.fieldTemplate = options.fieldTemplate;
      this.listenTo(this.controller, 'select:field', this.selectField);
      this.listenTo(this.collection, 'reset', this.render);
      this.render();
    },

    render: function() {
      var groupedFields = this.collection.getTree();

      this.$el.html(this.template({
        nodes: groupedFields,
        showExpand: groupedFields.filter(function(node) {
          return !_.isEmpty(node.children);
        }).length
      }, {
        helpers: {
          fieldTemplate: this.fieldTemplate
        }
      }));

      return this;
    },

    expand: wdk.fn.preventEvent(function() {
      this.$('h4').removeClass('collapsed');
    }),

    collapse: wdk.fn.preventEvent(function() {
      this.$('h4').addClass('collapsed');
    }),

    toggleNext: function(e) {
      var $target = $(e.currentTarget);
      $target.toggleClass('collapsed');
    },

    // jshint ignore:start
    filter: function(e) {
      // var str = e.currentTarget.value;
      // this.$('div')
      // .hide()
      // .find(':contains(' + str + ')').show();
    },
    // jshint ignore:end

    selectField: function(field) {
      var term = field.get('term');
      var link = this.$('a[href="#' + term + '"]');
      this.$('li').removeClass('active');
      $(link).parent().addClass('active');
      $(link).parentsUntil(this.$el.find('>ul')).find('>h4').removeClass('collapsed');
    }

  });

});
