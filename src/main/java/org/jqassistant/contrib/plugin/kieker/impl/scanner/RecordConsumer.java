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

// todo: use this instead of ListCollectionFilter
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
    public void newMonitoringRecord(final IMonitoringRecord iMonitoringRecord) {
        if (iMonitoringRecord instanceof KiekerMetadataRecord) {
            kiekerHelper.createRecord((KiekerMetadataRecord) iMonitoringRecord);
        } else if (iMonitoringRecord instanceof TraceMetadata) {
            kiekerHelper.createTrace((TraceMetadata) iMonitoringRecord);
        } else if (iMonitoringRecord instanceof CPUUtilizationRecord) {
            kiekerHelper.createCpuUtilizationMeasurement((CPUUtilizationRecord) iMonitoringRecord);
        } else if (iMonitoringRecord instanceof DiskUsageRecord) {
            kiekerHelper.createDiskUsageMeasurement((DiskUsageRecord) iMonitoringRecord);
        } else if (iMonitoringRecord instanceof LoadAverageRecord) {
            kiekerHelper.createLoadAverageMeasurement((LoadAverageRecord) iMonitoringRecord);
        } else if (iMonitoringRecord instanceof MemSwapUsageRecord) {
            kiekerHelper.createMemSwapUsageMeasurement((MemSwapUsageRecord) iMonitoringRecord);
        } else if (iMonitoringRecord instanceof NetworkUtilizationRecord) {
            kiekerHelper.createNetworkUtilizationMeasurement((NetworkUtilizationRecord) iMonitoringRecord);
        } else if (iMonitoringRecord instanceof AbstractOperationEvent) {
            kiekerHelper.createEvent((AbstractOperationEvent) iMonitoringRecord);
        }
    }

    @Override
    public Configuration getCurrentConfiguration() {
        return new Configuration();
    }
}
