package com.ladeit.biz.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * @ClassName: HttpHelper
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author <a href="MailTo:data55.126.com">杨磊</a>
 * @date 2015-8-3 下午3:31:48
 * @version 1.0-SNAPSHOT
 */
public class HttpHelper {
	public static String encoding = Charset.defaultCharset().name();
	private static RequestConfig requestConfig;
	static {
		requestConfig = RequestConfig.custom().setConnectionRequestTimeout(50000).setConnectTimeout(50000).setSocketTimeout(50000).build();
	}

	public static String doSendPost(String url, String content, Map<String, String> header)
			throws Exception {
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		// 解决中文乱码
		StringEntity entity = new StringEntity(content, "utf-8");
		entity.setContentType("application/json");
		if (header != null && header.isEmpty()) {
			Iterator<String> keys = header.keySet().iterator();
			while (keys.hasNext()) {
				String name = keys.next();
				String value = header.get(name);
				httpPost.setHeader(name, value);
			}
		}
		httpPost.setEntity(entity);
		httpPost.setConfig(requestConfig);
		HttpResponse httpResponse = httpClient.execute(httpPost);
		BufferedReader buffer = new BufferedReader(new InputStreamReader(httpResponse.getEntity()
				.getContent()));
		StringBuffer stb = new StringBuffer();
		String line = null;
		while ((line = buffer.readLine()) != null) {
			stb.append(line);
		}
		buffer.close();
		return stb.toString();
	}

	public static String doSendPostText(String url, String content, Map<String, String> header)
			throws Exception {
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		// 解决中文乱码
		StringEntity entity = new StringEntity(content, "utf-8");
		entity.setContentType("application/text");
		if (header != null && !header.isEmpty()) {
			Iterator<String> keys = header.keySet().iterator();
			while (keys.hasNext()) {
				String name = keys.next();
				String value = header.get(name);
				httpPost.setHeader(name, value);
			}
		}
		httpPost.setEntity(entity);
		HttpResponse httpResponse = httpClient.execute(httpPost);
		BufferedReader buffer = new BufferedReader(new InputStreamReader(httpResponse.getEntity()
				.getContent()));
		StringBuffer stb = new StringBuffer();
		String line = null;
		while ((line = buffer.readLine()) != null) {
			line = new String(line.getBytes(), "utf-8");
			stb.append(line);
		}
		buffer.close();
		return stb.toString();
	}

	public static String doSendGet(String url, Map<String, String> params, String contentType,
			Map<String, String> header) throws Exception {
		// get方法参数显示在url中，需要将参数拼接到url上
		StringBuffer buf = new StringBuffer(url);
		if (params != null && !params.isEmpty()) {
			buf.append("?");
			for (String key : params.keySet()) {
				buf.append(key).append("=");

				buf.append(URLEncoder.encode(params.get(key), encoding));
				buf.append("&");
			}
		}
		HttpGet get = new HttpGet(buf.toString());
		get.setConfig(requestConfig);
		get.setHeader(HttpHeaders.ACCEPT, contentType);// "application/xml"//"application/json"
		get.setHeader(HttpHeaders.CONTENT_ENCODING, encoding);// utf-8
		if (header != null && !header.isEmpty()) {
			for (String keyHeader : header.keySet()) {
				get.setHeader(keyHeader, header.get(keyHeader));
			}
		}
		HttpResponse httpResponse = new DefaultHttpClient().execute(get);
		BufferedReader buffer = new BufferedReader(new InputStreamReader(httpResponse.getEntity()
				.getContent()));
		StringBuffer sbf = new StringBuffer();
		String line = null;
		while ((line = buffer.readLine()) != null) {
			sbf = sbf.append(line);
		}
		buffer.close();
		return sbf.toString();
	}

	public static void main(String[] args) {
		String json = "{\"OrderNo\":\"TEST15070713\",\"Status\":\"10030\",\"Porxy\":\"SADA\",\"SysCode\":\"NI1507310259\",\"DeliveryDate\":\"2015-08-20\"}";
		try {
			System.out.println(doSendPost(
					"http://services.rcmtm.com/jservice/api/queue/orderStatus", json, null));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
