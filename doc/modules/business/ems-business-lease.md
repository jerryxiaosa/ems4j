# ems-business-lease 模块文档

## 1. 模块概述

`ems-business-lease` 独立承载主体与空间的租赁关系，不再放在 `account` 模块中。

## 2. 核心功能

| 功能 | 说明 |
|------|------|
| 租赁 | 建立主体与空间的租赁关系 |
| 退租 | 校验空间下是否存在已开户电表，再解除租赁关系 |
| 租赁关系查询 | 按主体维度查询租赁空间关系，供其他业务模块复用 |

## 3. 核心 Service

| Service | 职责 |
|---------|------|
| `OwnerSpaceLeaseService` | 主体空间租赁与退租 |
| `OwnerSpaceRelationQueryService` | 查询主体空间租赁关系 |

## 4. 模块边界

- 对 `account` 暴露查询服务，`account` 不再直接依赖 `OwnerSpaceRelationRepository`
- 依赖 `device` 查询空间下是否存在已开户电表
- `OwnerSpaceRelationQueryService` 对外返回 DTO，而不是持久化实体，避免泄露仓储模型

## 5. 命名约定

- 关系模型统一使用 `OwnerSpaceRelation*`
- 不再使用 `OwnerSpaceRel*` 简写命名
