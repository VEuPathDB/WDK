import React from 'react';
import Router from 'react-router';

var { Link, RouteHandler } = Router;

export default React.createClass({
  displayName: 'TestPage',
  mixins: [ Router.Navigation ],
  render() {
    // redirect (replacing history) to answer
    // this is just for demo purposes
    // this.replaceWith('answer', {
    //   questionName: 'DatasetQuestions.AllDatasets'
    // });
    // return null;
    return (
      <div>
        <h1>This is a test page. It can have nested routes.</h1>
        Try going to <Link to="/test/answer/OrganismQuestions.GenomeDataTypes">this nested page</Link>
        <RouteHandler/>
      </div>
    );
  }
});
