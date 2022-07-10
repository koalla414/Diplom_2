package order;

import clients.OrderClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import user.User;
import clients.UserClient;
import user.UserCredentials;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class OrderCreateTest {
    private String[] inputHash;
    private int expectedStatusCode;
    private boolean success;
    private String expectedMessageResponse;
    private boolean isAuthorized;
    private UserClient userClient;

    @Before
    public void setup() {
        userClient = new UserClient();
    }

    public OrderCreateTest(String[] inputHash, boolean isAuthorized, int expectedStatusCode, boolean success, String expectedMessageResponse) {
        this.inputHash = inputHash;
        this.isAuthorized = isAuthorized;
        this.expectedStatusCode = expectedStatusCode;
        this.success = success;
        this.expectedMessageResponse = expectedMessageResponse;
    }

        @Parameterized.Parameters(name = "При хэше ингредиентов: {0}, стутусе авторизации пользователя: {1}. Ожидается код ответа: {2} и сообщение: {4}.")
        public static Object[][] checkChangeDataUser() {
            return new Object[][]{
                    {new String[] { "61c0c5d" }, false, 500, false, ""}, // создание заказа без авторизации с неверным хэшем ингредиентов
                    {new String[] { "61c0c5a71d1f82001bdaaa6d" }, true, 200, true, ""}, // корректное создание заказа: с авторизацией и ингредиентами
                    {null, true, 400, false, "Ingredient ids must be provided"}, // создание заказа с авторизацией без ингредиентов
                    {new String[] { "61c" }, true, 500, false, ""}, // создание заказа с авторизацией неверным хэшем ингредиентов
                    {new String[] { "61c0c5a71d1f82001bdaaa6d" }, false, 200, true, ""}, // создание заказа без авторизации с ингредиентами
                    {null, false, 400, false, "Ingredient ids must be provided"}, // создание заказа без авторизации без ингредиентов
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
        Order order = new Order(inputHash);
        ExtractableResponse<Response>  createOrderResponse = orderClient.createOrder(accessToken, order);
        assertEquals(expectedStatusCode, createOrderResponse.statusCode());

        if (expectedStatusCode != 500) {
            assertEquals(success, createOrderResponse.path("success"));
        }

        if (createOrderResponse.statusCode() >= 400 && createOrderResponse.statusCode() < 500) { // если статус код неуспешный
            assertEquals(expectedMessageResponse, createOrderResponse.path("message"));
        }

        if (isAuthorized) {
            ExtractableResponse<Response> deleteResponse = userClient.delete(accessToken);
            assertEquals(202, deleteResponse.statusCode());// удаляем созданного пользователя
        }
    }
}