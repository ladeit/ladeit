package com.ladeit.common.holder;

import io.kubernetes.client.ApiCallback;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1Deployment;

import java.util.Map;

/**
 * @author falcomlifew
 */
public class K8sApiCallback implements ApiCallback {

    private K8sHolder holder;

    public K8sApiCallback(K8sHolder holder) {
        this.holder = holder;
    }

    @Override
    public void onFailure(ApiException e, int i, Map map) {
        e.printStackTrace();
        System.out.println("fail");
        this.holder.go();
    }

    @Override
    public void onSuccess(Object o, int i, Map map) {
        V1Deployment deployment = (V1Deployment) o;
        System.out.println("namespace:" + deployment.getMetadata().getNamespace() + ",name:" + deployment.getMetadata().getName() + "success");
        this.holder.go();
    }

    @Override
    public void onUploadProgress(long l, long l1, boolean b) {
        System.out.println("upload");
    }

    @Override
    public void onDownloadProgress(long l, long l1, boolean b) {
        System.out.println("download");
    }
}
