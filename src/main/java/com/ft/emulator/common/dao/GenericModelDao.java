package com.ft.emulator.common.dao;

import com.ft.emulator.common.dao.helper.OrderBy;
import com.ft.emulator.common.dao.helper.Filter;
import com.ft.emulator.common.exception.EntityNotFoundException;
import com.ft.emulator.common.model.AbstractBaseModel;
import com.ft.emulator.common.model.annotation.SoftDelete;
import com.ft.emulator.common.service.EntityManagerUtil;
import com.ft.emulator.common.utilities.StringUtils;
import com.ft.emulator.common.validation.Validation;
import com.ft.emulator.common.validation.ValidationException;

import javax.persistence.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericModelDao<T extends AbstractBaseModel> {

    protected final EntityManagerFactory emFactory;

    protected Class<T> typeHint;

    public GenericModelDao(EntityManagerFactory emFactory, Class<T> entityClass) {
        this.emFactory = emFactory;
        this.typeHint = entityClass;
    }

    /**
     * Returns the total number of entries for this entity.
     *
     * @return number of entries.
     */
    @SuppressWarnings("unchecked")
    public long getRowCount() {

        EntityManager em = EntityManagerUtil.getEntityManager(emFactory);

	Query query = em.createQuery("SELECT COUNT(*) FROM " + typeHint.getSimpleName());
	List<Long> result = query.getResultList();

	if (!result.isEmpty()) {
	    return result.get(0);
	}

	if(em.isOpen())
	    em.close();

	return 0;
    }

    public void validate(T entity) throws ValidationException {

	EntityManager em = EntityManagerUtil.getEntityManager(emFactory);

	// get fields
	List<Field> fields = new ArrayList<>();

	// add all fields to list
	Class<?> superClass = entity.getClass();
	while (superClass != null) {
	    for (Field field : superClass.getDeclaredFields()) {
		fields.add(field);
	    }
	    superClass = superClass.getSuperclass();
	}

	// validate
	for (Field field : fields) {
	    // if nothing to validate
	    if (!field.isAnnotationPresent(Validation.class) && !field.isAnnotationPresent(Column.class)) {
		continue;
	    }

	    // get annotations
	    Validation validation = null;
	    Column column = null;
	    for (Annotation annotation : field.getAnnotations()) {
		if (annotation instanceof Validation) {
		    validation = (Validation) annotation;
		}
		else if (annotation instanceof Column) {
		    column = (Column) annotation;
		}
	    }

	    // create readMethodName
	    String readMethodName = "get" + StringUtils.firstCharToUpperCase(field.getName());

	    // if blank is not allowed
	    if (validation != null && !validation.allowBlank()) {
		Object value = null;

		try {
		    Method method = entity.getClass().getMethod(readMethodName);
		    value = method.invoke(entity);
		}
		catch (Exception e) {
		}
		finally {
		    em.close();
		}

		if (value == null || (value instanceof String && StringUtils.isEmpty((String) value))) {
		    em.close();
		    throw new ValidationException(StringUtils.firstCharToUpperCase(field.getName()) + " can not be empty.");
		}
	    }

	    // if this field is unique
	    if (column != null && column.unique()) {
		Object value = null;
		boolean objectsFound = false;
		try {
		    Method method = entity.getClass().getMethod(readMethodName);
		    value = method.invoke(entity);

		    // get list
		    String queryString = "FROM " + typeHint.getCanonicalName() + " e ";
		    queryString += "WHERE e." + field.getName() + "=:val";
		    TypedQuery<T> query = em.createQuery(queryString, typeHint).setParameter("val", value);
		    List<T> objects = query.getResultList();

		    T object = (objects != null && !objects.isEmpty()) ? objects.get(0) : null;
		    if (object != null && object.getId() != null && !object.getId().equals(entity.getId())) {
			objectsFound = true;
		    }
		}
		catch (Exception e) {
		    throw new ValidationException(StringUtils.firstCharToUpperCase(field.getName()) + " is a unique field.");
		}
		finally {
		    em.close();
		}

		if (objectsFound) {
		    if(em.isOpen())
			em.close();
		    throw new ValidationException(StringUtils.firstCharToUpperCase(field.getName()) + " is a unique field.");
		}
	    }
	}
	if(em.isOpen())
	    em.close();
    }

    public T save(T entity) throws ValidationException {

        validate(entity);

	EntityManager em = EntityManagerUtil.getEntityManager(emFactory);

        try {

	    em.getTransaction().begin();

	    entity = em.merge(entity);
	    em.persist(entity);
	    em.flush();

	    em.getTransaction().commit();
	}
        finally {
            if(em.getTransaction().isActive())
                em.getTransaction().rollback();

            em.close();
	}

        if(em.isOpen())
            em.close();

        return entity;
    }

    public List<T> getList() {

	Integer start = null;
	Integer limit = null;
	return getList(start, limit);
    }

    public List<T> getList(Integer start, Integer limit) {
	return getList(start, limit, new HashMap<String, Object>(), new String[] {});
    }

    public List<T> getList(Integer start, Integer limit, String... fetchProperties) {
	return getList(start, limit, new HashMap<String, Object>(), fetchProperties);
    }

    public List<T> getList(Integer start, Integer limit, OrderBy orderBy) {
	return getList(start, limit, new HashMap<String, Object>(), orderBy, new String[] {});
    }

    public List<T> getList(Integer start, Integer limit, List<OrderBy> orderByList) {
	return getList(start, limit, new HashMap<String, Object>(), orderByList, new String[] {});
    }

    public List<T> getList(Integer start, Integer limit, OrderBy orderBy, String... fetchProperties) {
	return getList(start, limit, new HashMap<String, Object>(), orderBy, fetchProperties);
    }

    public List<T> getList(Integer start, Integer limit, List<OrderBy> orderByList, String... fetchProperties) {
	return getList(start, limit, new HashMap<String, Object>(), orderByList, fetchProperties);
    }

    public List<T> getList(String... fetchProperties) {
	return getList(null, null, new HashMap<String, Object>(), new ArrayList<OrderBy>(), fetchProperties);
    }

    public List<T> getList(OrderBy orderBy, String... fetchProperties) {
	return getList(null, null, new HashMap<String, Object>(), orderBy, fetchProperties);
    }

    public List<T> getList(List<OrderBy> orderByList, String... fetchProperties) {
	return getList(null, null, new HashMap<String, Object>(), orderByList, fetchProperties);
    }

    public List<T> getList(Map<String, Object> filters) {
	return getList(filters, new String[] {});
    }

    public List<T> getList(Map<String, Object> filters, OrderBy orderBy) {
	return getList(filters, orderBy, new String[] {});
    }

    public List<T> getList(Map<String, Object> filters, List<OrderBy> orderByList) {
	return getList(filters, orderByList, new String[] {});
    }

    public List<T> getList(Integer start, Integer limit, Map<String, Object> filters) {
	return getList(start, limit, filters, new String[] {});
    }

    public List<T> getList(Integer start, Integer limit, Map<String, Object> filters, OrderBy orderBy) {
	return getList(start, limit, filters, orderBy, new String[] {});
    }

    public List<T> getList(Integer start, Integer limit, Map<String, Object> filters, List<OrderBy> orderByList) {
	return getList(start, limit, filters, orderByList, new String[] {});
    }

    public List<T> getList(Map<String, Object> filters, String... fetchProperties) {
	return getList(null, null, filters, fetchProperties);
    }

    public List<T> getList(Map<String, Object> filters, OrderBy orderBy, String... fetchProperties) {
	return getList(null, null, filters, orderBy, fetchProperties);
    }

    public List<T> getList(Map<String, Object> filters, List<OrderBy> orderByList, String... fetchProperties) {
	return getList(null, null, filters, orderByList, fetchProperties);
    }

    public List<T> getList(Integer start, Integer limit, Map<String, Object> filters, String... fetchProperties) {
	return getList(start, limit, filters, new ArrayList<OrderBy>(), fetchProperties);
    }

    public List<T> getList(Integer start, Integer limit, Map<String, Object> filters, OrderBy orderBy, String... fetchProperties) {

	List<OrderBy> orderByList = new ArrayList<OrderBy>();
	if (orderBy != null) {
	    orderByList.add(orderBy);
	}

	return getList(start, limit, filters, orderByList, fetchProperties);
    }

    public List<T> getList(Integer start, Integer limit, Map<String, Object> filters, List<OrderBy> orderByList, String... fetchProperties) {

	List<Filter> filterList = new ArrayList<Filter>();

	for (Map.Entry<String, Object> filter : filters.entrySet()) {
	    filterList.add(new Filter(filter.getKey(), filter.getValue(), "="));
	}

	return getList(start, limit, filterList, orderByList, fetchProperties);
    }

    public List<T> getList(Integer start, Integer limit, List<Filter> filters, List<OrderBy> orderByList, String... fetchProperties) {

	EntityManager em = EntityManagerUtil.getEntityManager(emFactory);

	String queryString = "FROM " + typeHint.getCanonicalName() + " e ";

	// add fetch properties
	for (String property : fetchProperties) {
	    if (property.contains(".")) {
		String propertyVal = property.replace(".", "");
		queryString += "LEFT JOIN FETCH " + property + " " + propertyVal + " ";
	    }
	    else {
		queryString += "LEFT JOIN FETCH e." + property + " " + property + " ";
	    }
	}

	filters = filters != null ? filters : new ArrayList<Filter>();
	if (isSoftDelete()) {
	    filters.add(new Filter("deleted", false, "="));
	}

	// add filters
	int count = 0;
	for (Filter filter : filters) {
	    if (count > 0) {
		queryString += "AND ";
	    }
	    else {
		queryString += "WHERE ";
	    }
	    queryString += "e." + filter.getProperty();

	    if (filter.getValue() != null) {
		queryString += " " + filter.getCompareOparant() + " :" + filter.getProperty() + " ";
	    }
	    else {
		queryString += " IS NULL ";
	    }

	    count++;
	}

	// add order
	if (orderByList != null && !orderByList.isEmpty()) {

	    String orderByClause = "";

	    for (OrderBy orderBy : orderByList) {

		String property = orderBy.getProperty();
		String direction = orderBy.getDirection();

		if (StringUtils.isEmpty(property) || StringUtils.isEmpty(direction)) {
		    continue;
		}

		if (!StringUtils.isEmpty(property) && !StringUtils.isEmpty(direction)) {
		    orderByClause += StringUtils.isEmpty(orderByClause) ? "ORDER BY " : ", ";
		    orderByClause += property.contains(".") ? property + " " : "e." + property + " ";
		    orderByClause += direction;
		}

	    }

	    queryString += orderByClause + " ";
	}

	// create typed query
	TypedQuery<T> query = em.createQuery(queryString, typeHint);

	// set filter values
	for (Filter filter : filters) {
	    if (filter.getValue() == null) {
		continue;
	    }
	    query.setParameter(filter.getProperty(), filter.getValue());
	}

	if (start != null) {
	    query.setFirstResult(start);
	}
	if (limit != null) {
	    query.setMaxResults(limit);
	}

	List<T> result = query.getResultList();

	if(em.isOpen())
	    em.close();

	return result;
    }

    public T getLast() {

	EntityManager em = EntityManagerUtil.getEntityManager(emFactory);

	String queryString = "FROM " + typeHint.getCanonicalName() + " e ORDER BY e.id DESC";
	List<T> result = em.createQuery(queryString, typeHint).setMaxResults(0).getResultList();

	if(em.isOpen())
	    em.close();

	return (result == null || result.isEmpty()) ? null : result.get(0);
    }

    public T find(Long id) throws EntityNotFoundException {

	EntityManager em = EntityManagerUtil.getEntityManager(emFactory);

	T entity = em.find(typeHint, id);

	if (entity == null) {
	    if(em.isOpen())
		em.close();
	    //throw new EntityNotFoundException(typeHint.getCanonicalName(), id);
	    return entity;
	}

	if(em.isOpen())
	    em.close();

	return entity;
    }

    public T find(Long id, String... fetchProperties) {

	if (id == null) {
	    throw new EntityNotFoundException(typeHint.getCanonicalName(), id);
	}

	EntityManager em = EntityManagerUtil.getEntityManager(emFactory);

	String queryString = "FROM " + typeHint.getCanonicalName() + " e ";

	// add fetch properties
	for (String property : fetchProperties) {
	    if (property.contains(".")) {
		String propertyVal = property.replace(".", "");
		queryString += "LEFT JOIN FETCH " + property + " " + propertyVal + " ";
	    }
	    else {
		queryString += "LEFT JOIN FETCH e." + property + " " + property + " ";
	    }
	}

	queryString += "WHERE e.id=:id";

	TypedQuery<T> query = em.createQuery(queryString, typeHint).setParameter("id", id);

	List<T> results = query.getResultList();

	if (results == null || results.isEmpty()) {
	    if(em.isOpen())
		em.close();
	    throw new EntityNotFoundException(typeHint.getCanonicalName(), id);
	}

	if(em.isOpen())
	    em.close();

	return results.get(0);
    }

    public T find(Map<String, Object> filters) throws EntityNotFoundException {

	String[] fetchProperties = {};
	return find(filters, fetchProperties);
    }

    public T find(Map<String, Object> filters, String... fetchProperties) throws EntityNotFoundException {

	List<T> list = getList(filters, fetchProperties);
	return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    public void remove(T entity) throws EntityNotFoundException {

	EntityManager em = EntityManagerUtil.getEntityManager(emFactory);

	if (!isSoftDelete()) {

	    try {

	        em.getTransaction().begin();

		if (!em.contains(entity)) {
		    entity = em.merge(entity);
		}
		em.remove(entity);

		em.getTransaction().commit();
	    }
	    finally {
		if(em.getTransaction().isActive())
		    em.getTransaction().rollback();

		em.close();
	    }

	    if(em.isOpen())
		em.close();
	    return;
	}

	entity = find(entity.getId());
	entity.setDeleted(true);
	try {

	    em.getTransaction().begin();

	    save(entity);

	    em.getTransaction().commit();
	}
	catch (Exception e) {
	    e.printStackTrace();
	    throw new EntityNotFoundException(typeHint.getCanonicalName(), entity.getId());
	}
	finally {
	    if(em.getTransaction().isActive())
		em.getTransaction().rollback();

	    em.close();
	}

	if(em.isOpen())
	    em.close();
    }

    public void remove(Long id) {

	T entity = find(id);
	remove(entity);
    }

    protected boolean isSoftDelete() {

	SoftDelete softDelete = null;

	Class<?> superClass = this.typeHint;
	while (superClass != null) {
	    for (Annotation annotation : superClass.getAnnotations()) {
		if (annotation instanceof SoftDelete) {
		    softDelete = (SoftDelete) annotation;
		    return softDelete.value();
		}
	    }
	    superClass = superClass.getSuperclass();
	}

	return false;
    }
}