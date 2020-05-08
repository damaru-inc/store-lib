
package com.damaru.store.messaging;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.solacesystems.jcsmp.BytesMessage;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishEventHandler;
import com.solacesystems.jcsmp.TextMessage;import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageConsumer;
import com.solacesystems.jcsmp.XMLMessageListener;
import com.solacesystems.jcsmp.XMLMessageProducer;

@Component
public class QueryChannel {

	// Channel name: estore/command/query/{originatorId}
	private static final String PUBLISH_TOPIC = "estore/command/query/%s";
	private static final String SUBSCRIBE_TOPIC = "estore/command/query/*";

	@Autowired
	private SolaceSession solaceSession;
	private JCSMPSession jcsmpSession;
	private Serializer<Query> serializer;
	private TextMessage textMessage = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
	private XMLMessageProducer producer;
	private XMLMessageConsumer consumer;

	@PostConstruct
	public void init() throws Exception {
		jcsmpSession = solaceSession.getSession();
		serializer = SerializerFactory.getSerializer("application/json", Query.class);
	}

	public void initPublisher(PublishListener publishListener) throws Exception {
		PublishEventHandler handler = new PublishEventHandler(publishListener);
		producer = jcsmpSession.getMessageProducer(handler);
		textMessage.setDeliveryMode(DeliveryMode.DIRECT);
	}


	public void subscribe(QueryEvent.SubscribeListener listener) throws Exception {
		MessageListener messageListener = new MessageListener(listener);
		consumer = jcsmpSession.getMessageConsumer(messageListener);
		Topic topic = JCSMPFactory.onlyInstance().createTopic(SUBSCRIBE_TOPIC);
		jcsmpSession.addSubscription(topic);
		consumer.start();
	}

	private Topic formatTopic(String originatorId) {
		String topicString = String.format(PUBLISH_TOPIC, originatorId);
		Topic topic = JCSMPFactory.onlyInstance().createTopic(topicString);
		return topic;
	}

	public void sendQueryEvent(QueryEvent queryEvent, String originatorId) throws Exception {
		Topic topic = formatTopic(originatorId);
		Query payload = queryEvent.getPayload();
		String payloadString = serializer.serialize(payload);
		textMessage.setText(payloadString);
		producer.send(textMessage, topic);
	}
   
	public void sendQuery(Query query, String originatorId) throws Exception {
		Topic topic = formatTopic(originatorId);
		String payloadString = serializer.serialize(query);
		textMessage.setText(payloadString);
		producer.send(textMessage, topic);
	}

	public void close() {

		if (consumer != null) {
			consumer.close();		
		}

		solaceSession.close();
	}


	class MessageListener implements XMLMessageListener {

		QueryEvent.SubscribeListener listener;
		
		public MessageListener(QueryEvent.SubscribeListener listener) {
			this.listener = listener;
		}
		
		@Override
		public void onException(JCSMPException exception) {
			listener.handleException(exception);
		}

		@Override
		public void onReceive(BytesXMLMessage bytesXMLMessage) {
			String text = null;

			if (bytesXMLMessage instanceof  TextMessage) {
				TextMessage textMessage = (TextMessage) bytesXMLMessage;
				text = textMessage.getText();
			} else if (bytesXMLMessage instanceof BytesMessage) {
				text = new String(((BytesMessage) bytesXMLMessage).getData());
			}

			Query payload;

			try {
				payload = serializer.deserialize(text);
				QueryEvent  queryEvent = new QueryEvent();
				queryEvent.setMessageId(bytesXMLMessage.getMessageId());
				queryEvent.setPayload(payload);
				queryEvent.setTopic(bytesXMLMessage	.getDestination().getName());
				listener.onReceive(queryEvent);
			} catch (Exception exception) {
				listener.handleException(exception);
			}			
		}
	}

	class PublishEventHandler implements JCSMPStreamingPublishEventHandler {
		
		PublishListener listener;
		
		public PublishEventHandler(PublishListener listener) {
			this.listener = listener;
		}

		@Override
		public void handleError(String messageId, JCSMPException exception, long timestamp) {
			listener.handleException(messageId, exception, timestamp);
		}

		@Override
		public void responseReceived(String messageId) {
			listener.onResponse(messageId);
		}
	}

}
