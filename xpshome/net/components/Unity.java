package xpshome.net.components;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Pair;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Objects;

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
    public static Unity Create(Context context) {
        Unity i = Instance();
        i.appContext = context;
        return i;
    }

    @SuppressWarnings("unused")
    public static class SingletonBase {
        public void init() {}
        public void destroy() {}
    }

    protected Context appContext;
    private final Object lockObject;
    private HashMap<Class<?>, Object> classes;
    protected Unity() {
        classes = new HashMap<>();
        lockObject = new Object();
    }

    private void instantiateNewObject(Class<?> cl) {
        try {
            Constructor ctor;
            Object o = null;
            Pair<Boolean, Constructor> res = findBestSuitableConstructor(cl);
            if (res.first) {
                o = res.second.newInstance();
            } else if (!res.first && res.second != null) {
                o = res.second.newInstance(appContext);
            }
            if (o != null) {
                if (o instanceof SingletonBase) {
                    ((SingletonBase) o).init();
                }
                classes.put(cl, o);
            }

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private Pair<Boolean, Constructor> findBestSuitableConstructor(Class<?> cl) {
        Constructor[] ctors =  cl.getConstructors();
        for (Constructor c : ctors) {
            Class<?>[] params = c.getParameterTypes();
            if (params == null || params.length <= 0) { // return default ctor
                return new Pair<>(true, c);
            } else {
                if (params.length == 1 && params[0].getName().compareTo(Context.class.getName()) == 0) { // return ctor with one param of type Context
                    return new Pair<>(false, c);
                }
            }
        }
        return new Pair<>(false, null);
    }



    @SuppressWarnings("unused")
    public Unity register(Class<?> object) {
        if (object == null) {
            return this;
        }
        synchronized (lockObject) {
            if (!classes.containsKey(object)) {
                classes.put(object, null);
            }
        }
        return this;
    }

    @SuppressWarnings("unused")
    public Unity register(Class<?> object, Object instance) {
        if (object == null || instance == null) {
            return this;
        }
        classes.put(object, instance);
        return this;
    }

    @SuppressWarnings("unused")
    public <T> Unity register(T object) {
        if (object == null) {
            return this;
        }

        if (!classes.containsKey(object.getClass())) {
            classes.put(object.getClass(), object);
        }
        return this;
    }

    @SuppressWarnings("unused")
    public Unity registerClasses(Class<?>... objects) {
        if (objects == null) {
            return this;
        }

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
    public Unity unregister(Class<?> object) {
        if (object == null) {
            return this;
        }

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
    public final <T> T getInstance(Class<T> object) {
        if (object == null) {
            return null;
        }
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
    public final <T> T getInstanceForceRegister(Class<T> object) {
        if (object == null) {
            return null;
        }

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
