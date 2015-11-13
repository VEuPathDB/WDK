/**
 * Utils for common category tree traversal
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
