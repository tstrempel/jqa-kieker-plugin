package org.jqassistant.contrib.plugin.kieker.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("LoadAverageRecord")
public interface LoadAverageRecordDescriptor extends NamedDescriptor, SystemPerformanceRecordDescriptor {

    void setLoadAverage15min(double loadAverage15min);

    double getLoadAverage15min();

    void setLoadAverage5min(double loadAverage5min);

    double getLoadAverage5min();

    void setLoadAverage1min(double loadAverage1min);

    double getLoadAverage1min();
}
