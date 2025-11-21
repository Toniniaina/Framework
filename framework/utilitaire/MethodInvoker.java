package framework.utilitaire;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility to invoke methods via reflection.
 * - execute: instance method by name and signature
 * - executeStatic: static method by name and signature
 * - invokeAllNoArg: invoke all no-arg methods (optionally including private)
 */
public class MethodInvoker {

    /**
     * Execute an instance method by name.
     */
    public static Object execute(Object target, String methodName, Class<?>[] paramTypes, Object[] args) {
        if (target == null) throw new IllegalArgumentException("target is null");
        try {
            Method m = findMethod(target.getClass(), methodName, paramTypes);
            if (!m.isAccessible()) m.setAccessible(true);
            return m.invoke(target, args);
        } catch (RuntimeException re) {
            throw re;
        } catch (Throwable t) {
            throw new RuntimeException("Failed to invoke method '" + methodName + "' on "
                    + target.getClass().getName() + ": " + t.getMessage(), t);
        }
    }

    /**
     * Execute a static method by name.
     */
    public static Object executeStatic(Class<?> clazz, String methodName, Class<?>[] paramTypes, Object[] args) {
        if (clazz == null) throw new IllegalArgumentException("clazz is null");
        try {
            Method m = findMethod(clazz, methodName, paramTypes);
            if (!m.isAccessible()) m.setAccessible(true);
            return m.invoke(null, args);
        } catch (RuntimeException re) {
            throw re;
        } catch (Throwable t) {
            throw new RuntimeException("Failed to invoke static method '" + methodName + "' on "
                    + clazz.getName() + ": " + t.getMessage(), t);
        }
    }

    /**
     * Invoke all no-arg methods on the target.
     * - includePrivate: when true, also includes private/protected methods
     * Returns a list of results in declaration order.
     */
    public static List<Object> invokeAllNoArg(Object target, boolean includePrivate) {
        if (target == null) throw new IllegalArgumentException("target is null");
        List<Object> results = new ArrayList<>();
        Class<?> clazz = target.getClass();
        // Use getDeclaredMethods to preserve declaration order best-effort
        List<Method> methods = new ArrayList<>(Arrays.asList(clazz.getDeclaredMethods()));
        for (Method m : methods) {
            if (m.getParameterCount() == 0) {
                boolean isPublic = Modifier.isPublic(m.getModifiers());
                if (includePrivate || isPublic) {
                    try {
                        if (!m.isAccessible()) m.setAccessible(true);
                        Object res = m.invoke(target);
                        results.add(res);
                    } catch (Throwable t) {
                        // collect error as string instead of failing the whole batch
                        results.add(new RuntimeException("Error invoking " + m.getName() + ": " + t.getMessage(), t));
                    }
                }
            }
        }
        return results;
    }

    private static Method findMethod(Class<?> clazz, String name, Class<?>[] paramTypes) throws NoSuchMethodException {
        // Try exact declared match first
        try {
            return clazz.getDeclaredMethod(name, paramTypes);
        } catch (NoSuchMethodException e) {
            // Walk up the hierarchy
            Class<?> current = clazz;
            while (current != null) {
                try {
                    return current.getDeclaredMethod(name, paramTypes);
                } catch (NoSuchMethodException ignore) {
                    current = current.getSuperclass();
                }
            }
            // As a last resort, try public methods
            return clazz.getMethod(name, paramTypes);
        }
    }
}
