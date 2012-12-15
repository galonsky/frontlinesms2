package frontlinesms2

class QuickMessageController {
	def create = {
		if( params.recipients?.contains(',')) {
			def recipientList = []
			params.recipients.tokenize(',').each {
				def msg = Fmessage.findById(it)
				if (msg.inbound)
				{
					recipientList << msg.src
				}
				else
				{
					msg.dispatches.each{ recipientList << it.dst }
				}
			}
			params.recipients = recipientList.unique()
		}
		def obfuscator = NumberObfuscator.getInstance()
		def recipients = params.recipients ? [params.recipients].flatten() : []
		def recipientName = recipients.size() == 1 ? (Contact.findByMobile(recipients[0])?.name ?: obfuscator.obfuscateNumber(recipients[0])) : ""
		def contacts = Contact.list(sort: "name")
		def configureTabs = params.configureTabs ? configTabs(params.configureTabs): ['tabs-1', 'tabs-2', 'tabs-3', 'tabs-4']
		def groupList = Group.getGroupDetails() + SmartGroup.getGroupDetails()
		[contactList: contacts,
				configureTabs: configureTabs,
				groupList:groupList,
				recipients:recipients,
				recipientName: recipientName,
				messageText: params.messageText ? params.messageText : [],
				nonExistingRecipients:recipients - contacts*.getMobile() - contacts*.getEmail()]
	}

	private def configTabs(configTabs) {
		return configTabs.tokenize(",")*.trim()
	}
}