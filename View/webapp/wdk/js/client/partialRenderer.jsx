import {cloneElement, Children, PropTypes} from 'react';
import {render} from 'react-dom';
import {Route, Router} from 'react-router';
import WdkContext from './WdkContext';

/**
 * Used to render a sub-component. This ensures that the router context is
 * created so that Link components from react-router work.
 */
export function create(context, history) {
  return function renderPartial(Component, props, targetNode) {
    let createElement = createPartialElementWith(props, context);
    return render((
      <WdkContext {...context}>
        <Router history={history} createElement={createElement}>
          <Route path="*" component={Component}/>
        </Router>
      </WdkContext>
    ), targetNode);
  }
}

/** merge props, routerProps, and context */
function createPartialElementWith(props, context) {
  return function createElement(Component, routerProps) {
    let finalProps = Object.assign({}, routerProps, context, props);
    return <Component {...finalProps} />;
  };
}
