import * as React from 'react';
import { wrappable } from '../utils/componentUtils';
import AbstractViewController from './AbstractViewController';
import * as SiteMapActionCreators from '../actioncreators/SiteMapActionCreators';
import SiteMapStore, { State } from '../stores/SiteMapStore';
import SiteMap from '../components/SiteMap';

type EventHandlers = typeof SiteMapActionCreators;

class SiteMapController extends AbstractViewController<State, SiteMapStore, EventHandlers> {

  getStoreClass() {
    return SiteMapStore;
  }

  getStateFromStore() {
    return this.store.getState();
  }

  getActionCreators() {
    return SiteMapActionCreators;
  }

  isRenderDataLoaded() {
    return (this.state.tree != null && !this.state.isLoading);
  }

  getTitle() {
    return "Data Finder";
  }

  renderView() {
    return ( <SiteMap {...this.state} siteMapActions={this.eventHandlers}/> );
  }

  loadData() {
    if (this.state.tree == null) {
      this.eventHandlers.loadCurrentSiteMap();
    }
  }

}

export default wrappable(SiteMapController);
