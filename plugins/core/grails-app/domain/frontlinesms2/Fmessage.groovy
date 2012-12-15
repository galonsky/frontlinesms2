package frontlinesms2

import groovy.time.*
import org.hibernate.FlushMode
import org.hibernate.criterion.CriteriaSpecification
import java.security.MessageDigest

class Fmessage {
	static final int MAX_TEXT_LENGTH = 1600

	static belongsTo = [messageOwner:MessageOwner]
	static transients = ['hasSent', 'hasPending', 'hasFailed', 'displayName' ,'outboundContactList']
	
	Date date = new Date() // No need for dateReceived since this will be the same as date for received messages and the Dispatch will have a dateSent
	Date dateCreated // This is unused and should be removed, but doing so throws an exception when running the app and I cannot determine why
	
	String src
	String text
	String inboundContactName
	String outboundContactName
	
	boolean read
	boolean starred
	boolean archived
	boolean isDeleted
	
	boolean inbound
	static hasMany = [dispatches:Dispatch]

	static mapping = {
		sort date:'desc'
		inboundContactName formula:'SELECT c.name FROM Contact c WHERE c.mobile=src'
		outboundContactName formula:'SELECT MAX(c.name) FROM Contact c, Dispatch d WHERE c.mobile=d.dst AND d.message_id=id'
		version false
	}
	
	static constraints = {
		messageOwner nullable:true
		src(nullable:true, validator: { val, obj ->
				val || !obj.inbound
		})
		text nullable:true, maxSize:MAX_TEXT_LENGTH
		inboundContactName nullable:true
		outboundContactName nullable:true
		archived(nullable:true, validator: { val, obj ->
				obj.messageOwner == null || obj.messageOwner.archived == val
		})
		inbound(nullable:true, validator: { val, obj ->
				val ^ (obj.dispatches? true: false)
		})
		dispatches nullable:true
	}

	def beforeInsert = {
		if(!this.inbound) this.read = true
	}

	static namedQueries = {
		inbox { getOnlyStarred=false, archived=false ->
			and {
				eq("isDeleted", false)
				eq("archived", archived)
				if(getOnlyStarred)
					eq("starred", true)
				eq("inbound", true)
				isNull("messageOwner")
			}
		}
		sent { getOnlyStarred=false, archived=false ->
			and {
				eq("isDeleted", false)
				eq("archived", archived)
				if(!archived) {
					projections { dispatches { eq('status', DispatchStatus.SENT) } }
				} else {
					projections {
						dispatches {
							or {
								eq('status', DispatchStatus.SENT)
								eq('status', DispatchStatus.PENDING)
								eq('status', DispatchStatus.FAILED)
							}
						}
					}
				}
				if(getOnlyStarred)
					eq("starred", true)
			}
		}
		pending { getOnlyFailed=false ->
			and {
				eq("isDeleted", false)
				eq("archived", false)
				if(getOnlyFailed) {
					projections { dispatches { eq('status', DispatchStatus.FAILED) } }
				} else {
					projections {
						dispatches {
							or {
								eq('status', DispatchStatus.PENDING)
								eq('status', DispatchStatus.FAILED)
							}
						}
					}
				}
			}
			projections {
				distinct 'id'
				property 'date'
				property 'id'
			}
		}
		deleted { getOnlyStarred=false ->
			and {
				eq("isDeleted", true)
				eq("archived", false)
				if(getOnlyStarred)
					eq('starred', true)
			}
		}
		owned { MessageOwner owner, boolean getOnlyStarred=false, boolean getSent=false ->
			and {
				eq("isDeleted", false)
				eq("messageOwner", owner)
				if(getOnlyStarred)
					eq("starred", true)
				if(!getSent)
					eq("inbound", true)
			}
		}
		unread {
			and {
				eq("isDeleted", false)
				eq("archived", false)
				eq("inbound", true)
				eq("read", false)
				isNull("messageOwner")
			}
		}

		search { search ->
			createAlias('dispatches', 'disp', CriteriaSpecification.LEFT_JOIN)
			if(search.searchString) {
				ilike("text", "%${search.searchString}%")
			}
			if(search.contactString) {
				def contactNumbers = Contact.findAllByNameIlike("%${search.contactString}%")*.mobile ?: ['']
				or {
					'in'('src', contactNumbers)
					'in'('disp.dst', contactNumbers)
				}
			} 
			if(search.group) {
				def groupMembersNumbers = search.group.addresses?: [''] //otherwise hibernate fail to search 'in' empty list
				or {
					'in'('src', groupMembersNumbers)
					'in'('disp.dst', groupMembersNumbers)
				}
			}
			if(search.status) {
				if(search.status.toLowerCase() == 'inbound') eq('inbound', true)
				else eq('inbound', false)
			}
			if(search.owners) {
				'in'("messageOwner", search.owners)
			}
			if(search.startDate && search.endDate) {
				between("date", search.startDate, search.endDate)
			} else if (search.startDate) {	
				ge("date", search.startDate)
			} else if (search.endDate) {
				le("date", search.endDate)
			}
			if(search.customFields.any { it.value }) {
				// provide empty list otherwise hibernate fails to search 'in' empty list
				def matchingContactsNumbers = Contact.findByCustomFields(search.customFields)*.mobile?: ['']
				or {
					'in'("src", matchingContactsNumbers)
					'in'('disp.dst', matchingContactsNumbers)
				}
			}
			if(!search.inArchive) {
				eq('archived', false)
			}
			eq('isDeleted', false)
			// order('date', 'desc') removed due to http://jira.grails.org/browse/GRAILS-8162; please reinstate when possible
		}
		
		forReceivedStats { params ->
			def groupInstance = params.groupInstance
			def messageOwner = params.messageOwner
			def startDate = params.startDate.startOfDay
			def endDate = params.endDate.endOfDay
			
			and {
				eq('inbound', true)
				eq('isDeleted', false)
				between("date", startDate, endDate)
				if(groupInstance) 'in'('src', groupInstance?.addresses ?: "")
				if(messageOwner) 'in'('messageOwner', messageOwner)
			}
		}
	}

	def getDisplayName() {
		def obfuscator = NumberObfuscator.getInstance()
		if(inbound) {
			if(inboundContactName) return inboundContactName
			else if(id) return obfuscator.obfuscateNumber(src)
			else return Contact.findByMobile(src)?.name?: obfuscator.obfuscateNumber(src)
		} else if(dispatches.size() == 1) {
			if(outboundContactName) return outboundContactName
			else {
				def dst = (dispatches as List)[0].dst
				if(id) return obfuscator.obfuscateNumber(dst)
				else return Contact.findByMobile(dst)?.name?: obfuscator.obfuscateNumber(dst)
			}
		} else {
			return Integer.toString(dispatches.size())
		}
	}

	def getHasSent() { areAnyDispatches(DispatchStatus.SENT) }
	def getHasFailed() { areAnyDispatches(DispatchStatus.FAILED) }
	def getHasPending() { areAnyDispatches(DispatchStatus.PENDING) }
	def getOutboundContactList(){ 
		def contactlist = []
		dispatches.each{ contactlist << Contact.findByMobile(it.dst)?.name }
		contactlist?contactlist:""
	}

	private def areAnyDispatches(status) {
		dispatches?.any { it.status == status }
	}

	public void setText(String text) {
		this.text = text?.truncate(MAX_TEXT_LENGTH)
	}

	// FIXME document what this is, and remove references to PollResponse.  Anyway this poll display
	// only makes sense within a poll - e.g. in a search result this is a bit dumb.
	def getDisplayText() {
		def p = PollResponse.withCriteria {
			messages {
				eq('isDeleted', false)
				eq('archived', false)
				eq('id', this.id)
			}
		}

		p?.size() ? "${p[0].value} (\"${this.text}\")" : this.text
	}

	static def listPending(onlyFailed, params=[:]) {
		Fmessage.getAll(pending(onlyFailed).list(params) as List)
	}

	static def countPending(onlyFailed) {
		pending(onlyFailed).list().size()
	}

	static def hasFailedMessages() {
		return pending(true).count() > 0
	}
	
	static def countUnreadMessages() {
		Fmessage.unread.count()
	}
	
	static def countAllMessages(params) {
		def inboxCount = Fmessage.inbox.count()
		def sentCount = Fmessage.sent.count()
		def pendingCount = Fmessage.pending.count()
		def deletedCount = Fmessage.deleted.count()
		[inbox: inboxCount, sent: sentCount, pending: pendingCount, deleted: deletedCount]
	}

	// TODO should this be in a service?
	static def getMessageStats(params) {
		def asKey = { date -> date.format('dd/MM') }
		
		def dates = [:]
		(params.startDate..params.endDate).each { date ->
			dates[asKey(date)] = [sent:0, received:0]
		}
		
		if(!params.inbound) {
			// TODO the named query should ideally do the counts for us
			Dispatch.forSentStats(params).list().each { d ->
				++dates[asKey(d.dateSent)].sent
			}
		}
		
		if(params.inbound == null || params.inbound) {
			// TODO the named query should ideally do the counts for us
			Fmessage.forReceivedStats(params).list().each { m ->
				++dates[asKey(m.date)].received
			}
		}
		
		dates
	}
	
	//TODO: Remove in Groovy 1.8 (Needed for 1.7)
	private static def countAnswer(final Map<Object, Integer> answer, Object mappedKey) {
		if (!answer.containsKey(mappedKey)) {
			answer.put(mappedKey, 0)
		}
		int current = answer.get(mappedKey)
		answer.put(mappedKey, current + 1)
	}
	
	def updateDispatches() {
		if(isDeleted) {
			dispatches.each {
				it.isDeleted = true
			}
		}
	}
}

