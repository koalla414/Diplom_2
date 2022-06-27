package user;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

import java.util.HashMap;

public class UserClient extends RestAssuredClient{

    private final String ROOT = "/auth";
    private final String REGISTER = ROOT + "/register";
    private final String USER = ROOT + "/user";
    private final String LOGIN = ROOT + "/login";

    public ExtractableResponse<Response> create(User user) {
        return regSpec
                .body(user)
                .when()
                .post(REGISTER)
                .then().log().all()
                .extract();
    }

    public ExtractableResponse<Response> login(UserCredentials creds) {
        return regSpec
                .body(creds)
                .when()
                .post(LOGIN)
                .then().log().all()
                .extract();
    }

    public ExtractableResponse<Response> delete(String accessToken) {
        return regSpec
                .header("Authorization", accessToken)
                .when()
                .delete(USER)
                .then().log().all()
                .extract();
    }

}
