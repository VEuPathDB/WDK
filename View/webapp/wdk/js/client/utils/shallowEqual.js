export default function shallowEqual(obj1, obj2) {
  if (obj1 === obj2) return true;
  if (obj1 === null) return false;
  if (obj2 === null) return false;

  for (let key in obj1) {
    if (obj1.hasOwnProperty(key) && !obj2.hasOwnProperty(key) || obj1[key] !== obj2[key]) {
      return false;
    }
  }

  for (let key in obj2) {
    if (obj1.hasOwnProperty(key) && !obj2.hasOwnProperty(key)) {
      return false;
    }
  }

  return true;
}
