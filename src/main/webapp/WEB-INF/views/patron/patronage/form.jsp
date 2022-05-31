<%--
 - form.jsp
 -
 - Copyright (C) 2012-2022 Rafael Corchuelo.
 -
 - In keeping with the traditional purpose of furthering education and research, it is
 - the policy of the copyright owner to permit non-commercial use and redistribution of
 - this software. It has been tested carefully, but it is not guaranteed for any particular
 - purposes.  The copyright owner does not offer any warranties or representations, nor do
 - they accept any liabilities with respect to them.
 --%>

 <%@page language="java"%>

 <%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
 <%@taglib prefix="acme" uri="urn:jsptagdir:/WEB-INF/tags"%>

 <acme:form>
 
	<jstl:if test="${published==true}">
		<acme:input-select code="patron.patronage.form.label.status" path="status">
			<acme:input-option code="patron.patronage.form.label.proposed" value="PROPOSED" selected="${ status == 'PROPOSED' }"/>
			<acme:input-option code="patron.patronage.form.label.accepted" value="ACCEPTED" selected="${ status == 'ACCEPTED' }"/>
			<acme:input-option code="patron.patronage.form.label.denied" value="DENIED" selected="${ status == 'DENIED' }"/>
		</acme:input-select>
	</jstl:if>
	
	<jstl:choose>
		<jstl:when test="${command=='update' or command=='show' or command=='publish'}">
	 		<acme:input-textbox code="patron.patronage.form.label.code" path="code" placeholder="ABC-123-A" readonly="true"/>
	 	</jstl:when>
	 	<jstl:otherwise>
	 		<acme:input-textbox code="patron.patronage.form.label.code" path="code" placeholder="ABC-123-A"/>
	 	</jstl:otherwise>
 	</jstl:choose>
 	<acme:input-textarea code="patron.patronage.form.label.legalStuff" path="legalStuff"/>
 	<acme:input-money code="patron.patronage.form.label.budget" path="budget"/>
 	<acme:input-moment code="patron.patronage.form.label.startDate" path="startDate"/>
 	<acme:input-moment code="patron.patronage.form.label.endDate" path="endDate"/>
 	<acme:input-url code="patron.patronage.form.label.moreInfo" path="moreInfo"/><br>
 	
    
   	<jstl:if test="${command == 'create' or command == 'update' or command == 'show' or command=='publish' and published==false}">
   		<acme:input-select code="patron.patronage.form.label.inventor" path="inventorId">
   			<jstl:forEach items="${inventors}" var="inventor">
				<acme:input-option code="${inventor.getUserAccount().getUsername()}" value="${inventor.getId()}" selected="${ inventor.getId() == inventId }"/>
			</jstl:forEach>
		</acme:input-select>
  	</jstl:if>
  	 	
	<jstl:choose>
		<jstl:when test="${acme:anyOf(command, 'show, update, delete, publish') && published == false}"> 
				<acme:submit code="patron.patronage.form.button.update" action="/patron/patronage/update"/>
				<acme:submit code="patron.patronage.form.button.delete" action="/patron/patronage/delete"/>
				<acme:submit code="patron.patronage.form.button.publish" action="/patron/patronage/publish"/>
		</jstl:when>
		
		<jstl:when test="${command=='create'}">
			<acme:submit code="patron.patronage.form.button.create" action="/patron/patronage/create"/>
		</jstl:when>
	</jstl:choose>   
  		
	<jstl:if test="${command == 'show'}">
	
   		<h2 class="text-center">-------------------------------------------------------------------------------------------------------</h2>
	    <acme:input-textbox code="patron.patronage.form.label.inventor.username" path="inventor.userAccount.username" readonly="true"/>
	    <acme:input-textbox code="patron.patronage.form.label.inventor.name" path="inventor.userAccount.identity.name" readonly="true"/>
	    <acme:input-textbox code="patron.patronage.form.label.inventor.surname" path="inventor.userAccount.identity.surname" readonly="true"/>
	    <acme:input-email code="patron.patronage.form.label.inventor.email" path="inventor.userAccount.identity.email" readonly="true"/>
	    
	 	<h2 class="text-center">-------------------------------------------------------------------------------------------------------</h2>
	 	<acme:input-textbox code="patron.patronage.form.label.patron.company" path="patron.company" readonly="true"/>
	 	<acme:input-textbox code="patron.patronage.form.label.patron.statement" path="patron.statement" readonly="true"/>
	 	<acme:input-url code="patron.patronage.form.label.patron.moreInfo" path="patron.moreInfo" readonly="true"/>
	 	<acme:input-textbox code="patron.patronage.form.label.patron.userAccount.username" path="patron.userAccount.username" readonly="true"/>
	 	<acme:input-textbox code="patron.patronage.form.label.patron.userAccount.identity.name" path="patron.userAccount.identity.name" readonly="true"/>
	 	<acme:input-textbox code="patron.patronage.form.label.patron.userAccount.identity.surname" path="patron.userAccount.identity.surname" readonly="true"/>
	 	<acme:input-textbox code="patron.patronage.form.label.patron.userAccount.identity.email" path="patron.userAccount.identity.email" readonly="true"/>

	</jstl:if>
 

 </acme:form>  