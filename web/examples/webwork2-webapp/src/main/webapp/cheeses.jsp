<%@ taglib uri="webwork" prefix="ww" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
    <head>
        <title>Cheese!</title>
    </head>
<body>
    <h1>Cheese!</h1>
    <br>
        <table style="text-align: left; width: 50%;" border="1" cellspacing="2" cellpadding="2">
        <thead>
            <tr>
                <td>Name</td>
                <td>Country</td>
            </tr>
        </thead>
        <tbody>
            <form action="addcheese.action">
                <tr>
                    <td>
                        <input value="<ww:property value="cheese.name"/>" name="cheese.name">
                    </td>
                    <td>
                        <input value="<ww:property value="cheese.country"/>" name="cheese.country">
                     </td>
                     <td>
                        <input type="submit" value="Store"/>
                     </td>
                 </tr>
             </form>
             <ww:iterator value="cheeses">
                <tr>
                    <td style="vertical-align: top;">
                        <ww:property value="name"/>
                    </td>
                    <td style="vertical-align: top;">
                        <ww:property value="country"/>
                    </td>
                    <td style="vertical-align: top;"><br>
                        <a href="removecheese.action?cheese.name=<ww:property value="name"/>">Dispose</a>
                    </td>
                </tr>
            </ww:iterator>
        </tbody>
    </table>
</body>
</html>
