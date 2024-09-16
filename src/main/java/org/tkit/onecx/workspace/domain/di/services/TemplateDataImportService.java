package org.tkit.onecx.workspace.domain.di.services;

import static java.util.stream.Collectors.toMap;

import java.util.*;

import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkit.onecx.workspace.domain.di.mappers.TemplateMapper;
import org.tkit.onecx.workspace.domain.di.models.TemplateConfig;
import org.tkit.onecx.workspace.domain.models.*;
import org.tkit.quarkus.dataimport.DataImport;
import org.tkit.quarkus.dataimport.DataImportConfig;
import org.tkit.quarkus.dataimport.DataImportService;

import com.fasterxml.jackson.databind.ObjectMapper;

import gen.org.tkit.onecx.workspace.template.di.model.TemplateImportDI;
import gen.org.tkit.onecx.workspace.template.di.model.TemplateWorkspaceDI;

@DataImport("template")
public class TemplateDataImportService implements DataImportService {

    private static final Logger log = LoggerFactory.getLogger(TemplateDataImportService.class);

    @Inject
    ObjectMapper objectMapper;

    @Inject
    TemplateConfig templateConfig;

    @Inject
    WorkspaceTemplateService service;

    @Inject
    TemplateMapper mapper;

    @Override
    public void importData(DataImportConfig config) {
        log.info("Import template workspace from configuration {}", config);
        try {

            List<String> tenants = templateConfig.config().tenants();
            TemplateImportDI data = objectMapper.readValue(config.getData(), TemplateImportDI.class);
            importWorkspaces(tenants, data.getWorkspaces());

        } catch (Exception ex) {
            throw new TemplateException(ex.getMessage(), ex);
        }
    }

    private void importWorkspaces(List<String> tenants, List<TemplateWorkspaceDI> dto) {

        log.info("Starting import workspaces for tenants: {}", tenants);
        if (dto.isEmpty()) {
            return;
        }

        var workspaceMap = dto.stream().collect(toMap(TemplateWorkspaceDI::getName, x -> x));
        var workspaceNames = workspaceMap.keySet();

        for (String tenantId : tenants) {

            // check tenant existing workspaces
            var tmp = new HashSet<>(workspaceNames);
            var existingWorkspaceNames = service.filterWorkspaceNames(tenantId, tmp);

            // remove existing workspaces from the import list
            existingWorkspaceNames.forEach(tmp::remove);

            // create missing workspaces
            List<Workspace> workspaces = new ArrayList<>();
            List<MenuItem> menuItems = new ArrayList<>();
            List<Assignment> assignments = new ArrayList<>();
            List<Product> products = new ArrayList<>();
            List<Slot> slots = new ArrayList<>();
            for (String name : tmp) {
                var item = workspaceMap.get(name);

                // create workspace
                var workspace = mapper.createWorkspace(item);
                workspaces.add(workspace);

                // create workspace menu items
                List<MenuItem> items = new ArrayList<>();
                Map<String, Set<String>> menuRoles = mapper.recursiveMappingTreeStructure(item.getMenuItems(), workspace, null,
                        items);

                // create assignments of menus and roles
                assignments.addAll(mapper.createAssignments(workspace.getRoles(), items, menuRoles));
                menuItems.addAll(items);

                // update role name base on the role mapping configuration.
                workspace.getRoles().forEach(
                        role -> role
                                .setName(templateConfig.config().roleMapping().getOrDefault(role.getName(), role.getName())));

                products.addAll(workspace.getProducts());
                slots.addAll(workspace.getSlots());
            }

            // create data in database for tenantId
            service.createWorkspaces(tenantId, workspaces, menuItems, assignments, products, slots);
        }
    }

    public static class TemplateException extends RuntimeException {

        public TemplateException(String message, Throwable ex) {
            super(message, ex);
        }
    }
}
