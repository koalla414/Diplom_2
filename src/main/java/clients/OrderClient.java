package clients;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import order.Order;

public class OrderClient extends RestAssuredClient {
    private final String ROOT = "/orders";

    public ExtractableResponse<Response> createOrder(String accessToken ,Order ingredients) {
        return regSpec()
                .header("Authorization", accessToken)
                .body(ingredients)
                .when()
                .post(ROOT)
                .then().log().all()
                .extract();
    }

    public ExtractableResponse<Response> getListOrder(String accessToken) {
        return regSpec()
                .header("Authorization", accessToken)
                .when()
                .get(ROOT)
                .then().log().all()
                .extract();
    }
}