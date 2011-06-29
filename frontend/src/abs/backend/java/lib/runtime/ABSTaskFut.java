/** 
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package abs.backend.java.lib.runtime;

import abs.backend.java.lib.types.ABSValue;
import abs.backend.java.observing.TaskView;

/**
 * Future implementation that depends on a task 

 * @author Jan Schäfer
 *
 * @param <V>
 */
public class ABSTaskFut<V extends ABSValue> extends ABSFut<V> {
    private final Task<?> resolvingTask;


    public ABSTaskFut(Task<?> task) {
        resolvingTask = task;
    }



    @SuppressWarnings("unchecked")
    public V get() {
        synchronized (this) {
            if (!isResolved() && resolvingTask.getCOG() == ABSRuntime.getCurrentCOG())
                throw new ABSDeadlockException();
        }

        final Task<?> t = ABSRuntime.getCurrentTask();
        if (t != null) {
            t.calledGetOnFut(this);
        }

        if (ABSRuntime.getCurrentRuntime().hasGlobalScheduler()) {
            ABSRuntime.getCurrentRuntime().getGlobalScheduler().handleGet(this);
        }

        await();
        if (Logging.DEBUG)
            log.finest("future awaited");

        if (t != null) {
            t.futureReady(this);
        }

        if (Logging.DEBUG)
            log.finest("continue after get");

        return value;
    }

    public synchronized String toString() {
        return "Future of " + resolvingTask + " (" + (isResolved ? value : "unresolved") + ")";
    }


    public Task<?> getResolvingTask() {
        return resolvingTask;
    }

    @Override
    protected abs.backend.java.lib.runtime.ABSFut.View createView() {
        return null;
    }
    
    private class TaskFutView extends abs.backend.java.lib.runtime.ABSFut.View {
        @Override
        public TaskView getResolvingTask() {
            return ABSTaskFut.this.resolvingTask.getView();
        }
    }

}
