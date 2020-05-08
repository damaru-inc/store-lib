
package com.damaru.store.messaging;

import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class Query {

	public Query () {
	}
	public Query (
		String entityType, 
		String eventType) {
		this.entityType = entityType;
		this.eventType = eventType;
	}


	private String entityType;
	private String eventType;

	public String getEntityType() {
		return entityType;
	}

	public Query setEntityType(String entityType) {
		this.entityType = entityType;
		return this;
	}


	public String getEventType() {
		return eventType;
	}

	public Query setEventType(String eventType) {
		this.eventType = eventType;
		return this;
	}


	public String toString() {
		return "Query ["
		+ " entityType: " + entityType
		+ " eventType: " + eventType
		+ " ]";
	}
}

