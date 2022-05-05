package ru.marinoidka.tests;

import com.github.javafaker.Faker;
import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.marinoidka.dao.Bookingdates;
import ru.marinoidka.dao.CreateBookingRequest;
import ru.marinoidka.dao.CreateTokenRequest;
import ru.marinoidka.tests.base.BaseTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsStringIgnoringCase;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static ru.marinoidka.tests.CreateTokenTests.properties;
import static ru.marinoidka.tests.base.BaseTest.PROPERTIES_FILE_PATH;

@Severity(SeverityLevel.BLOCKER)
@Story("delete a booking")
@Feature("Tests for booking deletion")

public class DeleteBookingTests extends BaseTest {

    private String id;
    Faker faker = new Faker();
    int idNonExisted = faker.number().numberBetween(10000, 39999);
    String stuff = faker.beer().toString();


    @BeforeEach
    void setUp() {
        CreateBookingRequest bookingRequest = CreateBookingRequest.builder()
                .firstname(faker.name().firstName())
                .lastname(faker.name().lastName())
                .totalprice(faker.number().numberBetween(1,99999))
                .depositpaid(true)
                .bookingdates(Bookingdates.builder()
                        .checkin("2018-01-01")
                        .checkout("2019-01-01")
                        .build())
                .additionalneeds(faker.gameOfThrones().quote())
                .build();
        //создает бронирование
        id = given()
                .log()
                .all()
                .header("Content-Type", "application/json")
                .body(bookingRequest)
                .expect()
                .statusCode(200)
                .when()
                .post("booking")
                .prettyPeek()
                .body()
                .jsonPath()
                .get("bookingid")
                .toString();
    }

    @Test
    @Step("delete an existing booking")
    void deleteBookingPositiveTest() {
        given()
                .log()
                .all()
                .header("Cookie", "token=" + token)
                .when()
                .delete("booking/" + id)
                .prettyPeek()
                .then()
                .statusCode(201);
    }

    @Test
    @Step("delete non existed booking negative test")
    void deleteNonExistedBookingNegativeTest() {
        Response response = given()
                .log()
                .all()
                .header("Cookie", "token=" + token)
                .when()
                .delete("booking/" + idNonExisted)
                .prettyPeek();
        assertThat(response.statusCode(), equalTo(405));
        assertThat(response.getStatusLine(), equalTo("HTTP/1.1 405 Method Not Allowed"));
    }

    @Test
    @Step("delete booking without id")
    void deleteEmptyBookingNegativeTest() {
        Response response = given()
                .log()
                .all()
                .header("Cookie", "token=" + token)
                .when()
                .delete("booking/")
                .prettyPeek();
        assertThat(response.statusCode(), equalTo(404));
        assertThat(response.getStatusLine(), equalTo("HTTP/1.1 404 Not Found"));
    }

    @Test
    @Step("delete booking with wrong URL")
    void deleteStuffBookingNegativeTest() {
        Response response = given()
                .log()
                .all()
                .header("Cookie", "token=" + token)
                .when()
                .delete("booking/" + stuff)
                .prettyPeek();
        assertThat(response.statusCode(), equalTo(405));
        assertThat(response.getStatusLine(), equalTo("HTTP/1.1 405 Method Not Allowed"));
    }
}
