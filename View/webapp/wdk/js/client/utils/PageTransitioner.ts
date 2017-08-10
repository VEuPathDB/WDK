import { History } from 'history';

export type TransitionFunction = (url: string) => void;

export interface PageTransitioner {
  transitionToExternalPage: TransitionFunction;
  transitionToInternalPage: TransitionFunction;
}

/**
 * Creates a page transitioner service that provides convenience methods for
 * navigating seamlessly to an internal page, or to an external page.
 * 
 * This provides a centralized location to perform page transition logic
 * from action creators.
 * 
 * @param {History} history
 */
export function getTransitioner(history: History) {
  let transitionToInternalPage: TransitionFunction = url => { history.push(url); };
  let transitionToExternalPage: TransitionFunction = url => { window.location.assign(url); };
  return { transitionToInternalPage, transitionToExternalPage };
}