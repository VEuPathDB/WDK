import {
  default as _,
  flowRight as compose,
  constant,
  keyBy,
  memoize,
  mapValues,
  sortBy,
  reject,
  values
} from 'lodash';
import { Seq } from 'Utils/IterableUtils';
import {
  Field,
  MemberFilter,
  RangeFilter
} from "./FilterService";

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


type FilterPredicateFactory = (metadata: Metadata, filter: MemberFilter|RangeFilter) =>
  (datum: Datum) => boolean

function wrapFilterPredicateFactory(predicateFactory: FilterPredicateFactory): FilterPredicateFactory {
  return function filterPredicateWrapper(metadata: Metadata, filter: MemberFilter|RangeFilter) {
    const innerPredicate = predicateFactory(metadata, filter);
    return function wrapperPredicate(datum: Datum) {
      const { includeUnknown = false, value } = filter;
      const metadataValues = metadata[datum.term];

      // check for unknowns
      return metadataValues.length === 0 ? includeUnknown
        : value === undefined ? metadataValues.length > 0
        : innerPredicate(datum);
    }
  }
}

export const getMemberPredicate = wrapFilterPredicateFactory(
  <T>(metadata: Metadata, filter: MemberFilter) => {
    const filterValue = filter.value;
    return function memberPredicate(datum: Datum) {
      var metadataValues = metadata[datum.term];
      var index = filterValue!.length;
      var vIndex: number;

      // Use a for loop for efficiency
      outer: while(index--) {
        vIndex = metadataValues.length;
        while(vIndex--) {
          if (filterValue![index] === metadataValues[vIndex]) break outer;
        }
      }

      return (index > -1);
    }
  }
);

const getRangePredicateWith = <U>(mapValue: (value: string) => U) =>
  <T>(metadata: Metadata, filter: RangeFilter) => {
    var { min, max } = mapValues(filter.value, mapValue)
    var test = min !== null && max !== null ? makeWithin(min, max)
             : min !== null ? makeGte(min)
             : max !== null ? makeLte(max)
             : T;

    if (test === T) {
      throw new Error('Count not determine range predicate.');
    }

    return function rangePredicate(datum: Datum) {
      const values = metadata[datum.term];
      return values.some(value => test(mapValue(value)));
    }

  }

export const getDateRangePredicate = wrapFilterPredicateFactory(getRangePredicateWith(Date))

export const getNumberRangePredicate = wrapFilterPredicateFactory(getRangePredicateWith(Number))

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

type FieldNode = {
  field: Field;
  children: FieldNode[];
}

type ParentTerm = string | undefined;

export const getTree = memoize((ontologyEntries: Iterable<Field>): FieldNode => {
  const entriesByParentTerm = mapBy(ontologyEntries, term => term.parent);
  const rootChildren = (entriesByParentTerm.has(undefined) ? entriesByParentTerm.get(undefined)! : [])
    .map(entry => makeOntologyNode(entry, entriesByParentTerm));

  if (rootChildren.length == 1) return rootChildren[0];

  return {
    field: {
      type: 'string',
      term: 'root',
      display: 'Root'
    },
    children: sortBy(rootChildren, entry => entry.children.length === 0 ? -1 : 1)
  }
});

function makeOntologyNode(entry: Field, ontologyEntriesByParent: Map<ParentTerm, Field[]>): FieldNode {
  const children = (ontologyEntriesByParent.has(entry.term) ? ontologyEntriesByParent.get(entry.term)! : [])
    .map(e => makeOntologyNode(e, ontologyEntriesByParent));
  return {
    field: entry,
    children: sortBy(children, entry => entry.children.length === 0 ? -1 : 1)
  };
}

function mapBy<T, S>(iter: Iterable<T>, keyAccessor: (item: T) => S) {
  return Seq.from(iter)
    .reduce(function(map: Map<S, T[]>, item: T) {
      const key = keyAccessor(item);
      const itemArray = map.get(key) || [];
      itemArray.push(item);
      map.set(key, itemArray);
      return map;
    }, new Map<S, T[]>());
}


// Formatting and display

/**
 * Creates a display string describing a filter.
 *
 * @param {Field} field
 * @param {any} value
 * @param {boolean} includeUnknown
 */
export function getFilterValueDisplay(field: Field, filter: MemberFilter | RangeFilter): string {
  if (isRange(field)) {
    let { value, includeUnknown } = filter as RangeFilter;
    if (value != null && value.min == null && value.max == null && includeUnknown == false) {
      return 'No value selected';
    }

    const displayValue = value == null ? 'has a value'
                       : value.min == null && value!.max == null ? ''
                       : value.min == null ? `less than ${value.max}`
                       : value.max == null ? `greater than ${value.min}`
                       : `from ${value!.min} to ${value.max}`;
    return displayValue +
      (includeUnknown ? ( displayValue ? ', or is unspecified' : 'unspecified') : '');
  }

  else {
    let { value, includeUnknown } = filter as MemberFilter;
    if (value != null && value.length === 0 && includeUnknown === false) {
      return 'No value selected'
    }
    return (value == null ? 'has a value' : value.join(', ')) +
      (includeUnknown ? (value && value.length === 0 ? 'unspecified' : ', or is unspecified') : '');
  }
}

/**
 * Determine if a field should use a range filter display
 *
 * @param {Field} field
 */
export function isRange(field: Field) {
  return field.isRange || field.filter === 'range';
}

export function isMulti(field: Field) {
  return field.type === 'multiFilter';
}

export function isFilterField(field: Field) {
  return field.type != null;
}
