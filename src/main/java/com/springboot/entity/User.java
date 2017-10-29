package com.springboot.entity;

import javax.persistence.Entity;
import java.io.Serializable;

/**
 * Created by user on 2017/5/24.
 */
public class User implements Serializable {

    private Long id;
    private Long secondId;


    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSecondId() {
        return secondId;
    }

    public void setSecondId(Long secondId) {
        this.secondId = secondId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
