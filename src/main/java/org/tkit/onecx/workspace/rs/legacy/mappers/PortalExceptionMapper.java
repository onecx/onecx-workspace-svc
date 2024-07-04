package org.tkit.onecx.workspace.rs.legacy.mappers;

import java.util.List;

import jakarta.ws.rs.core.Response;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.rs.legacy.model.RestExceptionDTO;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface PortalExceptionMapper {

    default Response create(Exception ex) {
        if (ex instanceof DAOException de) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
                    .entity(exception(de.getMessageKey().name(), ex.getMessage(), de.parameters))
                    .build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .entity(exception("UNDEFINED_ERROR_CODE", ex.getMessage()))
                .build();
    }

    @Mapping(target = "removeParametersItem", ignore = true)
    @Mapping(target = "namedParameters", ignore = true)
    @Mapping(target = "removeNamedParametersItem", ignore = true)
    @Mapping(target = "parameters", ignore = true)
    @Mapping(target = "codeClass", constant = "org.tkit.jee.rs.resources.RestServiceErrorKeys.ERROR_SERVER_UNDEFINED")
    RestExceptionDTO exception(String code, String message);

    @Mapping(target = "removeParametersItem", ignore = true)
    @Mapping(target = "namedParameters", ignore = true)
    @Mapping(target = "removeNamedParametersItem", ignore = true)
    @Mapping(target = "codeClass", constant = "org.tkit.jee.rs.resources.RestServiceErrorKeys.ERROR_SERVER_UNDEFINED")
    RestExceptionDTO exception(String code, String message, List<Object> parameters);
}
