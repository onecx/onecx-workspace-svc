package org.tkit.onecx.workspace.rs.internal.mappers;

import java.util.HashMap;
import java.util.Map;

import org.mapstruct.*;
import org.tkit.onecx.workspace.domain.criteria.WorkspaceSearchCriteria;
import org.tkit.onecx.workspace.domain.models.Workspace;
import org.tkit.onecx.workspace.domain.models.WorkspaceTranslationKey;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.rs.internal.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface WorkspaceMapper {

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "slots", ignore = true)
    @Mapping(target = "operator", ignore = true)
    Workspace create(CreateWorkspaceRequestDTO dto);

    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "modificationCount", source = "modificationCount")
    @Mapping(target = "slots", ignore = true)
    @Mapping(target = "operator", ignore = true)
    void update(UpdateWorkspaceRequestDTO dto, @MappingTarget Workspace workspace);

    @Mapping(target = "baseUrl", ignore = true)
    @Mapping(target = "names", ignore = true)
    WorkspaceSearchCriteria map(WorkspaceSearchCriteriaDTO dto);

    @Mapping(target = "removeStreamItem", ignore = true)
    WorkspacePageResultDTO mapPageResult(PageResult<Workspace> page);

    @Mapping(target = "removeI18nItem", ignore = true)
    WorkspaceDTO map(Workspace data);

    default Map<WorkspaceTranslationKey, String> mapToEntityI18n(Map<String, Map<String, String>> apiModel) {
        if (apiModel == null || apiModel.isEmpty()) {
            return new HashMap<>();
        }

        Map<WorkspaceTranslationKey, String> result = new HashMap<>();

        apiModel.forEach((language, fieldKeyI18nMap) -> {
            if (fieldKeyI18nMap != null) {
                fieldKeyI18nMap.forEach((fieldKey, i18n) -> {
                    if (fieldKey != null && i18n != null) {
                        WorkspaceTranslationKey workspaceTranslationKey = new WorkspaceTranslationKey();
                        workspaceTranslationKey.setLanguage(language);
                        workspaceTranslationKey.setFieldKey(fieldKey);

                        result.put(workspaceTranslationKey, i18n);
                    }
                });
            }
        });

        return result;
    }

    default Map<String, Map<String, String>> mapToDtoI18n(Map<WorkspaceTranslationKey, String> entity) {
        if (entity == null || entity.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Map<String, String>> result = new HashMap<>();

        entity.forEach(((workspaceTranslationKey, i18n) -> {
            if (workspaceTranslationKey != null) {
                String language = workspaceTranslationKey.getLanguage();
                String fieldKey = workspaceTranslationKey.getFieldKey();
                if (language != null && fieldKey != null) {
                    result.computeIfAbsent(language, l -> new HashMap<>()).put(fieldKey, i18n);
                }
            }
        }));

        return result;
    }
}
