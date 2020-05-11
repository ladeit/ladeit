package com.ladeit.biz.websocket.log;

import com.alibaba.fastjson.JSONObject;
import com.google.common.io.ByteStreams;
import com.ladeit.biz.config.SpringBean;
import com.ladeit.biz.config.WebSocketConfig;
import com.ladeit.biz.dao.ClusterDao;
import com.ladeit.biz.services.EnvService;
import com.ladeit.biz.services.ServiceService;
import com.ladeit.biz.websocket.events.EventSub;
import com.ladeit.biz.websocket.events.RedisReceiver;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.system.Code;
import com.ladeit.pojo.doo.Cluster;
import com.ladeit.pojo.doo.Env;
import com.ladeit.pojo.doo.Service;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.PodLogs;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.util.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @program: BuzzyCore
 * @description: JobLogWebSocket
 * @author: sora
 * @create: 2019/07/31
 * @version: 1.0.0
 */
@ServerEndpoint(value = "/api/v1/log/{serviceId}/{pod}/{container}", configurator = WebSocketConfig.class)
@Component
@Scope("prototype")
@Slf4j
public class LogWebSocket {

	private Session session;

	/**
	 * 开启链接
	 *
	 * @param session
	 * @FunctionName onOpen
	 * @author sora
	 * @date 19-7-31
	 * @version 1.0.0
	 * @Return void
	 */
	@OnOpen
	public void onOpen(Session session, @PathParam("pod") String pod, @PathParam("container") String container,
					   @PathParam("serviceId") String serviceId) throws IOException, ApiException {
		log.info("成功建立socket链接");
		this.session = session;
		ServiceService serviceService = SpringBean.getObject(ServiceService.class);
		EnvService envService = SpringBean.getObject(EnvService.class);
		ClusterDao clusterDao = SpringBean.getObject(ClusterDao.class);
		ExecuteResult<Service> service = serviceService.getById(serviceId);
		Env env = envService.getEnvById(service.getResult().getEnvId());
		Cluster cluster = clusterDao.getClusterById(env.getClusterId());
		Reader reader = new StringReader(cluster.getK8sKubeconfig());
		ApiClient apiClient = Config.fromConfig(reader);
		Configuration.setDefaultApiClient(apiClient);
		InputStream is = new PodLogs().streamNamespacedPodLog(env.getNamespace(),pod,container);
		ThreadFactory threadFactory = (ThreadFactory) SpringBean.getBean("threadPoolFactory");
		OutputStream outputStream = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				byte[] bb = new byte[4];
				bb[3] = (byte) (b & 0xff);
				bb[2] = (byte) ((b >> 8) & 0xff);
				bb[1] = (byte) ((b >> 16) & 0xff);
				bb[0] = (byte) ((b >> 24) & 0xff);
				ByteBuffer bf = ByteBuffer.wrap(bb);
				sendMessage(bf);
			}
		};
		ThreadPoolExecutor singleThreadPool = new ThreadPoolExecutor(20, 20,
				0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(1024), threadFactory, new ThreadPoolExecutor.AbortPolicy());
		singleThreadPool.execute(() -> {
			try {
 				ByteStreams.copy(is, outputStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * 关闭链接
	 *
	 * @param
	 * @FunctionName onClose
	 * @author sora
	 * @date 19-7-31
	 * @version 1.0.0
	 * @Return void
	 */
	@OnClose
	public void onClose() {
		try {
			this.session.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		log.info("socket链接关闭");
	}

	/**
	 * 接收到client端信息
	 *
	 * @param message
	 * @param session
	 * @FunctionName onMessagell
	 * @author sora
	 * @date 19-7-31
	 * @version 1.0.0
	 * @Return void
	 */
	@OnMessage
	public void onMessage(String message, Session session) {
		log.info("接收到client端信息" + message);
	}

	/**
	 * 异常
	 *
	 * @param session
	 * @param error
	 * @FunctionName onError
	 * @author sora
	 * @date 19-7-31
	 * @version 1.0.0
	 * @Return void
	 */
	@OnError
	public void onError(Session session, Throwable error) {
		log.info("发生异常" + error);
	}

	/**
	 * 向前台传送数据
	 *
	 * @param content
	 * @FunctionName sendMessage
	 * @author sora
	 * @date 19-7-31
	 * @version 1.0.0
	 * @Return void
	 */
	public synchronized void sendMessage(String content) throws IOException {
		this.session.getBasicRemote().sendText(content);
	}


	/**
	 * 向前台传送数据
	 *
	 * @param bf
	 * @FunctionName sendMessage
	 * @author sora
	 * @date 19-7-31
	 * @version 1.0.0
	 * @Return void
	 */
	public synchronized void sendMessage(ByteBuffer bf) throws IOException {
		this.session.getBasicRemote().sendBinary(bf);
	}
}
