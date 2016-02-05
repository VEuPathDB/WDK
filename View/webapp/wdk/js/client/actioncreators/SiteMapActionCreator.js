import ActionCreator from '../utils/ActionCreator';

// Action types
let actionTypes = {
  SITEMAP_LOADING: 'sitemap/loading',
  SITEMAP_INITIALIZE_STORE: 'sitemap/initialize',
  SITEMAP_UPDATE_EXPANDED: 'sitemap/updateExpanded',
  APP_ERROR: 'sitemap/error'
};

export default class SiteMapActionCreator extends ActionCreator {

  loadCurrentSiteMap() {
    this._dispatch({ type: actionTypes.SITEMAP_LOADING });

    let ontologyPromise = this._service.getOntology('Categories');

    ontologyPromise.then((ontology) => {
      this._dispatch({
        type: actionTypes.SITEMAP_INITIALIZE_STORE,
        payload: { tree: ontology.tree }
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
