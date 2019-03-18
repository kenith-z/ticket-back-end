package xyz.hcworld.ticketbackend.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import xyz.hcworld.ticketbackend.model.User;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

/**
*叫号后台
* @author: 张红尘
* @date: 2018/7/2
**/
@RestController
public class LineUp{
    /**
     * 常量
     */
    public final static String ZERO = "0", ONE = "1", TICKET_ = "ticket_", str_ = "_";
    /**
     * 号数（只增）
     */
    public final static String TICKET_SUM_NUMBER = "ticket_snm_number";
    /**
     * 排队数
     */
    public final static String TICKET_WAIT_NUMBER = "ticket_wait_number";
    @Autowired
    private JdbcTemplate jt;
    /**
     * redis连接对象
     */
    @Autowired
    RedisTemplate<String, String> rt;

    /**
     * 叫号系统
     *
     * @param user
     * @return
     */
    @PostMapping(value = "/line/index")
    public List getInfo(@RequestBody User user) {
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, String> cont = new HashMap<>(10);
        //radis字符串
        ValueOperations<String, String> vo = rt.opsForValue();
        //radis hash操作对象
        HashOperations<String, Object, Object> ho = rt.opsForHash();
        //radis list操作对象
        ListOperations<String, String> lo = rt.opsForList();
        //传User参数
        String UserId = user.getUserId(), Business = user.getBusiness();
        //拿到姓名
        String sql = "SELECT username FROM ticket_user WHERE userid=?";
        List<Map<String, Object>> listsql = jt.queryForList(sql, UserId);
        if (listsql.size() > 0) {
            Map<String, Object> map = listsql.get(0);
            cont.put("name", map.get("username").toString());
        } else {
            return null;
        }
        //拿到号数（只增）

        String sumNumber = vo.get(TICKET_SUM_NUMBER);
        int sum = 1, number = 0;
        if (sumNumber == null) {
            vo.set(TICKET_SUM_NUMBER, ONE);
        } else {
            sum = Integer.valueOf(sumNumber) + 1;
            vo.set(TICKET_SUM_NUMBER, String.valueOf(sum));
        }
        number = sum;
        cont.put(TICKET_SUM_NUMBER, vo.get(TICKET_SUM_NUMBER));
        //获取排队数
        sumNumber = vo.get(TICKET_WAIT_NUMBER);
        if (sumNumber == null) {
            vo.set(TICKET_WAIT_NUMBER, ZERO);
            sumNumber = ZERO;
        }
        sum = Integer.valueOf(sumNumber);
        cont.put(TICKET_WAIT_NUMBER, String.valueOf(sum));
        vo.set(TICKET_WAIT_NUMBER, String.valueOf(sum + 1));
        //把所有信息存进去
        String userId = "userid", business = "business", date = "date";
        ho.put(TICKET_ + number + str_ + Business, TICKET_SUM_NUMBER, cont.get(TICKET_SUM_NUMBER));
        ho.put(TICKET_ + number + str_ + Business, userId, user.getUserId());
        ho.put(TICKET_ + number + str_ + Business, business, user.getBusiness());
        lo.rightPush(TICKET_ + Business, String.valueOf(number));
        //时间
        Date day = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        cont.put(date, df.format(day));
        list.add(cont);

        Set keys = rt.keys("*");
        keys.forEach(string -> {
            System.out.println(string);
            /* rt.delete(String.valueOf(string));*/});

        return list;
    }

    @GetMapping(value = "/line/call")
    public List setInfo(@RequestParam(value = "Business") String Business) {
        //radis hash操作对象
        HashOperations<String, Object, Object> ho = rt.opsForHash();
        //radis字符串
        ValueOperations<String, String> vo = rt.opsForValue();
        //radis list操作对象
        ListOperations<String, String> lo = rt.opsForList();
        //叫道的号数
        String sumNumber = lo.leftPop(TICKET_ + Business);
        int number = 0;
        if (sumNumber != null) {
            number = Integer.valueOf(sumNumber);
        }
        //等待的号数
        String key = new String(TICKET_WAIT_NUMBER);
        String waitNumber = vo.get(key);
        int sum = 0;
        if (waitNumber == null) {
            vo.set(key, ZERO);
        } else {
            sum = Integer.valueOf(waitNumber) - 1;
            if (sum <= 1) {
                rt.delete(TICKET_WAIT_NUMBER);
            } else {
                vo.set(key, String.valueOf(sum));
            }
        }
        List<Map<Object, Object>> list = new ArrayList<>();
        Map<Object, Object> map = ho.entries(TICKET_ + number + str_ + Business);
        list.add(map);
        //清除redis的Map
        rt.delete(TICKET_ + number + str_ + Business);

        return list;
    }
    /**启动定时器*/
    @GetMapping(value = "/line/start")
    public void startTh(){
        /**线程池，
         * ThreadPoolExecutor(int corePoolSize,
         *  int maximumPoolSize,
         *  long keepAliveTime,
         *  TimeUnit unit,
         *  BlockingQueue<Runnable> workQueue,
         *  ThreadFactory threadFactory,
         *  RejectedExecutionHandler handler)
         *
         *  corePoolSize - 线程池核心池的大小。
         *  maximumPoolSize - 线程池的最大线程数。
         *  keepAliveTime - 当线程数大于核心时，此为终止前多余的空闲线程等待新任务的最长时间。
         *  unit - keepAliveTime 的时间单位。
         *  workQueue - 用来储存等待执行任务的队列。
         *  threadFactory - 线程工厂。
         *  handler - 拒绝策略。
         *
         * */
        ThreadPoolExecutor ex = new ThreadPoolExecutor(1, 1,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>());
        ex.execute(new ThreadMi(rt));
        ex.shutdown();
    }

}