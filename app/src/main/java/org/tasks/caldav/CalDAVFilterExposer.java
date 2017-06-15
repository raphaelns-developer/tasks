package org.tasks.caldav;

import com.todoroo.astrid.api.CaldavFilter;
import com.todoroo.astrid.api.Filter;
import com.todoroo.astrid.dao.CaldavDao;
import com.todoroo.astrid.data.CaldavAccount;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class CalDAVFilterExposer {
    private CaldavDao caldavDao;

    @Inject
    public CalDAVFilterExposer(CaldavDao caldavDao) {
        this.caldavDao = caldavDao;
    }

    public List<Filter> getFilters() {
        List<CaldavAccount> allOrderedByName = caldavDao.getAllOrderedByName();
        List<Filter> result = new ArrayList<>();
        for (CaldavAccount account : allOrderedByName) {
            result.add(new CaldavFilter(account));
        }
        return result;
    }
}
