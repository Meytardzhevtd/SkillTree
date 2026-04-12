package com.skilltree.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skilltree.dto.lessons.CreateLessonRequest;
import com.skilltree.dto.lessons.LessonResponse;
import com.skilltree.model.Lesson;
import com.skilltree.repository.LessonRepository;

@Service
@Transactional(readOnly = true)
public class LessonService {
    private final LessonRepository lessonRepository;

    public LessonService(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    @Transactional
    public LessonResponse createLesson(CreateLessonRequest request) {
        // TODO: надо проверять что добавляет создатель (потом решить)
        Lesson saved = lessonRepository
                .save(new Lesson(null, request.getModuleId(), request.getTitle(), request.getContent()));

        return new LessonResponse(saved.getId(), saved.getTitle(), saved.getContent());
    }

    public List<LessonResponse> getLessons(Long moduleId) {
        return lessonRepository.findByModuleId(moduleId)
                .stream()
                .map(lesson -> new LessonResponse(lesson.getId(), lesson.getTitle(), lesson.getContent()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long lessonId) {
        Optional<Lesson> oLesson = lessonRepository.findById(lessonId);
        if (oLesson.isEmpty()) {
            return;
        } else {
            lessonRepository.delete(oLesson.get());
        }

    }

}
