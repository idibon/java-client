/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.util;

import java.util.ArrayList;
import java.util.WeakHashMap;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Memoize can be used to perform memoization of arbitrary types, as long
 * as the type implements hashCode consistently with equals. This can be
 * used to ensure that any two equal instances of a particular object will
 * always refer to the same instance.
 *
 * Instances are cached in memory until evicted by the garbage collector.
 *
 * Every method in this class is thread-safe.
 */
public class Memoize<Type> {

    /**
     * Creates a memoizer that only memoizes objects as long as a reference
     * to the object is reachable (accessible from any scope in the program).
     *
     * @param type The type of object being memoized.
     * @return {@link com.idibon.api.util.Memoize} instance;
     */
    public static <T> Memoize<T> liveReferences(Class<T> type) {
        Constructor<WeakReference> ctor;
        try {
            ctor = WeakReference.class.getDeclaredConstructor(Object.class);
        } catch (Exception _) {
            throw new Error(""); // can't happen
        }
        return new Memoize<T>(ctor);
    }

    /**
     * Creates a memoizer that caches memoized objects as long as memory is
     * available.
     *
     * @param type The type of object being memoized.
     * @return {@link com.idibon.api.util.Memoize} instance;
     */
    public static <T> Memoize<T> cacheReferences(Class<T> type) {
        Constructor<SoftReference> ctor;
        try {
            ctor = SoftReference.class.getDeclaredConstructor(Object.class);
        } catch (Exception _) {
            throw new Error(""); // can't happen
        }
        return new Memoize<T>(ctor);
    }

    /**
     * Removes an existing reference from the pool.
     *
     * @param instance The reference to remove
     * @return The previous memoized object, if one exists
     */
    public Type remove(Type instance) {
        _lock.writeLock().lock();
        try {
            Reference<Type> ref = _pool.remove(instance);
            return ref == null ? null : ref.get();
        } finally {
            _lock.writeLock().unlock();
        }
    }

    /**
     * Creates a copy of all of the items currently in the pool
     */
    public Iterable<Type> items() {
        _lock.readLock().lock();
        try {
            ArrayList<Type> allItems = new ArrayList<>(_pool.size());
            for (Reference<Type> ref : _pool.values()) {
                Type t = ref.get();
                if (t != null) allItems.add(t);
            }
            return allItems;
        } finally {
            _lock.readLock().unlock();
        }
    }

    /**
     * Clears all cached references.
     */
    public void clear() {
        _lock.writeLock().lock();
        try {
            _pool.clear();
        } finally {
            _lock.writeLock().unlock();
        }
    }

    /**
     * Memoizes the provided object instance.
     *
     * @param instance The object to memoize.
     * @return The existing, equal instance, that was alread memoized (if
     *         an existing reference exists), or instance.
     */
    @SuppressWarnings("unchecked")
    public Type memoize(Type instance) {
        Type existing = null;

        _lock.readLock().lock();
        try {
            Reference<Type> existingRef = _pool.get(instance);
            if (existingRef != null) existing = existingRef.get();
        } finally {
            _lock.readLock().unlock();
        }

        if (existing != null) return existing;

        _lock.writeLock().lock();
        try {
            /* check to see if some other thread happened to memoize
             * a different reference to this instance during the
             * lock upgrade. */
            Reference<Type> existingRef = _pool.get(instance);
            if (existingRef != null) existing = existingRef.get();
            if (existing != null) return existing;

            Reference<Type> newRef;
            try {
                newRef = (Reference<Type>)_referenceCtor.newInstance(instance);
            } catch (Exception _) {
                throw new Error(""); // can't happen
            }
            // nope, memoize the current instance
            _pool.put(instance, newRef);
        } finally {
            _lock.writeLock().unlock();
        }

        return instance;
    }

    private <T extends Reference> Memoize(Constructor<T> ctor) {
        _lock = new ReentrantReadWriteLock();
        _pool = new WeakHashMap<>();
        _referenceCtor = ctor;
    }

    // protects access to the memoization pool
    private final ReentrantReadWriteLock _lock;

    // The memoization pool.
    private final WeakHashMap<Type, Reference<Type>> _pool;

    // controls the garbage collector policy for cached entries
    private final Constructor<? extends Reference> _referenceCtor;
}
