/**
 * Utils for common category tree traversal.
 *
 * TODO Make CategoryTree recursively iterable, and make the helpers below
 * generic for all iterables.
 */

export function reduce(categories, reducer, initialValue) {
  let acc = initialValue;
  for (let category of iter(categories)) {
    acc = reducer(acc, category);
  }
  return acc;
}

export function takeWhile(categories, test, includeChildren = true) {
  let acc = [];
  for (let category of iter(categories, includeChildren)) {
    if (test(category) === false) break;
    acc.push(category);
  }
  return acc;
}

/**
 * Ignore items until test returns false.
 */
export function dropWhile(categories, test, includeChildren = true) {
  let take = false;
  let acc = [];
  for (let category of iter(categories, includeChildren)) {
    if (take === false) take = !test(category);
    if (take === true) acc.push(category);
  }
  return acc;
}

export function find(categories, test, includeChildren = true) {
  for (let category of iter(categories, includeChildren)) {
    if (test(category) === true) return category;
  }
}

export function findLast(categories, test, includeChildren = true) {
  let r;
  for (let category of iter(categories, includeChildren)) {
    if (test(category)) r = category;
  }
  return r;
}


// Generators - Functions that return an iterable object
// see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Iterators_and_Generators
// FIXME Use better names.

export function* itercategory(category, includeChildren = true) {
  yield category;
  if (includeChildren)
    yield* iter(category.subCategories);
}

export function* iter(categories, includeChildren = true) {
  if (categories == null) return;
  for (let category of categories) {
    yield* itercategory(category, includeChildren);
  }
}
