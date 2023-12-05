package io.github.onecx.workspace.rs.external.v1.mappers;

import java.util.List;
import java.util.stream.Stream;

import org.mapstruct.Mapper;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.workspace.rs.external.v1.model.WorkspaceInfoDTOV1;
import gen.io.github.onecx.workspace.rs.external.v1.model.WorkspaceInfoListDTOV1;
import io.github.onecx.workspace.domain.models.WorkspaceInfo;

@Mapper(uses = { OffsetDateTimeMapper.class })
public abstract class WorkspaceMapper {

    public WorkspaceInfoListDTOV1 mapInfoList(Stream<WorkspaceInfo> data) {
        var result = new WorkspaceInfoListDTOV1();
        result.setWorkspaces(mapInfo(data));
        return result;
    }

    public abstract List<WorkspaceInfoDTOV1> mapInfo(Stream<WorkspaceInfo> page);
}
