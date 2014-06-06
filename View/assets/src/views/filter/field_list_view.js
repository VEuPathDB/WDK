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
      'click a[href="#expand"]': 'expand',
      'click a[href="#collapse"]': 'collapse',
      'click li a': 'triggerSelect',
      'click h4': 'toggleNext',
      'keyup input': 'filter'
    },

    template: wdk.templates['filter/field_list.handlebars'],

    initialize: function() {
      this.listenTo(this.model.fields, 'select', this.selectField);
      this.listenTo(this.model.fields, 'reset', this.render);
    },

    render: function() {
      //var groupedFields = _.groupBy(this.model.fields.toJSON(), 'parent');

      var fields = _.sortBy(this.model.fields.toJSON(), 'parent');
      // var groupedFields = (function appendChildren(nodes, fields) {
      //   return _.map(nodes, function(node) {
      //     var children = _.where(fields, { parent: node.term });

      //     return children.length
      //       ? { field: node, children: appendChildren(children) }
      //       : { field: node };
      //   });
      // }(_.where(fields, { leaf: 'true'}), fields));


      var leaves = _.where(fields, { filterable: true })
        .map(function(o) {
          return {
            field: o
          }
        })

      var groupedFields = (function prependParents(nodes, fields) {
        var tree = [], dirty;
        _.each(nodes, function(node) {
          // FIXME Hardcoded root -- should be a model param attribute??
          if (node.field.parent !== 'BioMaterialCharacteristics') {
            var parent = _.findWhere(fields, {term: node.field.parent});
            var f = { field: parent };

            var field = (function findField(tree, f) {
              var field = _.findWhere(tree, f);
              if (!field && tree.length) {
                return _.reduce(tree, function(acc, subtree) {
                  return acc || (subtree.children && findField(subtree.children, f));
                }, undefined);
              }
              return field;
            }(tree, f));

            if (!field) {
              tree.push(f);
              field = f;
            }

            (field.children || (field.children = [])).push(node);

            dirty = true;
          } else {
            tree.push(node);
          }
        });
        if (dirty) {
          return prependParents(tree, fields);
        } else {
          return nodes;
        }
      }(leaves, fields));



      this.$el.html(this.template(groupedFields));
      //this.$el.html(this.template(this.model.fields.toJSON()));
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
      this.$('div').slideDown(function() {
        $(this).prev().removeClass('collapsed');
      });
    }),

    collapse: wdk.fn.preventEvent(function() {
      this.$('div').slideUp(function() {
        $(this).prev().addClass('collapsed');
      });
    }),

    toggleNext: function(e) {
      var $target = $(e.currentTarget);
      var $next = $target.next().slideToggle(function() {
        $target.toggleClass('collapsed', $next.is(':hidden'));
      });
    },

    filter: function(e) {
      // var str = e.currentTarget.value;
      // this.$('div')
      // .hide()
      // .find(':contains(' + str + ')').show();
    },

    selectField: function(field) {
      var term = field.get('term');
      var link = this.$('a[href="#' + term + '"]');
      this.$('li').removeClass('active');
      $(link).parent().addClass('active');
    }

  });

});
