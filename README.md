[中文](./README_zh.md) | [English](./README.md)

# Ladeit
[![Build Status](https://travis-ci.com/ladeit/ladeit.svg?branch=master)](https://travis-ci.com/github/ladeit/ladeit)

## Overview
Ladeit is a _CD(Continuous Deployment)_ tool for services deployed on [kubernetes](https://github.com/kubernetes/kubernetes). It makes the deployment convenient and traceable.

As the containerization and micro-service going on, the operation work are more and more heavy. _DevOps_ as the basic support needs to solve that. _CI(continuous integration)_ and _CD_ as the key step of _DevOps_, have different condition. _jenkins_ _travis_ _gitlab-ci_ doing a good job on _CI_, furthermore a part job of _CD_ but not enough, e.g. the rollback and traceable and so on. And ladeit will do that work.

## Features
* continuous deployment
  * automatic/semi-automatic deployment
  * visual deployment
  * traceable deployment
  * rapid rollback
* cluster management
* visual servcies management
  * scaling pods 
  * resources quota
* oblate organization
* web terminal
* status monitoring
* service mesh *alpha*

## Quick start
### Installation

##### docker
```
docker run ladeit/ladeit
```
##### helm
Run the script in the root directory of the project.
``` 
helm install helm/
```
If you would like to create a ingress when init the helm chart,you can run the script like this.
```
helm install helm/ --set ingress.enabled=true
``` 
We strongly recommend that in a production environment, you should provide a persistent volume for the container to hold the database data.
```
helm install helm/ --set volume.enabled=true --set persistentVolumeClaim.enabled=true
```
### Initialization

## Guide

## Screenshot
![1](https://www.docker.com/sites/default/files/d8/styles/role_icon/public/2020-01/DesktopAction%402.png?itok=fSjduwO7)

## Next
-  **deployment in union**
-  **service mesh**   
`istio` doing， `smi` todo
-  **topology**
-  **deploy for thrid party servcie**
