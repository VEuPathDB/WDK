/**
 * Created by steve on 3/28/2016.
 */
import partial from 'lodash/function/partial';

/**
* Filter a provided list of (generic) items.
*
* @param {Array<Object>} items           The list of items to filter
* @param {function}   itemStringMaker       A function from item=>String.  Returns the string to search, given an item
* @param {string}     searchExpression      The query with which to filter records
* @return an Array of items that pass the filter
*/
export function filterItems(items, itemStringMaker, searchExpression) {
    if (!searchExpression || !items) return items;

    let terms = parseSearchExpression(searchExpression);
    return terms.reduce(function(items, term) {
        let predicate = partial(isTermInString, itemStringMaker(term));
        return items.filter(predicate);
    }, items);
}

// Split terms on whitespace, unless wrapped in quotes
function parseSearchExpression(terms) {
    let match = terms.match(/\w+|"[^"]*"/g) || [];
    return match.map(function(term) {
        // remove wrapping quotes from phrases
        return term.replace(/(^")|("$)/g, '');
    });
}

function isTermInString(term, stringToCheck) {
    return stringToCheck.toLowerCase().includes(term.toLowerCase());
}


