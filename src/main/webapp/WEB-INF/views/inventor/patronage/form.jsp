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

 	<acme:input-textbox code="inventor.patronage.form.label.code" path="code" readonly="true"/>
 	<acme:input-textarea code="inventor.patronage.form.label.legalStuff" path="legalStuff" readonly="true"/>
 	<acme:input-money code="inventor.patronage.form.label.budget" path="budget" readonly="true"/>
 	<acme:input-moment code="inventor.patronage.form.label.startDate" path="startDate" readonly="true"/>
 	<acme:input-moment code="inventor.patronage.form.label.endDate" path="endDate" readonly="true"/>
 	<acme:input-url code="inventor.patronage.form.label.moreInfo" path="moreInfo" readonly="true"/>
 	
 	<jstl:if test="${status != 'PROPOSED'}">
		<acme:input-textbox code="inventor.patronage.form.label.status" path="status" readonly="true"/><br>
	</jstl:if>
	
	<jstl:if test="${status == 'PROPOSED'}">
		<acme:input-select path="status" code="inventor.patronage.form.label.new-status">
			<acme:input-option code="PROPOSED" value="PROPOSED" selected="true"/>
			<acme:input-option code="ACCEPTED" value="ACCEPTED"/>
			<acme:input-option code="DENIED" value="DENIED"/>
		</acme:input-select><br>		
	</jstl:if>
			
	<acme:submit test="${acme:anyOf(command, 'show, update') && status == 'PROPOSED'}" code="inventor.patronage.form.button.update" action="/inventor/patronage/update"/>
	
 	<h2 class="text-center">-------------------------------------------------------------------------------------------------------</h2>
 	<acme:input-textbox code="inventor.patronage.form.label.patron.company" path="patron.company" readonly="true"/>
 	<acme:input-textbox code="inventor.patronage.form.label.patron.statement" path="patron.statement" readonly="true"/>
 	<acme:input-url code="inventor.patronage.form.label.patron.moreInfo" path="patron.moreInfo" readonly="true"/>
 	<acme:input-textbox code="inventor.patronage.form.label.patron.userAccount.username" path="patron.userAccount.username" readonly="true"/>
 	<acme:input-textbox code="inventor.patronage.form.label.patron.userAccount.identity.name" path="patron.userAccount.identity.name" readonly="true"/>
 	<acme:input-textbox code="inventor.patronage.form.label.patron.userAccount.identity.surname" path="patron.userAccount.identity.surname" readonly="true"/>
 	<acme:input-textbox code="inventor.patronage.form.label.patron.userAccount.identity.email" path="patron.userAccount.identity.email" readonly="true"/>

 </acme:form>  
 
 <acme:button code="inventor.patronage.form.button.create.patronage-report" action="/inventor/patronage-report/create?masterId=${id}"/>	

