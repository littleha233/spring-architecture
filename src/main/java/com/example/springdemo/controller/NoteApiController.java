package com.example.springdemo.controller;

import com.example.springdemo.biz.NoteBiz;
import com.example.springdemo.domain.Note;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteApiController {
    private final NoteBiz noteBiz;

    public NoteApiController(NoteBiz noteBiz) {
        this.noteBiz = noteBiz;
    }

    @GetMapping
    public List<Note> list() {
        return noteBiz.list();
    }

    @PostMapping
    public Note create(@RequestBody CreateNoteRequest request) {
        return noteBiz.create(request.title(), request.content());
    }

    public record CreateNoteRequest(String title, String content) {
    }
}
