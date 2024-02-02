package org.tkit.onecx.workspace.domain.di;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;

import java.util.ArrayList;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.tkit.quarkus.dataimport.DataImport;
import org.tkit.quarkus.dataimport.DataImportConfig;
import org.tkit.quarkus.dataimport.DataImportService;

import com.fasterxml.jackson.databind.ObjectMapper;

import gen.org.tkit.onecx.workspace.di.workspace.v1.model.ImportRequestDTOV1;
import gen.org.tkit.onecx.workspace.di.workspace.v1.model.WorkspaceDataImportDTOV1;
import gen.org.tkit.onecx.workspace.di.workspace.v1.model.WorkspaceImportDTOV1;

@DataImport("workspace")
public class WorkspaceDataImportService implements DataImportService {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    WorkspaceImportService importService;

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void importData(DataImportConfig config) {
        try {
            var operation = config.getMetadata().getOrDefault("operation", "NONE");
            if ("NONE".equals(operation)) {
                return;
            }
            if ("CLEAN_INSERT".equals(operation)) {
                var data = objectMapper.readValue(config.getData(), WorkspaceDataImportDTOV1.class);
                cleanInsert(data);
            }
        } catch (Exception ex) {
            throw new ImportException(ex.getMessage(), ex);
        }
    }

    public void cleanInsert(WorkspaceDataImportDTOV1 data) {
        if (data == null || data.getRequests() == null) {
            return;
        }
        var tenantIds = data.getRequests().stream().map(ImportRequestDTOV1::getWorkspace)
                .collect(groupingBy(WorkspaceImportDTOV1::getTenantId, toCollection(ArrayList::new))).keySet();

        for (var tenantId : tenantIds) {
            try {
                importService.deleteAll(tenantId);
            } catch (Exception ex) {
                throw new ImportException("Error deleting data from tenant " + tenantId, ex);
            }
        }

        // import portals
        importRequests(data);
    }

    public void importRequests(WorkspaceDataImportDTOV1 data) {
        for (var request : data.getRequests()) {
            try {
                importService.importRequest(request);
            } catch (Exception ex) {
                throw new ImportException("Error import portal " + request.getWorkspace().getName(), ex);
            }
        }
    }

    public static class ImportException extends RuntimeException {

        public ImportException(String message, Throwable ex) {
            super(message, ex);
        }
    }
}
