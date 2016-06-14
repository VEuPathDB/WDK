import $ from 'jquery';
import {Component, PropTypes} from 'react';
import {match, useRouterHistory, Router, Route, IndexRoute} from 'react-router';
import {createHistory} from 'history';
import AppController from './AppController';
import IndexController from './IndexController';
import RecordController from './RecordController';
import NotFoundController from './NotFoundController';
import AnswerRouteHandler from './AnswerRouteHandler';
import QuestionListController from './QuestionListController';
import StepDownloadFormController from './StepDownloadFormController';
import UserProfileController from './UserProfileController';
import SiteMapController from './SiteMapController';
import Loading from '../components/Loading';

let REACT_ROUTER_LINK_CLASSNAME = 'wdk-ReactRouterLink';
let GLOBAL_CLICK_HANDLER_SELECTOR = `a:not(.${REACT_ROUTER_LINK_CLASSNAME})`;
let RELATIVE_LINK_REGEXP = new RegExp('^((' + location.protocol + ')?//)?' + location.host);

/** Wdk Application Root */
export default class Root extends Component {

  constructor(props, context) {
    super(props, context);
    this.history = useRouterHistory(createHistory)({ basename: this.props.rootUrl });
    // Used to inject wdk content as props of Route Component
    this.createElement = (RouteComponent, routerProps) => {
      let { dispatchAction, stores, wdkService } = this.props;
      return (
        <RouteComponent {...routerProps} dispatchAction={dispatchAction} stores={stores} wdkService={wdkService}/>
      );
    };
    this.routes = (
      <Route path="/" component={AppController}>
        <IndexRoute component={IndexController}/>
        <Route path="search/:recordClass/:question/result" component={AnswerRouteHandler}/>
        <Route path="record/:recordClass/download/*" component={StepDownloadFormController}/>
        <Route path="record/:recordClass/*" component={RecordController}/>
        <Route path="step/:stepId/download" component={StepDownloadFormController}/>
        <Route path="user/profile" component={UserProfileController}/>
        <Route path="data-finder" component={SiteMapController}/>
        <Route path="question-list" component={QuestionListController}/>
        {this.props.applicationRoutes.map(route => ( <Route key={route.path} {...route}/> ))}
        <Route path="*" component={NotFoundController}/>
      </Route>
    );
    this.handleGlobalClick = this.handleGlobalClick.bind(this);
    this.state = { wdkModel: undefined, wdkConfig: undefined };
  }

  handleGlobalClick(event) {
    let hasModifiers = event.metaKey || event.altKey || event.shiftKey || event.ctrlKey || event.which !== 1;
    let href = event.currentTarget.getAttribute('href').replace(RELATIVE_LINK_REGEXP, '');
    if (!hasModifiers && href.startsWith(this.props.rootUrl)) {
      this.history.push(href.slice(this.props.rootUrl.length));
      event.preventDefault();
    }
  }

  componentDidMount() {
    /** install global click handler */
    $(document).on('click', GLOBAL_CLICK_HANDLER_SELECTOR, this.handleGlobalClick);
    // kick off loading of static resources
    Promise.all([
      this.props.wdkService.getConfig(),
      this.props.wdkService.getQuestions(),
      this.props.wdkService.getRecordClasses(),
      this.props.wdkService.getOntology()
    ]).then(([config, questions, recordClasses]) => {
      this.setState({ wdkModel: { config, questions, recordClasses }});
    });
  }

  componentWillUnmount() {
    $(document).off('click', GLOBAL_CLICK_HANDLER_SELECTOR, this.handleGlobalClick);
  }

  getChildContext() {
    return {
      wdkModel: this.state.wdkModel
    };
  }

  render() {
    if (this.state.wdkModel == null) return <Loading/>;
    return (
      <Router history={this.history} createElement={this.createElement} routes={this.routes}/>
    );
  }

}

Root.childContextTypes = {
  wdkModel: PropTypes.object
};

Root.propTypes = {
  rootUrl: PropTypes.string,
  dispatchAction: PropTypes.func.isRequired,
  stores: PropTypes.object.isRequired,
  applicationRoutes: PropTypes.array.isRequired,
  wdkService: PropTypes.object.isRequired
};

Root.defaultProps = {
  rootUrl: '/'
};

