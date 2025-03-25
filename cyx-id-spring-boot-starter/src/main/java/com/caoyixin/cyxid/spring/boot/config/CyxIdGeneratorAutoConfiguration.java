package com.caoyixin.cyxid.spring.boot.config;

import com.caoyixin.cyxid.core.IdGenerator;
import com.caoyixin.cyxid.core.provider.DefaultIdGeneratorProvider;
import com.caoyixin.cyxid.core.provider.IdGeneratorProvider;
import com.caoyixin.cyxid.segment.SegmentIdGenerator;
import com.caoyixin.cyxid.segment.SegmentIdGeneratorFactory;
import com.caoyixin.cyxid.segment.chain.SegmentChainIdGenerator;
import com.caoyixin.cyxid.segment.chain.SegmentChainIdGeneratorFactory;
import com.caoyixin.cyxid.snowflake.ClockBackwardsHandler;
import com.caoyixin.cyxid.snowflake.DefaultClockBackwardsHandler;
import com.caoyixin.cyxid.snowflake.SnowflakeIdGenerator;
import com.caoyixin.cyxid.snowflake.StaticWorkerIdAssigner;
import com.caoyixin.cyxid.snowflake.WorkerIdAssigner;
import com.caoyixin.cyxid.spring.boot.properties.CyxIdProperties;
import com.caoyixin.cyxid.storage.api.SegmentAllocator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;

/**
 * ID生成器自动配置类
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(CyxIdProperties.class)
@Import(CyxIdRedisAutoConfiguration.class)
@ConditionalOnProperty(prefix = "cyx-id", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CyxIdGeneratorAutoConfiguration {
    
    /**
     * 创建ID生成器提供者
     */
    @Bean
    @ConditionalOnMissingBean
    public IdGeneratorProvider idGeneratorProvider(List<IdGenerator> idGenerators) {
        DefaultIdGeneratorProvider provider = new DefaultIdGeneratorProvider();
        
        if (idGenerators != null && !idGenerators.isEmpty()) {
            for (IdGenerator idGenerator : idGenerators) {
                provider.registerGenerator(idGenerator.getName(), idGenerator);
                provider.registerGenerator(idGenerator.getType(), idGenerator);
                log.info("注册ID生成器：name={}, type={}", idGenerator.getName(), idGenerator.getType());
            }
        }
        
        return provider;
    }
    
    /**
     * 创建时钟回拨处理器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "cyx-id.snowflake", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ClockBackwardsHandler clockBackwardsHandler(CyxIdProperties properties) {
        CyxIdProperties.SnowflakeProperties.ClockBackwardsProperties clockBackwards = 
                properties.getSnowflake().getClockBackwards();
        
        log.info("创建时钟回拨处理器：自旋阈值={}ms, 最大阈值={}ms", 
                clockBackwards.getSpinThreshold(), clockBackwards.getBrokenThreshold());
        
        return new DefaultClockBackwardsHandler(
                clockBackwards.getSpinThreshold(),
                clockBackwards.getBrokenThreshold()
        );
    }
    
    /**
     * 创建静态工作节点ID分配器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "cyx-id.snowflake.worker", name = "allocator", havingValue = "static", matchIfMissing = true)
    public WorkerIdAssigner staticWorkerIdAssigner(CyxIdProperties properties) {
        long workerId = properties.getSnowflake().getWorker().getId();
        log.info("创建静态工作节点ID分配器：workerId={}", workerId);
        return new StaticWorkerIdAssigner(workerId);
    }
    
    /**
     * 创建雪花算法ID生成器
     */
    @Bean
    @ConditionalOnMissingBean(name = "snowflakeIdGenerator")
    @ConditionalOnProperty(prefix = "cyx-id", name = {"type", "snowflake.enabled"}, havingValue = "snowflake, true", matchIfMissing = true)
    public IdGenerator snowflakeIdGenerator(WorkerIdAssigner workerIdAssigner, 
                                         ClockBackwardsHandler clockBackwardsHandler,
                                         CyxIdProperties properties) {
        CyxIdProperties.SnowflakeProperties snowflake = properties.getSnowflake();
        
        log.info("创建雪花算法ID生成器：epoch={}, workerIdBits={}, sequenceBits={}", 
                snowflake.getEpoch(), snowflake.getWorker().getBits(), snowflake.getSequence().getBits());
        
        // 创建雪花算法ID生成器
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(
                workerIdAssigner.assignWorkerId(),
                snowflake.getEpoch(),
                snowflake.getWorker().getBits(),
                snowflake.getSequence().getBits(),
                clockBackwardsHandler
        );
        
        return generator;
    }
    
    /**
     * 创建分段ID生成器
     */
    @Bean
    @ConditionalOnMissingBean(name = "segmentIdGenerator")
    @ConditionalOnProperty(prefix = "cyx-id", name = {"type", "segment.enabled"}, havingValue = "segment, true")
    public IdGenerator segmentIdGenerator(SegmentAllocator segmentAllocator, CyxIdProperties properties) {
        log.info("创建分段ID生成器：步长={}, 安全距离={}%", 
                properties.getSegment().getStep(), properties.getSegment().getSafeDistancePercent());
        
        // 创建分段ID生成器工厂
        SegmentIdGeneratorFactory factory = new SegmentIdGeneratorFactory(segmentAllocator);
        
        // 创建默认生成器
        SegmentIdGenerator generator = (SegmentIdGenerator) factory.getGenerator("default");
        
        return generator;
    }
    
    /**
     * 创建链式分段ID生成器
     */
    @Bean
    @ConditionalOnMissingBean(name = "segmentChainIdGenerator")
    @ConditionalOnProperty(prefix = "cyx-id", name = {"type", "segmentChain.enabled"}, havingValue = "segment-chain, true")
    public IdGenerator segmentChainIdGenerator(SegmentAllocator segmentAllocator, CyxIdProperties properties) {
        CyxIdProperties.SegmentChainProperties segmentChain = properties.getSegmentChain();
        
        log.info("创建链式分段ID生成器：步长={}, 安全距离={}%, 最大链长={}, 预取周期={}ms", 
                segmentChain.getStep(), segmentChain.getSafeDistancePercent(), 
                segmentChain.getMaxChainLength(), segmentChain.getPrefetchPeriod());
        
        // 创建链式分段ID生成器工厂
        SegmentChainIdGeneratorFactory factory = new SegmentChainIdGeneratorFactory(
                segmentAllocator,
                segmentChain.getPrefetchPeriod(),
                segmentChain.getSafeDistancePercent(),
                segmentChain.getMaxChainLength()
        );
        
        // 创建默认生成器
        SegmentChainIdGenerator generator = (SegmentChainIdGenerator) factory.getGenerator("default");
        
        return generator;
    }
} 