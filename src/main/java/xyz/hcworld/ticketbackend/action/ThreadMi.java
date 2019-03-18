package xyz.hcworld.ticketbackend.action;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

/**
 * 描述:开辟一个多线程进行0点对key清空
 *
 * @author 张红尘
 * @create 2018-07-05 0:38
 */
 public class ThreadMi implements Runnable {
    /**
     * redis连接对象
     */

    RedisTemplate<String, String> rt;

    public static Logger LOG= Logger.getLogger(ThreadMi.class);

    public ThreadMi(RedisTemplate<String, String> rt){
        this.rt = rt;
    }
    /**定时器*/
    @Override
    public void run() {
        while (true) {
            //获取系统时间并计算出距离00：00的毫秒数
            LocalDateTime midnight = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            long millSeconds = ChronoUnit.MILLIS.between(LocalDateTime.now(), midnight);
            try {
                Thread.sleep(millSeconds);
                Set keys = rt.keys("*");
                //遍历Key并删除
                keys.forEach(string ->  rt.delete(String.valueOf(string)));
                //打印日志
                LOG.info("Delets:"+keys);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
