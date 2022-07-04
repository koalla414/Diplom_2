package user;

import clients.UserClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class UserLoginTest {
    private UserClient userClient;
    private String accessToken;

    @Before
    public void setup() { userClient = new UserClient(); }

    @Test
    @DisplayName("Успешный логин под существующим пользователем")
    @Description("Создаем пользователя и логинимся под ним")
    public void loginUserSuccessful() {
        User user = User.getRandomUser(); // сгенерировали данные пользователя
        ExtractableResponse<Response> createResponse = userClient.create(user); // регистрация пользователя - отправили сгенерированные данные на ручку АПИ
        assertEquals(200, createResponse.statusCode()); // проверили код ответа
        accessToken = createResponse.path("accessToken"); // получили accessToken

        UserCredentials creds = UserCredentials.from(user); // получаем учетные данные созданного пользователя для авторизации
        ExtractableResponse<Response> loginResponse = userClient.login(creds); // авторизуемся с полученными учетными данными созданного пользователя
        assertEquals(200, loginResponse.statusCode()); // проверили код ответа
        boolean authorized = loginResponse.path("success"); // проверили статус ответа

        assertFalse(accessToken.isBlank()); // проверяем, что accessToken ненулевой
        assertTrue(authorized); // проверяем, что пользователь залогинился

        userClient.delete(accessToken);
    }

    @Test
    @DisplayName("Система вернёт ошибку, если неправильно указать пароль.")
    @Description("Создаем пользователя, логинимся под ним, искажаем пароль в учетных данных, и логинимся с испорченным паролем, убеждаемся, что что возвращается ошибка.")
    public void loginWithModifiedPasswordIsUnauthorized() {
        User user = User.getRandomUser(); // сгенерировали данные пользователя
        ExtractableResponse<Response> createResponse = userClient.create(user); // регистрация пользователя - отправили сгенерированные данные на ручку АПИ
        assertEquals(200, createResponse.statusCode()); // проверили код ответа
        accessToken = createResponse.path("accessToken"); // получили accessToken

        UserCredentials creds = UserCredentials.from(user); // получаем учетные данные созданного пользователя для авторизации
        ExtractableResponse<Response> loginResponse = userClient.login(creds); // авторизуемся с полученными учетными данными созданного пользователя
        assertEquals(200, loginResponse.statusCode()); // проверили код ответа
        user.setPassword(user.getPassword() + "gYhgf67"); // исказили пароль
        creds = UserCredentials.from(user); // получили данные с искаженным паролем

        loginResponse = userClient.login(creds);
        assertEquals(401, loginResponse.statusCode());
        assertEquals("email or password are incorrect", loginResponse.path("message"));
    }
}