/*
 * Copyright (C) 2016-2016 52°North Initiative for Geospatial Open Source
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

package org.n52.eventing.wv.model;

import com.google.common.base.MoreObjects;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class Rule extends BaseEntity {

    private double threshold;
    private Trend trendCode;
    private int activeFlag;
    private Series series;

    public Rule() {
    }

    public Rule(double threshold, Trend trendCode, int activeFlag, Series series) {
        this.threshold = threshold;
        this.trendCode = trendCode;
        this.activeFlag = activeFlag;
        this.series = series;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public Trend getTrendCode() {
        return trendCode;
    }

    public void setTrendCode(Trend trendCode) {
        this.trendCode = trendCode;
    }

    public boolean isActive() {
        return activeFlag == 1;
    }

    public void setActive(boolean active) {
        this.activeFlag = active ? 1 : 0;
    }

    public int getActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(int activeFlag) {
        this.activeFlag = activeFlag;
    }

    public Series getSeries() {
        return series;
    }

    public void setSeries(Series series) {
        this.series = series;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("id", getId())
                .add("series", getSeries().getId())
                .add("threshold", getThreshold())
                .add("trend", getTrendCode())
                .toString();
    }



}
