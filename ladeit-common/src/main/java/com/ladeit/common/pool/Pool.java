package com.ladeit.common.pool;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class Pool<T> {

    /**
     * 池元素最大值
     */
    protected Long max;

    /**
     * 池元素最小值
     */
    protected Long min;

    /**
     * 池元素当前值
     */
    private Long count;

    /**
     * 获取元素超时时间
     */
    protected Long timeout;

    /**
     * 泛型类
     */
    protected Class<T> cls;
    /**
     * 池对象
     */
    protected Map<Integer,T> vector = new LinkedHashMap<>();

    private Queue<Ticket> queue = new LinkedBlockingQueue();


    /**
     * 生成池元素
     */
    public void generatePool() {
        this.generateItem();
    }

    /**
     * 得到一个元素
     *
     * @return
     */
    public synchronized T getItem() {
        if (!vector.isEmpty()) {
        } else if (vector.isEmpty() && count < max) {
            this.generateOne();
        } else if (vector.isEmpty() && count == max) {
            Ticket ticket = new Ticket(this.vector, this.queue, this.timeout);
            queue.add(ticket);
            int result = ticket.waitForItem();
            if(result==Ticket.TIMEOUT){
                queue.remove(ticket);
                throw new RuntimeException("池等待超时，当前排队数量是"+queue.size()+"，请稍后重试");
            }
        }
        T item = vector.get(vector.size()-1);
        vector.remove(item);
        queue.poll();
        return item;
    }

    /**
     * 生成"池元素最小值"个元素
     */
    private void generateItem() {
        this.count = this.min;
        for (int i = 0; i < min; i++) {
            T item = null;
            try {
                item = this.cls.newInstance();
                this.initItem(item);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            vector.put(vector.size()-1,item);
        }
    }

    /**
     * 生成一个元素
     */
    private synchronized void generateOne() {
        T item = null;
        try {
            item = this.cls.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        this.initItem(item);
        vector.put(vector.size()-1,item);
        count++;
    }

    /**
     * 元素返回池
     */
    public synchronized void close(T item) {
        vector.put(vector.size()-1,item);
        synchronized (vector) {
            vector.notifyAll();
        }
    }

    public Long getCount() {
        return this.count;
    }

    public abstract void initItem(T item);

    public abstract void setCls(Class<T> cls);

    public abstract void setMax(Long max);

    public abstract void setMin(Long min);

    public abstract void setTimeout(Long timeout);
}
