import { flow, throttle } from 'lodash';
import { preorder } from './TreeUtils';
import { find } from './IterableUtils';

/**
 * Check if a targetNode has an ancestor node that satisfies a predicate function.
 */
export function containsAncestorNode(
  targetNode: Node | null,
  predicate: (node: Node) => boolean,
  rootNode: Node = document
): boolean {
  if (targetNode == null || targetNode == rootNode) return false;
  return (
    predicate(targetNode) ||
    containsAncestorNode(targetNode.parentNode, predicate, rootNode)
  );
}


/**
 * Track scroll position of `element` and if height or width of `element`
 * changes, scroll to tracked position.
 */
export function addScrollAnchor(element: Element) {
  let anchorNode = findAnchorNode(element);

  // return composite cancellation function
  return flow(
    monitorRectChange(element, ['height', 'width'], () => { anchorNode && anchorNode.scrollIntoView() }),
    monitorScroll(() => anchorNode = findAnchorNode(element))
  );
}

/**
 * When properties of the client rectangle of `element` change, invoke callback.
 */
function monitorRectChange(element: Element, trackedProps: Array<keyof ClientRect>, callback: () => void) {
  let rect = element.getBoundingClientRect();
  let rafId: number;

  checkWidth();

  return function cancel() {
    cancelAnimationFrame(rafId);
  }

  function checkWidth() {
    rafId = requestAnimationFrame(function() {
      checkWidth();
      let newRect = element.getBoundingClientRect();
      if (trackedProps.some(prop => rect[prop] !== newRect[prop])) {
        callback();
      }
      rect = newRect;
    });
  }
}

/**
 * Invoke callback when window scroll event is fired.
 */
function monitorScroll(callback: () => void, throttleMs: number = 100) {
  const scrollHandler = throttle(callback, throttleMs);
  window.addEventListener('scroll', scrollHandler);
  return function cancel() {
    window.removeEventListener('scroll', scrollHandler);
    scrollHandler.cancel();
  }
}

/**
 * Find first descendent of `element` that is within viewport.
 */
function findAnchorNode(element: Element) {
  return find(
    (node: Element) => node.getBoundingClientRect().top > 0,
    preorder(element, getElementChildren)
  );
}

function getElementChildren(el: Element) {
  return Array.from(el.children);
}
