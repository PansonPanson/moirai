## **🚀 Spring Boot 自定义 Starter 的原理**
**Spring Boot Starter** 本质上是一个 **普通的 Maven/Gradle 模块**，通过 **自动配置机制（Auto-Configuration）** 和 `spring.factories` 文件，将功能**自动加载到 Spring 容器**中，简化了组件引入的复杂性。

✅ 当你引入自定义 Starter 时，Spring Boot 会：
1. **自动加载指定的配置类**（`@Configuration`）。
2. 根据条件（`@Conditional*`）**判断是否加载对应的 Bean**。
3. 将功能组件（如 `MyService`）**自动注册为 Spring Bean**，无需手动配置。

---

## **🌟 核心原理**
自定义 Starter 基于 **Spring Boot 自动配置机制**，主要涉及以下核心组件：

✅ **1. 自动配置类：`@Configuration` + `@Conditional`**  
✅ **2. `spring.factories` 文件**  
✅ **3. 条件装配机制：`@Conditional*` 注解**  
✅ **4. SPI（Service Provider Interface）机制**  
✅ **5. Spring Boot 自动装配原理**

---

## **📌 1. `spring.factories` 加载自动配置类**
自定义 Starter 的自动装配**核心在于 `spring.factories` 文件**。

### **🔹 spring.factories 文件**
```properties
# META-INF/spring.factories
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.example.starter.MyAutoConfiguration
```
✅ 作用：
- Spring Boot 启动时会自动扫描类路径下的 `spring.factories` 文件。
- 将配置类 `MyAutoConfiguration` 自动加载进 Spring 容器。

---

## **📌 2. Spring Boot 启动时如何加载 `spring.factories`？**
Spring Boot 启动时会调用：
```java
SpringApplication.run()
```
**核心逻辑在：`org.springframework.boot.SpringApplication` → `loadFactoryNames()` 方法**
```java
// 加载 spring.factories 文件中的配置类
List<String> configurations = SpringFactoriesLoader.loadFactoryNames(
        EnableAutoConfiguration.class, classLoader);
```

**`SpringFactoriesLoader.loadFactoryNames()`**
```java
public static List<String> loadFactoryNames(Class<?> factoryClass, ClassLoader classLoader) {
    String factoryClassName = factoryClass.getName();  // org.springframework.boot.autoconfigure.EnableAutoConfiguration
    Enumeration<URL> urls = classLoader.getResources("META-INF/spring.factories");
    while (urls.hasMoreElements()) {
        URL url = urls.nextElement();
        Properties properties = PropertiesLoaderUtils.loadProperties(new UrlResource(url));
        // 从 spring.factories 文件中读取配置类名
        List<String> configurations = properties.getProperty(factoryClassName);
    }
    return configurations;
}
```
✅ **Spring Boot 会读取 `META-INF/spring.factories` 文件，加载所有的自动配置类**。

---

## **📌 3. 自动配置类**
`spring.factories` 文件中指定的类，如：
```java
com.example.starter.MyAutoConfiguration
```
**自动配置类**本质上是一个普通的 `@Configuration` 类：
```java
@Configuration
@ConditionalOnProperty(name = "my.service.enabled", havingValue = "true", matchIfMissing = true)
public class MyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MyService myService() {
        return new MyService();
    }
}
```
✅ **Spring Boot 会自动扫描和加载该类**，并根据条件装配规则，注册对应的 Bean 到 Spring 容器。

---

## **📌 4. 条件装配机制：`@Conditional` 系列注解**
在自动配置类中，我们通常会使用 **条件装配注解**来控制 Bean 的加载：
1. **`@ConditionalOnProperty`**：根据 `application.properties` 中的配置加载 Bean。
   ```java
   @ConditionalOnProperty(name = "my.service.enabled", havingValue = "true", matchIfMissing = true)
   ```

2. **`@ConditionalOnMissingBean`**：只有当容器中没有指定类型的 Bean 时，才创建。
   ```java
   @Bean
   @ConditionalOnMissingBean
   public MyService myService() {
       return new MyService();
   }
   ```

3. **`@ConditionalOnClass`**：只有类路径中存在指定类时，才创建 Bean。
   ```java
   @ConditionalOnClass(name = "javax.sql.DataSource")
   ```

✅ **条件装配机制**确保：
- 不重复注册 Bean。
- 按需加载，保持灵活性。

---

## **📌 5. Spring Boot 自动装配机制**
### **🔹 Spring Boot 如何自动装配？**
当 Spring Boot 启动时：
1. **读取 `spring.factories` 文件**
2. 自动加载 `EnableAutoConfiguration` 类：
   ```java
   @SpringBootApplication
   @EnableAutoConfiguration
   public class MyApplication {
       public static void main(String[] args) {
           SpringApplication.run(MyApplication.class, args);
       }
   }
   ```
3. 扫描并加载 `MyAutoConfiguration` 中的 Bean：
    - 如果满足 `@Conditional` 条件，则将 Bean 注入容器。
    - 不满足则跳过。

---

## **📌 6. 自定义 Starter 的运行机制**
1. **Spring Boot 启动时**
    - 扫描 `META-INF/spring.factories`。
2. **加载自动配置类**
    - 读取 `EnableAutoConfiguration` 下的配置类。
    - 自动加载 `MyAutoConfiguration`。
3. **条件匹配**
    - 满足 `@ConditionalOnProperty` 条件 → 加载 `MyService`。
4. **Bean 自动注册**
    - 将 `MyService` 注入 Spring 容器。

---

## ✅ **🔥 核心原理图**
```
                Spring Boot Starter 加载流程
 ┌─────────────────────────────────────────────────┐
 │            Spring Boot 启动                     │
 └─────────────────────────────────────────────────┘
                         │
                         ▼
     1️⃣ 加载 `spring.factories` 文件  
                         │
                         ▼
     2️⃣ 解析 `EnableAutoConfiguration`
                         │
                         ▼
     3️⃣ 加载 `MyAutoConfiguration` 配置类
                         │
                         ▼
     4️⃣ 按条件装配 `MyService` Bean  
                         │
                         ▼
     5️⃣ 将 `MyService` 注册到 Spring 容器  
                         │
                         ▼
               🚀 Starter 功能生效
```

---

## **✅ 总结**
🔥 Spring Boot 自定义 Starter 的核心原理：
1. **`spring.factories` 文件** → 告诉 Spring Boot 加载哪些自动配置类。
2. **自动配置类** → 定义 Bean 和条件装配。
3. **条件装配机制** → 确保按需加载，灵活配置。
4. **SPI 机制 + SpringFactoriesLoader** → 动态加载配置类。
5. **Spring Boot 自动装配** → 将组件自动注册到容器中。

---

## **🚀 为什么使用 Starter？**
- **模块化封装**：将常用功能封装成 Starter，项目只需引入依赖即可使用。
- **自动加载**：简化配置，自动装配功能。
- **可扩展性**：支持按需加载，灵活配置。

✅ **自定义 Starter 的原理**基于 Spring Boot 自动装配机制，让功能模块化封装，简化开发流程！ 🚀