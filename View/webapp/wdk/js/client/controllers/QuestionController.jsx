import { wrappable } from '../utils/componentUtils';
import WdkViewController from './WdkViewController';
import { loadQuestion } from '../actioncreators/QuestionActionCreators';

export default wrappable(class QuestionController extends WdkViewController {

  getActionCreators() {
    return {
      loadQuestion
    };
  }

  getStoreName() {
    return "QuestionStore";
  }

  loadData(actionCreators, state, nextProps) {
    actionCreators.loadQuestion(nextProps.match.params.question);
  }

  isRenderDataLoaded(state) {
    return state.questionStatus === 'complete';
  }

  isRenderDataLoadError(state) {
    return state.questionStatus === 'error';
  }

  isRenderDataNotFound(state) {
    return state.questionStatus === 'not-found';
  }

  getTitle(state) {
    return `Search for ${this.props.match.params.recordClass} by ${state.question.displayName}`;
  }

  renderView() {
    const { question } = this.state;

    return (
      <div>
        <h1>{this.getTitle(this.state)}</h1>
        <pre>{JSON.stringify(question, null, 4)}</pre>
      </div>
    );
  }

})
