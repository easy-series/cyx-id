# CyxID Spring Boot Starter

## 概述

CyxID Spring Boot Starter模块提供了与Spring Boot的无缝集成，让开发者能够以最小的配置成本将CyxID框架引入Spring Boot应用。通过自动配置和丰富的配置选项，开发者可以轻松使用框架提供的各种ID生成策略。

## 主要特性

- **零代码集成**：通过简单的配置即可启用ID生成服务
- **多策略支持**：支持雪花算法、分段模式、链式分段模式等多种ID生成策略
- **自动配置**：基于Spring Boot自动配置机制，减少样板代码
- **灵活配置**：支持YAML或Properties格式的配置方式
- **多环境支持**：适配开发、测试、生产等不同环境的需求
- **健康检查**：提供ID生成器的健康状态检查
- **监控指标**：集成Spring Boot Actuator，提供性能指标监控

## 快速开始

### 添加依赖

```xml
<dependency>
    <groupId>com.caoyixin</groupId>
    <artifactId>cyx-id-spring-boot-starter</artifactId>
    <version>${cyx-id.version}</version>
</dependency>
```

### 基础配置

在`application.yml`或`application.properties`中添加配置：

```yaml
cyx-id:
  enabled: true
  type: snowflake  # 支持snowflake, segment, segment-chain
```

### 在代码中使用

```java
import com.caoyixin.cyxid.core.IdGenerator;
import com.caoyixin.cyxid.core.provider.IdGeneratorProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired
    private IdGeneratorProvider idGeneratorProvider;
    
    public Order createOrder() {
        // 获取默认ID生成器
        IdGenerator generator = idGeneratorProvider.getGenerator();
        
        // 生成订单ID
        long orderId = generator.generate();
        
        return new Order(orderId, ...);
    }
    
    public List<Order> createBatchOrders(int count) {
        // 批量生成ID
        IdGenerator generator = idGeneratorProvider.getGenerator();
        List<Long> ids = generator.batchGenerate(count);
        
        // 创建订单
        List<Order> orders = new ArrayList<>(count);
        for (Long id : ids) {
            orders.add(new Order(id, ...));
        }
        
        return orders;
    }
}
```

## 详细配置

### 雪花算法配置

```yaml
cyx-id:
  type: snowflake
  snowflake:
    enabled: true
    worker:
      # 节点ID分配策略: static(静态), redis(动态分配)
      allocator: static
      id: 1  # 当使用static分配器时的固定节点ID
    # 时钟回拨处理
    clock-backwards:
      # 处理策略: spin(自旋等待), exception(抛出异常), delay(延迟执行)
      strategy: spin
      # 最大容忍的时钟回拨毫秒数
      max-tolerance-millis: 10
```

### 分段ID配置

```yaml
cyx-id:
  type: segment
  segment:
    enabled: true
    # 默认业务名称
    business-name: default
    # 号段步长
    step: 1000
    # 触发加载下一个号段的百分比阈值
    loading-percent: 20
    # 加载失败重试次数
    retry-times: 3
    # 重试间隔(毫秒)
    retry-interval: 200
```

### 链式分段ID配置

```yaml
cyx-id:
  type: segment-chain
  segment-chain:
    enabled: true
    # 默认业务名称
    business-name: default
    # 号段步长
    step: 1000
    # 链表初始容量
    initial-capacity: 5
    # 预取阈值
    prefetch-threshold: 3
    # 链表最大容量
    max-capacity: 10
    # 预取周期(毫秒)
    prefetch-period: 500
    # 预取线程数
    scheduled-threads: 2
```

### Redis配置

当使用需要Redis的功能时(如Redis工作节点ID分配、Redis号段分配等)，需要配置Redis连接：

```yaml
cyx-id:
  redis:
    enabled: true
    # Redis服务器配置
    host: localhost
    port: 6379
    password: yourpassword
    database: 0
    timeout: 2000
    # 连接池配置
    pool:
      max-total: 100
      max-idle: 20
      min-idle: 10
      test-on-borrow: true
    # Sentinel配置
    sentinel:
      enabled: false
      master: mymaster
      nodes: sentinel1:26379,sentinel2:26379,sentinel3:26379
```

## 高级用法

### 多个ID生成器

可以通过命名方式注册和使用多个ID生成器：

```java
@Service
public class MultiGeneratorService {

    @Autowired
    private IdGeneratorProvider idGeneratorProvider;
    
    // 不同业务使用不同的ID生成器
    public void useMultipleGenerators() {
        // 用于订单ID生成
        IdGenerator orderGenerator = idGeneratorProvider.getGenerator("order");
        long orderId = orderGenerator.generate();
        
        // 用于用户ID生成
        IdGenerator userGenerator = idGeneratorProvider.getGenerator("user");
        long userId = userGenerator.generate();
    }
}
```

对应的配置：

```yaml
cyx-id:
  generators:
    # 订单ID生成器
    order:
      type: segment
      segment:
        business-name: order
        step: 1000
    # 用户ID生成器
    user:
      type: snowflake
      snowflake:
        worker:
          allocator: redis
```

### 自定义ID生成器

可以创建自定义ID生成器并注册到系统中：

```java
import com.caoyixin.cyxid.core.IdGenerator;
import com.caoyixin.cyxid.core.provider.IdGeneratorProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomIdGeneratorConfig {

    @Autowired
    private IdGeneratorProvider idGeneratorProvider;

    @Bean
    public void registerCustomGenerator() {
        // 创建自定义ID生成器
        IdGenerator customGenerator = new YourCustomIdGenerator();
        
        // 注册到提供者
        idGeneratorProvider.registerGenerator("custom", customGenerator);
    }
}
```

## 监控与管理

CyxID Spring Boot Starter集成了Spring Boot Actuator，提供了ID生成器的健康检查和监控指标：

### 健康检查

访问`/actuator/health`端点可查看ID生成器的健康状态。

### 监控指标

访问`/actuator/metrics`端点可查看ID生成性能指标，包括：

- `cyx-id.generate.count`：生成ID的总数
- `cyx-id.generate.time`：生成ID的耗时统计
- `cyx-id.segment.allocate.count`：号段分配次数
- `cyx-id.segment.allocate.time`：号段分配耗时

## 依赖关系

本模块依赖于：

- cyx-id-core：核心接口定义
- cyx-id-snowflake：雪花算法实现
- cyx-id-segment：分段模式实现
- cyx-id-segment-chain：链式分段模式实现
- cyx-id-storage-api：存储层抽象
- cyx-id-storage-redis：Redis存储实现
- Spring Boot Starter：Spring Boot集成支持
- Spring Boot Configuration Processor：配置元数据支持
- Spring Boot Actuator：监控与管理支持 