package com.zjj.aisearch.demo.thread;

/**
 * @program: AISearch
 * @description:
 * @author: zjj
 * @create: 2020-02-24 19:43:24
 **/
public class SubThread extends Thread {
    public void run() {
        for (int i = 1; i < 100; i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("subThead: " + getName());
        }
    }
}
