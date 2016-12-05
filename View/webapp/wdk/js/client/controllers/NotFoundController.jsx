import WdkViewController from './WdkViewController';
import { wrappable } from '../utils/componentUtils';
import NotFound from '../components/NotFound';

/**
 * Rendered whenever a URL does not match a route
 */
class NotFoundController extends WdkViewController {
  getStoreName() {
    return "NotFoundStore";
  }
  renderView() {
    return (
      <NotFound/>
    );
  }
}

export default wrappable(NotFoundController);
