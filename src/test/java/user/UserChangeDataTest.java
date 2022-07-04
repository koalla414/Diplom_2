package user;

import clients.UserClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class UserChangeDataTest {
    private String inputEmail;
    private String inputName;
    private String inputPassword;
    private int expectedStatusCode;
    private boolean success;
    private String expectedMessageResponse;
    private boolean isAuthorized;
    private UserClient userClient;

    @Before
    public void setup() {
        userClient = new UserClient();
    }

    public UserChangeDataTest(String inputEmail, String inputPassword, String inputName, boolean isAuthorized, int expectedStatusCode, boolean success, String expectedMessageResponse) {
    this.inputEmail = inputEmail;
    this.inputPassword = inputPassword;
    this.inputName = inputName;
    this.isAuthorized = isAuthorized;
    this.expectedStatusCode = expectedStatusCode;
    this.success = success;
    this.expectedMessageResponse = expectedMessageResponse;
}

    @Parameterized.Parameters(name = "Изменение данных: {0}, {1}, {2} при стутусе авторизации пользователя: {3}. Ожидается код ответа: {4} и сообщение: {5}.")
    public static Object[][] checkChangeDataUser() {
        return new Object[][]{
                {User.getRandomEmail(), null, null, true, 200, true, ""}, //изменение почты авторизованного пользователя
                {null, User.getRandomPassword(), null, true, 200, true, ""}, //изменение пароля авторизованного пользователя
                {null, null, User.getRandomName(), true, 200, true, ""}, //изменение имени авторизованного пользователя
                {User.getRandomEmail(), null, null, false, 401, false, "You should be authorised"}, //изменение почты неавторизованного пользователя
                {null, User.getRandomPassword(), null, false, 401, false, "You should be authorised"}, //изменение пароля неавторизованного пользователя
                {null, null, User.getRandomName(), false, 401, false, "You should be authorised"}, //изменение имени неавторизованного пользователя
                {User.getRandomEmail(), User.getRandomPassword(), User.getRandomName(), true, 200, true, ""}, //изменение всех полей авторизованного пользователя
                {User.getRandomEmail(), User.getRandomPassword(), User.getRandomName(), false, 401, false, "You should be authorised"}, //изменение всех полей неавторизованного пользователя
                {null, null, null, true, 200, true, ""}, //отправка неизмененных данных в запросе на изменение данных авторизованного пользователя
                {null, null, null, false, 401, false, "You should be authorised"}, //отправка неизмененных данных в запросе на изменение данных неавторизованного пользователя
                {user2.getEmail(), null, null, true, 403, false, "User with such email already exists"}, //изменение email на занятый
        };
    }

    public static String getToken(ExtractableResponse<Response> createResponse) {
    return createResponse.path("accessToken");
    }

    static User user2 = User.getRandomUser(); // сгенерировали данные пользователя
    static UserCredentials creds2 = UserCredentials.from(user2); // получаем учетные данные созданного пользователя
    static  ExtractableResponse<Response> createResponse2 = new UserClient().create(user2); // регистрация пользователя - отправили сгенерированные данные на ручку АПИ
    static String accessToken2 = createResponse2.path("accessToken"); // получили accessToken

    @Test
    @DisplayName("Изменение данных пользователя")
    @Description
    public void checkChangeData() {
        User user = User.getRandomUser(); // сгенерировали данные пользователя
        UserCredentials creds = UserCredentials.from(user); // получаем учетные данные созданного пользователя
        ExtractableResponse<Response> createResponse = new UserClient().create(user); // регистрация пользователя - отправили сгенерированные данные на ручку АПИ
        assertEquals(200, createResponse.statusCode()); // проверили код ответа
        String accessToken = createResponse.path("accessToken"); // получили accessToken

        if (inputEmail != null) {
            user.setEmail(inputEmail); // меняем почту на указанную
        }

        if (inputName != null) {
            user.setName(inputName); // меняем имя на указанное
        }

        if (inputPassword != null) {
            user.setPassword(inputPassword); // меняем пароль на указанный
        }

        String changeDataAccessToken;
        if (isAuthorized) {
            changeDataAccessToken = accessToken;
        } else {
            changeDataAccessToken = "";
        }

        ExtractableResponse<Response>  changeDataResponse = userClient.changeData(changeDataAccessToken, user);
        assertEquals(expectedStatusCode, changeDataResponse.statusCode());

        assertEquals(success, changeDataResponse.path("success"));

        if (changeDataResponse.statusCode() >= 400 && changeDataResponse.statusCode() < 500) { // если статус код неуспешный
            assertEquals(expectedMessageResponse, changeDataResponse.path("message"));
        }

        ExtractableResponse<Response>  deleteResponse = userClient.delete(accessToken);
        assertEquals(202, deleteResponse.statusCode());// удаляем созданного пользователя

        if (user2.getEmail().equals(inputEmail)) { // если создавался второй пользователь
            ExtractableResponse<Response>  deleteResponse2 = userClient.delete(accessToken2);
            assertEquals(202, deleteResponse2.statusCode());// удаляем его
        }
    }
}