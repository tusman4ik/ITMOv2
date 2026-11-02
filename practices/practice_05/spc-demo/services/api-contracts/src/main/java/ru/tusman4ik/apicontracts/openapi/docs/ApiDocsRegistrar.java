package ru.tusman4ik.apicontracts.openapi.docs;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import ru.tusman4ik.apicontracts.coderunner.api.SubmissionsApi;
import ru.tusman4ik.apicontracts.taskservice.api.TaskApi;

import java.beans.Introspector;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public class ApiDocsRegistrar implements BeanDefinitionRegistryPostProcessor {

    public static final List<Class<?>> CONTROLLERS = List.of(TaskApi.class, SubmissionsApi.class);

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        ClassLoader loader = ApiDocsRegistrar.class.getClassLoader();
        for (Class<?> api : CONTROLLERS) {
            RootBeanDefinition definition = new RootBeanDefinition(api);
            definition.setTargetType(api);
            definition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            definition.setInstanceSupplier(() -> Proxy.newProxyInstance(loader, new Class<?>[]{api}, new NullInvoker()));
            registry.registerBeanDefinition(beanName(api), definition);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    }

    private static String beanName(Class<?> api) {
        return Introspector.decapitalize(api.getSimpleName()) + "DocsController";
    }

    private static class NullInvoker implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "toString" -> proxy.getClass().getSimpleName() + " docs proxy";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == (args == null || args.length == 0 ? null : args[0]);
                default -> null;
            };
        }
    }
}
