package org.theperkinrex.util;

import org.theperkinrex.components.Chassis;
import org.theperkinrex.iface.Iface;
import org.theperkinrex.layers.link.LinkAddr;
import org.theperkinrex.process.IfaceRegistry;
import org.theperkinrex.process.Process;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ProcessManager implements Process, IfaceRegistry {
    private final ConcurrentMap<Class<?>, ConcurrentList<Process>> processes;
    private final ConcurrentMap<Chassis.IfaceId<? extends Iface<? extends LinkAddr>>, Chassis.IfaceData<LinkAddr, Iface<LinkAddr>>> ifaces;
    private boolean started;
    private final ReadWriteLock mutex;


    public ProcessManager() {
        processes = new ConcurrentHashMap<>();
        started = false;
        mutex = new ReentrantReadWriteLock();
        ifaces = new ConcurrentHashMap<>();
    }

    public int add(Process p) {
        try {
            mutex.readLock().lock();
            if (p instanceof IfaceRegistry i) {
                for (var entry : ifaces.entrySet()) {
                    i.registerIface(entry.getKey(), entry.getValue());
                }
            }
            if(started) p.start();
            return processes.computeIfAbsent(p.getClass(), k -> new ConcurrentList<>()).add(p);
        } finally {
            mutex.readLock().unlock();
        }
    }

    public <P> P get(Class<P> c, int i) {
        if (!processes.containsKey(c)) throw new IndexOutOfBoundsException();
        Process p = processes.get(c).get(i);
        if (p.getClass().equals(c)) {
            return (P) p;
        }else{
            throw new RuntimeException("Unexpected things happen");
        }
    }

    @Override
    public void registerIface(Chassis.IfaceId<? extends Iface<? extends LinkAddr>> ifaceId, Chassis.IfaceData<LinkAddr, Iface<LinkAddr>> ifaceData) {
        try {
            mutex.writeLock().lock();
            ifaces.put(ifaceId, ifaceData);
            for (ConcurrentList<Process> lp :
                    processes.values()) {
                for (Process p : lp) {
                    if (p instanceof IfaceRegistry i){
                        i.registerIface(ifaceId, ifaceData);
                    }
                }
            }
        }finally {
            mutex.writeLock().unlock();
        }
    }

    @Override
    public void unregisterIface(Chassis.IfaceId<? extends Iface<? extends LinkAddr>> ifaceId) {
        try {
            mutex.writeLock().lock();
            ifaces.remove(ifaceId);
            for (ConcurrentList<Process> lp :
                    processes.values()) {
                for (Process p : lp) {
                    if (p instanceof IfaceRegistry i){
                        i.unregisterIface(ifaceId);
                    }
                }
            }
        }finally {
            mutex.writeLock().unlock();
        }
    }

    @Override
    public void start() {
        try {
            mutex.writeLock().lock();
            started = true;
            for (ConcurrentList<Process> lp :
                    processes.values()) {
                for (Process p : lp) {
                    p.start();
                }
            }
        }finally {
            mutex.writeLock().unlock();
        }
    }

    @Override
    public void stop() {
        try {
            mutex.writeLock().lock();
            started = false;
            for (ConcurrentList<Process> lp :
                    processes.values()) {
                for (Process p : lp) {
                    p.stop();
                }
            }
        }finally {
            mutex.writeLock().unlock();
        }
    }
}
