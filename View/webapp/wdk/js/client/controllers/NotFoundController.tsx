import * as React from 'react';
import WdkPageController from './WdkPageController';
import { wrappable } from '../utils/componentUtils';
import NotFound from '../components/NotFound';

/**
 * Rendered whenever a URL does not match a route
 */
class NotFoundController extends WdkPageController {
  renderView() {
    return (
      <NotFound/>
    );
  }
}

export default wrappable(NotFoundController);
