package com.ladeit.common.holder;

public class K8sHolder {

    public synchronized void hold() throws InterruptedException {
        System.out.println("<<<<hold thread: " + Thread.currentThread().getThreadGroup() + "," + Thread.currentThread().getId() + ">>>>");
        this.wait();
        System.out.println("<<<<hold finished thread: " + Thread.currentThread().getThreadGroup() + "," + Thread.currentThread().getId() + ">>>>");
    }

    public synchronized void go() {
        System.out.println("<<<<notify thread: " + Thread.currentThread().getThreadGroup() + "," + Thread.currentThread().getId() + ">>>>");
        this.notify();
    }
}
