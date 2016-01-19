import test from 'tape';
import * as TreeUtils from '../../webapp/wdk/js/client/utils/TreeUtils';


test('pruneTreeByLeaves', function(t) {
  let tree = {
    id: 1,
    children: [
      { id: 2, children: [] },
      { id: 3, children: [
        { id: 4, children: [] }
      ]}
    ]
  };

  let prunedTree = TreeUtils.pruneTreeByLeaves(tree, n => n.id === 2);

  t.deepEqual(prunedTree, {
    id: 1,
    children: [
      { id: 2, children: [] }
    ]
  }, 'prunedTree does not have expected shape.');

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

  let prunedTree = TreeUtils.pruneDescendantNodes(tree, n => n.id !== 3 && n.id !== 2);

  t.deepEqual(prunedTree, expectedTree, 'prunedTree does not have expected shape.');

  t.end();

});

test('pruneDescendantNodes can replace pruneTreeByLeaves', function(t) {
  let tree = {
    id: 1,
    children: [
      { id: 2, children: [] },
      { id: 3, children: [
        { id: 4, children: [] }
      ]}
    ]
  };

  let prunedTree = TreeUtils.pruneDescendantNodes(tree, n => n.children.length > 0 || n.id === 2);

  t.deepEqual(prunedTree, {
    id: 1,
    children: [
      { id: 2, children: [] }
    ]
  }, 'prunedTree does not have expected shape.');

  t.end();
});

