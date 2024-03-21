package org.tkit.onecx.workspace.rs.external.v1.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.tkit.onecx.workspace.domain.daos.MenuItemDAO;
import org.tkit.onecx.workspace.rs.external.v1.mappers.MenuItemMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.workspace.rs.external.v1.MenuExternalV1Api;
import gen.org.tkit.onecx.workspace.rs.external.v1.model.MenuItemSearchCriteriaDTOV1;
import gen.org.tkit.onecx.workspace.rs.external.v1.model.MenuStructureSearchCriteriaDTOV1;

@LogService
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class MenuExternalV1RestController implements MenuExternalV1Api {
    @Inject
    MenuItemMapper mapper;
    @Inject
    MenuItemDAO dao;

    @Override
    public Response getMenuStructureV1(MenuStructureSearchCriteriaDTOV1 menuStructureSearchCriteriaDTOV1) {
        var criteria = mapper.map(menuStructureSearchCriteriaDTOV1);
        var result = dao.loadAllMenuItemsByCriteria(criteria);
        return Response.ok(mapper.mapTree(result)).build();
    }

    @Override
    public Response searchMenuItemsByCriteriaV1(MenuItemSearchCriteriaDTOV1 menuItemSearchCriteriaDTO) {
        var criteria = mapper.map(menuItemSearchCriteriaDTO);
        var result = dao.findByCriteria(criteria);
        return Response.ok(mapper.mapPage(result)).build();
    }
}
