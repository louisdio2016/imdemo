package com.louisdio.imdemo;

import com.louisdio.imdemo.server.IMServer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@MapperScan("com.louisdio.imdemo.mapper")
@SpringBootApplication
public class IMServerApp implements ApplicationContextAware {

    private static ApplicationContext applicationContext;
    private static DefaultListableBeanFactory defaultListableBeanFactory;

    public static void main(String[] args){
        ApplicationContext context = SpringApplication.run(IMServerApp.class, args);
        IMServer imServer = context.getBean(IMServer.class);
        imServer.run();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        defaultListableBeanFactory = (DefaultListableBeanFactory)applicationContext.getAutowireCapableBeanFactory();
    }

    public static <T> T getBean(Class<T> clazz) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        String className = clazz.getName();
        defaultListableBeanFactory.registerBeanDefinition(className, beanDefinitionBuilder.getBeanDefinition());
        return (T) applicationContext.getBean(className);
    }

    public static Object getBean(String className){
        return applicationContext.getBean(className);
    }

    public static void destroy(String className){
        defaultListableBeanFactory.removeBeanDefinition(className);
        System.out.println("destroy " + className);
    }
}
