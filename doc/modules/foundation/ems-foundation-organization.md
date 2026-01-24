# ems-foundation-organization 模块文档

## 1. 模块概述

`ems-foundation-organization` 提供组织/租户管理能力，支持多租户架构。

## 2. 核心功能

| 功能 | 说明 |
|------|------|
| 组织管理 | 组织 CRUD |
| 租户隔离 | 数据按组织隔离 |
| 组织查询 | 组织列表、详情查询 |

## 3. Service 类说明

### 3.1 OrganizationService

**主要方法：**

| 方法 | 说明 |
|------|------|
| `getDetail()` | 获取组织详情 |
| `findList()` | 查询组织列表 |
| `addOrganization()` | 新增组织 |
| `updateOrganization()` | 更新组织 |
| `deleteOrganization()` | 删除组织 |

## 4. 模块依赖

```
+-------------------------------+
| ems-foundation-organization   |
+-------------------------------+
            |
            v
+---------------------------+
| ems-components-datasource |
+---------------------------+
            |
            v
+---------------------------+
|       ems-common          |
+---------------------------+
```

## 5. Service 内部依赖

```
+-------------------------------------+
|      OrganizationServiceImpl        |
+-------------------------------------+
                |
                v
        +---------------------+
        |OrganizationRepository|
        +---------------------+
        (无外部模块依赖)
```

## 6. 数据实体

| 实体 | 说明 |
|------|------|
| `OrganizationEntity` | 组织表 |
