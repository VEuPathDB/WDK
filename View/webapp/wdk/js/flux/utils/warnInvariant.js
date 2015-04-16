/**
 * Log a warning message to the console if condition is false, along with stack
 * trace.
 */
export default function warnInvariant(condition, message, ...vars) {
  try {
    if (!condition) throw Error('invariant');
  }
  catch (e) {
    console.warn(message, ...vars, e);
  }
}
