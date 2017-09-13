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
