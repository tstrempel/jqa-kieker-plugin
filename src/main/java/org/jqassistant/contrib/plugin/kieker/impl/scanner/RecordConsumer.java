package org.jqassistant.contrib.plugin.kieker.impl.scanner;

import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.common.configuration.Configuration;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.flow.trace.TraceMetadata;
import kieker.common.record.flow.trace.operation.AbstractOperationEvent;
import kieker.common.record.misc.KiekerMetadataRecord;
import kieker.common.record.system.*;

@Plugin
class RecordConsumer extends AbstractFilterPlugin {

    public static final String INPUT_PORT_NAME = "newMonitoringRecord";
    KiekerHelper kiekerHelper;

    public RecordConsumer(Configuration configuration, IProjectContext projectContext, KiekerHelper kiekerHelper) {
        super(configuration, projectContext);
        this.kiekerHelper = kiekerHelper;
    }

    @InputPort(
        name = RecordConsumer.INPUT_PORT_NAME,
        eventTypes = { IMonitoringRecord.class }
    )
    public void newMonitoringRecord(Object iMonitoringRecord) {
        if (iMonitoringRecord instanceof KiekerMetadataRecord) {
            kiekerHelper.createRecord((KiekerMetadataRecord) iMonitoringRecord);
        } else if (iMonitoringRecord instanceof TraceMetadata) {

        } else if (iMonitoringRecord instanceof CPUUtilizationRecord) {
            kiekerHelper.createCpuUtilizationMeasurement((CPUUtilizationRecord) iMonitoringRecord);
        } else if (iMonitoringRecord instanceof DiskUsageRecord) {

        } else if (iMonitoringRecord instanceof LoadAverageRecord) {

        } else if (iMonitoringRecord instanceof MemSwapUsageRecord) {

        } else if (iMonitoringRecord instanceof NetworkUtilizationRecord) {

        } else if (iMonitoringRecord instanceof AbstractOperationEvent) {

        }

    }

    @Override
    public Configuration getCurrentConfiguration() {
        return new Configuration();
    }
}
