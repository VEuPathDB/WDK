<!doctype html>
<head>
  <link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/jqueryui/1.11.2/jquery-ui.css"/>
  <link rel="stylesheet" href="css/wdk3.css"/>
  <link rel="stylesheet" href="css/fixed-data-table.css"/>
  <script src="//cdnjs.cloudflare.com/ajax/libs/jquery/2.1.3/jquery.min.js"></script>
  <script src="//cdnjs.cloudflare.com/ajax/libs/jqueryui/1.11.2/jquery-ui.js"></script>
  <script src="lib/spin.min.js"></script>
  <script src="js/wdk-3.0.js"></script>
</head>
<body>
  <script>
    var app = wdk.createApplication({
      serviceUrl: '../service'
    });
    app.run();
  </script>
</body>
