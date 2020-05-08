
package com.damaru.store.messaging;
public interface Serializer<T> {
	public String serialize(T object) throws Exception;
	public T deserialize(String string) throws Exception;
}
