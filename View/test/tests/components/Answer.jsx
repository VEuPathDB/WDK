import assert from 'assert';

import React from 'react';
import Answer from 'wdk/flux/components/Answer';

describe('Answer component', () => {

  var answerResource = {
  meta: {
    count: 10,
    class: 'MyRecordClass',
    attributes: [ {
      name: 'name',
      displayName: 'Name',
      sortable: true,
      removable: true,
      category: 'general',
      type: 'string'
    } ]
  },
  records: [
    { id: '1', attributes: [ { name: 'name', value: 'Dave' } ] },
    { id: '2', attributes: [ { name: 'name', value: 'Ryan' } ] },
    { id: '3', attributes: [ { name: 'name', value: 'Jerric' } ] },
    { id: '4', attributes: [ { name: 'name', value: 'Steve' } ] },
    { id: '5', attributes: [ { name: 'name', value: 'Cristina' } ] },
    { id: '6', attributes: [ { name: 'name', value: 'Susanne' } ] },
    { id: '7', attributes: [ { name: 'name', value: 'Mark' } ] },
    { id: '8', attributes: [ { name: 'name', value: 'Matt' } ] },
    { id: '9', attributes: [ { name: 'name', value: 'Haiming' } ] },
    { id: '10', attributes: [ { name: 'name', value: 'Jessie' } ] }
  ]
};


  it('should render a table with questions', () => {
    var html = React.renderToStaticMarkup(<Answer questionName="MyQuestion" answer={answerResource} />);
    assert(/<table>.*<\/table>/.test(html));
    console.log(html);
  });

  it('should render one row per record', () => {
    var html = React.renderToStaticMarkup(<Answer questionName="MyQuestion" answer={answerResource} />);
    // 11 <tr> (1 in header, 10 in body)
    var matches = html.match(/<tr>/g);
    assert(matches.length === 11, 'Got ' + matches);
  })
})