
package com.damaru.store.messaging;
import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.nio.ByteBuffer;

@Component
public class ItemViewChannel {

	public static final Logger log = LoggerFactory.getLogger(ItemViewChannel.class);

	// Channel name: estore/data/queryResponse/{originatorId}
	private static final String PUBLISH_TOPIC = "estore/data/queryResponse/%s";
	private static final String SUBSCRIBE_TOPIC = "estore/data/queryResponse/*";

	@Autowired
	private SolaceSession solaceSession;
	private JCSMPSession jcsmpSession;
	private Serializer<ItemViewArray> serializer;
	private TextMessage textMessage = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
	private BytesXMLMessage bytesMessage = JCSMPFactory.onlyInstance().createMessage(BytesXMLMessage.class);
	private XMLMessageProducer producer;
	private XMLMessageConsumer consumer;

	@PostConstruct
	public void init() throws Exception {
		jcsmpSession = solaceSession.getSession();
		serializer = SerializerFactory.getSerializer("application/json", ItemViewArray.class);
	}

	public void initPublisher(PublishListener publishListener) throws Exception {
		PublishEventHandler handler = new PublishEventHandler(publishListener);
		producer = jcsmpSession.getMessageProducer(handler);
		textMessage.setDeliveryMode(DeliveryMode.DIRECT);
		bytesMessage.setDeliveryMode(DeliveryMode.DIRECT);
	}


	public void subscribe(ItemViewEvent.SubscribeListener listener) throws Exception {
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

	public void sendItemViewEvent(ItemViewEvent itemViewEvent, String originatorId) throws Exception {
		Topic topic = formatTopic(originatorId);
		ItemViewArray payload = itemViewEvent.getPayload();
		String payloadString = serializer.serialize(payload);
		log.info("Sending {}", payloadString);
		textMessage.setText(payloadString);
		producer.send(textMessage, topic);
	}
   
	public void sendItemViewArray(ItemViewArray itemViewArray, String originatorId) throws Exception {
		Topic topic = formatTopic(originatorId);
		String payloadString = serializer.serialize(itemViewArray);
		payloadString = "{\"itemView\":[{\"price\":23.45,\"description\":\"Komodo Dragon\",\"category\":\"coffee\"}]}";
		log.info("Sending array {}", payloadString);
		textMessage.writeAttachment(payloadString.getBytes());
		producer.send(textMessage, topic);
	}

	public void close() {

		if (consumer != null) {
			consumer.close();		
		}

		solaceSession.close();
	}


	class MessageListener implements XMLMessageListener {

		ItemViewEvent.SubscribeListener listener;
		
		public MessageListener(ItemViewEvent.SubscribeListener listener) {
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
				if (text == null) {
					ByteBuffer buffer = textMessage.getAttachmentByteBuffer();
					text = new String(buffer.array());
				}
			} else if (bytesXMLMessage instanceof BytesMessage) {
				text = new String(((BytesMessage) bytesXMLMessage).getData());
			}

			ItemViewArray payload;

			try {
				payload = serializer.deserialize(text);
				ItemViewEvent  itemViewEvent = new ItemViewEvent();
				itemViewEvent.setMessageId(bytesXMLMessage.getMessageId());
				itemViewEvent.setPayload(payload);
				itemViewEvent.setTopic(bytesXMLMessage	.getDestination().getName());
				listener.onReceive(itemViewEvent);
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
