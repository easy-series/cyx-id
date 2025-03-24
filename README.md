# Easy-ID

一个简单、高性能的分布式ID生成器框架，类似于 [CosId](https://github.com/Ahoo-Wang/CosId)，提供多种ID生成策略。

## 特性

- 支持多种ID生成策略
  - **雪花算法** (SnowflakeId)：高性能的时间戳型ID生成器，支持毫秒/秒级精度
  - **号段模式** (SegmentId)：预分配ID段，本地生成
  - **链式号段模式** (SegmentChainId)：优化的号段模式，异步预取，无锁设计
- 时钟回拨问题处理
- 多种机器号分配策略
  - Redis 分配器
  - 手动配置
- 与 Spring Boot 集成

## 快速开始

### Maven依赖

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>easy-id</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 基本使用

```java
// 使用雪花算法生成ID
SnowflakeId snowflakeId = new DefaultSnowflakeId();
long id = snowflakeId.nextId();

// 使用号段模式生成ID
SegmentIdDistributor distributor = new RedisSegmentIdDistributor(redisTemplate, "order");
SegmentId segmentId = new DefaultSegmentId(distributor);
long orderId = segmentId.nextId();
```

### Spring Boot配置

```yaml
easy-id:
  namespace: demo-app
  machine:
    enabled: true
    distributor:
      type: redis
  snowflake:
    enabled: true
    clock-backwards:
      spin-threshold: 10
      broken-threshold: 2000
  segment:
    enabled: true
    mode: chain
    chain:
      safe-distance: 5
    distributor:
      type: redis
    provider:
      order:
        offset: 10000
        step: 100
```

## 性能指标

- SnowflakeId: ~400万 ops/s
- SegmentChainId: ~800万 ops/s (比传统号段模式快5倍以上)

## 许可证

[Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt) 