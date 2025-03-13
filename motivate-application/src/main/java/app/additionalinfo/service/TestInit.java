package app.additionalinfo.service;

import app.additionalinfo.client.AdditionalInfoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TestInit implements ApplicationRunner {

    private final AdditionalInfoClient additionalInfoClient;

    @Autowired
    public TestInit(AdditionalInfoClient additionalInfoClient) {
        this.additionalInfoClient = additionalInfoClient;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ResponseEntity<String> response = additionalInfoClient.getHelloMessage();

        System.out.println(response.getBody());
    }
}
