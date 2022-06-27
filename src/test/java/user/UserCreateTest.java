package user;

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

        UserCredentials creds = UserCredentials.from(user); // получаем учетные данные созданного пользователя для авторизации
        ExtractableResponse<Response> loginResponse = userClient.login(creds); // авторизуемся с полученными учетными данными созданного пользователя
        assertEquals(200, loginResponse.statusCode()); // проверили код ответа
        boolean authorized = createResponse.path("success"); // проверили статус ответа

        assertFalse(accessToken.isBlank()); // проверяем, что accessToken ненулевой
        assertTrue(authorized); // проверяем, что пользователь залогинился

        ExtractableResponse<Response>  deleteResponse = userClient.delete(accessToken);
        assertEquals(202, deleteResponse.statusCode());// удаляем созданного пользователя
    }

//    создать пользователя, который уже зарегистрирован


//    создать пользователя и не заполнить одно из обязательных полей


}
