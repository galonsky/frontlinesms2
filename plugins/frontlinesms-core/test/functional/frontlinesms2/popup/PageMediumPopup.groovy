package frontlinesms2.popup

import frontlinesms2.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

abstract class MediumPopup extends geb.Page {
	static content = {
		popupTitle {
			$('#ui-dialog-title-modalBox').text()?.toLowerCase()
		}
		cancel { $('button#cancel') }
		next { $('button#nextPage') }
		previous(required:false) { $('button#prevPage') }
		submit { $('button#submit') }
		tab { tabId -> 
			$('#tabs a[href="#tabs-'+tabId+'"]')
		}
		errorPanel { $('div.error-panel') }
		validationError { $('label.error') }
		error { errorPanel.text()?.toLowerCase() }
	}
}

class QuickMessageDialog extends MediumPopup {
	static at = {
		popupTitle.contains("message") || popupTitle.contains("forward") || popupTitle.contains("reply")
	}
	static content = {
		compose { module QuickMessageComposeTab }
		recipients { module QuickMessageRecipientsTab }
		confirm { module QuickMessageConfirmTab }
		errorPanel { $(".error-panel") }
	}
}

class QuickMessageComposeTab extends geb.Module {
	static base = { $('div#tabs-1') }
	static content = {
		textArea { $('textarea#messageText') }
		wordCount { $("span#send-message-stats").text() }
		magicWand { $("#magicwand-selectmessageText") }
	}
}

class QuickMessageRecipientsTab extends geb.Module {
	static base = { $('div#tabs-2') }
	static content = {
		addField { $('input#address') }
		addButton { $('a.btn.add-address') }
		manual { $('li.manual.contact') }
		count { $('#recipient-count').text().toInteger() }
		manualContacts { $("li.manual").find("input", name:"addresses") }
		groupCheckboxes { $('input', type:'checkbox', name:'groups') }
		groupCheckboxesChecked { $('input:checked', type:'checkbox', name:'groups') }
		recipientCheckboxByValue { val -> $("input[value='" + val + "']") }
	}
}

class QuickMessageConfirmTab extends geb.Module {
	static base = { $('div#tabs-3') }
	static content = {
		messagesToSendCount { $('#contacts-count').text() }
		recipientName { $('td#recipient').text() }
		messageText { $('td#confirm-message-text').text() }
	}
}

class CreateActivityDialog extends MediumPopup {
	static at = {
		popupTitle.contains("create new activity")
	}
	static content = {
		poll { $('input[value="poll"]') }
		announcement { $('input[value="announcement"]') }
		autoreply { $('input[value="autoreply"]') }
		webconnection(wait:true) { $('input[value="webconnection"]') }
		subscription { $('input[value="subscription"]') }
	}
}

class PollDialog extends MediumPopup {
	static at = {
		popupTitle.contains("poll") || popupTitle.contains("edit activity")
	}
	static content = {
		compose { module ComposeTab }
		response { module ResponseTab }
		sort { module SortTab }
		autoreply { module AutoReplyTab }
		edit { module EditMessageTab }
		recipients { module RecipientsTab }
		confirm { module ConfirmTab }
		summary { module Summary }
	}
}

class EditPollDialog extends PollDialog {
	static at = {
		popupTitle.contains("edit activity")
	}
}

class ComposeTab extends geb.Module {
	static base = { $('div#tabs-1') }
	static content = {
		yesNo { $('div.input input[value="yesNo"]') }
		multiple { $('div.input input[value="multiple"]') }
		question { $('textarea#question') }
		dontSendQuestion { $('input#dontSendMessage') }
	}
}

class ResponseTab extends geb.Module {
	static base = { $('div#tabs-2') }
	static content = {
		choice { choiceLetter -> 
			$('#choice'+choiceLetter)
		}
		label { choiceLetter ->
			$('label[for="choice'+choiceLetter+'"]')
		}
		errorLabel { choiceLetter ->
			$('label.error[for="choice'+choiceLetter+'"]')
		}
	}
}

class SortTab extends geb.Module {
	static base = { $('div#tabs-3') }
	static content = {
		dontSort { $('ul.input input[value="disabled"]') }
		sort { $('ul.input input[value="enabled"]') }
		toggle { $('input#enableKeyword') }
		keyword { $('input#poll-keyword') }
		labels { $('#poll-aliases label') }
		inputs { $('#poll-aliases input.keywords') }
		pollKeywordsContainer { $('#sorting-details') }
	}
}

class AutoReplyTab extends geb.Module {
	static base = { $('div#tabs-4') }
	static content = {
		autoreplyCheck { $('input#enableAutoreply') }
		text { $('textarea#autoreplyText') }
		keyword { $('input#poll-keyword') }
	}
}

class EditMessageTab extends geb.Module {
	static base = { $('div#tabs-5') }
	static content = {
		text { $('textarea#messageText') }
	}
}

class RecipientsTab extends geb.Module {
	static base = { $('div#tabs-6') }
	static content = {
		addField { $('input#address') }
		addButton { $('a.btn.add-address') }
		manual { $('li.manual.contact') }
		count { $('#recipient-count').text().toInteger() }
	}
}

class ConfirmTab extends geb.Module {
	static base = { $('div#tabs-7') }
	static content = {
		pollName { $('input#name') }
		message { $("#poll-message").text() }
		recipientCount { $("#confirm-recipients-count").text() }
		noRecipients { $("#no-recipients") }
		messageCount { $("#confirm-messages-count").text() }
		autoreply { $("#auto-reply-read-only-text").text() }
	}
}

class Summary extends geb.Module {
	static base = { $('div.summary') }
	static content = {

	}
}

class ExportDialog extends MediumPopup {
	static at = {
		$('#ui-dialog-title-modalBox').text()?.toLowerCase().contains("export");
	}
	static content = {
	}
}

class RenameDialog extends MediumPopup {
	static at = {
		$('#ui-dialog-title-modalBox').text()?.toLowerCase().contains("rename");
	}
	static content = {
		name { $('input#name') }
		done { $('button#done') }
	}
}

class AnnouncementDialog extends MediumPopup {
	static at = {
		popupTitle.contains("announcement")
	}
	static content = {
		composeAnnouncement {module QuickMessageComposeTab}
		recipients {module AnnouncementRecipientsTab}
		confirm { module AnnouncementConfirmTab }
		summary { module AnnouncementSummary }
	}
}

class AnnouncementRecipientsTab extends RecipientsTab {
	static base = { $('div#tabs-2') }
}

class AnnouncementConfirmTab extends geb.Module {
	static base = { $('div#tabs-3') }
	static content = {
		announcementName { $('input#name') }
		message { $("#confirm-message-text").text() }
		recipientCount { $("#confirm-recipients-count").text() }
		recipientCount { $("#confirm-message-count").text() }
	}
}

class AnnouncementSummary extends geb.Module {
	static base = { $('div#tabs-4') }
	static content = {
		message { $("div.summary") }
	}
}

class DeleteDialog extends MediumPopup {
	static at = {
		popupTitle.contains("empty trash");
	}
	static content = {
		title { $("#title").text() }
		done { $('button#done') }
	}
}

class ConnectionDialog extends MediumPopup {
	static at = {
		popupTitle.contains('connection')
	}
	static content = {
		connectionType { $("#connectionType") }
		connectionForm { $('#connectionForm') }
		confirmName { $("#confirm-name") }
		confirmType { $("#confirm-type") }
		confirmPort { $("#confirm-port") }

		basicInfo { connectionType ->
			$("p.info.$connectionType").text()
		}

		confirmIntelliSmsConnectionName { $("#intellisms-confirm #confirm-name") }
		confirmIntelliSmsUserName { $("#intellisms-confirm #confirm-username") }
		confirmIntelliSmsType { $("#intellisms-confirm #confirm-type") }

		confirmSmssyncName { $('#smssync-confirm #confirm-name') }
		confirmSmssyncSecret { $('#smssync-confirm #confirm-secret') }
		confirmSmssyncReceiveEnabled { $('#smssync-confirm #confirm-receiveEnabled') }
		confirmSmssyncSendEnabled { $('#smssync-confirm #confirm-sendEnabled') }

		error {$('label', class:'error')}
	}
}

class SmartGroupCreateDialog extends MediumPopup {
	static at = {
		popupTitle.contains('create smart group')
	}
	static content = {

		rules { $('tr.smart-group-criteria') }
		ruleField { rules.find('select', name:'rule-field') }
		ruleValues { rules.find('input', name:'rule-text') }
		ruleMatchText { rules.find('.rule-match-text')*.text() }
		removeRuleButtons(required:false) { $('tr.smart-group-criteria a.remove-command') }
		smartGroupNameField { $('input', type:'text', name:'smartgroupname') }
		addRuleButton { $('.btn', text:"Add another rule") }
		editButton { $('button', text:'Edit')}
		flashMessage(required:false) { $('div.flash') }
	}
}


class SmartGroupEditDialog extends SmartGroupCreateDialog {
	static at = {
		popupTitle.contains('edit group')
	}
}

class WebconnectionWizard extends MediumPopup {
	static at = {
		waitFor('very slow') { popupTitle.contains("connection") || popupTitle == 'edit activity' }
		return true
	}
	static content = {
		error { $("label.error").text()}
		keywordTab { module WebconnectionKeywordTab }
		requestTab { module WebconnectionRequestFormatTab }
		confirmTab(required:false) { module WebconnectionConfirmTab }
		summary { module WebconnectionSummary }

		configureUshahidi(required:false) { module ConfigureUshahidiWebconnectionTab }

		option { shortName -> $('input', name:'webconnectionType', value:shortName) }
		getTitle { shortName -> option(shortName).previous('label').text() }
		getDescription { shortName -> option(shortName).previous('p').text() }
	}
}

class WebconnectionKeywordTab extends geb.Module {
	static base = { $('div.generic_sorting_tab') }
	static content = {
		useKeyword { value ->
				$('input#sorting',value:value)
		}
		keyword { $('input#keywords') }
	}
}

class WebconnectionRequestFormatTab extends geb.Module {
	static base = { $('div#tabs-2') }
	static content = {
		post { $("input[value='POST']") }
		get { $("input[value='GET']") }
		url { $("input#url") }
		addParam { $('a.btn.addNew') }
		parameters { moduleList WebconnectionParam, $('#web-connection-param-table tbody tr') }
	}
}

class WebconnectionParam extends geb.Module {
	static content = {
		value { $('input.param-value') }
		name { $("input.param-name") }
		remove { $("a.remove-command") }
	}
}

class WebconnectionConfirmTab extends geb.Module {
	static base = { $('div#webconnection-confirm') }
	static content = {
		name { $('input#name') }
		keyword { $("#confirm-keyword").text() }
		type { $("#confirm-type").text() }
		url { $("#confirm-url").text() }
		
		confirm{ label->
			$("#confirm-"+label).text()
		}
	}
}

class WebconnectionSummary extends geb.Module {
	static base = { $('div#tabs-4') }
	static content = {
		message { $("div.summary") }
	}
}

class SubscriptionCreateDialog extends MediumPopup {
	static at = {
		popupTitle.contains("subscription") || popupTitle.contains("edit activity")
	}
	static content = {
		group { module SubscriptionGroupTab }
		keywords { module SubscriptionKeywordsTab}
		autoreply { module SubscriptionAutoReplyTab }
		confirm { module SubscriptionConfirmTab }
		summary { module SubscriptionSummary }
		error { errorPanel }
	}
}

class SubscriptionGroupTab extends geb.Module {
	static base = { $('div#tabs-1') }
	static content = {
		addToGroup { groupId ->
			$('select#subscriptionGroup').jquery.val(groupId)
			$('select#subscriptionGroup').jquery.trigger("change")
		}
	}
}

class SubscriptionKeywordsTab extends geb.Module {
	static base = { $('div#tabs-2')}
	static content = {
		dontSort { $('ul.input input[value="disabled"]') }
		sort { $('ul.input input[value="enabled"]') }
		keywordText { $('input#topLevelKeywords') }
		joinKeywords {$('input#joinKeywords')}
		leaveKeywords {$('input#leaveKeywords')}
		defaultAction { $("input#defaultAction") }
		joinHelperMessage {$('#joinHelperMessage').text()}
		leaveHelperMessage {$('#leaveHelperMessage').text()}
	}
}

class SubscriptionAutoReplyTab extends geb.Module {
	static base = { $('div#tabs-3') }
	static content = {
		enableJoinAutoreply {$('input#enableJoinAutoreply')}
		joinAutoreplyText {$('textarea#joinAutoreplyText')}
		enableLeaveAutoreply{$('input#enableLeaveAutoreply')}
		leaveAutoreplyText {$('textarea#leaveAutoreplyText')}
	}
}

class SubscriptionConfirmTab extends geb.Module {
	static base = { $('div#tabs-4') }
	static content = {
		subscriptionName { $('input#name') }
		keyword {$("#confirm-keyword").text()}
		confirm {id->
			$("#confirm-"+id).text()
		}
		joinAliases {$("#confirm-joinAliases").text()}
		leaveAliases {$("#confirm-leaveAliases").text()}
		autoreplyText {$("#confirm-autoreplyText").text()}
	}
}

class SubscriptionSummary extends geb.Module {
	static base = { $('div#tabs-5') }
	static content = {
		message { $("div.summary") }
		ok { $('button#submit') }
	}
}

class EditSubsriptionDialog extends SubscriptionCreateDialog {
	static at = {
		popupTitle.contains('edit subscription')
	}
}

class WebconnectionTypeSelectTab extends geb.Module{
	static base = { $('div#tabs-1') }
	static content = {
		getDescription { shortName ->
			$("#"+shortName).text()
		}
		getTitle { shortName ->
			$("#"+shortName+" .info").text()
		}
		option { shortName ->
			$('#webconnectionType').value(shortName)
		}
	}
}

class ConfigureUshahidiWebconnectionTab extends geb.Module{
	static base = { $('#webconnection-config') }
	static content = {
		subType(required:false){ type->
			$('input', name:'serviceType', value:type)
		} 
		crowdmapDeployAddress{ $('#displayed_url') }
		ushahidiDeployAddress{ $('#displayed_url') }
		ushahidiKeyLabel { $("label", for:'key').first() }
		crowdmapKeyLabel { $("label", for:'key').last() }
		urlSuffix { $("label", for:'url').last() }
		crowdmapApiKey{ $('#key') }
		ushahidiApiKey{ $('#key') }
	}
}

class AutoreplyCreateDialog extends MediumPopup {
	static at = {
		popupTitle.contains("autoreply") || popupTitle.contains("edit activity")
	}
	static content = {
		message { module AutoreplyMessageTab}
		keyword { module AutoreplyKeywordTab}
		confirm { module AutoreplyConfirmTab}
		summary { module AutoreplySummaryTab}
		validationErrorText { $('label.error').text() }
		errorText { errorPanel.text()?.toLowerCase() }
		error { errorPanel }
		create { $('button#submit') }
	}
}

class AutoreplyMessageTab extends geb.Module {
	static base = { $('div#tabs-1')}
	static content = {
		messageText { $('area#messageText') }
	}
}

class AutoreplyKeywordTab extends geb.Module {
	static base = { $('div#tabs-2')}
	static content = {
		keywordText { $('#keywords') }
		blankKeyword {$('#blankKeyword')}
	}
}

class AutoreplyConfirmTab extends geb.Module {
	static base = { $('div#tabs-3') }
	static content = {
		keywordConfirm {$("#keyword-confirm").text()}
		autoreplyConfirm {$("#autoreply-confirm").text()}
		nameText {$("#name")}
	}
}

class AutoreplySummaryTab extends geb.Module {
	static base = { $('div#tabs-4 > div.summary') } //ensures div.summary has been loaded too
	static content = {
		message { $("p", 0).text() }
	}
}
