import ActionCreator from '../utils/ActionCreator';

// Action types
let actionTypes = {
  SITEMAP_LOADING: 'sitemap/loading',
  SITEMAP_INITIALIZE_STORE: 'sitemap/initialize',
  SITEMAP_UPDATE_EXPANDED: 'sitemap/updateExpanded',
  APP_ERROR: 'sitemap/error'
};
import {
  getTree,
  nodeHasProperty,
} from '../utils/OntologyUtils';

export default class SiteMapActionCreator extends ActionCreator {

 loadCurrentSiteMap() {
    this._dispatch({ type: actionTypes.SITEMAP_LOADING });

    let ontologyPromise = this._service.getOntology('SiteMap');

    let isQualifying =  node => {
      return nodeHasProperty('scope', 'record', node) || nodeHasProperty('scope', 'menu', node) || nodeHasProperty('scope', 'webservice', node) || nodeHasProperty('scope', 'gbrowse', node) || nodeHasProperty('targetType', 'track', node);
    }

    ontologyPromise.then((ontology) => {
      this._dispatch({
        type: actionTypes.SITEMAP_INITIALIZE_STORE,
          payload: { tree: getTree(ontology, isQualifying).children }
      });
    }, this._errorHandler(actionTypes.APP_ERROR));
  }

  updateExpanded (expandedList) {
    this._dispatch({
      type: actionTypes.SITEMAP_UPDATE_EXPANDED,
      payload: { expandedList: expandedList}
    });
  }

}

SiteMapActionCreator.actionTypes = actionTypes;
