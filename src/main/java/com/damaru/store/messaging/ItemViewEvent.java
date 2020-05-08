
package com.damaru.store.messaging;
import java.util.HashMap;

public class ItemViewEvent { 


	// topic and messageId: These fields allow the client to see the topic
	// and messageId of a received messages. It is not necessary to set these 
	// when publishing.

	private String topic;

	public String getTopic() {
		return topic;
	}

	public ItemViewEvent setTopic(String topic) {
		this.topic = topic;
		return this;
	}

	private String messageId;

	public String getMessageId() {
		return messageId;
	}

	public ItemViewEvent setMessageId(String messageId) {
		this.messageId = messageId;
		return this;
	}

	// Headers with their getters and setters.
	private HashMap<String, Object> headers = new HashMap<>();

	// Payload


	private ItemViewArray itemViewArray;

	public ItemViewArray getPayload() {
		return itemViewArray;
	}

	public ItemViewEvent setPayload(ItemViewArray itemViewArray) {
		this.itemViewArray = itemViewArray;
		return this;
	}

	// Listeners

	public interface SubscribeListener {
		public void onReceive(ItemViewEvent itemViewEvent);
		public void handleException(Exception exception);
	}
}
