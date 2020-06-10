package cn.hsb.router;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableDiscoveryClient
@SpringBootApplication
public class RouterApplication {
    public static void main(String[] args) {
        SpringApplication.run(RouterApplication.class, args);
    }

    @RestController
    static class Index {
        @GetMapping("/")
        public String index() {
            return "It works";
        }
    }
}
