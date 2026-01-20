package org.tkit.onecx.workspace.rs.common.mappers;

import java.util.HashMap;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.tkit.onecx.workspace.domain.models.WorkspaceTranslationKey;

@Mapper
public class WorkspaceTranslationKeyMapper {

    @Named("toEntityI18n")
    public Map<WorkspaceTranslationKey, String> toEntityI18n(Map<String, Map<String, String>> apiModel) {
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

    @Named("toDtoI18n")
    public Map<String, Map<String, String>> toDtoI18n(Map<WorkspaceTranslationKey, String> entity) {
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
