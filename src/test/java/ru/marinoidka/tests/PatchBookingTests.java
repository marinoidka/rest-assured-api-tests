package ru.marinoidka.tests;

import com.github.javafaker.Faker;
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

public class PatchBookingTests {

    private static String token;
    private String id;
    private static final String PROPERTIES_FILE_PATH = "src/test/resources/application.properties";
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

    @BeforeAll
    static void beforeAll() throws IOException {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        CreateTokenRequest request = CreateTokenRequest.builder()
                .username("admin")
                .password("password123")
                .build();

        properties.load(new FileInputStream(PROPERTIES_FILE_PATH));
        RestAssured.baseURI = properties.getProperty("base.url");

        token = given()//предусловия, подготовка
                .log()
                .all()
                .header("Content-Type", "application/json")
                .body(request)
                .expect()
                .statusCode(200)
                .body("token", is(CoreMatchers.not(nullValue())))
                .when()
                .post("auth")//шаг(и)
                .prettyPeek()
                .body()
                .jsonPath()
                .get("token")
                .toString();
    }


    @BeforeEach
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
