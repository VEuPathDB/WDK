import _test from 'ava';
import * as TreeUtils from '../../webapp/wdk/js/client/utils/TreeUtils';

const test = (label, testFn) => {
  _test('TreeUtils#' + label, testFn);
}

test('preorderSeq', function(t) {
  let tree = {
    id: 1,
    children: [
      { id: 2, children: [] },
      { id: 3, children: [
        { id: 4, children: [] }
      ]}
    ]
  };

  let ids = TreeUtils.preorderSeq(tree)
    .map(n => n.id)
    .toArray();

  t.deepEqual(ids, [ 1, 2, 3, 4]);

});

test('postorderSeq', function(t) {
  let tree = {
    id: 1,
    children: [
      { id: 2, children: [] },
      { id: 3, children: [
        { id: 4, children: [] }
      ]}
    ]
  };

  let ids = TreeUtils.postorderSeq(tree)
    .map(n => n.id)
    .toArray();

  t.deepEqual(ids, [ 2, 4, 3, 1 ]);

});

test('mapStructure', function(t) {
  let tree = {
    id: 1,
    children: [
      { id: 2, children: [] },
      { id: 3, children: [
        { id: 4, children: [] }
      ]}
    ]
  };
  let expectedStructure = {
    number: 1,
    subNumbers: [
      { number: 2, subNumbers: [] },
      { number: 3, subNumbers: [
        { number: 4, subNumbers: [] }
      ]}
    ]
  };
  let mappedStructure = TreeUtils.mapStructure(
    (node, mappedChildren) => {
      return {
        number: node.id,
        subNumbers: mappedChildren
      };
    },
    node => node.children, tree);
  t.deepEqual(mappedStructure, expectedStructure, 'mappedStructure does not match expectedStructure');
});

test('foldStructure', t => {
  /*
   *          (id: 1)
   *         /       \
   *     (id: 2)   (id: 3)
   *                   \
   *                 (id: 4)
   */
  let tree = {
    id: 1,
    children: [
      { id: 2, children: [] },
      { id: 3, children: [
        { id: 4, children: [
          { id: 5, children: [] },
          { id: 6, children: [] }
        ] }
      ]}
    ]
  };
  let expected = [ 1, 3, 4, 6 ]
  let fold = (path, node) => node.id === 6 || path.length ? [ node, ...path ] :  path;
  let result = TreeUtils.foldStructure(fold, [], tree).map(node => node.id)
  t.deepEqual(result, expected);
})

test('compactRootNodes', function(t) {
  let tree = {
    id: 1,
    children: [
      { id: 2,
        children: [
          {
            id: 3,
            children: [
              {
                id: 4,
                children: []
              }
            ]
          }
        ]
      }
    ]
  };

  let compactedTree = TreeUtils.compactRootNodes(tree);

  t.is(compactedTree.id, 4, 'compactedTree does not have expected root.');

});

test('pruneDescendantNodes', function(t) {
  let tree = {
    id: 1,
    children: [
      { id: 2,
        children: [
          {
            id: 3,
            children: [
              {
                id: 4,
                children: []
              }
            ]
          }
        ]
      }
    ]
  };

  let expectedTree = {
    id: 1,
    children: [
      {
        id: 4,
        children: [ ]
      }
    ]
  };

  let prunedTree = TreeUtils.pruneDescendantNodes(n => n.id !== 3 && n.id !== 2, tree);

  t.deepEqual(prunedTree, expectedTree, 'prunedTree does not have expected shape.');


  // Generate a tree where leaves have certain properties

  let tree2 = {
    id: 1,
    children: [
      { id: 2, children: [] },
      { id: 3, children: [
        { id: 4, children: [] }
      ]}
    ]
  };

  let prunedTree2 = TreeUtils.pruneDescendantNodes(n => n.children.length > 0 || n.id === 2, tree2);

  t.deepEqual(prunedTree2, {
    id: 1,
    children: [
      { id: 2, children: [] }
    ]
  }, 'prunedTree2 does not have expected shape.');

});

