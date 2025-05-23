package ru.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "user-service", path = "/api/v1/user", contextId = "userServiceClient")
public interface UserServiceClient extends UserClientOperations{

}
