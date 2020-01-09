package com.resources;

import com.codahale.metrics.annotation.Timed;
import com.nguyenConfiguration;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Optional;

@Path("/todo")
@Produces(MediaType.APPLICATION_JSON)
public class TodoListResource{
    private final AtomicLong counter;
    private final nguyenConfiguration configuration;

    public TodoListResource(nguyenConfiguration configuration) {
        this.configuration = configuration;
        this.counter = new AtomicLong();
    }

    @GET
    @Timed
    public String sayHello(@QueryParam("name") Optional<String> name) {
        return configuration.getEvernoteConsumerKey();
    }
}
