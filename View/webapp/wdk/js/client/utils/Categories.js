/**
 * Utils for common category tree traversal
 */

export function reduce(categories, reducer, initialValue) {
  if (categories == null) return initialValue;
  return categories.reduce(function(acc, category) {
    return reduce(category.subCategories, reducer, reducer(acc, category));
  }, initialValue);
}

export function takeWhile(categories, test, includeChildren = true) {
  let stack = [...categories];
  let acc = [];
  while (stack.length > 0) {
    let category = stack.shift();
    if (test(category) === false) break;
    if (includeChildren && category.subCategories != null)
      stack.unshift(...category.subCategories);
    acc.push(category);
  }
  return acc;
}
