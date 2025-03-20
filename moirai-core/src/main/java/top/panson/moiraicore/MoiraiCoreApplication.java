package top.panson.moiraicore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = "top.panson")
public class MoiraiCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoiraiCoreApplication.class, args);
    }

}
