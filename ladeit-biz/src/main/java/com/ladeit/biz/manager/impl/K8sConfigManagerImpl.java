package com.ladeit.biz.manager.impl;

import com.ladeit.biz.manager.K8sConfigManager;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.system.Code;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class K8sConfigManagerImpl implements K8sConfigManager {

}
