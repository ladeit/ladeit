package com.ladeit.biz.websocket.terminal;

import com.google.common.io.ByteStreams;
import com.ladeit.biz.config.SpringBean;
import com.ladeit.biz.config.WebSocketConfig;
import com.ladeit.biz.dao.ClusterDao;
import com.ladeit.biz.services.EnvService;
import com.ladeit.biz.services.ServiceService;
import com.ladeit.common.ExecuteResult;
import com.ladeit.pojo.doo.Cluster;
import com.ladeit.pojo.doo.Env;
import com.ladeit.pojo.doo.Service;
import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.Dispatcher;
import com.squareup.okhttp.OkHttpClient;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.util.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.cli.*;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@ServerEndpoint(value = "/api/v1/terminal/socket/{serviceId}/{pod}/{container}", configurator = WebSocketConfig.class)
@Component
@Slf4j
public class K8sTerminalSocket {
    private static CopyOnWriteArraySet<K8sTerminalSocket> socketSet = new CopyOnWriteArraySet<>();
    private Session session;
    private String serviceId;
    private Process process;
    private long timetest;
    private ThreadPoolExecutor singleThreadPool;
    private Thread in;
    private boolean threadFlag;

    /**
     * 开启链接
     *
     * @param session
     * @FunctionName onOpen
     * @author falcomlife
     * @date 19-7-31
     * @version 1.0.0
     * @Return void
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("pod") String pod, @PathParam("container") String container,
                       @PathParam("serviceId") String serviceId) throws IOException, ParseException {
        // 初始化参数
        this.session = session;
        this.serviceId = serviceId;
        this.threadFlag = true;
        String[] args = new String[0];
        ServiceService serviceService = SpringBean.getObject(ServiceService.class);
        EnvService envService = SpringBean.getObject(EnvService.class);
        ClusterDao clusterDao = SpringBean.getObject(ClusterDao.class);
        ExecuteResult<Service> service = serviceService.getById(serviceId);
        Env env = envService.getEnvById(service.getResult().getEnvId());
        Cluster cluster = clusterDao.getClusterById(env.getClusterId());
        Map<String, List<Byte>> pool = (Map<String, List<Byte>>) SpringBean.getBean("terminalPool");
        ThreadFactory threadFactory = (ThreadFactory) SpringBean.getBean("threadPoolFactory");
        List<Byte> listbyte = new ArrayList<>();
        String poolid = Thread.currentThread().getThreadGroup().getName() + Thread.currentThread().getId();
        pool.put(poolid, listbyte);
        // 封装命令行
        final Options options = new Options();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        List<String> commands = new ArrayList<>();
        args = cmd.getArgs();
        for (int i = 0; i < args.length; i++) {
            commands.add(args[i]);
        }
        // 设置okhttp参数
        Reader reader = new StringReader(cluster.getK8sKubeconfig());
        ApiClient apiClient = Config.fromConfig(reader);
        OkHttpClient httpClient = apiClient.getHttpClient();
        httpClient.setReadTimeout(1800, TimeUnit.SECONDS);
        httpClient.setWriteTimeout(1800, TimeUnit.SECONDS);
        ConnectionPool connectionPool = new ConnectionPool(10, 1000 * 60 * 5, TimeUnit.MILLISECONDS);
        httpClient.setConnectionPool(connectionPool);
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(1024);
        dispatcher.setMaxRequestsPerHost(3);
        httpClient.setDispatcher(dispatcher);
        apiClient.setHttpClient(httpClient);
        Configuration.setDefaultApiClient(apiClient);
        // 创建链接
        Exec exec = new Exec(apiClient);
        try {
            process = exec.exec(env.getNamespace(), pod, commands.isEmpty() ? new String[]{"/bin/bash"} :
                    commands. toArray(new String[commands.size()]), container, true, true, poolid);
            // 开启线程接受数据并发送到前台
            this.singleThreadPool = new ThreadPoolExecutor(20, 20,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(1024), threadFactory, new ThreadPoolExecutor.AbortPolicy());
            this.singleThreadPool.execute(() -> this.receiveMessage(listbyte));
        } catch (ApiException e) {
            log.error(e.getMessage(), e);
        }

    }

    /**
     * 关闭链接
     *
     * @param
     * @FunctionName onClose
     * @author falcomlife
     * @date 19-7-31
     * @version 1.0.0
     * @Return void
     */
    @OnClose
    public void onClose() {
        K8sTerminalSocket.socketSet.remove(this);
        try {
            this.session.close();
            this.process.destroy();
            this.threadFlag = false;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        log.info("K8sTerminalSocket close");
    }

    /**
     * 接收到client端信息
     *
     * @param messageInner
     * @param session
     * @FunctionName onMessagell
     * @author falcomlife
     * @date 19-7-31
     * @version 1.0.0
     * @Return void'
     */
    @OnMessage
    public void onMessage(String messageInner, Session session) throws IOException {
        this.timetest = System.currentTimeMillis();
        String str = "";
        // 转换换行符
        if ("\r".equals(messageInner)) {
            str = "\n";
        } else {
            str = messageInner;
        }
        // 把数据发送到容器内部
        InputStream inputStream = new ByteArrayInputStream(str.getBytes("UTF-8"));
        ByteStreams.copy(inputStream, process.getOutputStream());
    }

    /**
     * 异常
     *
     * @param session
     * @param error
     * @FunctionName onError
     * @author falcomlife
     * @date 19-7-31
     * @version 1.0.0
     * @Return void
     */
    @OnError
    public void onError(Session session, Throwable error) throws IOException {
        log.info("error:" + error.getMessage(), error);
        this.session.close();
        this.process.destroy();
        this.threadFlag = false;
    }

    /**
     * 向前台传送数据
     *
     * @param result
     * @FunctionName sendMessage
     * @author falcomlife
     * @date 19-7-31
     * @version 1.0.0
     * @Return void
     */
    public void sendMessage(String result) {
        this.session.getAsyncRemote().sendText(result);
    }

    /**
     * 向前台传送数据
     *
     * @FunctionName sendMessage
     * @author falcomlife
     * @date 19-7-31
     * @version 1.0.0
     * @Return void
     */
    public void sendMessage(ByteBuffer bf) throws IOException {
        this.session.getAsyncRemote().sendBinary(bf);
    }

    /**
     * 开启线程接受数据并发送到前台
     *
     * @param listbyte
     * @return void
     * @author falcomlife
     * @date 19-10-16
     * @version 1.0.0
     */
    public void receiveMessage(List<Byte> listbyte) {
        while (true) {
            if (!threadFlag) {
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Byte[] bytes = new Byte[listbyte.size()];
            listbyte.toArray(bytes);
            byte[] bytesOrigin = ArrayUtils.toPrimitive(bytes);
            try {
                if (listbyte.size() != 0) {
                    ByteBuffer bf = ByteBuffer.wrap(bytesOrigin);
                    sendMessage(bf);
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            listbyte.clear();
        }
    }
}
