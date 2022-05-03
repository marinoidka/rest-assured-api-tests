package ru.marinoidka.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

public class CreateTokenResponse {
    @Getter
    @JsonProperty("token")
    private String token;

}
