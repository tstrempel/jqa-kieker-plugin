package org.jqassistant.contrib.plugin.kieker.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.DirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractDirectoryScannerPlugin;
import kieker.analysis.AnalysisController;
import kieker.analysis.IAnalysisController;
import kieker.analysis.IProjectContext;
import kieker.analysis.exception.AnalysisConfigurationException;
import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.analysis.plugin.filter.forward.TeeFilter;
import kieker.analysis.plugin.reader.filesystem.FSReader;
import kieker.analysisteetime.plugin.reader.filesystem.fsReader.FSDirectoryReader;
import kieker.common.configuration.Configuration;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.flow.trace.TraceMetadata;
import kieker.common.record.flow.trace.operation.AbstractOperationEvent;
import kieker.common.record.misc.KiekerMetadataRecord;
import kieker.common.record.system.*;
import kieker.common.util.filesystem.FSUtil;
import org.jqassistant.contrib.plugin.kieker.api.model.RecordDescriptor;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@ScannerPlugin.Requires(DirectoryDescriptor.class)
public class KiekerDirectoryScannerPlugin extends AbstractDirectoryScannerPlugin<RecordDescriptor> {
    private static final Logger LOGGER = LoggerFactory.getLogger(KiekerDirectoryScannerPlugin.class);

    @Override
    public boolean accepts(File item, String path, Scope scope) throws IOException {
        return new File(item.getAbsolutePath() + File.separator + FSUtil.MAP_FILENAME).exists();
    }

    /**
     * Return the scope the plugin expects for execution.
     *
     * @return The scope.
     */
    @Override
    protected Scope getRequiredScope() {
        return DefaultScope.NONE;
    }

    /**
     * Return the descriptor representing the artifact.
     *
     * @param container      The container.
     * @param scannerContext The scanner context.
     * @return The artifact descriptor.
     */
    @Override
    protected RecordDescriptor getContainerDescriptor(File container, ScannerContext scannerContext) {
        LOGGER.info("Escaping unescaped backslashes in paths...");
        File[] files = container.listFiles((dir, name) -> name.endsWith(".dat"));
        try {
            for (File f : files) {
                String content = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
                content = content.replace(":\\;", ":\\\\;");
                Files.write(f.toPath(), content.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("Done");


        LOGGER.info("Kieker plugin scans records in '{}'", container.getAbsolutePath());

        // Get store, and record descriptor from scanner
        final DirectoryDescriptor directoryDescriptor = scannerContext.getCurrentDescriptor();
        final RecordDescriptor recordDescriptor = scannerContext.getStore()
            .addDescriptorType(directoryDescriptor, RecordDescriptor.class);
        final KiekerHelper kiekerHelper = new KiekerHelper(scannerContext, recordDescriptor);

//        // Set record receiver that maps read records to corresponding descriptors
//        KiekerRecordReceiver kiekerRecordReceiver = new KiekerRecordReceiver(new KiekerHelper(scannerContext, recordDescriptor));
//
//        // Set filesystem directory reader (reads *.map, *.dat, *.bin, *.xz files in a directory)
//        // Todo use another reader
//        FSDirectoryReader fsDirectoryReader = new FSDirectoryReader(container, kiekerRecordReceiver, true);
//        fsDirectoryReader.run();


        IAnalysisController analysisController = new AnalysisController();

        Configuration fsReaderConfig = new Configuration();
        fsReaderConfig.setProperty(FSReader.CONFIG_PROPERTY_NAME_INPUTDIRS,
            Paths.get(container.getAbsolutePath()).normalize().toString());
//        final String[] inputDirs = { args[0] };
//        readerConfiguration.setProperty(FSReader.CONFIG_PROPERTY_NAME_INPUTDIRS, Configuration.toProperty(inputDirs));
        FSReader fsReader = new FSReader(fsReaderConfig, analysisController);

        Configuration teeFilterConfig = new Configuration();
        teeFilterConfig.setProperty(TeeFilter.CONFIG_PROPERTY_NAME_STREAM,
            TeeFilter.CONFIG_PROPERTY_VALUE_STREAM_STDOUT);
        TeeFilter teeFilter = new TeeFilter(teeFilterConfig, analysisController);

        final RecordConsumer consumer = new RecordConsumer(new Configuration(), analysisController, kiekerHelper);

        try {
            // analysisController.connect(fsReader, FSReader.OUTPUT_PORT_NAME_RECORDS, teeFilter, TeeFilter.INPUT_PORT_NAME_EVENTS);
            analysisController.connect(fsReader, FSReader.OUTPUT_PORT_NAME_RECORDS, consumer, RecordConsumer.INPUT_PORT_NAME);
            analysisController.run();
        } catch (AnalysisConfigurationException e) {
            e.printStackTrace();
        }


/*
        String[] str = new String[1];
        str[0] = Paths.get(container.getAbsolutePath()).normalize().toString();

        config.setStringArrayProperty("CONFIG_PROPERTY_NAME_INPUTDIRS", str);
        // config.setProperty("CONFIG_PROPERTY_NAME_INPUTDIRS", Paths.get(container.getAbsolutePath()).normalize().toString());
        config.setProperty("CONFIG_PROPERTY_NAME_IGNORE_UNKNOWN_RECORD_TYPES", true);

        LOGGER.info("'{}'", Paths.get(container.getAbsolutePath()).normalize().toString());
        // LOGGER.info("'{}'", config.getPathProperty("CONFIG_PROPERTY_NAME_INPUTDIRS"));
        LOGGER.info("'{}'", config.getStringArrayProperty("CONFIG_PROPERTY_NAME_INPUTDIRS"));





        AnalysisController analysisController = new AnalysisController(config);
        // analysisController.connect(this, );
//        try {
//            analysisController.run();
//        } catch (AnalysisConfigurationException e) {
//            e.printStackTrace();
//        }
        FSReader fsReader = new FSReader(config, analysisController) {
            @Override
            public boolean newMonitoringRecord(IMonitoringRecord iMonitoringRecord) {
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
                return true;
            }
        };

        LOGGER.info("'{}'", fsReader.getCurrentConfiguration().getStringArrayProperty("CONFIG_PROPERTY_NAME_INPUTDIRS"));

        //LOGGER.info("'{}'", config.getPathProperty("CONFIG_PROPERTY_NAME_INPUTDIRS"));
        //LOGGER.info("'{}'", config.getStringProperty("CONFIG_PROPERTY_NAME_INPUTDIRS"));
        LOGGER.info("'{}'", fsReader.getCurrentConfiguration().getBooleanProperty("CONFIG_PROPERTY_NAME_IGNORE_UNKNOWN_RECORD_TYPES"));
        fsReader.read();*/

        return recordDescriptor;
    }

    /**
     * Create a scope depending on the container type, e.g. a JAR file should
     * return classpath scope.
     *
     * @param container           The container.
     * @param containerDescriptor The container descriptor.
     * @param scannerContext
     */
    @Override
    protected void enterContainer(File container, RecordDescriptor containerDescriptor, ScannerContext scannerContext) throws IOException {

    }

    /**
     * Destroy the container dependent scope.
     *
     * @param container           The container.
     * @param containerDescriptor The container descriptor
     * @param scannerContext
     */
    @Override
    protected void leaveContainer(File container, RecordDescriptor containerDescriptor, ScannerContext scannerContext) throws IOException {

    }
}

