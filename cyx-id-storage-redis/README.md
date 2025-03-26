# CyxID Redis Storage Module

## 概述

CyxID Redis Storage模块提供基于Redis的存储实现，为分布式ID生成提供高性能、高可用的持久化和协调服务。通过Redis的强大特性，该模块支持高并发的号段分配和可靠的机器ID管理，是CyxID框架在分布式环境中的重要支撑。

## 主要特性

- **高性能**：基于Redis的快速读写特性，提供毫秒级的存储响应
- **分布式协调**：利用Redis原子操作，实现可靠的分布式锁和资源分配
- **心跳机制**：自动维护工作节点心跳，支持故障检测和资源回收
- **连接池管理**：内置连接池管理，优化Redis连接资源使用
- **故障恢复**：支持Redis故障的检测与自动恢复
- **监控友好**：提供丰富的操作指标，便于系统监控

## 核心组件

### 连接管理

- **RedisConnectionManager**：Redis连接管理器，负责连接池的创建和维护

### 号段分配

- **RedisSegmentAllocator**：基于Redis的号段分配器，实现SegmentAllocator接口

### 机器ID管理

- **RedisWorkerIdAllocator**：基于Redis的工作节点ID分配器，实现MachineIdAllocator接口
- **RedisMachineStateStorage**：基于Redis的机器状态存储，实现MachineIdStorage接口

## Redis存储结构

### 号段分配相关键值

- **Key格式**：`cyx-id:segment:{业务键名}`
- **Value类型**：Hash结构
- **字段**：
  - `current_id`：当前已分配的最大ID
  - `step`：步长
  - `version`：乐观锁版本号

### 机器ID分配相关键值

- **Key格式**：
  - 分配管理：`cyx-id:worker:{组名}:allocation`
  - 机器信息：`cyx-id:worker:{组名}:machine:{机器ID}`
- **Value类型**：
  - 分配管理：Bitmap结构
  - 机器信息：Hash结构
- **字段**：
  - `machine_id`：机器ID
  - `host_name`：主机名
  - `ip_address`：IP地址
  - `process_id`：进程ID
  - `active_status`：活跃状态
  - `last_heartbeat`：最后心跳时间

## 使用示例

### 基本配置

```java
// 创建Redis连接管理器
RedisConnectionManager connectionManager = new RedisConnectionManager.Builder()
    .host("localhost")
    .port(6379)
    .password("yourpassword")
    .database(0)
    .timeout(2000)
    .poolConfig(new GenericObjectPoolConfig<>())
    .build();

// 创建Redis号段分配器
RedisSegmentAllocator segmentAllocator = new RedisSegmentAllocator(connectionManager);

// 创建Redis工作节点ID分配器
RedisWorkerIdAllocator workerIdAllocator = new RedisWorkerIdAllocator.Builder()
    .connectionManager(connectionManager)
    .build();

// 创建Redis机器状态存储
RedisMachineStateStorage machineStateStorage = new RedisMachineStateStorage(connectionManager);
```

### 高级配置

```java
// 自定义Redis连接池配置
GenericObjectPoolConfig<Jedis> poolConfig = new GenericObjectPoolConfig<>();
poolConfig.setMaxTotal(100);
poolConfig.setMaxIdle(20);
poolConfig.setMinIdle(10);
poolConfig.setTestOnBorrow(true);
poolConfig.setTestOnReturn(false);

// 带集群支持的Redis连接管理器
RedisConnectionManager connectionManager = new RedisConnectionManager.Builder()
    .sentinelMode(true)
    .sentinelMasterName("mymaster")
    .sentinelNodes(Arrays.asList("sentinel1:26379", "sentinel2:26379", "sentinel3:26379"))
    .password("yourpassword")
    .database(0)
    .timeout(2000)
    .poolConfig(poolConfig)
    .build();

// 自定义Redis工作节点ID分配器
RedisWorkerIdAllocator workerIdAllocator = new RedisWorkerIdAllocator.Builder()
    .connectionManager(connectionManager)
    .heartbeatInterval(5000)                  // 设置心跳间隔（毫秒）
    .heartbeatTimeout(15000)                  // 设置心跳超时时间（毫秒）
    .maxWorkerId(1024)                        // 设置最大工作节点ID
    .recycleEnabled(true)                     // 启用回收机制
    .retryTimes(3)                            // 设置重试次数
    .retryInterval(200)                       // 设置重试间隔（毫秒）
    .build();
```

## 配置说明

### Redis连接配置

| 参数 | 说明 | 默认值 |
| ---- | ---- | ------ |
| host | Redis服务器地址 | localhost |
| port | Redis服务器端口 | 6379 |
| password | Redis访问密码 | null |
| database | Redis数据库索引 | 0 |
| timeout | 连接超时时间（毫秒） | 2000 |
| sentinelMode | 是否启用Sentinel模式 | false |
| sentinelMasterName | Sentinel主节点名称 | null |
| sentinelNodes | Sentinel节点列表 | null |

### 机器ID分配器配置

| 参数 | 说明 | 默认值 |
| ---- | ---- | ------ |
| heartbeatInterval | 心跳间隔（毫秒） | 3000 |
| heartbeatTimeout | 心跳超时时间（毫秒） | 10000 |
| maxWorkerId | 最大工作节点ID | 1023 |
| recycleEnabled | 是否启用回收机制 | true |
| retryTimes | 分配重试次数 | 3 |
| retryInterval | 重试间隔（毫秒） | 200 |

## 性能优化建议

1. **合理配置连接池**：
   - 根据应用并发量设置合适的连接池大小
   - 生产环境建议开启连接测试，提高稳定性

2. **心跳参数调优**：
   - 在网络稳定的环境可适当延长心跳间隔，减少网络开销
   - 心跳超时时间应至少为心跳间隔的3倍，避免误判

3. **使用Redis集群或Sentinel**：
   - 生产环境建议使用Redis Sentinel或集群模式，提高可用性
   - 配置合理的故障转移策略

## 注意事项

- **键前缀冲突**：默认使用`cyx-id:`前缀，可能与其他应用冲突，可通过自定义配置修改
- **内存管理**：长期运行的系统会产生大量过期机器记录，建议定期清理
- **Redis版本要求**：建议使用Redis 4.0及以上版本，以支持所有功能

## 依赖关系

本模块依赖于：

- cyx-id-core：核心异常定义
- cyx-id-storage-api：存储接口定义
- Jedis：Redis Java客户端
- Apache Commons Pool2：连接池实现
- SLF4J：日志门面
- Lombok：减少样板代码 