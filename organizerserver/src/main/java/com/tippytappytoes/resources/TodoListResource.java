package com.tippytappytoes.resources;

import com.tippytappytoes.interfaces.OrganizerRepository;

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
  private final OrganizerRepository evernoteApi;

  public TodoListResource(OrganizerRepository evernoteApi) {
    this.evernoteApi = evernoteApi;
    this.counter = new AtomicLong();
  }

  @GET
  public TodoDto sayHello(@QueryParam("name") Optional<String> name) {
    try {
      return TodoDto.createTodo(evernoteApi.GetTodoList());
    } catch (Exception e) {
      return null;
    }
  }
}