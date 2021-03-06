package org.axonframework.common;

import java.util.concurrent.ThreadFactory;

/**
 * Thread factory that created threads in a given group.
 *
 * @author Allard Buijze
 * @since 2.0
 */
public class AxonThreadFactory implements ThreadFactory {

    private final int priority;
    private final ThreadGroup groupName;

    /**
     * Initializes a ThreadFactory instance that creates each thread in a group with given <code>groupName</code> with
     * default priority.
     *
     * @param groupName The name of the group to create each thread in
     * @see Thread#setPriority(int)
     */
    public AxonThreadFactory(String groupName) {
        this(new ThreadGroup(groupName));
    }

    /**
     * Initializes a ThreadFactory instance that create each thread in the given <code>group</code> with default
     * priority.
     *
     * @param group The ThreadGroup to create each thead in
     * @see Thread#setPriority(int)
     */
    public AxonThreadFactory(ThreadGroup group) {
        this(Thread.NORM_PRIORITY, group);
    }

    /**
     * Initializes a ThreadFactory instance that create each thread in the given <code>group</code> with given
     * <code>priority</code>.
     *
     * @param priority The priority of the threads to create
     * @param group    The ThreadGroup to create each thead in
     * @see Thread#setPriority(int)
     */
    public AxonThreadFactory(int priority, ThreadGroup group) {
        Assert.isTrue(priority <= Thread.MAX_PRIORITY && priority >= Thread.MIN_PRIORITY, "Given priority is invalid");
        this.priority = priority;
        this.groupName = group;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(groupName, r);
        thread.setPriority(priority);
        return thread;
    }
}
