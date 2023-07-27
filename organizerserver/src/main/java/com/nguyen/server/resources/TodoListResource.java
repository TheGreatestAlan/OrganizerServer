package com.nguyen.server.resources;

import com.nguyen.server.interfaces.OrganizerRepository;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Optional;

@Path("/todo")
@Produces(MediaType.APPLICATION_JSON)
public class TodoListResource {

  private final AtomicLong counter;
  private final OrganizerRepository organizerRepository;

  public TodoListResource(OrganizerRepository organizerRepository) {
    this.organizerRepository = organizerRepository;
    this.counter = new AtomicLong();
  }

  @GET
  public TodoDto sayHello(@QueryParam("name") Optional<String> name) {
    try {
      return TodoDto.createTodo(organizerRepository.getTodoList());
    } catch (Exception e) {
      return null;
    }
  }
}
