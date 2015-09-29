import test from 'tape';
import { selector } from '../../webapp/wdk/js/client/lib/utils';

let state = {
  name: 'Test',
  suites: [
    { name: 'a' },
    { name: 'b' },
    { name: 'c' },
    { name: 'd' },
    { name: 'e' },
    { name: 'f' },
    { name: 'g' },
    { name: 'h' },
    { name: 'i' },
    { name: 'j' },
    { name: 'k' },
    { name: 'l' },
    { name: 'm' },
    { name: 'n' },
    { name: 'o' },
    { name: 'p' },
    { name: 'q' },
    { name: 'r' },
    { name: 's' },
    { name: 't' },
    { name: 'u' },
    { name: 'v' },
    { name: 'w' },
    { name: 'x' },
    { name: 'y' },
    { name: 'z' }
  ]
};

// transforms
let map = xf => coll => coll.map(xf);
let filter = test => coll => coll.filter(test)

test('basic functionality', function(t) {
  t.plan(2);

  let selectSuiteNames = selector(['suites', map(suite => suite.name)]);
  let selectedNames = selectSuiteNames(state);
  let mappedNames = state.suites.map(s => s.name);
  t.deepEqual(selectedNames, mappedNames, 'both should be an array of names');

  let isLowerSuite = suite => suite.name.charCodeAt(0) < 110;
  let selectLowerSuites = selector([ 'suites', filter(isLowerSuite) ]);
  let lowerSuites = selectLowerSuites(state);
  t.deepEqual(
    lowerSuites,
    [
      { name: 'a' },
      { name: 'b' },
      { name: 'c' },
      { name: 'd' },
      { name: 'e' },
      { name: 'f' },
      { name: 'g' },
      { name: 'h' },
      { name: 'i' },
      { name: 'j' },
      { name: 'k' },
      { name: 'l' },
      { name: 'm' }
    ]
  );

});

test('composable', function(t) {
  t.plan(1);

  // create small selectors
  let selectNames = selector(map(o => o.name));
  let selectSuites = selector('suites');

  // compose a larger one from the smaller one
  let composedSelector = selector([ selectSuites, selectNames ]);

  // call composedSelector
  let names = composedSelector(state);

  t.deepEqual(
    names,
    [ 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
    'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' ],
    'both should be an array of names'
  );
});

test('nested object spec', function(t) {
  t.plan(1);

  let selectStuff = selector({
    a: {
      b: [ 'suites', 0 ]
    }
  });

  t.deepEqual(
    selectStuff(state),
    { a: { b: state.suites[0] } }
  );

});
