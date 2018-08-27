package org.jqassistant.contrib.plugin.kieker.api.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Defines the label for a Record stored in a kieker file. A Record contains
 * traces. It stores those properties: version, controllerName, hostname,
 * experimentId, debugMode(Yes or No), timeOffset and numberOfRecords
 */
@Label("Record")
public interface RecordDescriptor extends KiekerDescriptor, FileDescriptor {

	@Relation("CONTAINS")
	List<TraceDescriptor> getTraces();

	void setLoggingTimestamp(long timestamp);

	long getLoggingTimestamp();

	void setVersion(String version);

	String getVersion();

	void setControllerName(String controllerName);

	String getControllerName();

	void setHostname(String hostname);

	String getHostname();

	void setExperimentId(int experimentId);

	int getExperimentId();

	void setDebugMode(boolean debugMode);

	boolean getDebugMode();

	void setTimeOffset(long timeOffset);

	long getTimeOffset();

	void setTimeUnit(String timeUnit);

	String getTimeUnit();

	void setNumberOfRecords(long numberOfRecords);

	long getNumberOfRecords();
}
