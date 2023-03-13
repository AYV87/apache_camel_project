package com.example.camel_project.bitronix;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jms.PoolingConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.transaction.TransactionManager;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class BitronixConfig {

    // Bitronix Transaction Manager встроенная конфигурация
    @Bean(name = "btmConfig")
    public bitronix.tm.Configuration doConfigure() {
        bitronix.tm.Configuration conf
                = TransactionManagerServices.getConfiguration();
        conf.setServerId("TanderTask");
        conf.setLogPart1Filename("./tx-logs/part1.btm");
        conf.setLogPart2Filename("./tx-logs/part2.btm");
        return conf;
    }

    // Создаем bitronixTransactionManager
    @Bean(name = "bitronixTransactionManager", destroyMethod="shutdown")
    @DependsOn("btmConfig")
    public BitronixTransactionManager bitronixTransactionManager() throws Throwable {
        BitronixTransactionManager bitronixTransactionManager = TransactionManagerServices.getTransactionManager();
        bitronixTransactionManager.setTransactionTimeout(10000);
        return bitronixTransactionManager;
    }

    // Spring JtaTransactionManager
    @Bean(name = "transactionManager")
    @DependsOn("bitronixTransactionManager")
    public PlatformTransactionManager transactionManager(TransactionManager bitronixTransactionManager) {
        return new JtaTransactionManager(bitronixTransactionManager);
    }

    @Bean(name = "xaJmsConnectionFactory", destroyMethod="close")
    @DependsOn("bitronixTransactionManager")
    public PoolingConnectionFactory getPoolingConnectionFactory() {

        Properties properties = new Properties();
        properties.setProperty("brokerURL", "vm://localhost?broker.persistent=false&broker.useJmx=false&broker.useShutdownHook=false");
        PoolingConnectionFactory cf = new PoolingConnectionFactory();
        cf.setClassName("org.apache.activemq.ActiveMQXAConnectionFactory");
        cf.setMaxPoolSize(100);
        cf.setMinPoolSize(1);
        cf.setUniqueName("xaConnectionFactory");
        cf.setDriverProperties(properties);
        return cf;
    }
}
