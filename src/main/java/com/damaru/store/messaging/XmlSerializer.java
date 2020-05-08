
package com.damaru.store.messaging;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class XmlSerializer<T> extends JacksonSerializer<T> {
	
	public XmlSerializer(Class<T> objectClass) {
		super(objectClass, new XmlMapper());
	}

}
