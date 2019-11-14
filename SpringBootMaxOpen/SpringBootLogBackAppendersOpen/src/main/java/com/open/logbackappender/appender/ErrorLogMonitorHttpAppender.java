package com.open.logbackappender.appender;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import com.alibaba.fastjson.JSON;
import com.open.logbackappender.http.HttpClientUtil;
import com.open.logbackappender.util.IpUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import static ch.qos.logback.core.net.AbstractSocketAppender.DEFAULT_RECONNECTION_DELAY;


/**
 * Created by shuai on 2019/1/3.
 */
public class ErrorLogMonitorHttpAppender<E> extends AppenderBase<E> {

    private String url;
    private String localSystemName;
    private String env;
    private LinkedBlockingQueue<E> queue = new LinkedBlockingQueue<E>(512);
    protected Encoder<E> encoder;
    private HttpClientUtil httpClientUtil;
    private static final int DEFAULT_ACCEPT_CONNECTION_DELAY = 5000;
    private int acceptConnectionTimeout = DEFAULT_ACCEPT_CONNECTION_DELAY;
    private Integer isSleep = Integer.valueOf(0);

    public LinkedBlockingQueue<E> getQueue() {
        return queue;
    }

    public void setQueue(LinkedBlockingQueue<E> queue) {
        this.queue = queue;
    }

    public Encoder<E> getEncoder() {
        return encoder;
    }

    public void setEncoder(Encoder<E> encoder) {
        this.encoder = encoder;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocalSystemName() {
        return localSystemName;
    }

    public void setLocalSystemName(String localSystemName) {
        this.localSystemName = localSystemName;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public void start() {
        if (isStarted()) {
            addError(" Has been launched。 Don't create again！");
            return;
        }
        started = true;
        httpClientUtil = HttpClientUtil.getHttpClientUtil();
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        send();
                    }
                }
        ).start();
    }

    protected void send() {
        while (true) {
            try {
                if (isSleep.equals(Integer.valueOf(1))) {
                    Thread.sleep(DEFAULT_RECONNECTION_DELAY);
                    isSleep = Integer.valueOf(0);
                }
                E event = queue.take();
                System.out.println(JSON.toJSONString(event));
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("localIp", IpUtils.getRealIp());
                parameters.put("localSystemName", localSystemName);
                parameters.put("env", env);
                parameters.put("body", new String(this.encoder.encode(event), "utf-8"));
                httpClientUtil.doPost(url, null, parameters, "utf-8", acceptConnectionTimeout,
                        DEFAULT_ACCEPT_CONNECTION_DELAY);
            } catch (Throwable t) {
                addError("send errorLog Exception:", t);
                tryReAddingEventToFrontOfQueue();
            }
        }
    }

    private void tryReAddingEventToFrontOfQueue() {
        try {
            queue.clear();
            httpClientUtil = HttpClientUtil.getHttpClientUtil();
            isSleep = Integer.valueOf(1);
        } catch (Exception e) {
            addError("tryReAddingEventToFrontOfQueue exception :", e);
        }

    }


    @Override
    protected void append(E eventObject) {
        if (eventObject != null) {
            try {
                queue.offer(eventObject);
            } catch (Throwable t) {
                addError("offer LinkedBlockingQueue error:", t);
            }
        }
    }

}