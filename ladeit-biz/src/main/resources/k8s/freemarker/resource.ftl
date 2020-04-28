apiVersion: v1
kind: ${k8s.kind}
metadata:
  name: ${k8s.metaData.name}
  namespace: ${k8s.metaData.spec}
  labels:
  	<#list k8s.metaData.labels?keys as key>
    ${key}: ${k8s.metaData.labels["${key}"]}
  	</#list>
  annotations: 
    <#list k8s.metaData.annotations?keys as key>
    ${key}: ${k8s.metaData.labels["${key}"]}
  	</#list>
spec:
  selector:
    matchLabels:
      app: ${k8s.spec.app}
  serviceName: ${k8s.spec.serviceName}
  replicas: ${k8s.spec.replicas}
  volumeClaimTemplates:
  <#list k8s.spec.vctMetaData as vctMetaData>
  - metadata:
      name: ${vctMetaData.name}
      annotations:
        volume.beta.kubernetes.io/storage-class: ${vctMetaData.annotations}
    spec:
      accessModes: ${vctMetaData.accessModes}
      resources:
        requests:
          storage: ${vctMetaData.storage}
  </#list>
  template:
    metadata:
      labels:
      	<#list k8s.spec.template?keys as key>
        ${key}: ${k8s.spec.template["${key}"]}
        </#list>
    spec:
      terminationGracePeriodSeconds: 10
      containers:
      - name: ${k8s.containers.name}
        image: ${k8s.containers.image}
        imagePullPolicy: ${k8s.containers.imagePullPolicy}
        ports:
      <#list k8s.containers.ports as port>
      - name: ${port.portName}
        containerPort: ${port.containerPort}
      </#list>
      env:
      <#list k8s.containers.env as env>
      - name: ${env.envName} 
        value: ${env.envValue}
      </#list>
      volumeMounts:
      <#list k8s.containers.volumeMounts as volumeMounts>
      - mountPath: ${volumeMounts.mountPath}
        name: ${volumeMounts.name}
	  </#list>
      volumes:
      <#list k8s.containers.volumes as volumes>
      - name: ${volumes.name}
        hostPath:
          path: ${volumes.path}
      </#list>