package com.ladeit.util;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: ListUtil
 * @author: falcomlife
 * @create: 2019/07/09
 * @version: 1.0.0
 */
public class ListUtil<T, E> {
    /**
     * @param sources
     * @param targetClass
     * @FunctionName copyList
     * @author falcomlife
     * @date 19-7-9
     * @version 1.0.0
     * @Return List<E>
     */
    public List<E> copyList(List<T> sources, Class targetClass) {
        List<E> targets = new ArrayList<>();
        for (T source : sources) {
            E target = null;
            try {
                target = (E) Class.forName(targetClass.getName()).newInstance();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
            BeanUtils.copyProperties(source,target);
            targets.add(target);
        }
        return targets;
    }
}
