package com.springboot.service;


import com.springboot.entity.JsonResult;
import com.springboot.entity.User;

/**
 * Created by user on 2017/5/24.
 */
public interface UserService {

    /**
     *
     * @param id
     * @return
     */
    User findUserById(Long id);

    void deleteUserFromCache(Long id);

    /**
     *u
     * @param user
     * @return
     */
    JsonResult updateUsers(User user);
}
