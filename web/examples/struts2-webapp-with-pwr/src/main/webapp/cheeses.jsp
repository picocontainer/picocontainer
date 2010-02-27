<%@ taglib prefix="s" uri="/struts-tags" %>
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
                        <input value="<s:property value="cheese.name"/>" name="cheese.name">
                    </td>
                    <td>
                        <input value="<s:property value="cheese.country"/>" name="cheese.country">
                    </td>
                    <td>
                        <input type="submit" value="Store"/>
                    </td>
                 </tr>
             </form>
             <s:iterator value="cheeses">
                <tr>
                    <td style="vertical-align: top;">
                        <s:property value="name"/>
                    </td>
                    <td style="vertical-align: top;">
                        <s:property value="country"/>
                    </td>
                    <td style="vertical-align: top;"><br>
                        <a href="removecheese.action?cheese.name=<s:property value="name"/>">Dispose</a>
                    </td>
                </tr>
            </s:iterator>
            <s:property value="brand"/>
        </tbody>
    </table>
    <br>
    <form name="countForm">
    <p>Total Cheese Count (AJAX call to server): <input name="count"></input></p>
    </form>

    <!-- Ajax get follows -->
    <script type="text/javascript">
        function createXMLHttpRequest() {
            try {
                return new XMLHttpRequest();
            } catch(e) {
            }
            try {
                return new ActiveXObject("Msxml2.XMLHTTP");
            } catch (e) {
            }
            try {
                return new ActiveXObject("Microsoft.XMLHTTP");
            } catch (e) {
            }
            alert("XMLHttpRequest not supported");
            return null;
        }

        var xhReq = createXMLHttpRequest();
        xhReq.open("get",
                "pwr/org/picocontainer/web/sample/struts2/pwr/CheeseInventory/ajaxCheeseCount", true);
        xhReq.onreadystatechange = function() {
            if (xhReq.readyState != 4) {
                return;
            }
            document.countForm.count.value = xhReq.responseText;
        };
        xhReq.send(null);

    </script>
</body>
</html>
