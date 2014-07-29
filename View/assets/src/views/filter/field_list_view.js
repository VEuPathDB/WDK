wdk.namespace('wdk.views.filter', function(ns, $) {
  'use strict';

  /**
   * Renders a list of links
   *
   * When a link is clicked, a 'select' event is triggered by the view.
   * Event handlers will recieve the field model as an argument.
   */
  ns.FieldListView = wdk.views.View.extend({

    events: {
      'click a[href="#expand"]'   : 'expand',
      'click a[href="#collapse"]' : 'collapse',
      'click li a'                : 'triggerSelect',
      'click h4'                  : 'toggleNext',
      'keyup input'               : 'filter'
    },

    template: wdk.templates['filter/field_list.handlebars'],

    initialize: function() {
      this.listenTo(this.model.fields, 'select', this.selectField);
      this.listenTo(this.model.fields, 'reset', this.render);
    },

    render: function() {

      // get all ontology terms starting from `filterable` fields
      // and traversing upwards by the `parent` attribute
      var prunedFields = _.sortBy(function getFields(fields) {
        return _.where(fields, { filterable: true })
          .reduce(function(acc, field) {
            while (field.parent) {
              acc.push(field);
              field = _.findWhere(fields, {term: field.parent});
            }
            acc.push(field);
            return _.uniq(acc);
          }, []);
      }(this.model.fields.toJSON()), 'term');

      // get root nodes
      var rootNodes = _.where(prunedFields, { parent: undefined });

      // construct tree
      var groupedFields = (function appendChildren(nodes, fields) {
        return _.map(nodes, function(node) {
          var children = _.chain(fields)
            .where({ parent: node.term })
            .sortBy(function(c) { return c.filterable ? 0 : 1; }) // push leaves above nodes
            .value();

          return children.length
            ? { field: node, children: appendChildren(children, fields) }
            : { field: node };
        });
      }(rootNodes, prunedFields));

      // remove top level category if it's the only one
      groupedFields = (function removeLonelyTop(nodes) {
        if (nodes.length > 1 || !nodes[0].children) {
          return nodes;
        }
        return removeLonelyTop(nodes[0].children);
      }(groupedFields));

      // remove nodes with only one child, unless it's terminating
      groupedFields = (function removeParentsWithSingleChild(nodes) {
        return nodes.map(function(node) {

          // replace node with first child if only one child
          while (node.children && node.children.length === 1) {
            node = node.children[0];
          }

          // recur if node has children
          // (will be > 1 child due to above while loop)
          if (node.children) {
            node.children = removeParentsWithSingleChild(node.children);
          }

          // else, return node
          return node;
        });
      }(groupedFields));

      // sort node such that leaves are first
      groupedFields = _.sortBy(groupedFields, function(node) {
        return node.field.filterable ? 0 : 1;
      });

      this.$el.html(this.template({
        nodes: groupedFields,
        showExpand: groupedFields.filter(function(node) {
          return !_.isEmpty(node.children);
        }).length
      }));
      return this;
    },

    triggerSelect: function(e) {
      e.preventDefault();

      var link = e.currentTarget;
      if ($(link).parent().hasClass('active')) {
        return;
      }

      var term = link.hash.slice(1);
      var field = this.model.fields.findWhere({term: term});
      field.select();
    },

    expand: wdk.fn.preventEvent(function() {
      this.$('h4').removeClass('collapsed');
      // this.$('div').slideDown(function() {
      //   $(this).prev().removeClass('collapsed');
      // });
    }),

    collapse: wdk.fn.preventEvent(function() {
      this.$('h4').addClass('collapsed');
      // this.$('div').slideUp(function() {
      //   $(this).prev().addClass('collapsed');
      // });
    }),

    toggleNext: function(e) {
      var $target = $(e.currentTarget);
      $target.toggleClass('collapsed');
      // var $next = $target.next().slideToggle(function() {
      //   $target.toggleClass('collapsed', $next.is(':hidden'));
      // });
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
    }

  });

});
