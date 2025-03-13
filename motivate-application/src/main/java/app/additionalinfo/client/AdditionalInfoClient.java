package app.additionalinfo.client;

import app.additionalinfo.client.dto.AdditionalInfo;
import app.additionalinfo.client.dto.UpsertAdditionalInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "additional-info-svc", url = "http://localhost:8081/api/v1/additional-info")
public interface AdditionalInfoClient {

    @GetMapping("/test")
    ResponseEntity<String> getHelloMessage();

    @PostMapping("/form")
    ResponseEntity<Void> upsertAdditionalInfo(@RequestBody UpsertAdditionalInfo upsertAdditionalInfo);

    @GetMapping("/form")
    ResponseEntity<AdditionalInfo> getAdditionalInfo(@RequestParam(name = "userId") UUID userId);

}
