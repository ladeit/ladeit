package com.ladeit.util.k8s;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.GsonBuilder;
import com.ladeit.pojo.dto.metric.pod.PodMetric;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Response;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Pair;
import io.kubernetes.client.util.Config;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: ladeit-parent
 * @description: MetricApi
 * @author: falcomlife
 * @create: 2020/05/30
 * @version: 1.0.0
 */
@Component
public class MetricApi {

	public List<PodMetric> listPodMetric(String config) throws ApiException, IOException {
		Reader reader = new StringReader(config);
		ApiClient apiClient = Config.fromConfig(reader);
		String localVarPath = "/apis/metrics.k8s.io/v1beta1/pods";
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();
		final String[] localVarAccepts = {
				"application/json", "application/yaml", "application/vnd.kubernetes.protobuf", "application/json;" +
				"stream=watch", "application/vnd.kubernetes.protobuf;stream=watch"
		};
		final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}
		final String[] localVarContentTypes = {
				"*/*"
		};
		final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
		localVarHeaderParams.put("Content-Type", localVarContentType);
		String[] localVarAuthNames = new String[]{"BearerToken"};
		Call call = apiClient.buildCall(localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				null, localVarHeaderParams, localVarFormParams, localVarAuthNames, null);
		List<PodMetric> list = new ArrayList<>();
		Response response = call.execute();
		JSONObject j = JSONObject.parseObject(response.body().string());
		j.getJSONArray("items").stream().forEach(item ->{
			list.add(new GsonBuilder().create().fromJson(((JSONObject) item).toJSONString(), PodMetric.class));
		});
		return list;
	}
}
