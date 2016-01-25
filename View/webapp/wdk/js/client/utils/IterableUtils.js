/**
 * Useful operators for Iterables.
 *
 * The module exports the function `seq` which creates a wrapper that exposes
 * a fluent interface for traversing and manipulating the underlying iterable
 * object using operators.
 *
 * The module also exports each operator as a function for use in isolation.
 *
 * All methods and functions return a new Iterable object. Iteration is lazy
 * and will only execute when a value is requested (toArray, reduce, etc).
 * The iteration will terminate as early as possible.
 *
 * For example, the following `seq` code will only iterate 3 times:
 *
 *    let array = [];
 *    for (let i = 1; i <= 1000; i++) {
 *      array.push(i);
 *    }
 *
 *    // 3 iterations
 *    seq(array)
 *      .map(n => n * n)
 *      .filter(n => n % 2 === 1)
 *      .takeUntil(n => n > 30)
 *      .toArray() // [ 1, 9, 25 ]
 *
 */

/**
 * Wraps `iterable` in an object with collection operations.
 *
 * @param {Iterable<T>} iterable
 * @return {Seq<T>}
 */
export function seq(iterable) {
  return new Seq(iterable);
}

/**
 * Underlying class used by `seq`.
 */
class Seq {

  /**
   * @param {Iterable<T>} iterable
   */
  constructor(iterable) {
    this[Symbol.iterator] = iterable[Symbol.iterator].bind(iterable);
  }

  map(fn) {
    return new Seq(map(fn, this));
  }

  filter(fn) {
    return new Seq(filter(fn, this));
  }

  take(n) {
    return new Seq(take(n, this));
  }

  takeWhile(fn) {
    return new Seq(takeWhile(fn, this));
  }

  drop(n) {
    return new Seq(drop(n, this));
  }

  dropWhile(fn) {
    return new Seq(dropWhile(fn, this));
  }

  find(fn) {
    return find(fn, this);
  }

  findLast(fn) {
    return findLast(fn, this);
  }

  reduce(fn, value) {
    return arguments.length === 1 ? reduce(fn, this)
    : reduce(fn, value, this);
  }

  toArray() {
    return Array.from(this);
  }

  into(Type) {
    return Type.from(this);
  }

  first() {
    return first(this);
  }

  last() {
    return last(this);
  }

  rest() {
    return rest(this);
  }

}

export function map(fn, iterable) {
  return {
    *[Symbol.iterator]() {
      for (let x of iterable) {
        yield fn(x);
      }
    }
  }
}

export function filter(fn, iterable) {
  return {
    *[Symbol.iterator]() {
      for (let x of iterable) {
        if (fn(x)) yield x;
      }
    }
  }
}

export function take(n, iterable) {
  return {
    *[Symbol.iterator]() {
      let count = 0;
      for (let x of iterable) {
        if (count++ < n) yield x;
        else break;
      }
    }
  }
}

/**
 * Keep items until test returns false.
 */
export function takeWhile(fn, iterable) {
  return {
    *[Symbol.iterator]() {
      for (let x of iterable) {
        if (fn(x) === false) break;
        yield x;
      }
    }
  }
}

export function drop(n, iterable) {
  return {
    *[Symbol.iterator]() {
      for (let x of iterable) {
        if (n-- > 0) continue;
        yield x;
      }
    }
  }
}
/**
 * Ignore items until test returns false.
 */
export function dropWhile(fn, iterable) {
  return {
    *[Symbol.iterator]() {
      let take = false;
      for (let item of iterable) {
        if (take === false) take = !fn(item);
        if (take === true) yield item;
      }
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

export function first(iterable) {
  return iterable[Symbol.iterator]().next().value;
}

export function last(iterable) {
  let iterator = iterable[Symbol.iterator]();
  let current, value;
  while ( current = iterator.next(), !current.done) {
    value = current.value;
  }
  return value;
}

export function rest(iterable) {
  return drop(1, iterable);
}

/**
 * Reduce collection to a single value.
 */
export function reduce(fn, value, iterable) {
  if (arguments.length === 2) {
    iterable = rest(value);
    value = first(value);
  }
  for (let item of iterable) {
    value = fn(value, item);
  }
  return value;
}
