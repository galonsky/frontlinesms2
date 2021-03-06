package frontlinesms2

class SmartGroupControllerISpec extends grails.plugin.spock.IntegrationSpec {
	private static final def STANDARD_FIELD_NAMES = ['Phone number', 'Contact name', 'email', 'notes']
	private static final def STANDARD_FIELD_IDS = ['mobile', 'contactName', 'email', 'notes']
	private static final String CUSTOM_FIELD_ID_PREFIX = 'custom:'
	
	def controller
	
	def setup() {
		controller = new SmartGroupController()
	}
	
	def 'viewing smart group displays contents of that smart group'() {
		given:
			controller = new ContactController()
			def englishContacts = new SmartGroup(name:'English contacts', mobile:'+44').save(flush:true, failOnError:true)
			createContact('Alfred', '+4423456789')
			createContact('Bernadette', '+3323+4456789')
			createContact('Charles', '+440987654')
			createContact('Dupont', '+33098765432')
		when:
			controller.params.smartGroupId = englishContacts.id
			def model = controller.show()
		then:
			model.contactInstanceList*.name.sort() == ['Alfred', 'Charles']
			model.contactInstanceTotal == 2
		when:
			controller.params.searchString = 'ED'
			model = controller.show()
		then:
			model.contactInstanceList*.name == ['Alfred']
			model.contactInstanceTotal == 1
	}
	
	def 'CREATE returns a smartgroup instance'() {
		when:
			def model = controller.create()
		then:
			model.smartGroupInstance instanceof SmartGroup
	}
	
	def 'CREATE returns a list of field names including standard field names'() {
		when:
			def model = controller.create()
		then:
			model.fieldNames == STANDARD_FIELD_NAMES
			model.fieldIds == STANDARD_FIELD_IDS
	}
	
	def 'CREATE returns a list of field names including custom field names'() {
		given:
			['AIM name', 'Favourite Food'].each { CustomField.build(name:it) }
		when:
			def model = controller.create()
		then:
			model.fieldNames == STANDARD_FIELD_NAMES + ['AIM name', 'Favourite Food']
			model.fieldIds == STANDARD_FIELD_IDS + ['custom:AIM name', 'custom:Favourite Food']
	}
	
	def 'calling SAVE with a customfield rule defined will create a SmartGroup which owns a CustomField'() {
		given:
			controller.params.smartgroupname = 'Londons'
			controller.params.'rule-field' = 'custom:Town'
			controller.params.'rule-text' = 'London'
		when:
			controller.save()
			def g = SmartGroup.findByName('Londons')
		then:
			g
			g.customFields.size() == 1
		when:
			def cf = g.customFields.asList()[0]
		then:
			cf.name == 'Town'
			cf.value == 'London'
	}
	
	def 'can update smart group name'() {
		given:
			def smartGroup = new SmartGroup(name:'Smart Group', mobile:'+44').save(flush:true, failOnError:true)
			controller.params.id = smartGroup.id
			controller.params.smartgroupname = "renamed smart group"
		when:
			controller.save()
			def updatedGroup = SmartGroup.get(smartGroup.id)
		then:
			updatedGroup.name == "renamed smart group"
			controller.response.redirectedUrl == "/contact/show?smartGroupId=${smartGroup.id}"
	}
	
	def 'can update existing smartgroup mobile rules'() {
		given:
			def englishContacts = new SmartGroup(name:'English contacts', mobile:'+33').save(flush:true, failOnError:true)
			createContact('Alfred', '+4423456789')
			createContact('Bernadette', '+3323+4456789')
			createContact('Charles', '+440987654')
			createContact('Dupont', '+33098765432')
			createContact('Edgar de Gaulle', '+33098764677')
			assert englishContacts.members*.name.sort() == ["Bernadette", "Dupont", 'Edgar de Gaulle']
		when:
			controller.params.smartgroupname = 'Londons'
			controller.params.id = "${englishContacts.id}"
			controller.params.'rule-text' = "+44"
			controller.params.'rule-field' = "mobile"
			controller.save()
		then:
			englishContacts.members*.name.sort() == ['Alfred', 'Charles']
	}
	
	def 'can remove existing smartgroup mobile rules'() {
		given:
			def smartGroup = new SmartGroup(name:'English contacts', mobile:'+33').save(flush:true, failOnError:true)
			createContact('Alfred', '+4423456789')
			createContact('Bernadette', '+3323+4456789')
			def charles = createContact('Charles', '+440987654')
			charles.notes = "I am super awesome"
			charles.save(flush:true)
			assert smartGroup.members*.name == ["Bernadette"]
		when:
			controller.params.smartgroupname = 'Londons'
			controller.params.id = "${smartGroup.id}"
			controller.params.'rule-text' = "awesome"
			controller.params.'rule-field' = "notes"
			controller.save()
		then:
			!smartGroup.mobile
			smartGroup.members*.name == ['Charles']
	}
	
	def 'can update a single smart group rule when a smartgroup contains other rules'() {
		given:
			def englishContacts = new SmartGroup(name:'English contacts', mobile:'+33', notes:'test').save(flush:true, failOnError:true)
			def testContact1 = createContact('Alfred', '+4423456789')
			def testContact2 = createContact('Charles', '+442987654')
			testContact1.notes = "testing one two twa"
			testContact2.notes = "this is a test"
			testContact1.save(flush:true)
			testContact2.save(flush:true)
			def testContact3 = createContact('Bernadette', '+3323+4456789')
			testContact3.notes = "this is a test"
			testContact3.save(flush:true)
			createContact('Dupont', '+33098765432')
			createContact('Edgar de Gaulle', '+33098764677')
			assert englishContacts.members*.name == ["Bernadette"]
		when:
			controller.params.smartgroupname = 'Londons'
			controller.params.id = "${englishContacts.id}"
			controller.params.'rule-text' = ["+442", "test"] as String[]
			controller.params.'rule-field' = ["mobile", "notes"] as String[]
			controller.save()
		then:
			SmartGroup.count() == 1
			SmartGroup.get(englishContacts.id).name == "Londons"
			englishContacts.members*.name.sort() == ['Alfred', 'Charles']
	}
	
	def "can update the customfield rules of a smart group"() {
		given:
			def customFieldA = new CustomField(name:"location", value:"ken").save(flush:true)
			def customFieldB = new CustomField(name:"city", value:"nai").save(flush:true)
			
			def smartGroup = new SmartGroup(name:'English contacts', customFields:[customFieldA]).save(flush:true, failOnError:true)
			def testContact1 = createContact('Alfred', '+4423456789')
			def testContact2 = createContact('Charles', '+442987654')
			def testContact3 = createContact('Bernadette', '+3323+4456789')
			
			testContact1.addToCustomFields(new CustomField(name:"location", value:"Kenya"))
			testContact1.addToCustomFields(new CustomField(name:"city", value:"Kisumu"))
			testContact2.addToCustomFields(new CustomField(name:"city", value:"Nairobi"))
			testContact3.addToCustomFields(new CustomField(name:"location", value:"Kenturky"))
			
			testContact1.save(flush:true)
			testContact2.save(flush:true)
			testContact3.save(flush:true)
		expect:
			smartGroup.members*.name == ["Alfred", "Bernadette"]
		when:
			controller.params.id = "${smartGroup.id}"
			controller.params.smartgroupname = "${smartGroup.name}"
			controller.params.'rule-text' = "nai"
			controller.params.'rule-field' = "${CUSTOM_FIELD_ID_PREFIX + customFieldB.name}"
			controller.save()
			smartGroup.refresh()
		then:
			SmartGroup.count() == 1
			smartGroup.members*.name.sort() == ['Charles']
	}

	def "can remove the customfield rules of a smart group"() {
		given:
			def customFieldA = new CustomField(name:"location", value:"Ken").save(flush:true)
			def customFieldB = new CustomField(name:"city", value:"nai").save(flush:true)
			
			def smartGroup = new SmartGroup(name:'English contacts', customFields:[customFieldA, customFieldB]).save(flush:true, failOnError:true)
			def testContact1 = createContact('Alfred', '+4423456789')
			def testContact2 = createContact('Charles', '+442987654')
			
			testContact1.addToCustomFields(new CustomField(name:"location", value:"Kenya"))
			testContact1.addToCustomFields(new CustomField(name:"city", value:"Kisumu"))
			
			testContact2.addToCustomFields(new CustomField(name:"location", value:"Kenya"))
			testContact2.addToCustomFields(new CustomField(name:"city", value:"Nairobi"))
			
			testContact1.save(flush:true)
			testContact2.save(flush:true)
		expect:
			smartGroup.members*.name == ["Charles"]
		when:
			controller.params.smartgroupname = "${smartGroup.name}"
			controller.params.id = "${smartGroup.id}"
			controller.params.'rule-field' = "${CUSTOM_FIELD_ID_PREFIX + customFieldA.name}"
			controller.params.'rule-text' = "ken"
			controller.save()
			smartGroup.refresh()
		then:
			SmartGroup.count() == 1
			SmartGroup.get(smartGroup.id).customFields.size() == 1
			smartGroup.members*.name.sort() == ['Alfred', 'Charles']
	}
	
	def 'calling DELETE should permanently remove a smart group and not its contacts'() {
		given:
			def englishContacts = new SmartGroup(name:'English contacts', mobile:'+44').save(flush:true, failOnError:true)
			createContact('Alfred', '+4423456789')
			createContact('Charles', '+440987654')
		when:
			def members = englishContacts.members
		then:
			members*.name.sort() == ['Alfred', 'Charles'] 
		when:
			controller.params.id = "${englishContacts.id}"
			controller.delete()
		then:
			!SmartGroup.list()
			Contact.list()*.name.containsAll(["Alfred","Charles"])
	}
	
	def 'calling SAVE with multiple rules defined will create a smart group with the set rules'() {
		given:
			controller.params.smartgroupname = 'Londons'
			controller.params.'rule-field' = ['custom:Town', 'custom:Food', 'mobile'] as String[]
			controller.params.'rule-text' = ['London', 'Zucchini', '+789'] as String[]
		when:
			controller.save()
			def smartGroup = SmartGroup.findByName('Londons')
		then:
			smartGroup
			smartGroup.customFields.size() == 2
			smartGroup.mobile == '+789'			
	}
		
	private def createContact(String name, String mobile) {
		new Contact(name:name, mobile:mobile).save(flush:true, failOnError:true)
	}
}
