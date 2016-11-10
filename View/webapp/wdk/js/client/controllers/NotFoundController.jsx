import WdkViewController from './WdkViewController';
import { wrappable } from '../utils/componentUtils';

/**
 * Rendered whenever a URL does not match a route
 */
class NotFoundController extends WdkViewController {
  renderView() {
    return (
      <div className="wdkNotFoundPage">
        <h1>Page not found</h1>
        <p>The page you are looking for doesn't appear to exist.</p>
      </div>
    );
  }
}

export default wrappable(NotFoundController);
