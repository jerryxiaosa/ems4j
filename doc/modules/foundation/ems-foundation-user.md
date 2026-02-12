# ems-foundation-user 模块文档

## 1. 模块概述

`ems-foundation-user` 提供用户管理、角色权限、菜单管理、登录认证等基础能力。

## 2. 核心功能

| 功能 | 说明 |
|------|------|
| 用户管理 | 用户 CRUD、密码管理、状态管理 |
| 角色管理 | 角色 CRUD、角色分配、权限绑定 |
| 菜单管理 | 菜单 CRUD、菜单树查询 |
| 登录认证 | 登录/登出、Token 管理、验证码、登录态清理 |

## 3. Service 类说明

### 3.1 UserService

**主要方法：**

| 方法 | 说明 |
|------|------|
| `findUserPage()` | 分页查询用户 |
| `findUserList()` | 查询用户列表 |
| `getUserInfo()` | 获取用户详情 |
| `add()` | 新增用户 |
| `update()` | 更新用户 |
| `delete()` | 逻辑删除用户，并在事务提交后触发登录态清理 |
| `updatePassword()` | 用户自主修改密码 |
| `resetPassword()` | 管理员重置密码，并在事务提交后触发登录态清理 |
| `hasPermission()` | 判断用户是否拥有指定权限 |

### 3.2 RoleService

**主要方法：**

| 方法 | 说明 |
|------|------|
| `findList()` | 查询角色列表 |
| `getDetail()` | 获取角色详情 |
| `addRole()` | 新增角色 |
| `updateRole()` | 更新角色 |
| `deleteRole()` | 删除角色 |
| `assignMenus()` | 分配菜单权限 |

### 3.3 MenuService

**主要方法：**

| 方法 | 说明 |
|------|------|
| `findTree()` | 查询菜单树 |
| `getDetail()` | 获取菜单详情 |
| `addMenu()` | 新增菜单 |
| `updateMenu()` | 更新菜单 |
| `deleteMenu()` | 删除菜单 |
| `getUserMenus()` | 获取用户菜单 |

### 3.4 LoginService

**主要方法：**

| 方法 | 说明 |
|------|------|
| `login()` | 用户登录 |
| `logout()` | 用户登出 |
| `getCaptcha()` | 获取验证码 |
| `getCurrentUser()` | 获取当前用户 |

### 3.5 PasswordService

**主要方法：**

| 方法 | 说明 |
|------|------|
| `encode()` | 密码加密 |
| `matches()` | 密码校验 |

## 4. 模块依赖

```
+---------------------------+
|   ems-foundation-user     |
+---------------------------+
            |
            v
+---------------------------+     +---------------------------+
| ems-components-datasource |     |   ems-components-redis    |
+---------------------------+     +---------------------------+
            |
            v
+---------------------------+
|       ems-common          |
+---------------------------+
```

## 5. Service 内部依赖

```
+-------------------------------------+
|          UserServiceImpl            |
+-------------------------------------+
                |
    +-----------+-----------+
    |           |           |
    v           v           v
+-------+  +--------+  +---------+
|RoleSvc|  |Password|  |Repository|
+-------+  |  Svc   |  +---------+
           +--------+

+-------------------------------------+
|         LoginServiceImpl            |
+-------------------------------------+
                |
    +-----------+-----------+
    |           |           |
    v           v           v
+-------+  +--------+  +---------+
|UserSvc|  |Password|  |Sa-Token |
+-------+  |  Svc   |  +---------+
           +--------+
```

## 6. 数据实体

| 实体 | 说明 |
|------|------|
| `UserEntity` | 用户表 |
| `RoleEntity` | 角色表 |
| `MenuEntity` | 菜单表 |
| `UserRoleEntity` | 用户角色关联表 |
| `RoleMenuEntity` | 角色菜单关联表 |

## 7. 认证框架

使用 **Sa-Token** + **JWT** 实现认证：

| 配置 | 说明 |
|------|------|
| `token-name` | Token 名称（Authorization） |
| `timeout` | Token 有效期 |
| `is-concurrent` | 是否允许多端登录 |
| `token-style` | Token 风格（UUID） |

## 8. 事务事件与登录态清理

### 8.1 事件定义

| 事件 | 触发点 | 作用 |
|------|--------|------|
| `UserDeletedEvent` | `UserServiceImpl.delete()` 删除成功后 | 触发用户登录态清理 |
| `UserPasswordResetEvent` | `UserServiceImpl.resetPassword()` 重置成功后 | 触发用户登录态清理 |

### 8.2 监听与执行时机

- 监听器：`UserLoginStateCleanupListener`
- 触发机制：`@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`
- 设计目的：只在事务提交成功后清理登录态，避免“业务回滚但会话被提前踢下线”

### 8.3 清理动作与容错策略

| 动作 | 说明 | 异常处理 |
|------|------|----------|
| 清理失败计数 | 删除 Redis 键 `LoginConstant.PWD_ERR + userId` | 异常仅告警日志，不中断主流程 |
| 强制下线 | 调用 `StpUtil.logout(userId)` | `NotLoginException` 忽略，其他异常告警日志 |
