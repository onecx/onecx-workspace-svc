package org.tkit.onecx.workspace.domain.template.services;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.tkit.onecx.workspace.domain.models.*;
import org.tkit.onecx.workspace.domain.services.WorkspaceService;
import org.tkit.onecx.workspace.domain.template.mappers.CreateTemplateMapper;
import org.tkit.onecx.workspace.domain.template.models.CreateTemplateConfig;
import org.tkit.onecx.workspace.domain.template.models.WorkspaceCreateTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import gen.org.tkit.onecx.workspace.template.create.model.WorkspaceCreateTemplateDTO;

@ApplicationScoped
public class CreateTemplateService {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    CreateTemplateConfig config;

    @Inject
    CreateTemplateMapper mapper;

    public WorkspaceCreateTemplate createTemplate(Workspace workspace) {
        if (!config.enabled()) {
            return null;
        }

        var dto = loadTemplate();
        var slots = mapper.createSlots(dto.getSlots(), workspace);
        var products = mapper.createProducts(dto.getProducts(), workspace);
        var roles = mapper.createRoles(dto.getRoles(), workspace);
        List<MenuItem> menus = new ArrayList<>();
        var menuMap = mapper.recursiveMappingTreeStructure(dto.getMenuItems(), workspace, null, menus);

        List<Assignment> assignments = mapper.createAssignments(workspace, roles, menus, menuMap);

        // update role name base on the role mapping configuration.
        roles.forEach(role -> role.setName(config.roleMapping().getOrDefault(role.getName(), role.getName())));

        return new WorkspaceCreateTemplate(roles, slots, products, assignments, menus);
    }

    private WorkspaceCreateTemplateDTO loadTemplate() {
        try {
            if (config.classPathResource()) {
                var url = WorkspaceService.class.getClassLoader().getResource(config.resource());
                if (url == null) {
                    throw new FileNotFoundException(
                            "Workspace template class-path resource does not found. resource: " + config.resource());
                }
                try (InputStream in = WorkspaceService.class.getClassLoader()
                        .getResourceAsStream(config.resource())) {
                    return objectMapper.readValue(in, WorkspaceCreateTemplateDTO.class);
                }
            } else {
                var path = Paths.get(config.resource());
                if (!Files.exists(path)) {
                    throw new FileNotFoundException("Workspace template file does not found. resource: " + config.resource());
                }
                return objectMapper.readValue(path.toFile(), WorkspaceCreateTemplateDTO.class);
            }
        } catch (Exception ex) {
            throw new TemplateException(ex);
        }
    }

    public static class TemplateException extends RuntimeException {

        public TemplateException(Throwable ex) {
            super("Error parsing create workspace template", ex);
        }
    }
}
