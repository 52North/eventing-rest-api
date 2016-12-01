
package org.n52.eventing.wv.model.i18n;

import org.n52.eventing.wv.model.Trend;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class I18nTrend {

    private int id;
    private Trend trendCode;
    private String locale;
    private String name;
    private String description;

    public I18nTrend() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Trend getTrendCode() {
        return trendCode;
    }

    public void setTrendCode(Trend trendCode) {
        this.trendCode = trendCode;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
