package org.tkit.onecx.workspace.domain.daos;

import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.onecx.workspace.domain.models.Microfrontend;
import org.tkit.onecx.workspace.domain.models.Microfrontend_;
import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.exceptions.DAOException;

@ApplicationScoped
public class MicrofrontendDAO extends AbstractDAO<Microfrontend> {

    public List<Microfrontend> findByProductNames(Set<String> productNames) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Microfrontend.class);
            var root = cq.from(Microfrontend.class);
            cq.where(root.get(Microfrontend_.PRODUCT_ID).in(productNames));
            return getEntityManager().createQuery(cq).getResultList();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_BY_PRODUCT_NAMES, ex);
        }
    }

    enum ErrorKeys {

        ERROR_FIND_BY_PRODUCT_NAMES,
    }

}
