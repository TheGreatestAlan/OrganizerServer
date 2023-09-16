<<<<<<<< HEAD:organizerserver/src/main/java/com/nguyen/server/resources/TodoDto.java
package com.nguyen.server.resources;
========
package com.tippytappytoes.resources;
>>>>>>>> local:organizerserver/src/main/java/com/tippytappytoes/resources/TodoDto.java

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TodoDto {

  private List<String> todoList;

  public TodoDto() {
  }

  @JsonProperty
  public List<String> getTodoList() {
    return todoList;
  }

  public static TodoDto createTodo(List<String> todo) {
    TodoDto todoDto = new TodoDto();
    todoDto.todoList = todo;
    return todoDto;
  }
}
