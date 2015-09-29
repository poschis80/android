package xpshome.net.components;

import android.support.annotation.NonNull;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * Created by Christian Poschigner on 25.09.2015.
 */
public class Unity {
    private static Unity instance = null;
    @SuppressWarnings("unused")
    public static Unity Instance() {
        if (instance == null) {
            instance = new Unity();
        }
        return instance;
    }

    @SuppressWarnings("unused")
    public static class SingletonBase {
        public void init() {}
        public void destroy() {}
    }


    private final Object lockObject;
    private HashMap<Class<?>, Object> classes;
    protected Unity() {
        classes = new HashMap<>();
        lockObject = new Object();
    }

    private void instantiateNewObject(Class<?> cl) {
        try {
            Object o = cl.getConstructors()[0].newInstance();
            if (o instanceof SingletonBase) {
                ((SingletonBase)o).init();
            }
            classes.put(cl, o);

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }



    @SuppressWarnings("unused")
    public Unity register(@NonNull Class<?> object) {
        synchronized (lockObject) {
            if (!classes.containsKey(object)) {
                classes.put(object, null);
            }
        }
        return this;
    }

    @SuppressWarnings("unused")
    public Unity register(@NonNull Class<?> object, @NonNull Object instance) {
        classes.put(object, instance);
        return this;
    }

    @SuppressWarnings("unused")
    public <T> Unity register(@NonNull T object) {
        if (!classes.containsKey(object.getClass())) {
            classes.put(object.getClass(), object);
        }
        return this;
    }

    @SuppressWarnings("unused")
    public Unity registerClasses(@NonNull Class<?>... objects) {
        for (Class<?> c : objects) {
            register(c);
        }
        return this;
    }

    @SuppressWarnings("unused")
    public Unity unregisterAll() {
        synchronized (lockObject) {
            for (Object o : classes.values()) {
                if (o instanceof SingletonBase) {
                    ((SingletonBase)o).destroy();
                }
            }
            classes.clear();
        }
        return this;
    }

    @SuppressWarnings("unused")
    public Unity unregister(@NonNull Class<?> object) {
        synchronized (lockObject) {
            Object o = classes.get(object);
            if (o != null) {
                if (o instanceof SingletonBase) {
                    ((SingletonBase) o).destroy();
                }
            }
            classes.remove(object);
        }
        return this;
    }

    @SuppressWarnings("unused unchecked")
    public final <T> T getInstance(@NonNull final String className) {
        try {
            return getInstance((Class<T>)Class.forName(className));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unused unchecked")
    public final <T> T getInstance(@NonNull Class<T> object) {
        synchronized (lockObject) {
            if (classes.containsKey(object)) {
                if (classes.get(object) == null) {
                    instantiateNewObject(object);
                }
                return (T) classes.get(object);
            }
        }
        return null;
    }

    @SuppressWarnings("unused unchecked")
    public final <T> T getInstanceForceRegister(@NonNull Class<T> object) {
        synchronized (lockObject) {
            if (!classes.containsKey(object)) {
                register(object);
                instantiateNewObject(object);
            } else if (classes.get(object) == null) {
                instantiateNewObject(object);
            }
            return (T) classes.get(object);
        }
    }
}
