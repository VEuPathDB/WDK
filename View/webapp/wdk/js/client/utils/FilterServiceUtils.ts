import {
  default as _,
  flowRight as compose,
  constant,
  memoize,
  mapValues,
  sortBy,
  reject,
  values
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
  const filterValue = filter.value;
  return withUnknownCheck(metadata, filter, function memberPredicate(datum: Datum) {
    var metadataValues = metadata[datum.term];
    var index = filterValue.length;
    var vIndex: number;

    // Use a for loop for efficiency
    outer: while(index--) {
      vIndex = metadataValues.length;
      while(vIndex--) {
        if (filterValue[index] === metadataValues[vIndex]) break outer;
      }
    }

    return (index > -1);
  });
}

export function getDateRangePredicate<T>(metadata: Metadata, filter: RangeFilter) {
  return getRangePredicate(metadata, filter, Date);
}

export function getNumberRangePredicate<T>(metadata: Metadata, filter: RangeFilter) {
  return getRangePredicate(metadata, filter, Number);
}

function getRangePredicate<T, U>(metadata: Metadata, filter: RangeFilter, mapValue: (value: string) => U) {
  var { min, max } = mapValues(filter.value, mapValue)
  var test = min !== null && max !== null ? makeWithin(min, max)
           : min !== null ? makeGte(min)
           : max !== null ? makeLte(max)
           : T;

  if (test === T) {
    throw new Error('Count not determine range predicate.');
  }

  return withUnknownCheck(metadata, filter, function rangePredicate(datum: Datum) {
    const values = metadata[datum.term];
    return values.some(value => test(mapValue(value)));
  });

}

function withUnknownCheck(metadata: Metadata, filter: MemberFilter|RangeFilter, predicate: (datum: Datum) => boolean) {
  return function (datum: Datum) {
    const { includeUnknown = false } = filter;
    const metadataValues = metadata[datum.term];
    if (!Array.isArray(metadataValues)) throw new Error("Could not find metadata values for `" + datum.term + "`");
    return metadataValues.length === 0 ? includeUnknown : predicate(datum);
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

type Field = {
  parent?: string;
  term: string;
  display?: string;
}

type FieldNode = {
  field: Field;
  children: FieldNode[];
}

export const getTree = memoize((ontologyDict: Record<string, Field>): FieldNode => {
  const ontologyEntries = values(ontologyDict);
  const rootChildren = ontologyEntries
    .filter((entry) => entry.parent == null)
    .map((entry) => makeOntologyNode(entry, ontologyEntries));

  if (rootChildren.length == 1) return rootChildren[0];

  return {
    field: {
      term: 'root',
      display: 'Root'
    },
    children: sortBy(rootChildren, entry => entry.children.length === 0 ? -1 : 1)
  }
});

function makeOntologyNode(entry: Field, ontologyEntries: Field[]): FieldNode {
  const children = ontologyEntries
    .filter(e => e.parent === entry.term)
    .map(e => makeOntologyNode(e, ontologyEntries));
  return {
    field: entry,
    children: sortBy(children, entry => entry.children.length === 0 ? -1 : 1)
  };
}
