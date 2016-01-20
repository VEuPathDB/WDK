import test from 'tape';
import * as TreeUtils from '../../webapp/wdk/js/client/utils/TreeUtils';

test('reduce', function(t) {
  let tree = {
    id: 1,
    children: [
      { id: 2, children: [] },
      { id: 3, children: [
        { id: 4, children: [] }
      ]}
    ]
  };

  // without initial value
  let max = TreeUtils.reduce((max, node) => max.id > node.id ? max : node, tree);
  t.equal(max, tree.children[1].children[0]);

  // with initial values
  let sum = TreeUtils.reduce((sum, node) => sum + node.id, 0, tree);
  t.equal(sum, 1 + 2 + 3 + 4);

  t.end();
});

test('filter', function(t) {
  let tree = {
    id: 1,
    children: [
      { id: 2, children: [] },
      { id: 3, children: [
        { id: 4, children: [] }
      ]}
    ]
  };

  let matchingNodes = TreeUtils.filter(node => node.id > 2, tree);
  t.deepEqual(matchingNodes, [ tree.children[1], tree.children[1].children[0] ]);

  t.end();
});

test('find', function(t) {
  let tree = {
    id: 1,
    children: [
      { id: 2, children: [] },
      { id: 3, children: [
        { id: 4, children: [] }
      ]}
    ]
  };

  let matchingNode = TreeUtils.find(node => node.id > 2, tree);
  t.equal(matchingNode, tree.children[1]);

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

