import invariant from 'invariant';

export const REST_TYPE = Symbol('REST_TYPE');

/**
 * Filter to encapsulate calls to REST service. The REST spec should have the
 * following keys:
 *
 *    * resource *      Path to resource
 *
 *    * method *        One of GET, POST, PUT, PATCH, DELETE
 *
 *    * types *         An array of types to dispatch:
 *                        1. The first type is dispatched when the request begins
 *                        2. The second type is dispatched if the request fails.
 *                        3. The third type is dispatched if the request succeeds
 *
 *                      The success action will have the following form:
 *                        Action { type, response }
 *
 *    * data *          An object to include as the request body.
 *
 *    * shouldFetch? *  A function that returns a boolean indicating if the
 *                      resource needs to be fetched. The function is called with
 *                      the current applicaiton state.
 */
export function createRestFilter(restAPI) {
  return function restFilter(store, next, action) {
    if (!(REST_TYPE in action)) {
      return next(action);
    }

    let spec = action[REST_TYPE];

    verifySpec(spec);

    let {
      resource,
      method,
      data,
      types: [ START_TYPE, ERROR_TYPE, SUCCESS_TYPE ],
      shouldFetch = getTrue
    } = spec;

    if (!shouldFetch(store.getState())) {
      return Promise.resolve(store.getState());
    }

    store.dispatch({ type: START_TYPE });

    return restAPI
    .requestResource(method, resource, data)
    .then(
      dispatchSuccessWith(store, SUCCESS_TYPE, data),
      dispatchErrorWith(store, ERROR_TYPE, data)
    );
  };
}

/**
 * Helper function to format the action properly. Also allows for early
 * validation.
 */
export function restAction(spec) {
  verifySpec(spec);
  return {
    [REST_TYPE]: spec
  };
}

function verifySpec(spec) {
  let methods = [ 'GET', 'POST', 'PUT', 'DELETE', 'PATCH' ];

  invariant(
    typeof spec.resource === 'string',
    'restAction `resource` must be a string.'
  );

  invariant(
    methods.includes(spec.method),
    'restAction `method` must be one of ' + methods.join(', ')
  );

  invariant(
    Array.isArray(spec.types),
    'restAction `types` must be an array of types specifying ' +
      '"request start", "request error", "request success".'
  );

  invariant(
    typeof spec.shouldUpdate === 'undefined' || typeof spec.shouldUpdate === 'function',
    'restAction `shouldUpdate` must be a function.'
  );
}

function dispatchSuccessWith(store, type, requestData) {
  return function dispatchSuccess(response) {
    return store.dispatch({ type, response, requestData });
  };
}

function dispatchErrorWith(store, type, requestData) {
  return function dispatchError(error) {
    return store.dispatch({ type, error, requestData });
  };
}

function getTrue() {
  return true;
}
