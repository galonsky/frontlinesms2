package frontlinesms2

import grails.test.mixin.*
import spock.lang.*
import frontlinesms2.*
import frontlinesms2.camel.*
import org.apache.camel.Exchange
import org.apache.camel.Message

@TestFor(GenericWebconnection)
@Mock([Keyword])
class GenericWebconnectionSpec extends CamelUnitSpecification {
	private static final String TEST_NUMBER = "+2345678"
	def setup() {
		Webconnection.metaClass.static.findAllByNameIlike = { name -> GenericWebconnection.findAll().findAll { it.name == name } }
	}

	@Unroll
	def "Test constraints"() {
		when:
			def extComm = new GenericWebconnection(name:name, url:"http://www.frontlinesms.com/sync",httpMethod:Webconnection.HttpMethod.GET)
		then:
			extComm.validate() == valid
		where:
			name   | valid
			'test' | true
			'test' | true
			''     | false
			null   | false
	}

	def 'apiProcess should pass call to service'() {
		given:
			WebconnectionService s = Mock()
			def c = new GenericWebconnection()
			c.webconnectionService = s
			def controller = [:]
		when:
			c.apiProcess(controller)
		then:
			1 * s.apiProcess(c, controller)
	}
}

