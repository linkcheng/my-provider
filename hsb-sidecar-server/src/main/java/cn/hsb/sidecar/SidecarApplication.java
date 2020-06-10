package cn.hsb.sidecar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableFeignClients
@SpringBootApplication
public class SidecarApplication {
    public static void main(String[] args) {
        SpringApplication.run(SidecarApplication.class, args);
    }

    @RestController
    static class Index {
        @GetMapping("/")
        public String index() {
            return "It works";
        }
    }
}
