import { wrappable } from '../utils/componentUtils';
import WdkViewController from './WdkViewController';
import * as SiteMapActionCreator from '../actioncreators/SiteMapActionCreator';
import SiteMap from '../components/SiteMap';

class SiteMapController extends WdkViewController {

  getStoreName() {
    return "SiteMapStore";
  }

  getActionCreators() {
    return SiteMapActionCreator;
  }

  isRenderDataLoaded(state) {
    return (state.tree != null && !state.isLoading);
  }

  getTitle() {
    return "Data Finder";
  }

  renderView(state, eventHandlers) {
    return ( <SiteMap {...state} siteMapActions={eventHandlers}/> );
  }

  componentDidMount() {
    if (this.state.tree == null) {
      this.props.dispatchAction(SiteMapActionCreator.loadCurrentSiteMap());
    }
  }
}

export default wrappable(SiteMapController);
