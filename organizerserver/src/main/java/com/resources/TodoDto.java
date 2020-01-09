package com.resources;

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

    public static TodoDto createTodo(List<String> todo)
    {
        TodoDto todoDto = new TodoDto();
        todoDto.todoList = todo;
        return todoDto;
    }
}
