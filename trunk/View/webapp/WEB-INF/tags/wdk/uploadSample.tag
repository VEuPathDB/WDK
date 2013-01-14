<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0" xmlns:jsp="http://java.sun.com/JSP/Page">
  <jsp:output doctype-root-element="html"
    doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
    doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>
  <html xmlns="http://www.w3.org/1999/xhtml">
    <head>
      <title>File Upload Form</title>
      <script type="text/javascript" src="http://code.jquery.com/jquery-latest.min.js"><jsp:text/></script>
      <script type="text/javascript">
        var addFormField = function() {
          var fieldCount = 1;
          return function() {
            fieldCount++;
            var html = 'File ' + fieldCount + ': <input type="file" name="file' + fieldCount + '"/><br/>';
            $('#uploadFields').append(html);
          }
        }();
      </script>
    </head>
    <body>
      <form method="post" action="uploadSample.do" enctype="multipart/form-data">
        <h3>Upload a file!</h3>
        <div id="uploadFields">
          File 1: <input type="file" name="file1"/><br/>
        </div>
        <a href="javascript:addFormField()">Add another file...</a><br/>
        <input type="submit" value="Submit"/>
      </form>
    </body>
  </html>
</jsp:root>
