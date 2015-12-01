/**
 * Useful operators for Iterators. For the most part, these will return an
 * iterable object. Iteration will not be performed until the iterable is
 * reduced to a value.
 *
 * The interface is currently functional but is subject to change.
 *
 *
 * A basic example where we square items and then filter out odds:
 *
 *   let square = map(n => n * n);
 *   let even = filter(n => n % 2 === 0);
 *   let transform = compose(even, square);
 *
 *   Array.from(transform([1, 2, 3, 4, 5])); //=> [ 4, 16 ]
 */

export function compose(fn, ...rest) {
  return function* composed(iter) {
    yield* rest.length === 0
      ? fn(iter)
      : fn(compose(...rest)(iter));
  }
}

export function map(transform) {
  return function* mapped(iter) {
    for (let item of iter) {
      yield transform(item);
    }
  }
}

export function filter(test) {
  return function* filtered(iter) {
    for (let item of iter) {
      if (test(item)) yield item;
    }
  }
}

export function take(n) {
  return function* taken(iter) {
    for (let item of iter) {
      if (n-- > 0) yield item;
      else break;
    }
  }
}

/**
 * Keep items until test returns false.
 */
export function takeWhile(test) {
  return function* takenWhile(iter) {
    for (let item of iter) {
      if (test(item) === false) break;
      yield item;
    }
  }
}

/**
 * Ignore items until test returns false.
 */
export function dropWhile(test) {
  return function* droppedWhile(iter) {
    let take = false;
    for (let item of iter) {
      if (take === false) take = !test(item);
      if (take === true) yield item;
    }
  }
}


// Return values -- an item from iterable collection, or a reduction

/**
 * Find the first item that test returns true for.
 */
export function find(test, iter) {
  for (let item of iter) {
    if (test(item) === true) return item;
  }
}

/**
 * Find the last item that the test returns true for.
 */
export function findLast(test, iter) {
  let last;
  for (let item of iter) {
    if (test(item)) last = item;
  }
  return last;
}

/**
 * Reduce collection to a single value.
 */
export function reduce(reducer, initialValue, iter) {
  let acc = initialValue;
  for (let item of iter) {
    acc = reducer(acc, item);
  }
  return acc;
}
