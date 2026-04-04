# SQL 目录说明

`sql/` 目录不再维护独立的数据库初始化脚本。

当前统一以 `deploy/mysql/init/` 下的脚本作为 MySQL 初始化入口：

- [001-ems.sql](/Users/jerry/Workspace/github/ems4j/deploy/mysql/init/001-ems.sql)：EMS 主库结构与基础配置
- [002-menu.sql](/Users/jerry/Workspace/github/ems4j/deploy/mysql/init/002-menu.sql)：菜单初始化数据
- [003-example.sql](/Users/jerry/Workspace/github/ems4j/deploy/mysql/init/003-example.sql)：示例业务数据
- [101-iot.sql](/Users/jerry/Workspace/github/ems4j/deploy/mysql/init/101-iot.sql)：IoT 数据库结构与默认设备数据

常见场景：

- 手工初始化数据库：按 README 中的顺序依次执行上述脚本
- Docker Compose：自动挂载 `deploy/mysql/init/`
- Helm / K3s：通过 `--set-file mysql.initScripts.*` 注入上述脚本
