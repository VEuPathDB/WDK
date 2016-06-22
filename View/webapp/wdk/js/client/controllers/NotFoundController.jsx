/**
 * Rendered whenever a URL does not match a route
 */
import { wrappable } from '../utils/componentUtils';

function NotFoundController() {
  return (
    <div className="wdkNotFoundPage">
      <h1>Page not found</h1>
      <p>The page you are looking for doesn't appear to exist.</p>
    </div>
  );
}

export default wrappable(NotFoundController);
