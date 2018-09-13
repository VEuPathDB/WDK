/// <reference path="../../../../../WDKClient/Client/src/index.d.ts" />

import { get } from 'lodash';
import React from 'react';
import ReactDOM from 'react-dom';
import { Router } from 'react-router';
import { Provider } from 'react-redux';

import {
  Controllers as WdkControllers,
  initialize as initializeWdk,
  IterableUtils,
  PromiseUtils
} from 'wdk-client';

export * from 'wdk-client';

const { Seq } = IterableUtils;

type ViewControllerResolver = (id: string) => Promise<WdkControllers.AbstractViewController> | WdkControllers.AbstractViewController;

declare const wdk: {
  namespace(nsString: string, nsFactory: (ns: object) => any): void;
}

declare const wdkConfig: {
  readonly wdkServiceUrl: string;
}

// Only allow initialize to be called once
let _context: any;
let _deferredContext = PromiseUtils.createDeferred();

export function initialize(config: any) {
  if (_context != null) {
    console.log('`initialize` may only be called once on legacy WDK pages.');
  }
  else {
    _context = initializeWdk(config);
    _deferredContext.resolve(_context);
  }
  return _context;
}

// FIXME Don't use `any`
export function getContext(): Promise<any> {
  return _deferredContext.asPromise();
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
        await Promise.all([ resolver(name), getContext() ]);
      let viewControllerRef = React.createRef();

      $el.data('viewControllerRef', viewControllerRef);

      observeMutations(el, {
        onPropsChanged(props: any) {
          ReactDOM.render(
            React.createElement(
              Provider,
              { store: context.store }, 
              React.createElement(
                WdkControllers.ErrorBoundary,
                { renderError: () => 'There was an error!' },
                React.createElement(
                  Router,
                  { history: context.history },
                  React.createElement(ViewController as any, { ...props, ...context, ref: viewControllerRef })
                )
              )
            ), el)
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
  async function defaultResolver (id: string) {
    if (!(id in WdkControllers)) {
      throw new Error(`Cannot find export '${id}' from module 'Core/Controllers'`);
    }
    return (<any>WdkControllers)[id] as any;
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
        handleDataProps(<Element>mutation.target)
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

    function handleDataProps (node: Element) {
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
