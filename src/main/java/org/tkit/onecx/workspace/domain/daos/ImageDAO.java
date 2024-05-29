package org.tkit.onecx.workspace.domain.daos;

import java.util.Collection;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;

import org.tkit.onecx.workspace.domain.models.*;
import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.exceptions.DAOException;

import gen.org.tkit.onecx.image.rs.internal.model.RefTypeDTO;

@ApplicationScoped
@Transactional
public class ImageDAO extends AbstractDAO<Image> {

    public Image findByRefIdAndRefType(String refId, String refType) {

        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = this.criteriaQuery();
            var root = cq.from(Image.class);

            cq.where(cb.and(cb.equal(root.get(Image_.refId), refId),
                    cb.equal(root.get(Image_.refType), refType)));

            return this.getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception ex) {
            throw new DAOException(ImageDAO.ErrorKeys.FIND_ENTITY_BY_REF_ID_REF_TYPE_FAILED, ex);
        }
    }

    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = DAOException.class)
    public void deleteQueryByRefId(String refId) throws DAOException {
        try {
            var cq = deleteQuery();
            var root = cq.from(Image.class);
            var cb = this.getEntityManager().getCriteriaBuilder();

            cq.where(cb.equal(root.get(Image_.REF_ID), refId));
            getEntityManager().createQuery(cq).executeUpdate();
            getEntityManager().flush();
        } catch (Exception e) {
            throw handleConstraint(e, ErrorKeys.FAILED_TO_DELETE_BY_REF_ID_QUERY);
        }
    }

    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = DAOException.class)
    public void deleteQueryByRefIdAndRefType(String refId, RefTypeDTO refType) throws DAOException {
        try {
            var cq = deleteQuery();
            var root = cq.from(Image.class);
            var cb = this.getEntityManager().getCriteriaBuilder();

            cq.where(cb.equal(root.get(Image_.REF_ID), refId));
            cq.where(cb.equal(root.get(Image_.REF_TYPE), refType.toString()));
            getEntityManager().createQuery(cq).executeUpdate();
            getEntityManager().flush();
        } catch (Exception e) {
            throw handleConstraint(e, ErrorKeys.FAILED_TO_DELETE_BY_REF_ID_REF_TYPE_QUERY);
        }
    }

    public List<Image> findByRefIds(Collection<String> refIds) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Image.class);
            var root = cq.from(Image.class);
            if (refIds != null && !refIds.isEmpty()) {
                cq.where(root.get(Image_.REF_ID).in(refIds));
            }
            return this.getEntityManager().createQuery(cq).getResultList();

        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_REF_IDS, ex);
        }
    }

    public enum ErrorKeys {

        ERROR_FIND_REF_IDS,

        FAILED_TO_DELETE_BY_REF_ID_QUERY,

        FIND_ENTITY_BY_REF_ID_REF_TYPE_FAILED,

        FAILED_TO_DELETE_BY_REF_ID_REF_TYPE_QUERY

    }
}
