package frontlinesms2

import grails.converters.JSON

import org.apache.camel.Exchange

import frontlinesms2.api.*

class SmssyncService {
	def processSend(Exchange x) {
		println "SmssyncService.processSend() :: ENTRY"
		println "SmssyncService.processSend() :: x=$x"
		println "SmssyncService.processSend() :: x.in.body=$x.in.body"
		println "SmssyncService.processSend() :: x.in.headers=${x.in?.headers}"
		def connection = SmssyncFconnection.get(x.in.headers['fconnection-id'])
		connection.addToQueuedDispatches(x.in.body)
		connection.save(failOnError:true)
		println "SmssyncService.processSend() :: EXIT"
	}

	def apiProcess(connection, controller) {
		controller.render(generateApiResponse(connection, controller) as JSON)
	}

	def generateApiResponse(connection, controller) {
		if(connection.secret && controller.params.secret != connection.secret) return failure()

		try {
			def payload = controller.params.task=='send'? handlePollForOutgoing(connection): handleIncoming(connection, controller.params)
			if(connection.secret) payload = [secret:connection.secret] + payload
			return [payload:payload]
		} catch(FrontlineApiException ex) {
// FIXME should send a non-200 status code here
			return failure(ex)
		}
	}

	private def handleIncoming(connection, params) {
		if(!connection.receiveEnabled) throw new FrontlineApiException("Receive not enabled for this connection")

		if(!params.from || params.message==null) throw new FrontlineApiException('Missing one or both of `from` and `message` parameters');

		/* parse received JSON with the following params:
		    from -- the number that sent the SMS
		    message -- the SMS sent
		    message_id -- the unique ID of the SMS
		    sent_to -- the phone number the SMS was sent to
		    sent_timestamp -- the timestamp the SMS was sent. In the UNIX timestamp format */
		sendMessageAndHeaders('seda:incoming-fmessages-to-store',
				new Fmessage(inbound:true, src:params.from, text:params.message),
				['fconnection-id':connection.id])

		def response = success()
		if(connection.sendEnabled) response += generateOutgoingResponse(connection, false)
		return response
	}

	private def handlePollForOutgoing(connection) {
		if(!connection.sendEnabled) throw new FrontlineApiException("Send not enabled for this connection")

		return success(generateOutgoingResponse(connection, true))
	}

	private def generateOutgoingResponse(connection, boolean includeWhenEmpty) {
		def responseMap = [:]

		def q = connection.queuedDispatches
		if(q || includeWhenEmpty) {
			responseMap.task = 'send'

			connection.removeDispatchesFromQueue(q)

			responseMap.messages = q.collect { d ->
				d.status = DispatchStatus.SENT
				d.dateSent = new Date()
				d.save(failOnError: true)
				[to:d.dst, message:d.text]
			}
		}
		return responseMap
	}

	private def failure(FrontlineApiException ex=null) {
		if(ex) {
			return [payload:[success:'false', error:ex.message]]
		} else {
			return [payload:[success:'false']]
		}
	}

	private def success(additionalContent=null) {
		def responseMap = [success:'true']
		if(additionalContent) responseMap += additionalContent
		return responseMap
	}
}

