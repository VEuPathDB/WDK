<%@ attribute name="groupName"
              required="true"
              type="java.lang.String"
%>

<a class="small" href="javascript:void(0)" onclick="chooseAll(1, $(this).parents('form').get(0), '${groupName}' )">select all</a> | <a class="small" href="javascript:void(0)" onclick="chooseAll(0, $(this).parents('form').get(0), '${groupName}' )">clear all</a>
