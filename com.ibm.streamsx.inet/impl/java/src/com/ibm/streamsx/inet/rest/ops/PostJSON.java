/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2011, 2014 
*/
package com.ibm.streamsx.inet.rest.ops;

import com.ibm.streams.operator.Attribute;
import com.ibm.streams.operator.OperatorContext.ContextCheck;
import com.ibm.streams.operator.StreamSchema;
import com.ibm.streams.operator.StreamingOutput;
import com.ibm.streams.operator.Type.MetaType;
import com.ibm.streams.operator.compile.OperatorContextChecker;
import com.ibm.streams.operator.model.OutputPortSet;
import com.ibm.streams.operator.model.OutputPorts;
import com.ibm.streams.operator.model.Parameter;
import com.ibm.streams.operator.model.PrimitiveOperator;
import com.ibm.streams.operator.model.Icons;

@PrimitiveOperator(name="HTTPJSONInjection", description=PostJSON.DESC)
@OutputPorts({@OutputPortSet(cardinality=1,
description="Emits a tuple for each POST request on the inject URL with port index 0"),
@OutputPortSet(optional=true,
description="Optional additional ports that emit a tuple for each POST request on the inject URL with the corresponding port index")})
@Icons(location32="impl/java/icons/HTTPTupleInjection_32.gif", location16="impl/java/icons/HTTPTupleInjection_16.gif")
public class PostJSON extends ServletOperator {
	
	/**
	 * Verify the either the schema contains an attribute of type
	 * rstring jsonString, or the first attribute is an rstring  for each output port.
	 */
	@ContextCheck
	public static void checkJSONAttribute(OperatorContextChecker checker) {	
		for (StreamingOutput<?> port : checker.getOperatorContext()
				.getStreamingOutputs()) {
			
			StreamSchema schema = port.getStreamSchema();
			Attribute jsonString = schema.getAttribute("jsonString");
			
			if (jsonString != null) {
				checker.checkAttributeType(jsonString, MetaType.RSTRING);
				continue;
			}
							
			Attribute first = port.getStreamSchema().getAttribute(0);
			checker.checkAttributeType(first, MetaType.RSTRING);
		}
	}
	
	static final String DESC =
			"Embeds a Jetty web server to allow HTTP POST requests with mime type `application/json` to submit a tuple on " + 
			"its output ports. Each output port corresponds to a unique URL comprising the operator name " + 
			"and the port index.\\n" + 
			"\\n" + 
			"A single tuple is generated for an incoming POST request. The incoming JSON value is assigned to the `rstring jsonString` attribute if it exists," +
			"otherwise to the first attribute, which must be of SPL type `rstring` in the output port's " + 
			"schema corresponds to the contents of the POST requiring content-type `application/json`. " + 
			"Any other attributes will be set to their default value.\\n" + 
			"\\n" +
			"The URLs defined by this operator are:\\n" +
			"* *prefix*`/ports/output/`*port index*`/inject` - Accepts POST requests of type `application/xml`.\\n" +
			"* *prefix*`/ports/output/`*port index*`/info` - Output port meta-data including the stream attribute names and types (content type `application/json`).\\n" +
			"\\nThe *prefix* for the URLs is:\\n" +
			"* *context path*`/`*base operator name* - When the `context` parameter is set.\\n" +
			"* *full operator name* - When the `context` parameter is **not** set.\\n" +
			"\\n" + 
			"**Maximum Content Size**:\\n" +
			"Jetty limits the amount of data that can post back from a browser " +
			"or other client to this operator. This helps protect the operator against " +
			"denial of service attacks by malicious clients sending huge amounts of data. " +
			"The default limit is 200K bytes, a client will receive an HTTP 500 error response code if it " +
			"tries to POST too much data. The limit may be increased using the `maxContentSize` parameter.\\n" +
			"\\n" +
			"**Limitations**:\\n" + 
			"* Error handling is limited, incorrect URLs can crash the application.\\n" + 
			"* No security access is provided to the data. This is mainly aimed at demos.";

	/*
	 * The ServletEngine accesses all parameters through the operator
	 * context, as that is an object that is not specific to each
	 * operator's class loader.
	 */
	@Parameter(optional=true, description=PostTuple.MAX_CONTEXT_SIZE_DESC)
	public void setMaxContentSize(int maxContentSize) {}	
}
