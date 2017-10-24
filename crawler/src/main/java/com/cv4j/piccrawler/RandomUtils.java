package com.cv4j.piccrawler;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by tony on 2017/10/24.
 */
public class RandomUtils {

    public static ThreadLocalRandom getRandom() {
        return ThreadLocalRandom.current();
    }

    /**
     * 从set中随机取得一个元素
     * @param set
     * @return
     */
    public static <E> E getRandomElement(Set<E> set){
        int rn = getRandomInt(set.size());
        int i = 0;
        for (E e : set) {
            if(i==rn){
                return e;
            }
            i++;
        }
        return null;
    }

    /**
     * 获得一个[0,max)之间的随机整数。
     * @param max
     * @return
     */
    public static int getRandomInt(int max) {
        return getRandom().nextInt(max);
    }
}
