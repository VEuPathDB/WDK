/**
 * Standard alerts. These can be thought of as platform-level utilities
 * to be used with action creators, etc, and should not be used in UI
 * components. The fact that they use the DOM is an implementation detail.
 */

import $ from 'jquery';

/**
 * @return {Promise<void>}
 */
export function alert(title, message) {
  return dialog(title, message, [
    { text: 'OK', focus: true }
  ]);
}

/**
 * @return {Promise<boolean>}
 */
export function confirm(title, message) {
  return dialog(title, message, [
    { text: 'Cancel', value: false },
    { text: 'OK', value: true, focus: true }
  ], false);
}

/**
 * @param {string} title
 * @param {string} message
 * @param {Array<ButtonDescriptor>} buttons
 * @param {any} escapeValue The value to use when dialog is closed via pressing the escape key
 * @returns {Promise<any>}
 */
export function dialog(title, message, buttons, escapeValue) {
  return new Promise(function(resolve, reject) {
    let $node = $('<div><p>' + message + '</p><div class="wdk-AlertButtons"></div></div>');
    let $buttons = buttons.map(button => {
      return $('<button>' + button.text + '</button>')
      .attr('autofocus', !!button.focus)
      .click(() => {
        $node.dialog('close');
        resolve(button.value);
      });
    });
    $node.find('.wdk-AlertButtons').append($buttons);
    try {
      $node.dialog({
        title: title,
        modal: true,
        position: [ 'center', window.innerHeight * .3 ],
        resizable: false,
        dialogClass: 'wdk-Alert',
        minWidth: 350,
        open() {
          $node.parent().find('[autofocus]').focus();
        },
        close(event) {
          if (event.key === 'Escape') {
            resolve(escapeValue);
          }
          $node.dialog('destroy').remove();
        }
      });
    }
    catch(err) {
      reject(err);
    }
  });
}
