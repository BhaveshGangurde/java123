package com.zenith.accountInfo.configurations;

import static com.zenith.accountInfo.commons.Examples.SUCCESSFUL_RESPONSE;
import static org.apache.camel.builder.Builder.simple;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;



@ContextConfiguration
public class CamelRouterTestSupport {

	@Autowired
	private CamelContext camelContext;

	@Autowired
	private ProducerTemplate producerTemplate;

	public static final ObjectMapper payloadObjectMapper = new ObjectMapper();

	static {
		// Add some extra configuration as needed
		payloadObjectMapper.disable(SerializationFeature.INDENT_OUTPUT);
		payloadObjectMapper.setSerializationInclusion(Include.NON_NULL);
	}

	/**
	 * Method to send a message to a proper endpoint
	 * 
	 * @param endpoint String
	 * @param <T>
	 * @param body     T
	 * @param header   Map<String, Object>[]
	 * @return String
	 */
	public <T> String sendMessage(String endpoint, T body, Map<String, Object> headers) {

		Object result = producerTemplate.sendBodyAndHeaders(endpoint, ExchangePattern.InOut, body, headers);

		return camelContext.getTypeConverter().convertTo(String.class, result);
	}

	/**
	 * @return the context
	 */
	public CamelContext getContext() {
		return camelContext;
	}

	/**
	 * @return the producerTemplate
	 */
	public ProducerTemplate getProducerTemplate() {
		return producerTemplate;
	}

	@BeforeEach
	public void testContextStarted() throws Exception {

		AdviceWith.adviceWith(this.getContext(), "accountinfo.zenith.request.CustAcctInquiry", a -> {
			// weaving particular node in
			// the route by id
			a.weaveById("callServiceBack")
					// providing advised (weaved) node replacement
					.replace().setBody(simple(SUCCESSFUL_RESPONSE));
			// adding new (mock) node to the route definition
			a.weaveAddLast().to("mock:callServiceBack");
		});

		this.getContext().start();
	}

}
