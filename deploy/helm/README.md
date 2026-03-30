# Helm 部署说明

这里放的是 EMS4J 在单机 K3s 场景下的首版 Helm Chart。

## Chart 划分

- `ems-infra`：部署 MySQL、Redis、RabbitMQ
- `ems-app`：部署 Backend、Frontend

## 前置条件

- K3s 已安装并正常运行
- 已创建命名空间：
  - `ems-infra`
  - `ems-app`
- 两个命名空间中都已创建 Harbor 拉取密钥：
  - `harbor-pull-secret`

## 环境变量

先设置 Harbor 地址：

```bash
export HARBOR=<harbor-host>:<harbor-port>
```

## 构建并推送镜像

## 创建命名空间和拉取密钥
```bash
kubectl create namespace ems-infra
kubectl create namespace ems-app
kubectl create secret docker-registry harbor-pull-secret \
  --namespace ems-infra \
  --docker-server=$HARBOR \
  --docker-username='<harbor-username>' \
  --docker-password='<harbor-password>'

kubectl create secret docker-registry harbor-pull-secret \
  --namespace ems-app \
  --docker-server=$HARBOR \
  --docker-username='<harbor-username>' \
  --docker-password='<harbor-password>'

```

## 安装 `ems-infra`

```bash
helm upgrade --install ems-infra ./deploy/helm/ems-infra \
  -n ems-infra \
  --set global.imagePullSecrets[0].name=harbor-pull-secret \
  --set image.registry=$HARBOR/ems \
  --set-file mysql.initScripts.ems=deploy/mysql/init/001-ems.sql \
  --set-file mysql.initScripts.menu=deploy/mysql/init/002-menu.sql \
  --set-file mysql.initScripts.example=deploy/mysql/init/003-example.sql \
  --set-file mysql.initScripts.iot=deploy/mysql/init/101-iot.sql
```

## 安装 `ems-app`

```bash
helm upgrade --install ems-app ./deploy/helm/ems-app \
  -n ems-app \
  --set global.imagePullSecrets[0].name=harbor-pull-secret \
  --set image.registry=$HARBOR/ems
```

## 访问方式

- 前端：`http://<服务器IP>:30080`
- 如需直接暴露后端，可在 `ems-app/values.yaml` 中打开 backend 的 `NodePort`

## 说明

- 前端镜像中已经带了 `/api` 反向代理，请保持后端 `Service` 名称为 `backend`
- 当前 chart 默认使用以下服务名：
  - MySQL：`mysql.ems-infra.svc.cluster.local`
  - Redis：`redis.ems-infra.svc.cluster.local`
  - RabbitMQ：`rabbitmq.ems-infra.svc.cluster.local`
