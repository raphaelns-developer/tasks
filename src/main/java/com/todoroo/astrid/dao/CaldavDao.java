/**
 * Copyright (c) 2012 Todoroo Inc
 *
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.astrid.dao;

import com.todoroo.andlib.data.Property;
import com.todoroo.andlib.sql.Criterion;
import com.todoroo.andlib.sql.Functions;
import com.todoroo.andlib.sql.Order;
import com.todoroo.andlib.sql.Query;
import com.todoroo.astrid.data.CaldavAccount;

import org.tasks.injection.ApplicationScope;

import java.util.List;

import javax.inject.Inject;

/**
 * Data Access layer for {@link CaldavAccount}-related operations.
 *
 * @author Tim Su <tim@todoroo.com>
 */
@ApplicationScope
public class CaldavDao {

    private final RemoteModelDao<CaldavAccount> dao;

    @Inject
    public CaldavDao(Database database) {
        dao = new RemoteModelDao<>(database, CaldavAccount.class);
    }

    public CaldavAccount getAccountByName(String name) {
        return dao.getFirst(Query.select(CaldavAccount.PROPERTIES).where(CaldavAccount.NAME.eqCaseInsensitive(name)));
    }

    public CaldavAccount getByUuid(String uuid) {
        return getByUuid(uuid, CaldavAccount.PROPERTIES);
    }

    public CaldavAccount getByUuid(String uuid, Property<?>... properties) {
        return dao.getFirst(Query.select(properties).where(CaldavAccount.UUID.eq(uuid)));
    }

    public List<CaldavAccount> getAllOrderedByName() {
        return dao.toList(Query.select(CaldavAccount.PROPERTIES).where(Criterion.and(
                CaldavAccount.DELETION_DATE.eq(0),
                CaldavAccount.NAME.isNotNull()))
                .orderBy(Order.asc(Functions.upper(CaldavAccount.NAME))));
    }

    public void persist(CaldavAccount CaldavAccount) {
        dao.persist(CaldavAccount);
    }

    public void update(Criterion where, CaldavAccount template) {
        dao.update(where, template);
    }

    public void delete(long id) {
        dao.delete(id);
    }

    public void createNew(CaldavAccount tag) {
        dao.createNew(tag);
    }
}

