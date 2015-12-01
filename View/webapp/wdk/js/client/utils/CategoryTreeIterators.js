/**
 * Generator functions to create iterators for category trees.
 *
 * Both expect an array of categories.
 */

export function* preorder(categories) {
  yield* iterate(categories, preorderCategory);
}

export function* postorder(categories) {
  yield* iterate(categories, postorderCategory);
}

function* iterate(categories, categoryIterator) {
  if (categories == null) return;
  for (let category of categories) {
    yield* categoryIterator(category);
  }
}

function* preorderCategory(category) {
  yield category;
  yield* preorder(category.subCategories);
}

function* postorderCategory(category) {
  yield* postorder(category.subCategories);
  yield category;
}
