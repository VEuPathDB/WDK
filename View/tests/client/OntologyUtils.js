import test from 'tape';
import * as OntologyUtils from '../../webapp/wdk/js/client/utils/OntologyUtils';


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

  let prunedTree = OntologyUtils.pruneTreeByLeaves(tree, n => n.id === 2);

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

  let compactedTree = OntologyUtils.compactRootNodes(tree);

  t.ok(compactedTree.id === 3, 'compactedTree does not have expected root.');

  t.end();
});
