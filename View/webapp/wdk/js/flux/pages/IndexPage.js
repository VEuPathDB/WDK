import React from 'react';
import Router from 'react-router';

var { Link } = Router;

export default React.createClass({
  displayName: 'IndexPage',
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
        <h1>This is the index page. Try clicking a link above.</h1>
      </div>
    );
  }
});
