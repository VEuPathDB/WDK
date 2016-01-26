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

  let prunedTree = TreeUtils.pruneDescendantNodes(n => n.children.length > 0 || n.id === 2, tree);

  t.deepEqual(prunedTree, {
    id: 1,
    children: [
      { id: 2, children: [] }
    ]
  }, 'prunedTree does not have expected shape.');

  t.end();
});

