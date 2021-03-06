<%@ page contentType="text/html;charset=UTF-8" %>
<ul id="system-menu">
	<li class="${params.controller=='settings'?'selected':''}">
		<g:link controller="settings">
			<g:message code="common.settings"/>
		</g:link>
	</li>
	<li class="nav ${params.controller=='help' ? 'selected' : ''}">
		<g:remoteLink controller="help" onSuccess="mediumPopup.launchHelpWizard(data);">
			<g:message code="common.help"/>
		</g:remoteLink>
	</li>
</ul>
<div id="version-number">
	<g:message code="app.version.label"/> 
	<g:meta name="app.version"/>
</div>
