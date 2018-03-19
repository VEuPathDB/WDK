import * as React from 'react';
import PropTypes from 'prop-types';
import { DispatchAction } from 'Core/CommonTypes';
import Error from 'Components/PageStatus/Error';
import { emptyAction } from 'Utils/ActionCreatorUtils';

export default class ErrorBoundary extends React.Component {

  static contextTypes = {
    dispatchAction: PropTypes.func
  }

  context: {
    dispatchAction?: DispatchAction
  }

  state = {
    hasError: false
  }

  props: {
    renderError?: () => React.ReactNode;
    children: React.ReactNode;
    dispatchAction?: DispatchAction;
  }

  componentDidCatch(error: Error, info: React.ErrorInfo) {
    this.setState({ hasError: true });

    const dispatch = this.props.dispatchAction || this.context.dispatchAction;
    if (dispatch == null) {
      console.warn('`dispatchAction` function not found. Unable to log render error to server.');
    }
    else {
      dispatch(({ wdkService }) => {
        return wdkService.submitError({
          name: error.name,
          message: error.message,
          stack: error.stack,
          componentStack: info.componentStack
        }).then(() => emptyAction)
      });
    }
  }

  render() {
    return this.state.hasError
      ? this.props.renderError ? this.props.renderError() : ( <Error/>)
      : this.props.children;
  }

}