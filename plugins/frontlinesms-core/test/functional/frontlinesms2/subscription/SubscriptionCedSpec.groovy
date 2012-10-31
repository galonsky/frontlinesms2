package frontlinesms2.subscription

import frontlinesms2.*
import frontlinesms2.page.*
import frontlinesms2.popup.*
import frontlinesms2.message.PageMessageInbox

class SubscriptionCedSpec extends SubscriptionBaseSpec  {
	def "can launch subscription wizard from create new activity link" () {
		when:
			to PageMessageInbox
			bodyMenu.newActivity.click()
		then:
			waitFor { at CreateActivityDialog }
		when:
			subscription.click()
		then:
			waitFor { at SubscriptionCreateDialog }
	}

	def "Can create a new subscription" () {
		setup:
			new Group(name:"Friends").save(failOnError:true, flush:true)
		when:
			to PageMessageInbox
			bodyMenu.newActivity.click()
		then:
			waitFor {at CreateActivityDialog}
		when:
			subscription.click()
		then:
			waitFor {at SubscriptionCreateDialog}
		when:
			group.addToGroup Group.findByName('Friends').id
			next.click()
		then:
			waitFor { keywords.displayed }
		when:
			keywords.sort.click()
			keywords.keywordText = 'FRIENDS'
			keywords.joinKeywords = 'join, start'
			keywords.leaveKeywords = 'leave, stop'
			keywords.defaultAction = "join"
		then:
			keywords.joinHelperMessage == "FRIENDS join, FRIENDS start"
			keywords.leaveHelperMessage == "FRIENDS leave, FRIENDS stop"
			next.click()
		then:
			waitFor {autoreply.displayed}
		when:
			autoreply.enableJoinAutoreply.click()
			autoreply.joinAutoreplyText = "You have been successfully subscribed to Friends group"
			autoreply.enableLeaveAutoreply.click()
			autoreply.leaveAutoreplyText = "You have been unsubscribed from Friends group"
			next.click()
		then:
			waitFor { confirm.subscriptionName.displayed }
		when:
			confirm.subscriptionName.value("Friends subscription")
			submit.click()
		then:
			waitFor { summary.message.displayed }
		when:
			submit.click()
		then:
			waitFor { at PageMessageSubscription }
	}

	def "If no top level keyword is provided, default action is disabled, and is only enabled when a T.L.K is added" () {
		setup:
			new Group(name:"Friends").save(failOnError:true)
		when:
			to PageMessageInbox
			bodyMenu.newActivity.click()
		then:
			waitFor {at CreateActivityDialog}
		when:
			subscription.click()
		then:
			waitFor {at SubscriptionCreateDialog}
		when:
			group.addToGroup Group.findByName('Friends').id
			next.click()
		then:
			waitFor { keywords.displayed }
		when:
			keywords.sort.click()
			keywords.joinKeywords = 'join, start'
			keywords.leaveKeywords = 'leave, stop'
		then:
			keywords.joinHelperMessage == 'join, start'
			keywords.leaveHelperMessage == 'leave, stop'
			keywords.defaultAction.disabled
		when:
			keywords.keywordText = 'a'
		then:
			!keywords.defaultAction.disabled
	}

	def "Can edit an existing subscription"() {
		setup:
			createTestSubscriptions()
		when:
			to PageMessageInbox
			bodyMenu.activityLinks[0].click()//click on the subscription
		then:
			waitFor { at PageMessageSubscription }
		when:
			header.moreActions.value("edit").click()
		then:
			waitFor { at SubscriptionCreateDialog }
		when:
			println "***"+group.text()
			group.addToGroup Group.findByName("Camping").id.toString()
			next.click()
		then:
			waitFor { keywords.displayed }
			keywords.joinHelperMessage == "CAMPING JOIN, CAMPING IN, CAMPING START"
			keywords.leaveHelperMessage == "CAMPING LEAVE, CAMPING OUT, CAMPING STOP"
		when:
			keywords.keywordText = 'NOTABOUTFOOTBALL'
		then:
			keywords.joinHelperMessage == "NOTABOUTFOOTBALL JOIN, NOTABOUTFOOTBALL IN, NOTABOUTFOOTBALL START"
			keywords.leaveHelperMessage == "NOTABOUTFOOTBALL LEAVE, NOTABOUTFOOTBALL OUT, NOTABOUTFOOTBALL STOP"
		when:
			next.click()
			autoreply.enableJoinAutoreply.click()
			autoreply.joinAutoreplyText = "You have been successfully subscribed to some other group"
			autoreply.enableLeaveAutoreply.click()
			autoreply.leaveAutoreplyText = "You have been unsubscribed from some other group"
			next.click()
		then:
			waitFor { confirm.subscriptionName.displayed }
		when:
			confirm.subscriptionName.value("Not cats subscription")
			submit.click()
		then:
			waitFor { summary.message.displayed }
	}

	def "Should not proceed if subscription not named"() {
		setup:
			new Group(name:"Friends").save(failOnError:true, flush:true)
		when:
			launchSubscriptionPopup()
			waitFor { at SubscriptionCreateDialog }
			group.addToGroup Group.findByName('Friends').id.toString()
			next.click()
			keywords.sort.click()
			keywords.keywordText = 'FRIENDS'
			keywords.joinKeywords = 'join, start'
			keywords.leaveKeywords = 'leave, stop'
			next.click()
			autoreply.enableJoinAutoreply.click()
			autoreply.joinAutoreplyText = "You have been successfully subscribed to Friends group"
			autoreply.enableLeaveAutoreply.click()
			autoreply.leaveAutoreplyText = "You have been unsubscribed from Friends group"
			next.click()
		then:
			waitFor { confirm.subscriptionName.displayed }
		when:
			submit.click()
		then:
			waitFor {validationError.text().contains('This field is required.')}
			at SubscriptionCreateDialog
	}

	def "Subscriptions must be associated with a group" () {
		when:
			launchSubscriptionPopup()
			waitFor { at SubscriptionCreateDialog }
			group.addToGroup('Select group')
			next.click()
		then:
			waitFor {validationError.text().contains('Subscriptions must have a group')}
			at SubscriptionCreateDialog
	}

	def "keyword aliases must be unique if provided"() {
		setup:
			new Group(name:"Friends").save(failOnError:true, flush:true)
		when:
			launchSubscriptionPopup()
			waitFor { at SubscriptionCreateDialog }
			group.addToGroup Group.findByName('Friends').id
			next.click()
			keywords.sort.click()
			keywords.keywordText = 'FRIENDS'
			keywords.joinKeywords = 'team'
			keywords.leaveKeywords = 'team'
			next.click()
		then:
			waitFor {validationError.text().contains('Keywords should be unique')}
			keywords.joinKeywords.displayed
			at SubscriptionCreateDialog
	}

	def "keyword aliases must have valid commas seperated values if provided"() {
		setup:
			new Group(name:"Friends").save(failOnError:true, flush:true)
		when:
			launchSubscriptionPopup()
			waitFor { at SubscriptionCreateDialog }
			group.addToGroup Group.findByName('Friends').id
			next.click()
			keywords.sort.click()
			keywords.keywordText = 'FRIENDS'
			keywords.joinKeywords = 'team'
			keywords.leaveKeywords = 'team%^&%^%&'
			next.click()
		then:
			waitFor {validationError.text().contains('Invalid keyword. Try a, name, word')}
			keywords.joinKeywords.displayed
			at SubscriptionCreateDialog
	}

	def "autoreply text must be provided if join/leave autoreply is enabled"() {
		setup:
			new Group(name:"Friends").save(failOnError:true, flush:true)
		when:
			launchSubscriptionPopup()
			waitFor { at SubscriptionCreateDialog }
			group.addToGroup Group.findByName('Friends').id
			next.click()
			keywords.sort.click()
			keywords.keywordText = 'FRIENDS'
			keywords.joinKeywords = 'join, start'
			keywords.leaveKeywords = 'leave, stop'
			next.click()
		then:
			waitFor {autoreply.displayed}
		when:
			autoreply.enableJoinAutoreply.click()
			autoreply.joinAutoreplyText = "You have been successfully subscribed to Friends group"
			autoreply.enableLeaveAutoreply.click()
			next.click()
		then:
			waitFor {validationError.text().contains('Please enter leave autoreply text')}
			autoreply.enableLeaveAutoreply.displayed
	}

	def "Confirm screen should display all the necessary data" () {
		setup:
			new Group(name:"Friends").save(failOnError:true, flush:true)
		when:
			launchSubscriptionPopup()
			waitFor { at SubscriptionCreateDialog }
			group.addToGroup Group.findByName('Friends').id
			next.click()
		then:
			waitFor { keywords.displayed }
		when:
			keywords.sort.click()
			keywords.keywordText = 'FRIENDS'
			keywords.joinKeywords = 'join, start'
			keywords.leaveKeywords = 'leave, stop'
			keywords.defaultAction = "join"
			next.click()
		then:
			waitFor {autoreply.displayed}
		when:
			autoreply.enableJoinAutoreply.click()
			autoreply.joinAutoreplyText = "You have been successfully subscribed to Friends group"
			autoreply.enableLeaveAutoreply.click()
			autoreply.leaveAutoreplyText = "You have been unsubscribed from Friends group"
			next.click()
		then:
			waitFor { confirm.subscriptionName.displayed }
			confirm.confirm("keyword-text") ==  "FRIENDS"
			confirm.confirm("join-alias-text") ==  "join, start"
			confirm.confirm("leave-alias-text") ==  "leave, stop"
			confirm.confirm("default-action-text") ==  "join"
			confirm.confirm("join-autoreply-text") ==  "You have been successfully subscribed to Friends group"
			confirm.confirm("leave-autoreply-text") ==  "You have been unsubscribed from Friends group"
	}

	def launchSubscriptionPopup() {
		to PageMessageInbox
		bodyMenu.newActivity.click()
		waitFor('slow') { at CreateActivityDialog }
		subscription.click()
		waitFor('slow') { at SubscriptionCreateDialog }
	}
}
