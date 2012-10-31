<%@ page import="frontlinesms2.ConnectionStatus" %>
<div id="body-content-head">
	<div class="content">
		<h1><g:message code="connection.header"/></h1>
		<ul class="buttons">
			<li>
				<g:remoteLink class="btn" name="addConnection" controller='connection' action="wizard" onLoading="showThinking()" onSuccess="hideThinking(); mediumPopup.launchMediumWizard(i18n('connection.add'), data, i18n('wizard.create'), 675, 500, false)">
					<g:message code="connection.add" />
				</g:remoteLink>
			</li>
		</ul>
	</div>
</div>
<div id="body-content" class="connections">
	<g:if test="${fconnectionInstanceTotal==0}">
		<p class="no-content"><g:message code="connection.list.none"/></p>
	</g:if>
	<g:else>
		<ul>
			<g:each in="${connectionInstanceList}" status="i" var="c">
				<li class="connection ${c == connectionInstance ? 'selected' : ''}">
					<g:link action="show" id="${c.id}">
						<div class="connection-header">
							<h2>'${c.name}'</h2>
							<p class="connection-type">(<g:message code="${c.getClass().simpleName.toLowerCase()}.label"/>)</p>
							<g:if test="${c instanceof frontlinesms2.SmssyncFconnection}">
								<p class="smssync-url">${"http://you-ip-address"+createLink(uri: '/')+"api/1/smssync/"+c.id+"/"+(c.secret?:'')}</p>
							</g:if>
							<p class="connection-status"><g:message code="${c.status.i18n}"/></p>
						</div>
					</g:link>
					
					<g:if test="${c == connectionInstance}">
						<div class="controls">
							<g:if test="${c.status == ConnectionStatus.NOT_CONNECTED}">
								<g:link controller="connection" action="createRoute" class="btn route" id="${c.id}"><g:message code="connection.route.create"/></g:link>
								<g:remoteLink controller="connection" action="wizard" class="btn route" id="${c.id}" onSuccess="mediumPopup.launchMediumWizard(i18n('connection.edit'), data, i18n('action.done'), 675, 500, false)">
										<g:message code="connection.edit"/>
									</g:remoteLink>
								<g:link controller="connection" action="delete" class="btn route" id="${c.id}">
									<g:message code="connection.delete"/>
								</g:link>
							</g:if>
							<g:elseif test="${c.status == ConnectionStatus.CONNECTED}">
								<g:remoteLink controller="connection" action="createTest" class="btn test" id="${c.id}" onSuccess="launchSmallPopup(i18n('smallpopup.test.message.title'), data, i18n('action.send'))">
										<g:message code="connection.send.test.message"/>
								</g:remoteLink>
								<g:link controller="connection" action="destroyRoute" class="btn" id="${c.id}">
									<g:message code="connection.route.destroy"/>
								</g:link>
							</g:elseif>
						</div>
					</g:if>
				</li>
			</g:each>
		</ul>
	</g:else>
</div>

