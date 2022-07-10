package user;

import clients.UserClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserCreateTest {
    private UserClient userClient;
    private String accessToken;

    @Before
    public void setup() {userClient = new UserClient();}

    @Test
    @DisplayName("Можно создать уникального пользователя.")
    @Description("Создаем пользователя, логинимся под ним, чтобы убедиться что действительно создан.")
    public void createRandomUserSuccessful() {
        User user = User.getRandomUser(); // сгенерировали данные пользователя
        ExtractableResponse<Response> createResponse = userClient.create(user); // регистрация пользователя - отправили сгенерированные данные на ручку АПИ
        assertEquals(200, createResponse.statusCode()); // проверили код ответа
        accessToken = createResponse.path("accessToken"); // получили accessToken
        boolean created = createResponse.path("success"); // проверили статус ответа

        UserCredentials creds = UserCredentials.from(user); // получаем учетные данные созданного пользователя для авторизации
        ExtractableResponse<Response> loginResponse = userClient.login(creds); // авторизуемся с полученными учетными данными созданного пользователя
        assertEquals(200, loginResponse.statusCode()); // проверили код ответа

        assertFalse(accessToken.isBlank()); // проверяем, что accessToken ненулевой
        assertTrue(created); // проверяем, что пользователь создался

        userClient.delete(accessToken);
    }

    @Test
    @DisplayName("Нельзя создать пользователя, который уже зарегистрирован")
    @Description("Создаем пользователя, логинимся под ним, чтобы убедиться что действительно создан, повторно создаем пользователя с теми же учетными данными, проверяем, что возвращается ошибка.")
    public void createDuplicateUserIsConflict() {
        User user = User.getRandomUser(); // сгенерировали данные пользователя
        ExtractableResponse<Response> createResponse = userClient.create(user); // регистрация пользователя - отправили сгенерированные данные на ручку АПИ
        assertEquals(200, createResponse.statusCode()); // проверили код ответа
        accessToken = createResponse.path("accessToken"); // получили accessToken

        UserCredentials creds = UserCredentials.from(user); // получаем учетные данные созданного пользователя для авторизации
        ExtractableResponse<Response> loginResponse = userClient.login(creds); // авторизуемся с полученными учетными данными созданного пользователя
        assertEquals(200, loginResponse.statusCode()); // проверили код ответа
        boolean authorized = createResponse.path("success"); // проверили статус ответа

        assertFalse(accessToken.isBlank()); // проверяем, что accessToken ненулевой
        assertTrue(authorized); // проверяем, что пользователь залогинился

        createResponse = userClient.create(user); // повторная регистрация того же пользователя
        if (createResponse.statusCode() >= 200 && createResponse.statusCode() < 300) { // если статус код на создание второго пользователя успешный
            String accessToken2 = createResponse.path("accessToken"); // получили accessToken второго пользователя
            userClient.delete(accessToken2); // то удаляем второго пользователя тоже
        }

        assertEquals(403, createResponse.statusCode()); // сравнили статус-код
        assertEquals("User already exists", createResponse.path("message")); // сравнили сообщение об ошибке

        userClient.delete(accessToken); // удаляем созданного пользователя
    }

    @Test
    @DisplayName("Нельзя создать пользователя и не заполнить одно из обязательных полей")
    @Description
    public void createUserWithoutPasswordIsBadRequest() {
        User user = User.getRandomUser();
        user.setPassword("");
        ExtractableResponse createResponse = userClient.create(user);

        if (createResponse.statusCode() >= 200 && createResponse.statusCode() < 300) { // если статус код на создание пользователя успешный
            String accessToken = createResponse.path("accessToken"); // получили accessToken пользователя
            userClient.delete(accessToken); // то удаляем пользователя
        }

        assertEquals(403, createResponse.statusCode());
        assertEquals("Email, password and name are required fields", createResponse.path("message"));
    }
}