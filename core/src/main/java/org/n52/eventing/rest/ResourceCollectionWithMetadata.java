/*
 * Copyright (C) 2016-2020 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as publishedby the Free
 * Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU General Public License version 2
 * and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */
package org.n52.eventing.rest;

import com.fasterxml.jackson.annotation.JsonView;
import java.util.List;
import org.n52.eventing.rest.model.views.Views;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 * @param <T> The collection type
 */
public class ResourceCollectionWithMetadata<T> {

    private List<T> data;
    private Metadata metadata;

    public ResourceCollectionWithMetadata(List<T> data, Metadata metadata) {
        this.data = data;
        this.metadata = metadata;
    }

    public ResourceCollectionWithMetadata(QueryResult<T> qr, Pagination page) {
        this.data = qr.getResult();
        this.metadata = new Metadata(qr.getTotalHits(), page);
    }

    @JsonView(Views.BaseView.class)
    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    @JsonView(Views.BaseView.class)
    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public static class Metadata {

        private Long offset;
        private Long limit;
        private Long total;

        public Metadata(Pagination page) {
            this.offset = Integer.valueOf(page.getOffset()).longValue();
            this.limit = Integer.valueOf(page.getLimit()).longValue();
        }

        public Metadata(int total, Pagination page) {
            this(Integer.valueOf(total).longValue(), page);
        }

        public Metadata(Long total, Pagination page) {
            this.total = total;
            this.offset = Integer.valueOf(page.getOffset()).longValue();
            this.limit = Integer.valueOf(page.getLimit()).longValue();
        }

        @JsonView(Views.BaseView.class)
        public Long getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
           setOffset(Integer.valueOf(offset).longValue());
        }

        public void setOffset(Long offset) {
            this.offset = offset;
        }

        @JsonView(Views.BaseView.class)
        public Long getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            setLimit(Integer.valueOf(limit).longValue());
        }

        public void setLimit(Long limit) {
            this.limit = limit;
        }

        @JsonView(Views.BaseView.class)
        public Long getTotal() {
            return total;
        }

        public void setTotal(int total) {
            setTotal(Integer.valueOf(total).longValue());
        }

        public void setTotal(Long total) {
            this.total = total;
        }

    }

}
