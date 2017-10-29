package com.springboot.entity;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Created by user on 2017/5/24.
 */
public class ZookeeperLock implements Lock{

    /**
     * zookeeper节点的默认分隔符
     */
    private final static String SEPARATOR = "/";
    /**
     * pandaLock在zk中的根节点
     */
    private final static String ROOT_NODE = SEPARATOR + "zyZookeeperLock";// 熊猫锁的zk根节点

    private static ZooKeeper zooKeeper = null;

    private CountDownLatch latch = new CountDownLatch(1);

    private CountDownLatch getTheLocklatch = new CountDownLatch(1);

    private String lockName = null;

    private String rootPath = null;

    private String lockPath = null;

    private String competitorPath = null;

    private String thisCompetitorPath = null;

    private String waitCompetitorPath = null;

    @Override
    public void lock() {
        List<String> allCompetitorList = null;
        try {
            createComPrtitorNode();
            allCompetitorList = zooKeeper.getChildren(lockPath, false);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Collections.sort(allCompetitorList);
        int index = allCompetitorList.indexOf(thisCompetitorPath
                .substring((lockPath + SEPARATOR).length()));
        if (index == -1) {
            throw new RuntimeException();
        } else if (index == 0) {
            // 如果发现自己就是最小节点,那么说明本人获得了锁
            return;
        } else {
            // 说明自己不是最小节点
            waitCompetitorPath = lockPath+SEPARATOR + allCompetitorList.get(index - 1);
            // 在waitPath上注册监听器, 当waitPath被删除时, zookeeper会回调，通知他的下一个他释放了锁
            Stat waitNodeStat;
            try {
                waitNodeStat = zooKeeper.exists(waitCompetitorPath, new Watcher() {
                    @Override
                    public void process(WatchedEvent event) {
                        if (event.getType().equals(Event.EventType.NodeDeleted)&&event.getPath().equals(waitCompetitorPath)) {
                            getTheLocklatch.countDown();
                        }
                    }
                });
                if (waitNodeStat==null) {
                    //如果运行到此处发现前面一个节点已经不存在了。说明前面的进程已经释放了锁
                    return;
                }else {
                    getTheLocklatch.await();
                    return;
                }
            } catch (KeeperException e) {
                throw new RuntimeException();
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {
        try {
            zooKeeper.delete(thisCompetitorPath, -1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Condition newCondition() {
        return null;
    }

    public void connectZooKeeper(String hosts, String lockName)
            throws InterruptedException,
            IOException, KeeperException {
        Stat rootStat = null;
        Stat lockStat = null;
        if (zooKeeper != null) {
            zooKeeper.close();
            zooKeeper = null;
        }

        if (zooKeeper == null) {
            //创建zookeeper
            zooKeeper = new ZooKeeper(hosts, 500000000,
                    new Watcher() {
                        @Override
                        public void process(WatchedEvent event) {
                            if (event.getState().equals(
                                    Event.KeeperState.SyncConnected)) {
                                latch.countDown();
                            }else if (event.getState() == Event.KeeperState.Expired) {
                                System.out.println("[SUC-CORE] session expired. now rebuilding");

                                //session expired, may be never happending.
                                //close old client and rebuild new client
                                close();

                                try {
                                    connectZooKeeper(hosts, lockName);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (KeeperException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
        }
        latch.await();
        //判断zookeep是否存在根节点
        rootStat = zooKeeper.exists(ROOT_NODE, false);
        if (rootStat == null) {
            //如果不存在创建持久化的根节点
            rootPath = zooKeeper.create(ROOT_NODE, null,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } else {
            rootPath = ROOT_NODE;
        }
        String lockNodePathString = ROOT_NODE + SEPARATOR + lockName;
        lockStat = zooKeeper.exists(lockNodePathString, false);
        if (lockStat != null) {
            // 说明此锁已经存在
            lockPath = lockNodePathString;
        } else {
            // 创建相对应的锁节点
            lockPath = zooKeeper.create(lockNodePathString, null,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }

        this.lockName = lockName;
    }

    public void close() {
        if (zooKeeper != null) {
            try {
                zooKeeper.close();
                zooKeeper = null;
            } catch (InterruptedException e) {
                //ignore exception
            }
        }
    }

    /**
     * 创建竞争者节点
     */
    private void createComPrtitorNode() throws KeeperException,
            InterruptedException {
        competitorPath = lockPath + SEPARATOR + "competitorNode";
        thisCompetitorPath = zooKeeper.create(competitorPath, null,
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    }
}
