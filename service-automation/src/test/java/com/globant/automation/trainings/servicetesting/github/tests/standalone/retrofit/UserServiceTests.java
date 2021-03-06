package com.globant.automation.trainings.servicetesting.github.tests.standalone.retrofit;

import com.globant.automation.trainings.servicetesting.authenticators.BasicAuthenticator;
import com.globant.automation.trainings.servicetesting.github.api.retrofit.UserApi;
import com.globant.automation.trainings.servicetesting.github.models.User;
import com.globant.automation.trainings.servicetesting.standalone.RetrofitServiceTestFor;
import okhttp3.OkHttpClient;
import org.junit.Test;
import retrofit2.Response;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Juan Krzemien
 */
public class UserServiceTests extends RetrofitServiceTestFor<UserApi> {

    @Override
    protected void configure(OkHttpClient.Builder client) {
        client.authenticator(new BasicAuthenticator("anEmail@somewhere.com", "aPassword"));
    }

    @Test
    public void getAuthenticatedUser() throws IOException {
        Response<User> response = call(api().getAuthenticatedUser());
        assertEquals("HTTP status is not OK", 200, response.code());
        User user = response.body();
        assertEquals("User name does not match", "John Doe", user.name);
    }

}
