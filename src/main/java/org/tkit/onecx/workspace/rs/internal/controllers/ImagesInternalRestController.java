package org.tkit.onecx.workspace.rs.internal.controllers;

import static jakarta.transaction.Transactional.TxType.NOT_SUPPORTED;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.*;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.workspace.domain.daos.ImageDAO;
import org.tkit.onecx.workspace.domain.models.Image;
import org.tkit.onecx.workspace.rs.internal.mappers.ExceptionMapper;
import org.tkit.onecx.workspace.rs.internal.mappers.ImageMapper;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.image.rs.internal.ImagesInternalApi;
import gen.org.tkit.onecx.image.rs.internal.model.RefTypeDTO;
import gen.org.tkit.onecx.workspace.rs.internal.model.ProblemDetailResponseDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@LogService
@ApplicationScoped
@Transactional(value = NOT_SUPPORTED)
public class ImagesInternalRestController implements ImagesInternalApi {

    @Inject
    ExceptionMapper exceptionMapper;

    @Inject
    ImageDAO imageDAO;

    @Context
    UriInfo uriInfo;

    @Context
    HttpHeaders httpHeaders;

    @Inject
    ImageMapper imageMapper;

    @Override
    @Transactional
    public Response getImage(String refId, RefTypeDTO refType) {
        Image image = imageDAO.findByRefIdAndRefType(refId, refType.toString());
        if (image == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(image.getImageData(), image.getMimeType())
                .header(HttpHeaders.CONTENT_LENGTH, image.getLength()).build();
    }

    @Override
    public Response updateImage(String refId, RefTypeDTO refType, byte[] body, Integer contentLength) {

        Image image = imageDAO.findByRefIdAndRefType(refId, refType.toString());
        if (image == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var contentType = httpHeaders.getMediaType();
        contentType = new MediaType(contentType.getType(), contentType.getSubtype());

        image.setLength(contentLength);
        image.setMimeType(contentType.toString());
        image.setImageData(body);

        image = imageDAO.update(image);
        return Response.ok(imageMapper.map(image)).build();
    }

    @Override
    public Response uploadImage(Integer contentLength, String refId, RefTypeDTO refType, byte[] body) {

        var contentType = httpHeaders.getMediaType();
        contentType = new MediaType(contentType.getType(), contentType.getSubtype());
        var image = imageMapper.create(refId, refType.toString(), contentType.toString(), contentLength);
        image.setLength(contentLength);
        image.setImageData(body);
        image = imageDAO.create(image);

        var imageInfoDTO = imageMapper.map(image);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(imageInfoDTO.getId()).build())
                .entity(imageInfoDTO)
                .build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> exception(ConstraintException ex) {
        return exceptionMapper.exception(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

}
