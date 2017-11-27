import $ from 'jquery';
import React from 'react';
import { get } from 'lodash';
import ReactDOM from 'react-dom';
import { createDeferred } from './client/utils/PromiseUtils';
import { Seq } from './client/utils/IterableUtils';
import * as Wdk from './client/main';
import AbstractViewController from './client/controllers/AbstractViewController';
import { V4MAPPED } from 'dns';
import { MultiGrid } from 'react-virtualized/dist/es/MultiGrid';

export * from './client/index';

type ViewControllerResolver = (id: string) => Promise<AbstractViewController> | AbstractViewController;

// Only allow initialize to be called once
let _context: any;
let _deferredContext = createDeferred();

export function initialize(config: any) {
  if (_context != null) {
    console.log('`initialize` may only be called once on legacy WDK pages.');
  }
  else {
    _context = Wdk.initialize(config);
    _deferredContext.resolve(_context);
  }
  return _context;
}

wdk.namespace('wdk', ns => {

  /**
   * DOMInitializer for rendering a ViewController, as specified by data-*
   * attributes.
   *
   * Supported data-* attributes are:
   * - data-name: The name of a ViewController
   * - data-props: (optional) JSON that will be parsed and passed to
   *   ViewController as
   *   props. They will be merged with the contextual props `stores` and
   *   `makeDispatchAction`.
   * - data-resolver: (optional) A string representing an object-path (on the
   *   window object) to a function that will resolve the value of data-name
   *   to a ViewController React Component.
   *
   * @example
   * ```
   *  <div
   *    data-controller="wdk.clientAdapter"
   *    data-name="SomeViewControler"
   *    data-props="{\"recordClass\":\"genes\"}"
   *  ></div>
   * ```
   */
  async function clientAdapter($el: JQuery) {
    let el = $el[0];
    let { name, resolver: resolverName } = el.dataset;
    try {
      if (name == null) {
        throw new Error("The attribute `data-name` must be specified.");
      }
      let resolver: ViewControllerResolver =
        resolverName == null ? defaultResolver : get(window, resolverName);
      let [ ViewController, context ] =
        await Promise.all([ resolver(name), _deferredContext ]);

      observeMutations(el, {
        onPropsChanged(props: any) {
          ReactDOM.render(React.createElement(ViewController as any, { ...props, ...context }), el)
        },
        onRemoved() {
          ReactDOM.unmountComponentAtNode(el)
        }
      })
    }
    catch(error) {
      el.innerText = 'There was an error!';
      console.error(error);
    }
  }

  /** Default `ViewControllerResolver`.  */
  async function defaultResolver(id: string) {
    let module = await import(`./client/controllers/${id}`);
    return module.default;
  }

  Object.assign(ns, { clientAdapter });
});

type MutationOptions = {
  onPropsChanged: (props: any) => void;
  onRemoved: () => void;
}

/**
 * Invoke callback `cb` when `el` is removed from the DOM.
 *
 * Because we are attaching a React component to a DOM element that is
 * externally managed, we don't know which ancestor, if any, will be removed
 * from the DOM, causing `el` to be removed. Thus, all ancestors of `el` must be
 * observed for child nodes being removed. This does add some overhead and could
 * potentially be expensive if elements are removed from an ancestor element in
 * rapid succession. The assumption is that this is an unlikely scenario.
 */
function observeMutations(el: Element, options: MutationOptions) {

  handleDataProps(el);

  let propsChangedObserver = new MutationObserver(function(mutations) {
    mutations.forEach(mutation => {
      if (mutation.type === 'attributes' && mutation.attributeName === 'data-props') {
        handleDataProps(mutation.target)
      }
    })
  });

  propsChangedObserver.observe(el, { attributes: true })

  let removeObservers = [...ancestors(el)]
    .map(parent => {
      let observer = new MutationObserver(function(mutations) {
        let elRemoved = Seq.from(mutations)
          .flatMap(mutation => mutation.removedNodes)
          .some(removedNode => removedNode.contains(el));

        if (elRemoved) {
          options.onRemoved();
          removeObservers.forEach(observer => observer.disconnect());
          propsChangedObserver.disconnect();
        }

      });
      observer.observe(parent, { childList: true });
      return observer;
    });

    function handleDataProps (node: Node) {
      const dataProps = node.attributes.getNamedItem('data-props');
      options.onPropsChanged(dataProps && JSON.parse(dataProps.value));
    }
}

function* ancestors(el: Element) {
  while (el.parentElement != null) {
    yield el.parentElement;
    el = el.parentElement;
  }
}
