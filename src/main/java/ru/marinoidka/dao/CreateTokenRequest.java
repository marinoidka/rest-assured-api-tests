package ru.marinoidka.dao;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.With;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@With

public class CreateTokenRequest {

    public CreateTokenRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @JsonProperty("username")
    private String username;
    @JsonProperty("password")
    private String password;

}