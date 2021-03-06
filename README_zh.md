[English](./README.md) | [中文](./README_zh.md)

# Ladeit
[![Build Status](https://travis-ci.com/ladeit/ladeit.svg?branch=master)](https://travis-ci.com/github/ladeit/ladeit)
![ladeit](https://img.shields.io/github/license/ladeit/ladeit?style=flat&color=success)
![ladeit logo](docs/images/ladeit-logo.svg)

## Overview
ladeit 是一个基于 [kubernetes](https://github.com/kubernetes/kubernetes) 的 _CD(Continuous Deployment)_ 工具，可以方便、可追溯的发布服务。

随着容器化和微服务化的发展，_DevOps_ 作为基础支持急需将开发及运维人员从繁重的上线流程中解救出来，_CI(continuous integration)_ _CD_ 作为 _DevOps_ 的关键步骤，其中 _jenkins_ _travis_ _gitlab-ci_ 等优秀的 _CI_ 工具完美的解决了持续交付问题，虽然也提供了部分 _CD_ 功能，但 _CD_ 环节中回滚、操作可追溯等需求还在困扰着运维人员，而 ladeit 要解决的就是上述问题。

## 概念/功能介绍
* 持续发布
  * 自动/半自动发布
  * 可视化发布
  * 发布可追溯
  * 快速回滚
* 集群管理
* 可视化服务管理
  * pod 伸缩
  * 资源配置
* 成员扁平化管理
* web terminal
* 运行状态监控
* *服务网格 alpha*

## Quick start


### 试用

docker
```
// docker 启动不支持 webkubectl 功能
docker run -p 8000:8000 ladeit/ladeit
```
k8s
```
kubectl apply -f "https://raw.githubusercontent.com/ladeit/ladeit/master/ladeit.yml"
```
helm
``` 
helm repo add ladeit https://ladeit.github.io/charts
helm install ladeit/ladeit --version {LATEST_VERSION}
```

### 正式使用

docker
```
// docker 启动不支持 webkubectl 功能
docker run -idt --name ladeit -p 8000:8000 -v PATH_ON_HOST:/root/.ladeit ladeit/ladeit
```
k8s
> 请创建 `persistent volume` 后执行: 
```
kubectl apply -f "https://raw.githubusercontent.com/ladeit/ladeit/master/ladeit.yml"
```
helm
> 请创建 `persistent volume` 后执行: 
```
helm repo add ladeit https://ladeit.github.io/charts
helm install ladeit/ladeit --set volume.enabled=true --set persistentVolumeClaim.enabled=true --version {LATEST_VERSION}
```

## Guide

## 截图

## 计划

-  **联合发布**
-  **服务网格**   
`istio`进行中， `smi`尚未开始
-  **服务拓扑**
-  **第三方服务发布**
