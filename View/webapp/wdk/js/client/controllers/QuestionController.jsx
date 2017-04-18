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

  loadData(state, nextProps) {
    this.dispatchAction(loadQuestion(nextProps.params.question));
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
    return `Search for ${this.props.params.recordClass} by ${state.question.displayName}`;
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
