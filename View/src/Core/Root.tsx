import $ from 'jquery';
import * as React from 'react';
import PropTypes from 'prop-types';
import { Router, Switch, Route, RouteComponentProps } from 'react-router';
import { History, Location } from 'history';
import { MakeDispatchAction, Container, ViewControllerProps, RouteSpec, LocatePlugin } from "Core/CommonTypes";
import WdkStore from "Core/State/Stores/WdkStore";
import ErrorBoundary from 'Core/Controllers/ErrorBoundary';
import LoginFormController from 'Views/User/LoginForm/LoginFormController';

type Props = {
  rootUrl: string,
  makeDispatchAction: MakeDispatchAction,
  stores: Container<WdkStore>,
  routes: RouteSpec[],
  onLocationChange: (location: Location) => void;
  history: History;
  locatePlugin: LocatePlugin;
};


let REACT_ROUTER_LINK_CLASSNAME = 'wdk-ReactRouterLink';
let GLOBAL_CLICK_HANDLER_SELECTOR = `a:not(.${REACT_ROUTER_LINK_CLASSNAME})`;
let RELATIVE_LINK_REGEXP = new RegExp('^((' + location.protocol + ')?//)?' + location.host);

/** WDK Application Root */
export default class Root extends React.Component<Props> {

  static propTypes = {
    rootUrl: PropTypes.string,
    makeDispatchAction: PropTypes.func.isRequired,
    stores: PropTypes.object.isRequired,
    routes: PropTypes.array.isRequired,
    onLocationChange: PropTypes.func
  };

  static defaultProps = {
    rootUrl: '/',
    onLocationChange: () => {}    // noop
  };

  removeHistoryListener: () => void;

  dispatchAction = this.props.makeDispatchAction('error');

  constructor(props: Props) {
    super(props);
    this.renderRoute = this.renderRoute.bind(this);
    this.handleGlobalClick = this.handleGlobalClick.bind(this);
    this.removeHistoryListener = this.props.history.listen(location => this.props.onLocationChange(location));
    this.props.onLocationChange(this.props.history.location);
  }

  renderRoute(RouteComponent: React.ComponentType<ViewControllerProps<WdkStore>>) {
    // Used to inject wdk content as props of Route Component
    return (routerProps: RouteComponentProps<any>) => {
      let { locatePlugin, makeDispatchAction, stores } = this.props;
      return (
        <RouteComponent
          {...routerProps}
          locatePlugin={locatePlugin}
          makeDispatchAction={makeDispatchAction}
          stores={stores}
        />
      );
    };
  }

  handleGlobalClick(event: JQuery.Event<HTMLAnchorElement>) {
    const target = event.currentTarget;
    if (!target.href) return;

    let hasModifiers = event.metaKey || event.altKey || event.shiftKey || event.ctrlKey || event.button !== 0;
    let href = (target.getAttribute('href') || '').replace(RELATIVE_LINK_REGEXP, '');
    if (!hasModifiers && href.startsWith(this.props.rootUrl)) {
      this.props.history.push(href.slice(this.props.rootUrl.length));
      event.preventDefault();
    }
  }

  componentDidMount() {
    /** install global click handler */
    $(document).on('click', GLOBAL_CLICK_HANDLER_SELECTOR, this.handleGlobalClick);
  }

  componentWillUnmount() {
    $(document).off('click', GLOBAL_CLICK_HANDLER_SELECTOR, this.handleGlobalClick);
    this.removeHistoryListener();
  }

  render() {
    return (
      <ErrorBoundary dispatchAction={this.dispatchAction}>
        <Router history={this.props.history}>
          <React.Fragment>
            <Switch>
              {this.props.routes.map(route => (
                <Route key={route.path} exact path={route.path} render={this.renderRoute(route.component)}/>
              ))}
            </Switch>
            <LoginFormController
              locatePlugin={this.props.locatePlugin}
              makeDispatchAction={this.props.makeDispatchAction}
              stores={this.props.stores}
            />
          </React.Fragment>
        </Router>
      </ErrorBoundary>
    );
  }
}
