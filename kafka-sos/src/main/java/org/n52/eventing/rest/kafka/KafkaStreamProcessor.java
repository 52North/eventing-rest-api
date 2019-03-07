/*
 * Copyright (C) 2016-2019 52°North Initiative for Geospatial Open Source
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
/*
* Copyright (C) 2016-2017 52°North Initiative for Geospatial Open Source
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

package org.n52.eventing.rest.kafka;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.n52.eventing.rest.Pagination;
import org.n52.eventing.rest.publications.Publication;
import org.n52.eventing.rest.publications.PublicationsService;
import org.n52.eventing.rest.publications.UnknownPublicationsException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class KafkaStreamProcessor implements InitializingBean, DisposableBean, PublicationsService {


    @Autowired
    private KafkaSosConsumerWrapper wrapper;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    public static final String CONSUMER_PROPERTIES_FILE = "/kafka-sos-consumer.properties";

    @Override
    public void destroy() throws Exception {
    }

    @Override
    public boolean hasPublication(String id) {
        return this.wrapper.getProcessor().getOfferingIdentifiers().stream()
                .filter(s -> id.equals(s))
                .findFirst()
                .isPresent();
    }

    @Override
    public List<Publication> getPublications(Pagination p) {
        return this.wrapper.getProcessor().getOfferingIdentifiers().stream()
                .map(s -> asPublication(s))
                .collect(Collectors.toList());
    }

    @Override
    public Publication getPublication(String id) throws UnknownPublicationsException {
        Optional<String> result = this.wrapper.getProcessor().getOfferingIdentifiers().stream()
                .filter(s -> id.equals(s))
                .findFirst();

        if (result.isPresent()) {
            return asPublication(id);
        }

        throw new UnknownPublicationsException("Could not resolve offering: "+id);
    }

    private Publication asPublication(String id) {
        return new Publication(id, String.format("SOS Offering '%s'", id), null);
    }

}
