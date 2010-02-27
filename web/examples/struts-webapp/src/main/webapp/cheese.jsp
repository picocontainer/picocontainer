<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ page language="java" %>

<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>  
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>  
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>  

<html:html>
<head>
</head>
<body>
<h1>Cheese!</h1>
<br>

<h2>Cheeses of the World:</h2>
<table style="text-align: left; width: 50%;"
       border="1"
       cellspacing="2"
       cellpadding="2">
    <tr>
      <td><b>Name</b></td>
      <td><b>Country</b></td>
    </tr>
    <logic:present name="cheesesOfTheWord">
      <logic:iterate name="cheesesOfTheWord" id="cheese">
        <tr>
          <td>
            <bean:write name="cheese" property="name"/>
          </td>
          <td>
            <bean:write name="cheese" property="country"/>
          </td>
        </tr>
      </logic:iterate>
    </logic:present>
</table>

<br><br>
<h2>Enter new cheese (or change country of existing cheese):</h2>
<html:form action="/cheese">
  <table style="text-align: left; width: 50%;"
         border="1"
         cellspacing="2"
         cellpadding="2">
      <tr>
        <td>
          <b>Name:</b>
        </td>
        <td>
          <html:text property="name"/>
        </td>
      </tr>
      <tr>
        <td>
          <b>Country:</b>
        </td>
        <td>
          <html:text property="country"/>
        </td>
      </tr>
      <tr>
   </table>
   <br>
   <html:submit>Cheese Me!</html:submit>
</html:form>

</body>
</html:html>