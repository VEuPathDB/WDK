wdk.namespace('wdk.models.filter', function(ns) {
  'use strict';

  //
  // Helper functions
  //

  // Given a list of fields:
  // First, find all fields marked as `filterable` (this means
  //   the field is terminating and data can be filtered by it).
  // Then, for each field, find all parents.
  function pruneFields(fields) {
    var missing = [];
    var prunedFields = _.where(fields, { leaf: 'true' })
      .reduce(function(acc, field) {
        while (field.parent) {
          acc.push(field);
          field = _.findWhere(fields, {term: field.parent});
          if (_.isUndefined(field)) {
            missing.push(_.last(acc).parent);
            break;
          }
        }
        acc.push(field);
        return _.uniq(_.compact(acc));
      }, []);

    if (missing.length) {
      alert('The following properties are missing from the metadata_spec query:\n\n  ' + missing.join('\n  '));
    }

    return prunedFields;
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
  //     A
  //      \
  //       B
  //        \
  //         C
  //        / \
  //       D   E
  //      /     \
  //        ...
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
  //       *
  //      / \
  //     A   B
  //          \
  //           C
  //            \
  //             D
  //
  // into this:
  //
  //       *
  //      / \
  //     A   D
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

  // Sort tree such that terminal nodes are before non-terminal nodes
  function sortTree(tree) {
    return _(tree)
      .map(function(node) {
        if (node.children) {
          node.children = sortTree(node.children);
        }
        return node;
      })
      .sortBy(function(node) {
        return Boolean(node.children);
      })
      .value();
  }


  //
  // Model and Collection
  //

  var Field = Backbone.Model.extend({

    // instance properties

    idAttribute: 'term',

    defaults: {
      filteredValues: [],
      display: false
    },

    select: function() {
      this.trigger('select', this);
    }
  }, {

    // static properties

    // Use String as constructor so that the following is true:
    //
    //     'Uknown' !== Field.UNKNOWN_VALUE
    //
    // This will make it possible to differentiate between this
    // and a valid value of 'Unknown'.
    UNKNOWN_VALUE: new String('Unknown') // jshint ignore:line

  });


  var Fields = Backbone.Collection.extend({

    // Instance properties

    model: Field,

    initialize: function(models, options) {
      this.trimMetadataTerms = options.trimMetadataTerms;
    },

    // See static property getTree
    getTree: function() {
      var options = {
        trimMetadataTerms: this.trimMetadataTerms
      };
      return Fields.getTree(options, this.toJSON());
    }

  }, {

    // Static properties

    // Create a tree from a Fields-like object
    getTree: function(options, fields) {

      // Create tree, then prune it so it's easier to read
      var makeTree = options.trimMetadataTerms
        ? _.compose(sortTree, removeParentsWithSingleChild, removeSingleTopNode, constructTree)
        : _.compose(sortTree, constructTree);

      // get all ontology terms starting from `filterable` fields
      // and traversing upwards by the `parent` attribute
      var prunedFields = _.sortBy(pruneFields(fields), 'term');

      // get root tree
      var parentFields = _.reject(prunedFields, 'parent');

      // construct tree
      var groupedFields = makeTree(parentFields, prunedFields);

      // sort node such that leaves are first
      groupedFields = _.sortBy(groupedFields, function(node) {
        return node.field.leaf === 'true' ? 0 : 1;
      });

      return groupedFields;
    }
  });

  ns.Field = Field;
  ns.Fields = Fields;
});
