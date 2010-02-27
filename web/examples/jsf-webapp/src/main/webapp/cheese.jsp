<%-- The actual Cheese listing page --%>
<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<f:view>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>Sample Cheese List</title>
</head>
<body>
<h1>Cheeses of The World Sample</h1>
<h:panelGrid columns="1" id="MessageBlock">
    <h:messages globalOnly="true"/>
</h:panelGrid>
<h:form>
<h:panelGroup>
<h:panelGroup rendered="#{not empty cheeseBean.cheeses}">
<h:dataTable binding="#{cheeseBean.cheeseList}" id="CheeseTable" var="cheeseRow" value="#{cheeseBean.cheeses}" border="1">
    <h:column id="rowName">
        <f:facet name="header">       
            <h:outputText value="Cheese Name"/>
        </f:facet>
        <h:outputText value="#{cheeseRow.name}"/>        
    </h:column>
    <h:column id="countryName">
        <f:facet name="header">       
            <h:outputText value="Country of Origin"/>
        </f:facet>
        <h:outputText value="#{cheeseRow.country}"/>
    </h:column>
    <h:column id="actionName">
        <f:facet name="header">       
            <h:outputText value="Delete"/>
        </f:facet>
        <h:commandButton id="deleteCheese" immediate="true" action="#{cheeseBean.removeCheese}" value=" X " title="Delete This Cheese"/>
    </h:column>
</h:dataTable>
</h:panelGroup>
<h:outputText style="color: red" rendered="#{empty cheeseBean.cheeses}" value="No Cheeses Found!"></h:outputText>
<h:outputText escape="false" value="<hr/>"/>
<h:outputText escape="false" value="Add a Cheese"/>
<h:panelGrid columns="3">
    <h:outputLabel for="cheeseName" value="Name:"/>
    <h:inputText id="cheeseName" size="40" required="true" value="#{addCheeseBean.name}"/>
    <h:message for="cheeseName" style="color: red"/>
    <h:outputLabel for="cheeseCountry" value="Country:"/>
    <h:inputText id="cheeseCountry" size="40" required="true"  value="#{addCheeseBean.country}"/>
    <h:message for="cheeseCountry" style="color: red"/>
</h:panelGrid>
<h:commandButton action="#{addCheeseBean.addCheese}" value="Add Cheese"></h:commandButton>
</h:panelGroup>
</h:form>
</body>
</f:view>
</html>