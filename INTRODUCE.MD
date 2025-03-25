# 🚀 CyxID - 高性能分布式ID生成框架

![版本](https://img.shields.io/badge/版本-1.0.0--SNAPSHOT-blue)
![JDK版本](https://img.shields.io/badge/JDK-1.8+-green)
![许可证](https://img.shields.io/badge/许可证-MIT-yellow)

## 💫 项目概述

**CyxID** 是一个专为高并发、分布式环境设计的ID生成框架，提供多种ID生成策略，满足不同场景下的唯一标识需求。无论是单机应用还是大型微服务集群，CyxID都能提供**极高性能**、**可靠性**和**灵活性**的ID生成解决方案。

## ✨ 核心特性

- **🔥 极致性能**：单机QPS高达数百万，链式分段模式下无锁设计进一步提升多线程性能
- **💪 高可用性**：支持多种容灾策略，确保服务稳定运行
- **🔄 多种生成策略**：雪花算法、分段模式、链式分段模式等多种策略灵活选择
- **🌐 分布式友好**：原生支持分布式环境下的ID生成，解决时钟同步等问题
- **🛠️ 可扩展性**：插件化设计，支持自定义ID生成器和存储实现
- **🔌 Spring Boot集成**：提供Spring Boot Starter，一键集成到Spring生态
- **📊 全面监控**：内置性能指标收集和监控功能，助力系统调优
- **🔍 易于使用**：简洁API设计，完善的文档和示例

## 🏗️ 架构设计

CyxID基于多模块化设计，核心组件包括：

### 核心模块

- **cyx-id-core**: 框架核心接口和异常定义
- **cyx-id-snowflake**: 雪花算法实现，支持自定义工作节点分配和时钟回拨处理
- **cyx-id-segment**: 基础分段ID生成器，支持双buffer机制
- **cyx-id-segment-chain**: 高性能链式分段ID生成器，基于无锁设计和预取机制
- **cyx-id-storage-api**: 存储层抽象接口，用于持久化工作节点ID和分段信息
- **cyx-id-storage-redis**: Redis存储实现，提供分布式协调能力
- **cyx-id-spring-boot-starter**: Spring Boot自动配置，便于快速集成
- **cyx-id-benchmark**: 性能基准测试，提供全面的性能评估

## 🚀 性能对比

基于JMH框架的基准测试显示，CyxID在各种场景下均具有卓越的性能表现：

| 生成策略 | 单线程性能(ops/s) | 多线程性能(8线程, ops/s) | 批量生成(100个/批, ops/s) |
| -------- | ----------------- | ------------------------ | ------------------------- |
| 雪花算法 | ~5,000,000        | ~20,000,000              | ~200,000                  |
| 分段模式 | ~3,000,000        | ~12,000,000              | ~180,000                  |
| 链式分段 | ~4,500,000        | ~30,000,000              | ~250,000                  |

> 注：实际性能可能因硬件配置、JVM参数等因素而异，详细性能报告请参考benchmark模块

## 💡 应用场景

- **微服务架构**：为分布式服务提供全局唯一标识
- **高并发交易系统**：订单号、交易流水号生成
- **分库分表环境**：分布式数据库主键生成
- **消息中间件**：消息ID生成
- **日志系统**：日志事件唯一标识
- **分布式任务调度**：任务ID生成

## 📋 快速开始

### Maven依赖

```xml
<!-- 引入BOM -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.caoyixin</groupId>
            <artifactId>cyx-id-bom</artifactId>
            <version>1.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<!-- Spring Boot环境 -->
<dependency>
    <groupId>com.caoyixin</groupId>
    <artifactId>cyx-id-spring-boot-starter</artifactId>
</dependency>
```

### Spring Boot配置

```yaml
cyx-id:
  enabled: true
  # 选择生成器类型: snowflake, segment, segment-chain
  type: snowflake
  
  # 雪花算法配置
  snowflake:
    enabled: true
    worker:
      # 节点ID分配策略: static(静态), redis(动态分配)
      allocator: static
      id: 1
```

### 代码示例

```java
@Service
public class OrderService {
    
    @Autowired
    private IdGeneratorProvider idGeneratorProvider;
    
    public Order createOrder() {
        // 获取ID生成器
        IdGenerator generator = idGeneratorProvider.getGenerator();
        
        // 生成唯一订单号
        long orderId = generator.generate();
        
        // 创建订单...
        return new Order(orderId, ...);
    }
}
```

## 🔄 ID生成策略详解

### 雪花算法 (Snowflake)

基于Twitter Snowflake算法的改进实现，提供毫秒级时间戳、工作节点ID和序列号组合的64位长整型ID。特点是高性能、趋势递增，适合绝大多数场景。

### 分段模式 (Segment)

预先分配ID段，本地缓存ID段并按需消费，支持双缓冲机制，在保证全局唯一性的同时大幅减少对存储层的访问频率，适合高并发场景。

### 链式分段模式 (SegmentChain)

创新性地采用链表结构管理ID段，结合无锁设计和预取机制，实现了极高的并发性能，特别适合多线程高QPS环境，是CyxID的一大亮点。

## 💼 企业级特性

- **优雅降级**：存储层故障时支持降级策略，保障核心功能可用
- **自动恢复**：存储层恢复后自动同步状态
- **配置热更新**：部分配置支持动态调整，无需重启服务
- **资源隔离**：不同业务使用不同的ID生成器实例，避免相互影响
- **容量规划**：提供ID容量估算工具，便于系统规划

## 📚 文档与支持

更详细的文档和示例请参考各模块的README文件，包括：

- [核心模块文档](cyx-id-core/README.md)
- [雪花算法模块文档](cyx-id-snowflake/README.md)
- [分段模式模块文档](cyx-id-segment/README.md)
- [链式分段模块文档](cyx-id-segment-chain/README.md)
- [Redis存储模块文档](cyx-id-storage-redis/README.md)
- [Spring Boot集成文档](cyx-id-spring-boot-starter/README.md)
- [性能测试文档](cyx-id-benchmark/README.md)

## 🤝 贡献指南

欢迎贡献代码、报告问题或提出新功能建议！请遵循以下步骤：

1. Fork项目并克隆到本地
2. 创建新分支 `git checkout -b feature/your-feature`
3. 提交更改 `git commit -m 'Add new feature'`
4. 推送到远程 `git push origin feature/your-feature`
5. 创建Pull Request

## 📄 许可证

本项目采用MIT许可证，详情请参阅[LICENSE](LICENSE)文件。

## 🌟 关于作者

CyxID由[曹义鑫]开发，旨在提供一个高性能、易于使用、适应各种场景的分布式ID生成解决方案。
