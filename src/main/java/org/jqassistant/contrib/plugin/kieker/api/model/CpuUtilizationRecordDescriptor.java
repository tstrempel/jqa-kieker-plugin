package org.jqassistant.contrib.plugin.kieker.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("CpuUtilizationRecord")
public interface CpuUtilizationRecordDescriptor extends NamedDescriptor, SystemPerformanceRecordDescriptor {

    void setCpuID(String cpuID);

    String getCpuID();

    void setIdle(double idle);

    double getIdle();

    void setIrq(double irq);

    double getIrq();

    void setNice(double nice);

    double getNice();

    void setSystem(double system);

    double getSystem();

    void setTotalUtilization(double totalUtilization);

    double getTotalUtilization();

    void setWait(double wait);

    double getWait();
}
