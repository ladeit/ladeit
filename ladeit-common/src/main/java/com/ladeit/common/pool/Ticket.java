package com.ladeit.common.pool;


import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.Map;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Ticket {

    /**
     * 排队成功
     */
    public final static int MY_YURN = 0;

    /**
     * 超时
     */
    public final static int TIMEOUT = 1;

    private Queue queue;

    private Map vector;

    private Long time;

    private Long timeout;

    private ScheduledExecutorService executorService;

    public Ticket(Map vector, Queue queue, Long timeout) {
        this.vector = vector;
        this.queue = queue;
        this.timeout = timeout;
        this.time = 0L;
        executorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("example-schedule-pool-%d").daemon(true).build());
        executorService.scheduleAtFixedRate(() -> {
            time += 1L;
            if (this.time >= this.timeout) {
                this.executorService.shutdown();
                synchronized (vector) {
                    vector.notifyAll();
                }
            }
        }, 0L, 1L, TimeUnit.MILLISECONDS);
    }

    public int waitForItem() {
        synchronized (vector) {
            while (true) {
                boolean timeout = this.time >= this.timeout;
                boolean empty = this.vector.isEmpty();
                boolean myturn = this.queue.peek() == this;
                if ((!timeout && (!empty && myturn)) || timeout) {
                    break;
                }
                try {
                    vector.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (this.time >= this.timeout) {
                return Ticket.TIMEOUT;
            } else {
                return Ticket.MY_YURN;
            }
        }
    }
}
