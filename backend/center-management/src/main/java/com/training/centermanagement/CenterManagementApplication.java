package com.training.centermanagement;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableEncryptableProperties
public class CenterManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(CenterManagementApplication.class, args);
    }

}
