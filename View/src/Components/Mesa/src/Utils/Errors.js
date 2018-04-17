/*    Error Handlers   */
export const fail = (fn, message, Err = Error) => {
  console.error(`<${fn}>: ${message}`);
  return undefined;
}

export const badType = (fn, parameter, expected, actual) => {
  const message = `parameter "${parameter}"  is not of type ${expected} (got ${actual})`;
  return fail(fn, message, TypeError);
};

export const missingFromState = (fn, missing, obj = {}) => {
  const present = Object.keys(obj).join(', ');
  const message = `state branch "${missing}" not found in state. Found sibling keys: [${present}]`;
  return fail(fn, message, ReferenceError);
};
