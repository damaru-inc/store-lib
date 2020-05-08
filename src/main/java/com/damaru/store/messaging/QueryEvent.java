
package com.damaru.store.messaging;
import java.util.HashMap;

public class QueryEvent { 


	// topic and messageId: These fields allow the client to see the topic
	// and messageId of a received messages. It is not necessary to set these 
	// when publishing.

	private String topic;

	public String getTopic() {
		return topic;
	}

	public QueryEvent setTopic(String topic) {
		this.topic = topic;
		return this;
	}

	private String messageId;

	public String getMessageId() {
		return messageId;
	}

	public QueryEvent setMessageId(String messageId) {
		this.messageId = messageId;
		return this;
	}

	// Headers with their getters and setters.
	private HashMap<String, Object> headers = new HashMap<>();

	// Payload


	private Query query;

	public Query getPayload() {
		return query;
	}

	public QueryEvent setPayload(Query query) {
		this.query = query;
		return this;
	}

	// Listeners

	public interface SubscribeListener {
		public void onReceive(QueryEvent queryEvent);
		public void handleException(Exception exception);
	}
}
