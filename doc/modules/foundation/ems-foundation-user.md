# ems-foundation-user 模块文档

## 1. 模块概述

`ems-foundation-user` 提供用户管理、角色权限、菜单管理、登录认证等基础能力。

## 2. 核心功能

| 功能 | 说明 |
|------|------|
| 用户管理 | 用户 CRUD、密码管理、状态管理 |
| 角色管理 | 角色 CRUD、角色分配、权限绑定 |
| 菜单管理 | 菜单 CRUD、菜单树查询 |
| 登录认证 | 登录/登出、Token 管理、验证码 |

## 3. Service 类说明

### 3.1 UserService

**主要方法：**

| 方法 | 说明 |
|------|------|
| `findPage()` | 分页查询用户 |
| `getDetail()` | 获取用户详情 |
| `addUser()` | 新增用户 |
| `updateUser()` | 更新用户 |
| `deleteUser()` | 删除用户 |
| `resetPassword()` | 重置密码 |
| `assignRoles()` | 分配角色 |

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
