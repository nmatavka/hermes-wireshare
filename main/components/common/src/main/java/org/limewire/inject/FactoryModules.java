package org.limewire.inject;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * Helpers for migrating legacy assisted factories to {@link FactoryModuleBuilder}.
 */
public final class FactoryModules {

    private FactoryModules() {
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <F, T> Module newFactory(Class<F> factoryType, Class<? extends T> implementationType) {
        FactoryModuleBuilder builder = new FactoryModuleBuilder();
        Set<Class<?>> returnTypes = new LinkedHashSet<Class<?>>();

        for (Method method : factoryType.getMethods()) {
            if (method.getDeclaringClass() == Object.class) {
                continue;
            }

            Class<?> returnType = method.getReturnType();
            if (!returnType.isAssignableFrom(implementationType)) {
                throw new IllegalArgumentException("Factory return type " + returnType.getName()
                        + " is not assignable from " + implementationType.getName());
            }
            returnTypes.add(returnType);
        }

        for (Class<?> returnType : returnTypes) {
            if (!returnType.equals(implementationType)) {
                builder = builder.implement((Class) returnType, (Class) implementationType);
            }
        }

        return builder.build(factoryType);
    }
}
