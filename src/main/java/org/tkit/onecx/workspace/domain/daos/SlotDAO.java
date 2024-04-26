package org.tkit.onecx.workspace.domain.daos;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;

import org.tkit.onecx.workspace.domain.models.Slot;
import org.tkit.onecx.workspace.domain.models.Slot_;
import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.jpa.models.TraceableEntity_;

@ApplicationScoped
public class SlotDAO extends AbstractDAO<Slot> {

    @Transactional
    public void deleteById(Object id) throws DAOException {
        var item = findById(id);
        if (item != null) {
            delete(item);
        }
    }

    // https://hibernate.atlassian.net/browse/HHH-16830#icft=HHH-16830
    @Override
    public Slot findById(Object id) throws DAOException {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Slot.class);
            var root = cq.from(Slot.class);
            cq.where(cb.equal(root.get(TraceableEntity_.ID), id));
            return this.getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception e) {
            throw new DAOException(ErrorKeys.FIND_ENTITY_BY_ID_FAILED, e, entityName, id);
        }
    }

    public Slot loadById(Object id) throws DAOException {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Slot.class);
            var root = cq.from(Slot.class);
            cq.where(cb.equal(root.get(TraceableEntity_.ID), id));
            return this.getEntityManager()
                    .createQuery(cq)
                    .setHint(HINT_LOAD_GRAPH, this.getEntityManager().getEntityGraph(Slot.SLOT_LOAD))
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception e) {
            throw new DAOException(ErrorKeys.LOAD_ENTITY_BY_ID_FAILED, e, entityName, id);
        }
    }

    public List<Slot> findSlotsByWorkspaceId(String workspaceId) throws DAOException {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Slot.class);
            var root = cq.from(Slot.class);
            cq.where(cb.equal(root.get(Slot_.WORKSPACE_ID), workspaceId));
            return getEntityManager().createQuery(cq).getResultList();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_SLOTS_BY_WORKSPACE_ID, ex);
        }
    }

    public enum ErrorKeys {
        LOAD_ENTITY_BY_ID_FAILED,
        FIND_ENTITY_BY_ID_FAILED,
        ERROR_FIND_SLOTS_BY_WORKSPACE_ID,
    }

}
