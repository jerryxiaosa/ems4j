# ems-foundation 模块文档

## 1. 模块概述

`ems-foundation` 是基础服务层，提供业务无关的通用能力，包括用户管理、空间管理、组织管理、系统配置、设备集成等功能。

## 2. 子模块列表

| 模块 | 说明 |
|------|------|
| [ems-foundation-user](ems-foundation-user.md) | 用户、角色、菜单、登录认证 |
| [ems-foundation-space](ems-foundation-space.md) | 空间/区域树形管理 |
| [ems-foundation-organization](ems-foundation-organization.md) | 组织/租户管理 |
| [ems-foundation-system](ems-foundation-system.md) | 系统配置管理 |
| [ems-foundation-integration](ems-foundation-integration.md) | 设备命令、能源数据服务 |
| ems-foundation-notification | 通知服务（待实现） |

## 3. 模块依赖总览

```
+------------------------------------------------------------------+
|                      ems-foundation                               |
+------------------------------------------------------------------+
|                                                                  |
|  +------------------+     +------------------+                    |
|  |      user        |     |      space       |                    |
|  +------------------+     +------------------+                    |
|          |                        |                              |
|          v                        v                              |
|  +------------------+     +------------------+                    |
|  | components-redis |     |    ems-common    |                    |
|  | components-datasrc|     | components-datasrc|                   |
|  +------------------+     +------------------+                    |
|                                                                  |
|  +------------------+     +------------------+                    |
|  |   organization   |     |      system      |                    |
|  +------------------+     +------------------+                    |
|          |                        |                              |
|          v                        v                              |
|  +------------------+     +------------------+                    |
|  |    ems-common    |     | components-datasrc|                   |
|  | components-datasrc|     +------------------+                    |
|  +------------------+                                            |
|                                                                  |
|  +------------------+                                            |
|  |   integration    |                                            |
|  +------------------+                                            |
|          |                                                       |
|          v                                                       |
|  +------------------+     +------------------+                    |
|  |      system      |     | components-lock  |                    |
|  +------------------+     +------------------+                    |
|                                                                  |
+------------------------------------------------------------------+
```

## 4. 被业务模块依赖情况

```
+------------------+     +------------------+     +------------------+
| business-device  |     | business-account |     | business-finance |
+------------------+     +------------------+     +------------------+
        |                        |                        |
        v                        v                        v
+------------------+     +------------------+     +------------------+
| foundation-      |     | foundation-user  |     | foundation-space |
|   integration    |     +------------------+     +------------------+
+------------------+     | foundation-org   |     | foundation-org   |
| foundation-space |     +------------------+     +------------------+
+------------------+                              | foundation-system|
                                                  +------------------+
```

## 5. 各模块核心 Service

| 模块 | Service | 职责 |
|------|---------|------|
| user | UserService | 用户 CRUD、用户查询 |
| user | RoleService | 角色管理、权限分配 |
| user | MenuService | 菜单管理 |
| user | LoginService | 登录认证、Token 管理 |
| user | PasswordService | 密码加密、校验 |
| space | SpaceService | 空间树管理、层级查询 |
| organization | OrganizationService | 组织管理 |
| system | ConfigService | 系统配置读写 |
| integration | DeviceCommandService | 设备命令下发 |
| integration | EnergyService | 能源数据服务 |
| integration | DeviceModelService | 设备型号管理 |
| integration | DeviceTypeService | 设备类型管理 |
