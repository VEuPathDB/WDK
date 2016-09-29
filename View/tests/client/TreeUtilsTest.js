import test from 'tape';
import * as TreeUtils from '../../webapp/wdk/js/client/utils/TreeUtils';

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

  t.end();

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

  t.end();

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
  t.end();
});

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

  t.ok(compactedTree.id === 4, 'compactedTree does not have expected root.');

  t.end();
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

  t.end();
});

