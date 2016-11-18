import {
  default as _,
  flowRight as compose,
  constant,
  mapValues,
  sortBy,
  reject
} from 'lodash';
import {MemberFilter, RangeFilter} from "./FilterService";

type Metadata = {
  [key: string]: string[];
};

type Datum = {
  term: string;
};

type Predicate<T> = (value: T) => boolean;

const T = constant(true);

/**
 * Returns a lodash-wrapped array of metadata values.
 * See https://lodash.com/docs/#_ for details of lodash-wrapped object.
 *
 * There are two main benefits to the lodash wrapper:
 *   1. Operations can be expressed fluently as method calls.
 *   2. lodash will merge operations to use as few overall iterations as
 *      possible. It will also reduce the number of intermediate objects
 *      created on each iteration, thus reducing GC pressure.
 */
function flattenMetadataValues(metadata: Metadata) {
  return _(metadata).values().flatten();
}

/**
 * Calculate the occurrence of each value present in metadata.
 */
export function countByValues(metadata: Metadata) {
  return flattenMetadataValues(metadata).countBy().value();
}

/**
 * Create a array of unique metadata values
 */
export function uniqMetadataValues(metadata: Metadata) {
  return flattenMetadataValues(metadata).sortBy().sortedUniq().value() as string[];
}

export function getMemberPredicate<T>(metadata: Metadata, filter: MemberFilter) {
  let filterValues = filter.values;
  return function memberPredicate(datum: Datum) {
    var metadataValues = metadata[datum.term];
    var index = filterValues.length;
    var vIndex: number;

    // Use a for loop for efficiency
    outer: while(index--) {
      if (metadataValues.length === 0) {
        if (filterValues[index] === null) return true;
        continue;
      }
      vIndex = metadataValues.length;
      while(vIndex--) {
        if (filterValues[index] === metadataValues[vIndex]) break outer;
      }
    }

    return (index > -1);
  };
}

export function getRangePredicate<T>(metadata: Metadata, filter: RangeFilter) {
  var { min, max } = filter.field.type === 'number'
    ? mapValues(filter.values, s => Number(s))
    : mapValues(filter.values, s => new Date(s));
  var test = min !== null && max !== null ? makeWithin(min, max)
           : min !== null ? makeGte(min)
           : max !== null ? makeLte(max)
           : T;

  if (test === T) {
    throw new Error('Count not determine range predicate.');
  }

  return function rangePredicate(datum: Datum) {
    return metadata[datum.term].some(value =>
      test(filter.field.type === 'number' ? Number(value) : new Date(value)));
  }

}

// Helper filtering functions
// --------------------------

export function makeGte<T>(min: T) {
  return function gte(value: T) {
    return value >= min;
  };
}

export function makeLte<T>(max: T) {
  return function lte(value: T) {
    return value <= max;
  };
}

export function makeWithin<T>(min: T, max: T) {
  let gte = makeGte(min);
  let lte = makeLte(max);
  return function within(value: T) {
    return gte(value) && lte(value);
  };
}

export function passesWith<T>(value: T) {
  return function passes(predicate: Predicate<T>) {
    return predicate(value);
  };
}

export function combinePredicates<T>(predicates: Predicate<T>[]) {
  return function predicate(value: T) {
    return predicates.every(passesWith(value));
  };
}

// Field tree
// ----------

type Field = {
  term: string;
  parent?: string;
  leaf?: 'true';
};

type FieldTreeNode = {
  field: Field;
  children: FieldTreeNode[];
};

type Options = {
  trimMetadataTerms: boolean;
};

export function makeTree(fields: Field[], options: Options = { trimMetadataTerms: false }) {

  // Create tree, then prune it so it's easier to read
  var make = options.trimMetadataTerms
    ? compose(/* sortTree, */ removeParentsWithSingleChild, removeSingleTopNode, constructTree)
    : compose(/* sortTree, */ constructTree);

  // get all ontology terms starting from `filterable` fields
  // and traversing upwards by the `parent` attribute
  var prunedFields: Field[] = sortBy(pruneFields(fields), 'term');

  // get root tree
  var parentFields: Field[] = reject(prunedFields, 'parent');

  // construct tree
  var groupedFields = make(parentFields, prunedFields);

  // sort node such that leaves are first
  groupedFields = sortBy(groupedFields, function(node: FieldTreeNode) {
    return node.field.leaf === 'true' ? 0 : 1;
  });

  return groupedFields;
}

// Given a list of fields:
// First, find all fields marked as `leaf` (this means
//   the field is terminating and data can be filtered by it).
// Then, for each field, find all parents.
function pruneFields(fields: Field[]) {
  var missing: string[] = [];
  var prunedFields = fields
    .filter(field => field.leaf === 'true')
    .reduce(function(prunedFields, field) {
      while (field.parent) {
        prunedFields.add(field);
        let nextField = fields.find(f => f.term === field.parent);
        if (!nextField) {
          missing.push(field.parent);
          break;
        }
        field = nextField;
      }
      return prunedFields.add(field);
    }, new Set() as Set<Field>)

  if (missing.length) {
    alert('The following properties are missing from the metadata_spec query:\n\n  ' + missing.join('\n  '));
  }
  return Array.from(prunedFields);
}

// Convert a list to a tree* based on the `parent` property
// given a list of root nodes and a list of all fields
//
// * More accurately, this will create one or more trees,
//   one for each initial sibling field. But we can imagine
//   a common, hidden, root node.
function constructTree(siblingFields: Field[], allFields: Field[]): FieldTreeNode[] {
  return siblingFields
    .map(function(field) {
      var children = allFields.filter(child => child.parent === field.term);

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
function removeSingleTopNode(tree: FieldTreeNode[]) {
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
function removeParentsWithSingleChild(tree: FieldTreeNode[]) {
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
