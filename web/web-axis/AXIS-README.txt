Usage:

1. Register the servlet container listener in web.xml:

<listener>
  <listener-class>org.nanocontainer.nanowar.ServletContainerListener</listener-class>
</listener>

2. Register a container composer in web.xml to handle your component registrations:

<context-param>
  <param-name>assembler</param-name>
  <param-value>com.company.WebContainerComposer</param-value>
</context-param>

3. Register your service in your Axis WSDD using the custom provider:

<service name="MyService" provider="Handler">
  <parameter name="handlerClass" value="org.nanocontainer.nanowar.axis.NanoRPCProvider"/>
  <parameter name="className" value="com.company.MyService"/>
  <parameter name="allowedMethods" value="*"/>
</service>

or if you want to use message-style encoding use org.nanocontainer.nanowar.axis.NanoMsgProvider.

4. Register the servlet:

<servlet>
  <servlet-name>AxisServlet</servlet-name>
  <display-name>Apache-Axis Servlet</display-name>
  <servlet-class>org.nanocontainer.nanowar.axis.NanoAxisServlet</servlet-class>
</servlet>
 