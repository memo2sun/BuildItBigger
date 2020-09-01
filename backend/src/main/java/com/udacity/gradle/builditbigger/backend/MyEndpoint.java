package com.udacity.gradle.builditbigger.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

import net.sunyounglee.javajokes.Joker;

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "myApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "backend.builditbigger.gradle.udacity.com",
                ownerName = "backend.builditbigger.gradle.udacity.com",
                packagePath = ""
        )
)

//Get today's joke from java library, and set to endpoint.
public class MyEndpoint {
    @ApiMethod(name = "getTodayJoke")
    public MyJoke getTodayJoke() {
        Joker objJoker = new Joker();
        String todayJoke = objJoker.getJoke();
        MyJoke response = new MyJoke();
        response.setData(todayJoke);
        return response;
    }
}
