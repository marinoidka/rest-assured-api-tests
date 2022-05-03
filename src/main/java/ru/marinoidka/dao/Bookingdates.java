package ru.marinoidka.dao;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@With
@JsonPropertyOrder({
        "checkin",
        "checkout"
})
@Generated("jsonschema2pojo")
public class Bookingdates {
    @Getter
    @JsonProperty("checkin")
    private String checkin;

    @Getter
    @JsonProperty("checkout")
    private String checkout;

}
