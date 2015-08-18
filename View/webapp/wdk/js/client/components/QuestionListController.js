import React from 'react';
import { Link } from 'react-router';
import wrappable from '../utils/wrappable';
import ContextMixin from '../utils/contextMixin';

let QuestionListController = React.createClass({

  componentWillMount() {
    let { store } = this.props;
    this.selectState(store.getState());
    this.storeSubscription = store.subscribe(this.selectState);
  },

  componentWillUnmount() {
    this.storeSubscription.dispose();
  },

  selectState(state) {
    this.setState({ questions: state.questions });
  },

  render() {
    if (!this.state) { return null; }
    let { questions } = this.state;

    return (
      <div>
        <ol>
          {questions.map(question => (
            <li key={question.name}>
              {question.displayName + ' - '}
              <Link to="answer" params={{ questionName: question.name }}>answer page</Link>
            </li>
          ))}
        </ol>
      </div>
    );
  }

});

export default wrappable(QuestionListController);
