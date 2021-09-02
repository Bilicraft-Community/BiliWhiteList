# BiliWhiteList
BilicraftWhiteList 的BC版本

## 插件特性

* 白名单管理，拒绝无白名单玩家加入服务器
* 服务器豁免，允许无白名单玩家使用加入特定无需白名单的服务器
* 玩家邀请，支持玩家使用 `/invite` 命令邀请
* 回绝名单管理，被加入回绝名单的玩家无法被邀请或再次添加白名单
* 邀请历史查询，双向历史查询，查看玩家邀请了谁/谁被谁邀请

## 命令

* /whitelist add <player> - 将指定玩家加入白名单
* /whitelist remove <player> - 将指定玩家从白名单移除
* /whitelist query <player> - 查询玩家白名单状态(无/管理添加/玩家邀请/回绝)
* /whitelist list <random player> - 输出所有白名单玩家列表
* /whitelist block <player> - 回绝特定玩家
* /whitelist unblock <player> - 取消回绝特定玩家
