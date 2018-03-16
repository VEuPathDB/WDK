import * as React from 'react';
import PropTypes from 'prop-types';
import { DispatchAction } from 'Core/CommonTypes';
import Error from 'Components/PageStatus/Error';

export default class ErrorBoundary extends React.Component {

  static contextTypes = {
    dispatchAction: PropTypes.func.isRequired
  }

  context: {
    dispatchAction: DispatchAction
  }

  state = {
    hasError: false
  }

  props: {
    renderError?: () => React.ReactNode;
    children: React.ReactNode;
  }

  componentDidCatch(error: Error, info: React.ErrorInfo) {
    this.setState({ hasError: true });
    this.context.dispatchAction((dispatch, { wdkService }) => {
      wdkService.submitError({
        name: error.name,
        message: error.message,
        stack: error.stack,
        componentStack: info.componentStack
      })
    });
  }

  render() {
    return this.state.hasError
      ? this.props.renderError ? this.props.renderError() : ( <Error/>)
      : this.props.children;
  }

}