package com.skilltree.controller;
import com.skilltree.Service.TakeCourseService;
import com.skilltree.dto.takeCourse.TakeCourseDto;
import com.skilltree.dto.courses.CourseSimpleDto;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/take/course")
public class TakeCourseController {
	private final TakeCourseService takeCourseService;

	public TakeCourseController(TakeCourseService takeCourseService) {
		this.takeCourseService = takeCourseService;
	}

	@PostMapping()
	public CourseSimpleDto takeCourse(@RequestBody TakeCourseDto request) {
		return takeCourseService.takeCourse(request);
	}

}