package com.example.camel_project.camel;

import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.spring.javaconfig.CamelConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelConfigurationImpl extends CamelConfiguration {
    // настраиваем camel
    @Override
    protected void setupCamelContext(CamelContext camelContext) {
        ActiveMQXAConnectionFactory connectionFactory = new ActiveMQXAConnectionFactory();
        // устанавливаем соединение с брокером сообщений
        connectionFactory.setBrokerURL("vm://localhost?broker.persistent=false&broker.useJmx=false&broker.useShutdownHook=false");
        JmsComponent answer = new JmsComponent();
        answer.setConnectionFactory(connectionFactory);
        // в контекст самого camel кладем новый компонент (наш брокер сообщений)
        camelContext.addComponent("jms", answer);
    }
}
