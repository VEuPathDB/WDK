/**
 * Mixin that is added to the predicate function
 */
let PredicateMixin = {

  or(test) {
    return predicate((...args) => this(...args) || test(...args));
  },

  and(test) {
    return predicate((...args) => this(...args) && test(...args));
  }

};

/**
 * Create a chainable function that wraps `test`.
 */
export default function predicate(test) {
  return Object.assign((...args) => test(...args), PredicateMixin);
}
