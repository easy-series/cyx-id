package com.example.easyid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 基础Redis连接测试
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class},
        scanBasePackages = {"com.example.easyid.test"})
public class BasicRedisTest {
    
    public static void main(String[] args) {
        SpringApplication.run(BasicRedisTest.class, args);
    }
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Bean(name = "testRedisRunner")
    public CommandLineRunner testRedisRunner() {
        return args -> {
            System.out.println("======== Redis连接测试 ========");
            
            try {
                String testKey = "easy-id:test:basic-key";
                String testValue = "test-value-" + System.currentTimeMillis();
                
                System.out.println("正在写入测试数据：" + testKey + " = " + testValue);
                redisTemplate.opsForValue().set(testKey, testValue);
                
                System.out.println("正在读取测试数据...");
                String fetchedValue = redisTemplate.opsForValue().get(testKey);
                
                if (testValue.equals(fetchedValue)) {
                    System.out.println("测试成功！写入值与读取值匹配");
                } else {
                    System.out.println("测试失败：值不匹配");
                    System.out.println("写入值: " + testValue);
                    System.out.println("读取值: " + fetchedValue);
                }
            } catch (Exception e) {
                System.err.println("Redis连接测试失败: " + e.getMessage());
                e.printStackTrace();
            } finally {
                System.out.println("测试完成，应用将退出");
                System.exit(0);
            }
        };
    }
} 