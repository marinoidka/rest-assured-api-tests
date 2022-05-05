package ru.marinoidka.tests;

import com.github.javafaker.Faker;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import io.restassured.RestAssured;
import io.restassured.mapper.ObjectMapperType;
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
import java.text.SimpleDateFormat;

import static io.restassured.RestAssured.certificate;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static ru.marinoidka.tests.CreateTokenTests.properties;
import static ru.marinoidka.tests.base.BaseTest.PROPERTIES_FILE_PATH;

@Story("patch a booking")
@Feature("Tests for booking patching")

public class PatchBookingTests extends BaseTest {

    private String id;
    Faker faker = new Faker();

    CreateBookingRequest jsonStringNewFirstname = CreateBookingRequest.builder()
            .firstname(faker.name().firstName())
            .build();

    CreateBookingRequest jsonStringTwoParams = CreateBookingRequest.builder()
            .lastname(faker.name().lastName())
            .totalprice(faker.number().numberBetween(0, 44444))
            .build();

    CreateBookingRequest jsonStringCheckIn = CreateBookingRequest.builder()
            .bookingdates(Bookingdates.builder()
                    .checkin(new SimpleDateFormat("yyyy-MM-dd").format(faker.date().birthday()))
            .build())
            .build();


    CreateBookingRequest jsonStringAllParams = CreateBookingRequest.builder()
            .firstname(faker.name().firstName())
            .lastname(faker.name().lastName())
            .totalprice(faker.number().numberBetween(0,10000))
            .bookingdates(Bookingdates.builder()
                    .checkin(new SimpleDateFormat("yyyy-MM-dd").format(faker.date().birthday()))
                    .checkout(new SimpleDateFormat("yyyy-MM-dd").format(faker.date().birthday()))
                    .build())
            .depositpaid(Boolean.valueOf(properties.getProperty("depositpaid")))
            .additionalneeds(String.valueOf(faker.gameOfThrones().toString()))
            .build();


    @BeforeEach
    @Step("create a standart booking")
    void setUp() {
        CreateBookingRequest bookingRequest = CreateBookingRequest.builder()
                .firstname(properties.getProperty("firstname"))
                .lastname(properties.getProperty("lastname"))
                .totalprice(Integer.valueOf(properties.getProperty("totalprice")))
                .depositpaid(Boolean.valueOf(properties.getProperty("depositpaid")))
                .bookingdates(Bookingdates.builder()
                        .checkin(properties.getProperty("checkin"))
                        .checkout(properties.getProperty("checkout"))
                        .build())
                .additionalneeds(properties.getProperty("additionalneeds"))
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
    @Step("patch an existing booking with firstname")
    @Description("random name")
    void patchBookingFirstnamePositiveTest() {

        Response response = given()
                .log()
                .all()
                .header("Content-type", "application/json")
                .header("Cookie", "token=" + token)
                .and()
                .body(jsonStringNewFirstname)
                .when()
                .patch("booking/" + id)
                .prettyPeek();
        assertThat(response.statusCode(), equalTo(200));
        assertThat(response.body().jsonPath().get("firstname"), not(properties.getProperty("firstname")));

    }

    @Test
    @Step("patch an existing booking with lastname and totalprice")
    @Description("random lastname and number from 0 to 44444")
    void patchBookingTwoParamPositiveTest() {
        Response response = given()
                .log()
                .all()
                .header("Content-type", "application/json")
                .header("Cookie", "token=" + token)
                .and()
                .body(jsonStringTwoParams)
                .when()
                .patch("booking/" + id)
                .prettyPeek();
        assertThat(response.statusCode(), equalTo(200));
        assertThat(response.body().jsonPath().get("lastname"), not(properties.getProperty("lastname")));
        assertThat(response.body().jsonPath().get("firstname"), equalTo(properties.getProperty("firstname")));
    }

    @Test
    @Step("patch an existing booking with checkin only")
    @Description("random date")
    void patchBookingCheckInPositiveTest() {

        Response response = given()
                .log()
                .all()
                .header("Content-type", "application/json")
                .header("Cookie", "token=" + token)
                .and()
                .body(jsonStringCheckIn)
                .when()
                .patch("booking/" + id)
                .prettyPeek();
        assertThat(response.statusCode(), equalTo(200));
        assertThat(response.body().jsonPath().get("bookingdates.checkin"), notNullValue());
        assertThat(response.body().jsonPath().get("bookingdates.checkin"), not(properties.getProperty("checkin")));

    }

    @Test
    @Step("patch an existing booking with all fields")
    @Description("all random fields except depositpaid from properties")
    void patchAllBookingPositiveTest() {
        Response response = given()
                .log()
                .all()
                .header("Content-type", "application/json")
                .header("Cookie", "token=" + token)
                .and()
                .body(jsonStringAllParams)
                .when()
                .patch("booking/" + id)
                .prettyPeek();
        assertThat(response.statusCode(), equalTo(200));
        assertThat(response.body().jsonPath().get("firstname"), notNullValue());
        assertThat(response.body().jsonPath().get("lastname"), notNullValue());
        assertThat(response.body().jsonPath().get("totalprice"), notNullValue());
        assertThat(response.body().jsonPath().get("bookingdates.checkin"), notNullValue());
        assertThat(response.body().jsonPath().get("bookingdates.checkout"), notNullValue());
        assertThat(response.body().jsonPath().get("additionalneeds"), notNullValue());
    }

    @Test
    @Step("patch an existing booking without token")
    void patchBookingWithoutTokenNegativeTest() {

        Response response = given()
                .log()
                .all()
                .header("Content-type", "application/json")
                .and()
                .body(jsonStringNewFirstname)
                .when()
                .patch("booking/" + id)
                .prettyPeek();
        assertThat(response.statusCode(), equalTo(403));
        assertThat(response.getStatusLine(), equalTo("HTTP/1.1 403 Forbidden"));

    }

    @Test
    @Step("patch booking using url without id")
    void patchBookingWithoutIdNegativeTest() {
        Response response = given()
                .log()
                .all()
                .header("Content-type", "application/json")
                .header("Cookie", "token=" + token)
                .and()
                .body(jsonStringNewFirstname)
                .when()
                .patch("booking/")
                .prettyPeek();
        assertThat(response.statusCode(), equalTo(404));
        assertThat(response.getStatusLine(), equalTo("HTTP/1.1 404 Not Found"));
    }
}
