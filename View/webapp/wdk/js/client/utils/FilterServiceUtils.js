import _ from 'lodash';


/**
 * Returns a lodash-wrapped array of metadata values.
 * See https://lodash.com/docs/#_ for details of lodash-wrapped objectd.
 *
 * There are two main benefits to the lodash wrapper:
 *   1. Operations can be expressed fluently as method calls.
 *   2. lodash will merge operations to use as few overall iterations as
 *      possible. It will also reduce the number of intermediate objects
 *      created on each iteration, thus reducing GC pressure.
 */
function flattenMetadataValues(metadata) {
  return _(metadata).values().flatten();
}

/**
 * Calculate the occurence of each value present in metadata.
 *
 * @param {object} metadata A key-value map of { sample: [ { value } ] }
 * @returns {object} A key-value map of { value: count }
 */
export function countByValues(metadata) {
  return flattenMetadataValues(metadata).countBy().value();
}

/**
 * Create a array of uniq metadata values
 */
export function uniqMetadataValues(metadata) {
  return flattenMetadataValues(metadata).sortBy().uniq(true).value();
}

export function getMemberPredicate(metadata, filter) {
  var filterValues = filter.values;

  return function memberPredicate(datum) {
    var metadataValues = metadata[datum.term];
    var index = filterValues.length;
    var vIndex;

    // Use a for loop for efficiency
    outer: while(index--) {
      if (metadataValues.length === 0) {
        if (filterValues[index] === null) return true;
        continue outer;
      }
      vIndex = metadataValues.length;
      while(vIndex--) {
        if (filterValues[index] === metadataValues[vIndex]) break outer;
      }
    }

    return (index > -1);
  };
}

export function getRangePredicate(metadata, filter) {
  var min = filter.values.min;
  var max = filter.values.max;
  var test = min !== null && max !== null ? makeWithin(min, max)
           : min !== null ? makeGte(min)
           : max !== null ? makeLte(max)
           : undefined;

  if (test === undefined) throw new Error('Count not determine range predicate.');

  return function rangePredicate(datum) {
    return metadata[datum.term].some(test);
  }
}

// Helper filtering functions
// --------------------------

export function makeGte(min) {
  return function gte(value) {
    return value >= min;
  };
}

export function makeLte(max) {
  return function lte(value) {
    return value <= max;
  };
}

export function makeWithin(min, max) {
  let gte = makeGte(min);
  let lte = makeLte(max);
  return function within(value) {
    return gte(value) && lte(value);
  };
}

export function passesWith(value) {
  return function passes(predicate) {
    return predicate(value);
  };
}

export function combinePredicates(predicates) {
  return function predicate(value) {
    return predicates.every(passesWith(value));
  };
}

// Field tree
// ----------

export function makeTree(fields, options) {

  // Create tree, then prune it so it's easier to read
  var make = options.trimMetadataTerms
    ? _.compose(/* sortTree, */ removeParentsWithSingleChild, removeSingleTopNode, constructTree)
    : _.compose(/* sortTree, */ constructTree);

  // get all ontology terms starting from `filterable` fields
  // and traversing upwards by the `parent` attribute
  var prunedFields = _.sortBy(pruneFields(fields), 'term');

  // get root tree
  var parentFields = _.reject(prunedFields, 'parent');

  // construct tree
  var groupedFields = make(parentFields, prunedFields);

  // sort node such that leaves are first
  groupedFields = _.sortBy(groupedFields, function(node) {
    return node.field.leaf === 'true' ? 0 : 1;
  });

  return groupedFields;
}

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
        : { field: field, children: [] };
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
