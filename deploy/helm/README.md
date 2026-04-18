# Helm 部署说明

这里放的是 EMS4J 在单机 K3s 场景下的首版 Helm Chart。

## Chart 划分

- `ems-infra`：部署 MySQL、Redis、RabbitMQ
- `ems-app`：部署 Backend、Frontend、IOT、IOT Simulator

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
```bash
export TAG=x.y.z
docker build -f deploy/backend/Dockerfile -t $HARBOR/ems/backend:$TAG .
docker build -f deploy/frontend/Dockerfile -t $HARBOR/ems/frontend:$TAG .
docker build -f deploy/iot/Dockerfile -t $HARBOR/ems/iot:$TAG .
docker build -f deploy/iot-simulator/Dockerfile -t $HARBOR/ems/iot-simulator:$TAG .
docker build -f deploy/rabbitmq/Dockerfile -t $HARBOR/ems/rabbitmq-delayed:$TAG .

docker login $HARBOR
docker push $HARBOR/ems/backend:$TAG
docker push $HARBOR/ems/frontend:$TAG
docker push $HARBOR/ems/iot:$TAG
docker push $HARBOR/ems/iot-simulator:$TAG
docker push $HARBOR/ems/rabbitmq-delayed:$TAG
```

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

## `ems-app` 版本管理

`ems-app` 默认按以下优先级解析镜像版本：

- `backend.image.tag` / `frontend.image.tag` / `iot.image.tag` / `iotSimulator.image.tag`
- `global.imageTag`
- `Chart.yaml` 中的 `appVersion`

常规整体发版时，只需要修改 [deploy/helm/ems-app/Chart.yaml](./ems-app/Chart.yaml) 的 `appVersion`。
如果四个应用镜像版本一致，不需要再分别修改 `values.yaml` 中的四处 `image.tag`。

例如，将 `appVersion` 从：

```yaml
appVersion: "0.6.0"
```

改为：

```yaml
appVersion: "0.6.1"
```

即可让 `backend`、`frontend`、`iot`、`iot-simulator` 默认都使用 `0.6.1`。

如果发版时不想改文件，也可以在命令行统一覆盖：

```bash
helm upgrade --install ems-app ./deploy/helm/ems-app \
  -n ems-app \
  --set global.imagePullSecrets[0].name=harbor-pull-secret \
  --set image.registry=$HARBOR/ems \
  --set global.imageTag=$TAG
```

如果只有单个服务要临时覆盖版本，继续使用对应的组件字段即可：

```bash
helm upgrade --install ems-app ./deploy/helm/ems-app \
  -n ems-app \
  --set global.imagePullSecrets[0].name=harbor-pull-secret \
  --set image.registry=$HARBOR/ems \
  --set global.imageTag=0.6.1 \
  --set iot.image.tag=0.6.1-hotfix
```

建议保持以下约定：

- `Chart.yaml.version`：Helm Chart 自身版本
- `Chart.yaml.appVersion`：默认应用镜像版本

## 部署后检查

```bash
kubectl get pods -n ems-infra
kubectl get pods -n ems-app
kubectl get svc -n ems-app
kubectl get pvc -n ems-app
```

重点确认：

- `ems-infra` 中 `mysql`、`redis`、`rabbitmq` 均为 `Running`
- `ems-app` 中 `backend`、`frontend`、`iot`、`iot-simulator` 均为 `Running`
- `frontend` 的 `NodePort` 为 `30080`
- `iot-netty-nodeport` 的 `NodePort` 为 `31950`

## 查看 IOT 与模拟器日志

```bash
kubectl logs -n ems-app deploy/iot --tail=200
kubectl logs -n ems-app deploy/iot-simulator --tail=200
```

重点观察：

- `iot` 已启动 `8880` 和 `19500`
- `iot-simulator` 已连接到 `iot:19500`
- `iot` 已向 `backend` 推送标准能耗上报

## 访问方式

- 前端：`http://<服务器IP>:30080`
- IOT 设备接入端口：`<服务器IP>:31950`
- 如需直接暴露后端，可在 `ems-app/values.yaml` 中打开 backend 的 `NodePort`

## 说明

- 前端镜像中已经带了 `/api` 反向代理，请保持后端 `Service` 名称为 `backend`
- `iot` 默认使用 `docker,netty` profile，对外提供 HTTP 和 Netty 端口
- `iot-simulator` 默认使用 `docker` profile，通过集群内 `iot:19500` 连接 `iot`
- `iot-simulator` 默认会将运行状态写入 `/data/iot-simulator-state.json`
- 当前 chart 默认统一设置 `TZ=Asia/Shanghai`，并挂载节点的 `/usr/share/zoneinfo/Asia/Shanghai` 到容器 `/etc/localtime`
- `backend`、`iot`、`iot-simulator` 还会额外下发 `JAVA_TOOL_OPTIONS=-Duser.timezone=Asia/Shanghai`
- 当前 chart 默认使用以下服务名：
  - MySQL：`mysql.ems-infra.svc.cluster.local`
  - Redis：`redis.ems-infra.svc.cluster.local`
  - RabbitMQ：`rabbitmq.ems-infra.svc.cluster.local`
  - IOT：`iot.ems-app.svc.cluster.local`
  - Backend：`backend.ems-app.svc.cluster.local`
