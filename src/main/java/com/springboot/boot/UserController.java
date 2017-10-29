package com.springboot.boot;

import com.springboot.entity.CreateIOrderNum;
import com.springboot.entity.JsonResult;
import com.springboot.entity.ResultCode;
import com.springboot.entity.ZookeeperLock;
import com.springboot.service.UserService;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by user on 2017/5/24.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    CreateIOrderNum createIOrderNum = new CreateIOrderNum();;

    /**
     *
     * @return
     */
    @RequestMapping(value = "/testZookeeperLock", method = RequestMethod.POST)
    public JsonResult testZookeeperLock(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, String> map) throws InterruptedException {

        JsonResult jsonResult = null;
        String count = map.get("limitCount");
        //模拟并发量
        CountDownLatch mainlatch = new CountDownLatch(Integer.parseInt(count));
        for (int i = 0; i < Integer.parseInt(count); i++) {
            LimitRun limitRun = new LimitRun(mainlatch);
            Thread thread = new Thread(limitRun);
            thread.setName("limitThread" + i);
            thread.start();
            mainlatch.countDown();
        }


        jsonResult = new JsonResult();
        jsonResult.setCode(ResultCode.SUCCESS);

        return jsonResult;
    }

    public class LimitRun implements Runnable {

        private CountDownLatch mainLatch;


        public LimitRun(CountDownLatch mainLatch) {
            this.mainLatch = mainLatch;
        }

        @Override
        public void run() {
            try {
                mainLatch.await();
                ZookeeperLock zookeeper = new ZookeeperLock();
                try {
                    zookeeper.connectZooKeeper("127.0.0.1:2181", "zyZookeeperNode");
                    zookeeper.lock();
                    String num = createIOrderNum.createOrderNum();
                    System.out.println(num);
                    zookeeper.unlock();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (KeeperException e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
