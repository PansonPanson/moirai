## **🚀 如何自定义一个 Spring Boot Starter（自定义 Starter）**
Spring Boot Starter 本质上是一个**依赖包**，可以让开发者通过 `pom.xml` 轻松集成特定的功能。  
本文将手把手教你如何创建一个 Spring Boot Starter，并且支持 **自动配置**。

---

## **📌 1. 创建一个新的 Starter 项目**
### **（1）创建一个新的 Maven 项目**
创建一个 **Maven 项目**，并命名为 `my-spring-boot-starter`。  
（你也可以用 `spring-boot-starter-xxx` 作为命名规范）

**目录结构：**
```
my-spring-boot-starter
│── src/main/java/com/example/starter/
│   │── MyService.java  （核心逻辑）
│   │── MyAutoConfiguration.java  （自动配置类）
│── src/main/resources/META-INF/
│   │── spring.factories  （Spring Boot 自动加载）
│── pom.xml
```

---

## **📌 2. 编写核心逻辑（提供一个 Service）**
假设我们的 Starter 提供了一个 `MyService`，用于打印日志：

**创建 `MyService.java`：**
```java
package com.example.starter;

public class MyService {
    public void printMessage() {
        System.out.println("Hello from MyService!");
    }
}
```

---

## **📌 3. 编写 `@Configuration` 自动配置类**
Spring Boot 需要一个 **自动配置类** 来管理 `MyService`，并根据配置文件 `application.properties` 进行控制。

**创建 `MyAutoConfiguration.java`：**
```java
package com.example.starter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
### **🔹 这里做了什么？**
1. **`@Configuration`**：标明这是一个 Spring 配置类。
2. **`@ConditionalOnProperty`**：
    - 只有 `application.properties` 里 `my.service.enabled=true` 时，`MyService` 才会被创建。
    - `matchIfMissing = true` 代表 **默认启用**。
3. **`@ConditionalOnMissingBean`**：
    - 如果 Spring **上下文中没有 `MyService`**，才会创建 Bean，避免重复注册。

---

## **📌 4. 让 Spring Boot 发现 Starter**
Spring Boot **不会自动加载**你的 Starter，必须配置 `spring.factories`。

**创建 `src/main/resources/META-INF/spring.factories` 文件：**
```
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.example.starter.MyAutoConfiguration
```

### **🔹 作用**
- 这让 Spring Boot **在启动时自动加载** `MyAutoConfiguration`，无需手动 `@Import`。

---

## **📌 5. 发布 Starter**
### **（1）打包**
在 `pom.xml` 里添加：
```xml
<packaging>jar</packaging>
```
然后执行：
```sh
mvn clean install
```
这将在本地 `.m2` 仓库安装 Starter。

### **（2）发布到 Maven**
你可以将 Starter 发布到 **私有仓库（Nexus、Artifactory）** 或 **Maven Central**。

---

## **📌 6. 在 Spring Boot 项目中使用 Starter**
现在，我们可以在 **另一个 Spring Boot 项目** 里使用这个 Starter 了！

### **（1）引入 Starter**
在 `pom.xml` 里添加依赖：
```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>my-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### **（2）在 `application.properties` 里启用**
```properties
my.service.enabled=true
```

### **（3）使用 `MyService`**
```java
import com.example.starter.MyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyController {

    @Autowired
    private MyService myService;

    @GetMapping("/test")
    public String test() {
        myService.printMessage();
        return "Service executed!";
    }
}
```

---

## **📌 7. 结果**
1. **启动 Spring Boot**，访问 `http://localhost:8080/test`
2. **控制台会输出**：
   ```
   Hello from MyService!
   ```

🎉 **恭喜，你的 Spring Boot Starter 已经成功运行！**

---

## **✅ 总结**
1. **创建 Starter 项目**
2. **编写核心逻辑 `MyService`**
3. **创建 `MyAutoConfiguration` 作为自动配置**
4. **配置 `spring.factories` 让 Spring Boot 发现它**
5. **打包并发布**
6. **在 Spring Boot 项目中引入并使用**

**🔥 这样，你就能创建自己的 Spring Boot Starter，方便复用和分享功能！** 🚀