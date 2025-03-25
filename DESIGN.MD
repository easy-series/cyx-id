# CyxId 多模块项目设计方案

## 1. 多模块项目结构

```
cyx-id/
├── cyx-id-bom                  # 依赖管理 (Bill of Materials)
├── cyx-id-core                 # 核心功能与接口
├── cyx-id-snowflake            # 雪花算法实现
├── cyx-id-segment              # 分段ID实现
├── cyx-id-segment-chain        # 链式分段ID实现
├── cyx-id-storage-api          # 存储接口定义
├── cyx-id-storage-redis        # Redis存储实现
├── cyx-id-spring-boot-starter  # Spring Boot 自动配置
└── cyx-id-benchmark            # 性能测试
```

## 2. 各模块职责说明

### 2.1 cyx-id-bom
- 依赖管理模块，统一管理版本号
- 简化用户的依赖管理，只需要引入BOM即可

### 2.2 cyx-id-core
- 定义核心接口与抽象类
- 包含异常定义、工具类和通用组件
- 所有其他模块都依赖于此模块

```java
// 核心接口
public interface IdGenerator {
    long generate();
    List<Long> batchGenerate(int size);
    String getName();
}

// 异常体系
public class CyxIdException extends RuntimeException { /*...*/ }
public class ClockBackwardsException extends CyxIdException { /*...*/ }
public class MachineIdAllocationException extends CyxIdException { /*...*/ }
```

### 2.3 cyx-id-snowflake
- 雪花算法的完整实现
- 时钟回拨处理机制
- 机器ID分配

```java
public class SnowflakeIdGenerator implements IdGenerator {
    // 实现细节
}

public interface MachineIdAllocator {
    int allocate();
    boolean release(int machineId);
}

public interface ClockBackwardsHandler {
    long handleBackwards(long lastTimestamp, long currentTimestamp);
}
```

### 2.4 cyx-id-segment
- 分段ID生成器实现
- 基本的分段分配与管理

```java
public class SegmentIdGenerator implements IdGenerator {
    // 实现细节
}

public class IdSegment {
    private final long minId;
    private final long maxId;
    // getter方法
}
```

### 2.5 cyx-id-segment-chain
- 基于链式结构的高性能分段ID实现
- 无锁设计和预取机制

```java
public class ChainedSegmentIdGenerator implements IdGenerator {
    // 实现细节
}

public class PrefetchWorker implements Runnable {
    // 实现细节
}
```

### 2.6 cyx-id-storage-api
- 定义存储接口，与具体实现解耦
- 包含机器ID存储和分段ID分配器接口

```java
public interface MachineIdStorage {
    MachineState load();
    void save(MachineState state);
}

public interface SegmentAllocator {
    IdSegment nextSegment(String name);
}
```

### 2.7 cyx-id-storage-redis
- 基于Redis的存储实现
- 包括机器ID分配和分段ID分配

```java
public class RedisMachineIdAllocator implements MachineIdAllocator {
    // 实现细节
}

public class RedisSegmentAllocator implements SegmentAllocator {
    // 实现细节
}
```

### 2.8 cyx-id-spring-boot-starter
- Spring Boot自动配置
- 提供基于属性的配置机制

```java
@Configuration
@EnableConfigurationProperties(CyxIdProperties.class)
@ConditionalOnClass(IdGenerator.class)
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class CyxIdAutoConfiguration {
    // 自动配置
}

@ConfigurationProperties(prefix = "cyx-id")
public class CyxIdProperties {
    // 配置属性
}
```

### 2.9 cyx-id-benchmark
- 性能测试代码
- 使用JMH进行性能基准测试

## 3. 依赖关系

```
cyx-id-core <-- cyx-id-storage-api
             <-- cyx-id-snowflake
             <-- cyx-id-segment <-- cyx-id-segment-chain

cyx-id-storage-api <-- cyx-id-storage-redis

cyx-id-core <-- cyx-id-spring-boot-starter
```

## 4. 构建配置

### 4.1 父POM配置

```xml
<project>
    <groupId>com.caoyixin</groupId>
    <artifactId>cyx-id</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    
    <modules>
        <module>cyx-id-bom</module>
        <module>cyx-id-core</module>
        <module>cyx-id-snowflake</module>
        <module>cyx-id-segment</module>
        <module>cyx-id-segment-chain</module>
        <module>cyx-id-storage-api</module>
        <module>cyx-id-storage-redis</module>
        <module>cyx-id-spring-boot-starter</module>
        <module>cyx-id-benchmark</module>
    </modules>
    
    <properties>
        <java.version>1.8</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>${java.version}</maven.compiler.source>
        <maven.compiler.target>${java.version}</maven.compiler.target>
        
        <!-- 依赖版本 -->
        <spring.version>5.3.23</spring.version>
        <spring-boot.version>2.7.5</spring-boot.version>
        <jedis.version>4.3.1</jedis.version>
        <junit.version>5.9.1</junit.version>
    </properties>
    
    <dependencyManagement>
        <dependencies>
            <!-- 内部模块依赖 -->
            <dependency>
                <groupId>${project.groupId}</groupId>
                <artifactId>cyx-id-bom</artifactId>
                <version>${project.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            
            <!-- 外部依赖 -->
            <dependency>
                <groupId>org.springframework</groupId>
                <artifactId>spring-framework-bom</artifactId>
                <version>${spring.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <!-- 其他依赖 -->
        </dependencies>
    </dependencyManagement>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.10.1</version>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                </configuration>
            </plugin>
            <!-- 其他插件 -->
        </plugins>
    </build>
</project>
```

### 4.2 BOM配置

```xml
<project>
    <groupId>com.caoyixin</groupId>
    <artifactId>cyx-id-bom</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>${project.groupId}</groupId>
                <artifactId>cyx-id-core</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>${project.groupId}</groupId>
                <artifactId>cyx-id-snowflake</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>${project.groupId}</groupId>
                <artifactId>cyx-id-segment</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>${project.groupId}</groupId>
                <artifactId>cyx-id-segment-chain</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>${project.groupId}</groupId>
                <artifactId>cyx-id-storage-api</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>${project.groupId}</groupId>
                <artifactId>cyx-id-storage-redis</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>${project.groupId}</groupId>
                <artifactId>cyx-id-spring-boot-starter</artifactId>
                <version>${project.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

## 5. 用户使用示例

### 5.1 Maven依赖引入方式

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

<!-- 引入需要的模块 -->
<dependencies>
    <!-- Spring Boot 用户 -->
    <dependency>
        <groupId>com.caoyixin</groupId>
        <artifactId>cyx-id-spring-boot-starter</artifactId>
    </dependency>
    
    <!-- 仅使用雪花算法 -->
    <dependency>
        <groupId>com.caoyixin</groupId>
        <artifactId>cyx-id-snowflake</artifactId>
    </dependency>
</dependencies>
```

### 5.2 Spring Boot 配置示例

```yaml
cyx-id:
  # 雪花算法配置
  snowflake:
    enabled: true
    epoch: 1577836800000  # 2020-01-01
    machine:
      allocator: redis
      bits: 10
    sequence:
      bits: 12
    clock-backwards:
      spin-threshold: 10  # 毫秒
      broken-threshold: 2000  # 毫秒
  
  # 分段ID配置
  segment:
    enabled: true
    allocator: redis
    providers:
      order:
        step: 1000
        offset: 0
      user:
        step: 500
        offset: 100000
  
  # 链式分段ID配置
  segment-chain:
    enabled: true
    safe-distance: 5
    prefetch-period: 1000  # 毫秒
    
  # Redis配置 (如果已有Spring Redis配置则可省略)
  redis:
    host: localhost
    port: 6379
```

### 5.3 代码使用示例

```java
// 直接使用
@Autowired
private IdGeneratorProvider idGeneratorProvider;

public void example() {
    // 获取雪花算法生成器
    IdGenerator snowflakeGenerator = idGeneratorProvider.getGenerator("snowflake");
    long id = snowflakeGenerator.generate();
    
    // 获取分段ID生成器
    IdGenerator orderIdGenerator = idGeneratorProvider.getGenerator("order");
    long orderId = orderIdGenerator.generate();
    
    // 批量生成
    List<Long> batchIds = orderIdGenerator.batchGenerate(100);
}

// 自定义配置
SnowflakeIdGenerator customGenerator = SnowflakeIdGenerator.builder()
    .machineId(1)
    .sequenceBits(12)
    .timestampBits(41)
    .epoch(1577836800000L)
    .build();
```

## 6. 实现计划

### 阶段1：基础架构与核心功能
- 建立多模块项目结构
- 实现核心接口和基础组件
- 实现雪花算法的基础功能

### 阶段2：存储实现与分段ID
- 完成存储接口定义
- 实现分段ID基础功能
- 实现Redis存储

### 阶段3：高级功能
- 实现链式分段ID
- 完善时钟回拨处理
- 优化性能和稳定性

### 阶段4：Spring Boot集成
- 开发Spring Boot Starter
- 提供自动配置

### 阶段5：性能测试与优化
- 开发基准测试
- 执行性能测试
- 性能调优
