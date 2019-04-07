# ticket-back-end
基于redis和mysql的叫号系统<br/>
用户通过登陆（从mysql获取信息进行验证），进行取号（存Redis中）<br/>
叫号员通过点击叫号，从redis中去除数据进行叫号<br/>
开辟多线程并定时对当天的号数进行清零。<br/>
并使用log4j对程序error进行记录<br/>
第一次使用redis和log4j<br/>

![Image text](https://github.com/z875479694h/ticket-back-end/blob/master/src/main/resources/static/QueueAnNumber.png)
