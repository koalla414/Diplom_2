package clients;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class RestAssuredClient {

    protected final String URL = "https://stellarburgers.nomoreparties.site/api";

    protected final RequestSpecification regSpec = given()
            .log().all()
            .contentType(ContentType.JSON)
            .baseUri(URL);

    protected RequestSpecification regSpec() {
        return given()
                .log().all()
                .contentType(ContentType.JSON)
                .baseUri(URL);
    }
}