package com.springboot.serviceImpl;

import com.springboot.entity.JsonResult;
import com.springboot.entity.ResultCode;
import com.springboot.entity.User;
import com.springboot.mapper.UserMapper;
import com.springboot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;

/**
 * Created by user on 2017/5/24.
 */
@Service
@Transactional(value="txManager1")
public class UserServiceImpl  implements UserService {

   @Autowired
   private UserMapper userMapper;

    @Resource
    private RedisTemplate<String,String> redisTemplate;

    @Cacheable(value="user") //缓存,这里没有指定key.
    public User findUserById(Long id){
       System.err.println("=========从数据库中进行获取的....id="+id);
       return userMapper.findUserById(id);
        // return user;
    }

    @CacheEvict(value="user")
    public void deleteUserFromCache(Long id) {
        System.out.println("user从缓存中删除.");
    }

    public JsonResult updateUsers(User user){
        JsonResult jsonResult = null;
        try {
            int updatecount = userMapper.updateUsers(user.getId());
            int updatecount2 = userMapper.updateUsersIds(user.getSecondId());
            jsonResult = new JsonResult(ResultCode.SUCCESS, "更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            jsonResult = new JsonResult(ResultCode.PARAMS_ERROR, "更新失败");
        }

        return jsonResult;
    }

}
