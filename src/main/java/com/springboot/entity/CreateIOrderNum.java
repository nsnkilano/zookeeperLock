package com.springboot.entity;

import java.util.Date;

/**
 * Created by user on 2017/5/24.
 */
public class CreateIOrderNum {

    public static int num = 0;

    public String createOrderNum () {

        String numStr = new Date().toString() + "--" +  num;
        num++;
        return numStr;
    }

}
