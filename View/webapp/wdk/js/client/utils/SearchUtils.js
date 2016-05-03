/**
 * Created by steve on 3/28/2016.
 */
import partial from 'lodash/function/partial';

/**
* Filter a provided list of (generic) items.
* Uses a "multi-term" search approach.  The search expression is parsed into query terms
* (space delimited; quoted strings treated as one term).  Items match if their searchable string contains
* all of the terms.
*
* @param {Array<Object>} items           The list of items to filter
* @param {function}   itemToSearchableString A function from item=>String.  Returns the searchable string, given an item
* @param {string}     searchQueryString      The query, in string form, with which to filter records
* @return {Array<Object>} an Array of items that pass the filter
*/
export function filterItems(items, itemToSearchableString, searchQueryString) {
    if (!searchQueryString || !items) return items;

    let terms = parseSearchQueryString(searchQueryString);
    let predicate = function (item) { return areTermsInString(terms, itemToSearchableString(item))};
    return items.filter(predicate);
}

/**
 * Split search query string on whitespace, unless wrapped in quotes
 * @param {string} searchQueryString A string representing the search query
 * @returns {Array<String>} A set of query terms parsed from searchQueryString
 */
export function parseSearchQueryString(searchQueryString) {
    let match = searchQueryString.match(/\w+|"[^"]*"/g) || [];
    return match.map(function(queryTerm) {
        // remove wrapping quotes from phrases
        return queryTerm.replace(/(^")|("$)/g, '');
    });
}

/**
 * Return a boolean indicating if all the queryTerms are found in the searchableString
 * @param queryTerms An array of queryTerms to search with
 * @param searchableString The string to search.
 * @returns boolean
 */
export function areTermsInString(queryTerms, searchableString) {
    return queryTerms.reduce(function (matchesFlag, term) {
        return matchesFlag && isTermInString(term, searchableString)
    }, true);
}

/**
 * Return a boolean indicating if the query term is found in the searchableString
 * @param {string} queryTerm
 * @param {string} searchableString
 * @returns {boolean} true if a match
 */
export function isTermInString(queryTerm, searchableString) {
    return !queryTerm || (searchableString && searchableString.toLowerCase().includes(queryTerm.toLowerCase()));
}


