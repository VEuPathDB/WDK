import React from 'react';
import Router from 'react-router';
import QuestionListStore from '../stores/QuestionListStore';
import { loadQuestions } from '../actions/QuestionListPageActions';
import StoreMixin from '../mixins/StoreMixin';
var { Link } = Router;

export default React.createClass({

  displayName: 'QuestionListPage',

  mixins: [ StoreMixin(QuestionListStore) ],

  render() {
    var { questions, isLoading, error } = this.state;

    if (error) {
      return (
        <div>There was an error: {error}</div>
      );
    } else {
      return (
        <div>
          <ol>
            {_.map(questions, question => (
              <li>
                {question} -
                <Link to="answer" params={{ questionName: question }}>answer page</Link>
              </li>
            ))}
          </ol>
        </div>
      );
    }
  },

  statics: {
    willTransitionTo() {
      loadQuestions();
    }
  }

});
