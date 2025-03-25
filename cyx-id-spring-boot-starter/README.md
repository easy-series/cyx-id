# CyxID Spring Boot Starter

这个模块提供Spring Boot集成，使ID生成器能够轻松集成到Spring Boot应用中。

## 快速开始

1. 添加依赖

```xml
<dependency>
    <groupId>com.caoyixin</groupId>
    <artifactId>cyx-id-spring-boot-starter</artifactId>
    <version>${cyx-id.version}</version>
</dependency>
```

2. 配置应用

在你的`application.yml`或`application.properties`中添加配置：

```yaml
cyx-id:
  enabled: true
  type: snowflake  # snowflake, segment, segment-chain
  
  # 根据需要配置各种ID生成器...
```

3. 在代码中使用

```java
import com.caoyixin.cyxid.core.IdGenerator;
import com.caoyixin.cyxid.core.provider.IdGeneratorProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class YourService {

    @Autowired
    private IdGeneratorProvider idGeneratorProvider;
    
    public long generateId() {
        // 使用配置的默认生成器
        IdGenerator generator = idGeneratorProvider.getDefaultGenerator();
        return generator.nextId();
    }
    
    public long generateCustomId(String name) {
        // 使用指定名称的生成器
        IdGenerator generator = idGeneratorProvider.getGenerator(name);
        return generator.nextId();
    }
}
```

## 配置参考

完整配置请参考[application.yml.example](src/main/resources/application.yml.example)文件。

### 雪花算法配置

```yaml
cyx-id:
  type: snowflake
  snowflake:
    enabled: true
    worker:
      allocator: static  # static, redis
      id: 1  # 当使用static分配器时
```

### 分段ID配置

```yaml
cyx-id:
  type: segment
  segment:
    enabled: true
    step: 1000
    safe-distance-percent: 30
```

### 链式分段ID配置

```yaml
cyx-id:
  type: segment-chain
  segment-chain:
    enabled: true
    step: 1000
    max-chain-length: 10
    prefetch-period: 1000
```

### Redis配置

对于需要Redis的功能（如Redis工作节点ID分配、Redis号段分配等），需要配置Redis连接：

```yaml
cyx-id:
  redis:
    host: localhost
    port: 6379
    password: yourpassword
    database: 0
```

## 高级用法

### 自定义ID生成器

您可以创建自己的ID生成器并注册到系统中：

```java
import com.caoyixin.cyxid.core.IdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomIdGeneratorConfig {

    @Bean
    public IdGenerator customIdGenerator() {
        // 创建并返回自定义ID生成器
        return new YourCustomIdGenerator();
    }
}
```

自定义生成器会自动注册到`IdGeneratorProvider`中。 