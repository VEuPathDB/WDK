/**
 * Created by steve on 3/28/2016.
 */
import partial from 'lodash/function/partial';

/**
* Filter a provided list of (generic) items.
* Uses a "multi-term" search approach.  The search expression is parsed into terms
* (space delimited; quoted strings treated as one term).  Items match if their searchable string contains
* all of the terms.
*
* @param {Array<Object>} items           The list of items to filter
* @param {function}   itemToSearchableString A function from item=>String.  Returns the string to search, given an item
* @param {string}     searchExpression      The query with which to filter records
* @return an Array of items that pass the filter
*/
export function filterItems(items, itemToSearchableString, searchExpression) {
    if (!searchExpression || !items) return items;

    let terms = parseSearchExpression(searchExpression);
    return terms.reduce(function(items, term) {
        let predicate = partial(isTermInString, itemToSearchableString(term));
        return items.filter(predicate);
    }, items);
}

/**
 * Split terms on whitespace, unless wrapped in quotes
 * @param terms
 * @returns {Array}
 */
export function parseSearchExpression(terms) {
    let match = terms.match(/\w+|"[^"]*"/g) || [];
    return match.map(function(term) {
        // remove wrapping quotes from phrases
        return term.replace(/(^")|("$)/g, '');
    });
}

/**
 * Return a boolean indicating if all the terms are found in the searchableString
 * @param terms An array of terms to search with
 * @param searchableString The string to search.
 */
export function areTermsInString(terms, searchableString) {
    return terms.reduce(function (matchesFlag, term) {
        return matchesFlag && isTermInString(term, searchableString)
    }, true);
}

/**
 *
 * @param term
 * @param searchableString
 * @returns {boolean}
 */
export function isTermInString(term, searchableString) {
    return searchableString.toLowerCase().includes(term.toLowerCase());
}


