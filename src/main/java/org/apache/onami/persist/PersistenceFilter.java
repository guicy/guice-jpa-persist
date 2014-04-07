package org.apache.onami.persist;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import com.google.inject.Inject;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

import static org.apache.onami.persist.Preconditions.checkNotNull;

/**
 * Filter for use in container.
 * The filter will start all persistence services upon container start and span a unit of work
 * around every request which is filtered.
 * <p/>
 * Usage example:
 * <pre>
 *  public class MyModule extends ServletModule {
 *    public void configure() {
 *      // bind your persistence units here
 *
 *      filter("/*").through(PersistenceFilter.class);
 *    }
 *  }
 * </pre>
 */
public class PersistenceFilter
    implements Filter
{

    /**
     * Container of all known persistence unit and units of work.
     */
    private final PersistenceUnitContainer persistenceUnitsContainer;

    /**
     * Constructor.
     *
     * @param persistenceUnitsContainer container of all known persistence unit and units of work.
     */
    @Inject
    PersistenceFilter( PersistenceUnitContainer persistenceUnitsContainer )
    {
        checkNotNull( persistenceUnitsContainer );
        this.persistenceUnitsContainer = persistenceUnitsContainer;
    }

    /**
     * {@inheritDoc}
     */
    // @Override
    public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain )
        throws IOException, ServletException
    {
        try
        {
            persistenceUnitsContainer.beginAllInactiveUnitsOfWork();
            chain.doFilter( request, response );
        }
        finally
        {
            persistenceUnitsContainer.endAllUnitsOfWork();
        }
    }

    /**
     * {@inheritDoc}
     */
    // @Override
    public void init( FilterConfig filterConfig )
        throws ServletException
    {
        persistenceUnitsContainer.startAllStoppedPersistenceServices();
    }

    /**
     * {@inheritDoc}
     */
    // @Override
    public void destroy()
    {
        persistenceUnitsContainer.stopAllRunningPersistenceServices();
    }
}