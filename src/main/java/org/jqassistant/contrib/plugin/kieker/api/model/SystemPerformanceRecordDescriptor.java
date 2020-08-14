package org.jqassistant.contrib.plugin.kieker.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("SystemPerformanceRecord")
public interface SystemPerformanceRecordDescriptor extends KiekerDescriptor {

    void setTimestamp(long timestamp);

    long getTimestamp();

    void setHostname(String hostname);

    String getHostname();
}
