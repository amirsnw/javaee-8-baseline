/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.income.rest;

import com.income.rest.mapper.CustomExceptionMapper;
import com.income.rest.mapper.ObjectMapperProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api")
public class JaxRsActivator extends ResourceConfig {

    public JaxRsActivator() {
        this.property("jersey.config.disableAutoDiscovery",
                Boolean.valueOf(true));
        this.packages("com.income.rest", "com.fasterxml.jackson.jaxrs");
        this.register(RolesAllowedDynamicFeature.class);
        register(ObjectMapperProvider.class);
        register(CustomExceptionMapper.class);
    }

}
