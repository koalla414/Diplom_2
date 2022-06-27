package user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
        private String email;
        private String password;
        private String name;

        public static User getRandomUser() {
            String email = RandomStringUtils.randomAlphanumeric(10) + "@" + RandomStringUtils.randomAlphabetic(10) + ".ru";
            String password = RandomStringUtils.randomAlphanumeric(10);
            String name = RandomStringUtils.randomAlphanumeric(10);

            return new User(email, password, name);
        }

}
