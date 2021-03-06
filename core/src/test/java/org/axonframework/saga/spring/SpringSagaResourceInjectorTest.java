/*
 * Copyright (c) 2010. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.axonframework.saga.spring;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.saga.ResourceInjector;
import org.axonframework.saga.annotation.AbstractAnnotatedSaga;
import org.junit.*;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.*;

/**
 * @author Allard Buijze
 */
public class SpringSagaResourceInjectorTest {

    private static ResourceInjector testSubject;

    @BeforeClass
    public static void beforeClass() throws Exception {
        ApplicationContext appCtx = new ClassPathXmlApplicationContext("META-INF/spring/commandbus-context.xml",
                                                                       "META-INF/spring/saga-injector-context.xml");
        testSubject = appCtx.getBean(ResourceInjector.class);
    }

    @Test
    public void testInjectSaga() {
        InjectableSaga injectableSaga = new InjectableSaga();
        testSubject.injectResources(injectableSaga);
        assertNotNull(injectableSaga.getCommandBus());
        assertNull(injectableSaga.getNonAnnotatedCommandBus());
        assertNull(injectableSaga.getEventBus());
    }

    @Test(expected = BeanCreationException.class)
    public void testResourcesNotAvailable() {
        ProblematicInjectableSaga injectableSaga = new ProblematicInjectableSaga();
        testSubject.injectResources(injectableSaga);
    }

    public static class InjectableSaga extends AbstractAnnotatedSaga {

        private static final long serialVersionUID = 6273830321273396327L;

        @Autowired
        private CommandBus commandBus1;
        @SuppressWarnings({"UnusedDeclaration"})
        private CommandBus commandBus2;

        private EventBus eventBus;

        public CommandBus getCommandBus() {
            return commandBus1;
        }

        public CommandBus getNonAnnotatedCommandBus() {
            return commandBus2;
        }

        public EventBus getEventBus() {
            return eventBus;
        }

        @Autowired(required = false)
        public void setEventBus(EventBus eventBus) {
            this.eventBus = eventBus;
        }
    }

    public static class ProblematicInjectableSaga extends InjectableSaga {

        private static final long serialVersionUID = 3731262948334502511L;

        @Override
        @Autowired(required = true)
        public void setEventBus(EventBus eventBus) {
            throw new UnsupportedOperationException("Method not implemented");

        }
    }

}
