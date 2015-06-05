import Flux from 'flux';

function createDispatcher() {
  return new Flux.Dispatcher;
}

export default {
  createDispatcher
}
