package ru.marinoidka.tests;

import io.restassured.RestAssured;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.response.Response;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.certificate;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsStringIgnoringCase;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

public class PatchBookingTests {

    String id;
    static String token;
    private static String jsonStringFirstname =  "{\n \"firstname\": \"James\" \n}";
    private static String jsonStringTwoParams =  "{\n"
            + "    \"lastname\" : \"Show\",\n"
            + "    \"totalprice\" : 222\n"
            + "}";
    private static String jsonStringCheckIn = "{\n"
            + "    \"bookingdates\" : {\n"
            + "        \"checkin\" : \"2020-11-11\"\n"
            + "    }\n"
            + "}";
    private static String jsonStringAllParams = "{\n"
            + "    \"firstname\" : \"Hanna\",\n"
            + "    \"lastname\" : \"Bobby\",\n"
            + "    \"totalprice\" : 1,\n"
            + "    \"depositpaid\" : false,\n"
            + "    \"bookingdates\" : {\n"
            + "        \"checkin\" : \"2020-11-11\",\n"
            + "        \"checkout\" : \"2022-01-01\"\n"
            + "    },\n"
            + "    \"additionalneeds\" : \"Cat\"\n"
            + "}";

    @BeforeAll
    static void beforeAll() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }


    @BeforeAll
    static void setToken() {
        token = given()//предусловия, подготовка
                .log()
                .all()
                .header("Content-Type", "application/json")
                .body("{\n"
                        + "    \"username\" : \"admin\",\n"
                        + "    \"password\" : \"password123\"\n"
                        + "}")
                .expect()
                .statusCode(200)
                .body("token", is(CoreMatchers.not(nullValue())))
                .when()
                .post("https://restful-booker.herokuapp.com/auth")//шаг(и)
                .prettyPeek()
                .body()
                .jsonPath()
                .get("token")
                .toString();
    }

    @BeforeEach
    void setUp() {
        //создает бронирование
        id = given()
                .log()
                .all()
                .header("Content-Type", "application/json")
                .body("{\n"
                        + "    \"firstname\" : \"Jim\",\n"
                        + "    \"lastname\" : \"Brown\",\n"
                        + "    \"totalprice\" : 111,\n"
                        + "    \"depositpaid\" : true,\n"
                        + "    \"bookingdates\" : {\n"
                        + "        \"checkin\" : \"2018-01-01\",\n"
                        + "        \"checkout\" : \"2019-01-01\"\n"
                        + "    },\n"
                        + "    \"additionalneeds\" : \"Breakfast\"\n"
                        + "}")
                .expect()
                .statusCode(200)
                .when()
                .post("https://restful-booker.herokuapp.com/booking")
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
                .body(jsonStringFirstname)
                .when()
                .patch("https://restful-booker.herokuapp.com/booking/" + id)
                .prettyPeek();
        assertThat(response.statusCode(), equalTo(200));
        assertThat(response.body().jsonPath().get("firstname"), containsStringIgnoringCase("James"));

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
                .patch("https://restful-booker.herokuapp.com/booking/" + id)
                .prettyPeek();
        assertThat(response.statusCode(), equalTo(200));
        assertThat(response.body().jsonPath().get("lastname"), containsStringIgnoringCase("Show"));
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
                .patch("https://restful-booker.herokuapp.com/booking/" + id)
                .prettyPeek();
        assertThat(response.statusCode(), equalTo(200));
        assertThat(response.body().jsonPath().get("bookingdates.checkin"), containsStringIgnoringCase("2020-11-11"));

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
                .patch("https://restful-booker.herokuapp.com/booking/" + id)
                .prettyPeek();
        assertThat(response.statusCode(), equalTo(200));
        assertThat(response.body().jsonPath().get("firstname"), containsStringIgnoringCase("Hanna"));
        assertThat(response.body().jsonPath().get("lastname"), containsStringIgnoringCase("Bobby"));
        assertThat(response.body().jsonPath().get("totalprice"), equalTo(1));
        assertThat(response.body().jsonPath().get("bookingdates.checkin"), containsStringIgnoringCase("2020-11-11"));
        assertThat(response.body().jsonPath().get("bookingdates.checkout"), containsStringIgnoringCase("2022-01-01"));
        assertThat(response.body().jsonPath().get("additionalneeds"), containsStringIgnoringCase("Cat"));
    }

    @Test
    void patchBookingWithoutTokenNegativeTest() {

        Response response = given()
                .log()
                .all()
                .header("Content-type", "application/json")
                .and()
                .body(jsonStringFirstname)
                .when()
                .patch("https://restful-booker.herokuapp.com/booking/" + id)
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
                .body(jsonStringFirstname)
                .when()
                .patch("https://restful-booker.herokuapp.com/booking/")
                .prettyPeek();
        assertThat(response.statusCode(), equalTo(404));
        assertThat(response.getStatusLine(), equalTo("HTTP/1.1 404 Not Found"));
    }
}
