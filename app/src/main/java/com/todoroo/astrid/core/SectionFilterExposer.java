package com.todoroo.astrid.core;

import com.todoroo.astrid.api.Filter;
import com.todoroo.astrid.api.SectionFilter;
import com.todoroo.astrid.dao.SectionDao;
import com.todoroo.astrid.data.Section;

import java.util.List;

import javax.inject.Inject;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;

/**
 * Created by rapha on 17/01/2018.
 */

public final class SectionFilterExposer {

    private final SectionDao sectionDao;

    @Inject
    public SectionFilterExposer(SectionDao sectionDao) {
        this.sectionDao = sectionDao;
    }

    public List<Filter> getFilters() {
        return newArrayList(transform(sectionDao.allSection(), this::load));
    }

    public Filter getFilter(long id) {
        return load(sectionDao.getSectionByID(id));
    }

    private Filter load(Section section) {
        if (section == null) {
            return null;
        }

        SectionFilter customFilter = new SectionFilter(section);
        return customFilter;
    }
}
