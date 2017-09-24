package com.khudim.utils;

public class Utilities {

    public static void sleep() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }
}
