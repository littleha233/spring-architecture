package com.example.springdemo.biz;

import com.example.springdemo.domain.Note;

import java.util.List;

public interface NoteBiz {
    Note create(String title, String content);

    List<Note> list();
}
