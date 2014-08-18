wdk.namespace('wdk.views.filter', function(ns, $) {
  'use strict';

  // # Helper functions

  // Create tree, then prune it so it's easier to read
  var makeTree = _.compose(removeParentsWithSingleChild, removeSingleTopNode,
    constructTree);

  // Given a list of fields:
  // First, find all fields marked as `filterable` (this means
  //   the field is terminating and data can be filtered by it).
  // Then, for each field, find all parents.
  function pruneFields(fields) {
    return _.where(fields, { filterable: true })
      .reduce(function(acc, field) {
        while (field.parent) {
          acc.push(field);
          field = _.findWhere(fields, {term: field.parent});
        }
        acc.push(field);
        return _.uniq(acc);
      }, []);
  }

  // Convert a list to a tree* based on the `parent` property
  // given a list of root nodes and a list of all fields
  //
  // * More accurately, this will create one or more trees,
  //   one for each initial sibling field. But we can imagine
  //   a common, hidden, root node.
  function constructTree(siblingFields, allFields) {
    return siblingFields
      .map(function(field) {
        var children = _.chain(allFields)
          .where({ parent: field.term })

          // sort leaves to top
          .sortBy(function(c) { return c.filterable ? 0 : 1; })
          .value();

        return children.length
          ? { field: field, children: constructTree(children, allFields) }
          : { field: field };
      });
  }

  // Remove top level category if it's the only one.
  //
  // E.g., turn this:
  //
  //    A
  //     \
  //      B
  //       \
  //        C
  //       / \
  //      D   E
  //     /     \
  //       ...
  //
  // into this:
  //
  //      D   E
  //     /     \
  //       ...
  //
  function removeSingleTopNode(tree) {
    while (tree.length === 1 && tree[0].children) {
      tree = tree[0].children;
    }

    return tree;
  }

  // Remove nodes with only one child, unless it's terminating.
  //
  // E.g., turn this:
  //
  //      *
  //     / \
  //    A   B
  //         \
  //          C
  //           \
  //            D
  //
  // into this:
  //
  //      *
  //     / \
  //    A   D
  //
  function removeParentsWithSingleChild(tree) {
    return tree
      .map(function(node) {

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
  }

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

      var fields = this.model.fields.toJSON();

      // get all ontology terms starting from `filterable` fields
      // and traversing upwards by the `parent` attribute
      var prunedFields = _.sortBy(pruneFields(fields), 'term');

      // get root tree
      var parentFields = _.where(prunedFields, { parent: undefined });

      // construct tree
      var groupedFields = makeTree(parentFields, prunedFields);

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

      var field;
      if (this.model.filters.length) {
        // select first filtered field
        var fieldTerm = this.model.filters.at(0).get('field');
        field = this.model.fields.get(fieldTerm);
      } else {
        // select first field
        var node = groupedFields[0];
        while (!node.field.leaf) {
          node = node.children[0];
        }
        field = this.model.fields.get(node.field.term);
      }
      _.defer(function() {
        field.select();
      }.bind(this));

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
      $(link).parentsUntil(this.$el.find('>ul')).find('>h4').removeClass('collapsed');
    }

  });

});
