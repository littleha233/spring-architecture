package com.example.springdemo.service;

import com.example.springdemo.biz.NoteBiz;
import com.example.springdemo.domain.Note;
import com.example.springdemo.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteService implements NoteBiz {
    private final NoteRepository noteRepository;

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @Override
    public Note create(String title, String content) {
        Note note = new Note(title, content);
        return noteRepository.save(note);
    }

    @Override
    public List<Note> list() {
        return noteRepository.findAll();
    }
}
