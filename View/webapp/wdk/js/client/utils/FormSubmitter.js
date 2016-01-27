
export function submitAsForm(conf) {

  // get supported values from conf
  let method = conf.method || 'post';
  let target = conf.target || '_self';
  let action = (conf.action == "" ? undefined : conf.action);
  let inputs = conf.inputs || {};

  // build the form
  let form = document.createElement("form");
  form.setAttribute("method", method);
  form.setAttribute("target", target);
  if (action !== undefined) {
    form.setAttribute("action", action);
  }

  // add input values
  Object.keys(inputs).forEach(function(name) {
    let input = document.createElement("input");
    input.setAttribute("name", name);
    input.setAttribute("value", inputs[name]);
    form.appendChild(input);
  });

  // submit the form
  form.submit();
}
