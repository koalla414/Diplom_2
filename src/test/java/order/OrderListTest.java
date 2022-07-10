package order;

import clients.OrderClient;
import clients.UserClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import user.User;
import user.UserCredentials;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class OrderListTest {
    private int expectedStatusCode;
    private boolean success;
    private String expectedMessageResponse;
    private boolean isAuthorized;
    private UserClient userClient;

    @Before
    public void setup() {
        userClient = new UserClient();
    }

    public OrderListTest(boolean isAuthorized, int expectedStatusCode, boolean success, String expectedMessageResponse) {
        this.isAuthorized = isAuthorized;
        this.expectedStatusCode = expectedStatusCode;
        this.success = success;
        this.expectedMessageResponse = expectedMessageResponse;
    }

    @Parameterized.Parameters(name = "При стутусе авторизации пользователя: {0}. Ожидается код ответа: {1} и сообщение: {3}.")
    public static Object[][] checkChangeDataUser() {
        return new Object[][]{
                {true, 200, true, ""}, // Получение заказов авторизованного пользователя
                {false, 401, false, "You should be authorised"}, // Получение заказов неавторизованного пользователя
        };
    }

    @Test
    @DisplayName("Создание заказа")
    @Description
    public void checkChangeData() {
        String accessToken;
        if (isAuthorized) {
            User user = User.getRandomUser(); // сгенерировали данные пользователя
            UserCredentials creds = UserCredentials.from(user); // получаем учетные данные созданного пользователя
            ExtractableResponse<Response> createResponse = userClient.create(user); // регистрация пользователя - отправили сгенерированные данные на ручку АПИ
            assertEquals(200, createResponse.statusCode()); // проверили код ответа
            accessToken = createResponse.path("accessToken"); // получили accessToken
        } else {
            accessToken = "";
        }

        OrderClient orderClient = new OrderClient();
        ExtractableResponse<Response>  getListOrderResponse = orderClient.getListOrder(accessToken);
        assertEquals(expectedStatusCode, getListOrderResponse.statusCode());

        if (expectedStatusCode != 500) {
            assertEquals(success, getListOrderResponse.path("success"));
        }

        if (getListOrderResponse.statusCode() >= 400 && getListOrderResponse.statusCode() < 500) { // если статус код неуспешный
            assertEquals(expectedMessageResponse, getListOrderResponse.path("message"));
        }

        if (isAuthorized) {
            ExtractableResponse<Response> deleteResponse = userClient.delete(accessToken);
            assertEquals(202, deleteResponse.statusCode());// удаляем созданного пользователя
        }
    }
}