package org.tkit.onecx.workspace.rs.admin.v1.mappers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;
import org.tkit.quarkus.log.cdi.LogService;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.rs.admin.v1.model.ProblemDetailInvalidParamDTOAdminV1;
import gen.org.tkit.onecx.workspace.rs.admin.v1.model.ProblemDetailParamDTOAdminV1;
import gen.org.tkit.onecx.workspace.rs.admin.v1.model.ProblemDetailResponseDTOAdminV1;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Mapper(uses = { OffsetDateTimeMapper.class })
public abstract class AdminExceptionMapperV1 {

    public RestResponse<ProblemDetailResponseDTOAdminV1> constraint(ConstraintViolationException ex) {
        var dto = exception(ErrorKeys.CONSTRAINT_VIOLATIONS.name(), ex.getMessage());
        dto.setInvalidParams(createErrorValidationResponse(ex.getConstraintViolations()));
        return RestResponse.status(Response.Status.BAD_REQUEST, dto);
    }

    public RestResponse<ProblemDetailResponseDTOAdminV1> exception(ConstraintException ex) {
        var dto = exception(ex.getMessageKey().name(), ex.getConstraints());
        dto.setParams(map(ex.namedParameters));
        return RestResponse.status(Response.Status.BAD_REQUEST, dto);
    }

    @LogService(log = false)
    public RestResponse<ProblemDetailResponseDTOAdminV1> optimisticLock(OptimisticLockException ex) {
        var dto = exception(ErrorKeys.OPTIMISTIC_LOCK.name(), ex.getMessage());
        return RestResponse.status(Response.Status.BAD_REQUEST, dto);
    }

    @Mapping(target = "removeParamsItem", ignore = true)
    @Mapping(target = "params", ignore = true)
    @Mapping(target = "invalidParams", ignore = true)
    @Mapping(target = "removeInvalidParamsItem", ignore = true)
    public abstract ProblemDetailResponseDTOAdminV1 exception(String errorCode, String detail);

    public List<ProblemDetailParamDTOAdminV1> map(Map<String, Object> params) {
        if (params == null) {
            return List.of();
        }
        return params.entrySet().stream().map(e -> {
            var item = new ProblemDetailParamDTOAdminV1();
            item.setKey(e.getKey());
            if (e.getValue() != null) {
                item.setValue(e.getValue().toString());
            }
            return item;
        }).toList();
    }

    public abstract List<ProblemDetailInvalidParamDTOAdminV1> createErrorValidationResponse(
            Set<ConstraintViolation<?>> constraintViolation);

    @Mapping(target = "name", source = "propertyPath")
    @Mapping(target = "message", source = "message")
    public abstract ProblemDetailInvalidParamDTOAdminV1 createError(ConstraintViolation<?> constraintViolation);

    public String mapPath(Path path) {
        return path.toString();
    }

    public enum ErrorKeys {

        OPTIMISTIC_LOCK,
        CONSTRAINT_VIOLATIONS;
    }
}
