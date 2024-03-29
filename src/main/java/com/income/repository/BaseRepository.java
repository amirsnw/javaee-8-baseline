package com.income.repository;

import com.income.dto.Filter;
import com.income.dto.FilterWrapper;
import com.income.entity.ReportEntity;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BaseRepository<T> {

    public abstract Class<T> getClazz();

    public abstract EntityManager getEntityManager();

    public List<T> search(FilterWrapper filterWrapper, Integer start, Integer limit) {

        TypedQuery createQuery = getEntityManager().createQuery(getListQuery(filterWrapper));

        List<T> list = new ArrayList<>();
        if (start != null && limit != null) {
            list = createQuery.setFirstResult(start).setMaxResults(limit).getResultList();
        }
        if (start == null && limit != null) {
            list = createQuery.setMaxResults(limit).getResultList();
        }
        if (start != null && limit == null) {
            list = createQuery.setFirstResult(start).getResultList();
        }
        if (start == null && limit == null) {
            list = createQuery.getResultList();
        }

        return list;
    }

    public Long getCount(FilterWrapper filter) {
        TypedQuery createQuery = getEntityManager().createQuery(getCountQuery(filter));
        return (Long) createQuery.getSingleResult();
    }

    private CriteriaQuery getListQuery(FilterWrapper filterWrapper) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery criteriaQuery = criteriaBuilder.createQuery();
        Root<T> entityFrom = criteriaQuery.from(getClazz());
        criteriaQuery = getFilteredQuery(criteriaQuery, entityFrom, filterWrapper);
        criteriaQuery.select(entityFrom);
        return criteriaQuery;
    }

    private CriteriaQuery getCountQuery(FilterWrapper filterWrapper) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery criteriaQuery = criteriaBuilder.createQuery();
        Root<T> entityFrom = criteriaQuery.from(getClazz());
        criteriaQuery = getFilteredQuery(criteriaQuery, entityFrom, filterWrapper);
        criteriaQuery.select(criteriaBuilder.count(entityFrom));
        return criteriaQuery;
    }

    public CriteriaQuery getFilteredQuery(CriteriaQuery criteriaQuery,
                                          Root<T> entityFrom,
                                          FilterWrapper filterWrapper) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        try {
            criteriaQuery.where(getFilters(filterWrapper, entityFrom, criteriaBuilder).toArray(new Predicate[]{}));

            // criteriaQuery.orderBy(getOrders(sortWrapper, entityFrom, criteriaBuilder));

            return criteriaQuery;

        } catch (Exception e) {
            return null;
        }
    }

    /*private List<Order> getOrders(SortWrapper sortWrapper, Root<T> entityFrom, CriteriaBuilder criteriaBuilder) {
        List<Order> orders = new ArrayList<>();
        if (sortWrapper != null) {
            for (Sort sortSet : sortWrapper.getSortSet()) {
                Order order = null;
                String[] sortProperties = sortSet.getProperty().split("\\.");

                Path path = entityFrom.get(sortProperties[0]);
                for (int j = 1; j < sortProperties.length; j++) {
                    path = path.get(sortProperties[j]);
                }
                if (sortProperties.length > 0) {
                    if (sortSet.getDirection().getName().equals(Sort.Direction.DESC.getName())) {
                        order = criteriaBuilder.desc(entityFrom.get(sortProperties[0]));
                    } else {
                        order = criteriaBuilder.asc(entityFrom.get(sortProperties[0]));
                    }
                }
                orders.add(order);
            }
        }
        return orders;
    }*/

    protected List<Predicate> getFilters(FilterWrapper filterWrapper, Root<T> entityFrom, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        Metamodel metaModel = getEntityManager().getMetamodel();
        EntityType<T> entityType = metaModel.entity(getClazz());
        List<Join> innerJoins = new ArrayList<>();
        if (filterWrapper != null && filterWrapper.getFilters() != null) {
            for (Filter filter : filterWrapper.getFilters()) {

                Object value = filter.getValue();
                String field = filter.getProperty();
                Filter.Operator operator = filter.getOperator();
                String[] f = field.split("\\.");
                Predicate predicate = null;

                Path path = entityFrom.get(f[0]);
                String name = f[0];
                for (int j = 1; j < f.length; j++) {
                    if(Collection.class.isAssignableFrom(path.getJavaType())) {
                        innerJoins.add(entityFrom.join(name));
                    }
                    path = path.get(f[j]);
                    name = f[j];
                }
                for (int j = 1; j < f.length; j++) {
                    path = path.get(f[j]);
                }
                switch (operator) {
                    case EQUAL:
                        predicates.add(criteriaBuilder.equal(path, value));
                        break;
                    case LIKE:
                        predicates.add(criteriaBuilder.like(entityFrom.get(entityType.getDeclaredSingularAttribute(field, String.class)), "%" + value + "%"));
                        break;
                    case AFTER:
                        predicates.add(criteriaBuilder.greaterThanOrEqualTo(path, value.toString()));
                        break;
                    case BEFORE:
                        predicates.add(criteriaBuilder.lessThanOrEqualTo(path, value.toString()));
                        break;
                    default:
                        break;
                }
            }
        }
        return predicates;
    }

    public ReportEntity save(ReportEntity reportEntity) {
        getEntityManager().persist(reportEntity);
        return reportEntity;
    }
}
