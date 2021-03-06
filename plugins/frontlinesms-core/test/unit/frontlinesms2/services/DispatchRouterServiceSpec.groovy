package frontlinesms2.services

import frontlinesms2.*

import spock.lang.*
import grails.test.mixin.*

import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.Message

@TestFor(DispatchRouterService)
@Mock([Dispatch, Fmessage])
class DispatchRouterServiceSpec extends Specification {
	def "should update the dispatch when no route is found"() {
		setup:
			def exchange = Mock(Exchange)
			def camelContext = Mock(CamelContext)
			camelContext.getRoutes()>> []

			def camelMessage = Mock(org.apache.camel.Message)
			exchange.getIn() >> camelMessage
			camelMessage.getBody() >> new Dispatch(dst:"dst", message:new Fmessage())

			service.camelContext = camelContext
		when:
			service.slip(exchange, null, null)
		then:
			RuntimeException ex = thrown()
	}
	
	@Unroll
	def 'slip should return null if previous is set'() {
		given:
			def x = Mock(Exchange)
		expect:
			service.slip(x, previous, null) == null
		where:
			previous << 'seda:out-99'
	}
	
	@Unroll
	def 'slip should route to specified route if fconnection header is set'() {
		given:
			def x = Mock(Exchange)
		expect:
			service.slip(x, null, "$id") == "seda:out-$id"
		where:
			id << [1, 10, 100]
	}
	
	def 'slip should assign messages to routes using round robin'() {
		given:
			mockRoutes(1, 2, 3)
		when:
			def routedTo = (1..5).collect { service.slip(mockExchange(), null, null) }
		then:
			routedTo == [1, 2, 3, 1, 2].collect { "seda:out-$it" }
	}
	
	def 'slip should prioritise internet services over modems'() {
		given:
			mockRoutes(1:'internet', 2:'modem', 3:'internet', 4:'modem')
		when:
			def routedTo = (1..5).collect { service.slip(mockExchange(), null, null) }
		then:
			routedTo == [1, 3, 1, 3, 1].collect { "seda:out-$it" }
	}
	
	private def mockExchange() {
		def x = Mock(Exchange)
		def out = Mock(Message)
		out.headers >> [:]
		x.out >> out
		return x
	}
	
	private def mockRoutes(int...ids) {
		CamelContext c = Mock()
		c.routes >> ids.collect { [[id:"in-$it"], [id:"out-$it"]] }.flatten()
		service.camelContext = c
	}
	
	private def mockRoutes(Map idsAndPrefixes) {
		CamelContext c = Mock()
		c.routes >> idsAndPrefixes.collect { k, v -> [[id:"in-$k"], [id:"out-$v-$k"]] }.flatten()
		service.camelContext = c
	}
}
