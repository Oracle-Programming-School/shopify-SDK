/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ordg.blueb.pos.returns;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.ordg.utl.exceptions.NonexistentEntityException;
import com.ordg.utl.exceptions.PreexistingEntityException;

/**
 *
 * @author Administrator
 */
public class EmployeedJpaController implements Serializable {

    public EmployeedJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(EmployeedEo employeed) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(employeed);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findEmployeed(employeed.getEmployeeId()) != null) {
                throw new PreexistingEntityException("Employeed " + employeed + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(EmployeedEo employeed) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            employeed = em.merge(employeed);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                BigDecimal id = employeed.getEmployeeId();
                if (findEmployeed(id) == null) {
                    throw new NonexistentEntityException("The employeed with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(BigDecimal id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            EmployeedEo employeed;
            try {
                employeed = em.getReference(EmployeedEo.class, id);
                employeed.getEmployeeId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The employeed with id " + id + " no longer exists.", enfe);
            }
            em.remove(employeed);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<EmployeedEo> findEmployeedEntities() {
        return findEmployeedEntities(true, -1, -1);
    }

    public List<EmployeedEo> findEmployeedEntities(int maxResults, int firstResult) {
        return findEmployeedEntities(false, maxResults, firstResult);
    }

    private List<EmployeedEo> findEmployeedEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(EmployeedEo.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public EmployeedEo findEmployeed(BigDecimal id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(EmployeedEo.class, id);
        } finally {
            em.close();
        }
    }

    public int getEmployeedCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<EmployeedEo> rt = cq.from(EmployeedEo.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
