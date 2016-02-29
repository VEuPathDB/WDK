// Action types
export let actionTypes = {
  SITEMAP_LOADING: 'sitemap/loading',
  SITEMAP_INITIALIZE_STORE: 'sitemap/initialize',
  SITEMAP_UPDATE_EXPANDED: 'sitemap/updateExpanded',
  APP_ERROR: 'sitemap/error'
};
import {
  getTree,
  nodeHasProperty,
} from '../utils/OntologyUtils';

export function loadCurrentSiteMap() {
  return function run(dispatch, { wdkService }) {
    dispatch({ type: actionTypes.SITEMAP_LOADING });

    let ontologyPromise = wdkService.getOntology('SiteMap');

    let isQualifying =  node => {
      return nodeHasProperty('scope', 'record', node) || nodeHasProperty('scope', 'menu', node) || nodeHasProperty('scope', 'webservice', node) || nodeHasProperty('scope', 'gbrowse', node) || nodeHasProperty('targetType', 'track', node);
    }

    return ontologyPromise.then((ontology) => {
      return dispatch({
        type: actionTypes.SITEMAP_INITIALIZE_STORE,
          payload: { tree: getTree(ontology, isQualifying).children }
      });
    }, error => {
      dispatch({
        type: actionTypes.APP_ERROR,
        payload: { error }
      });
      throw error;
    });
  }
}

export function updateExpanded (expandedList) {
  return {
    type: actionTypes.SITEMAP_UPDATE_EXPANDED,
    payload: { expandedList: expandedList}
  };
}
