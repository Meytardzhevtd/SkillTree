package com.skilltree.loadtest;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class LoadTest extends Simulation {

	private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");
	private static final String TOKEN = System.getProperty("token", "");
	private static final String TASK_ID = System.getProperty("taskId", "1");
	private static final String PROGRESS_ID = System.getProperty("progressModuleId", "1");
	private static final String COURSE_ID = System.getProperty("courseId", "1");
	private static final String MODULE_ID = System.getProperty("moduleId", "1");
	private static final String TAKEN_ID = System.getProperty("takenId", "1");
	private static final String USER_ID = System.getProperty("userId", "1");

	private final HttpProtocolBuilder httpProtocol = http.baseUrl(BASE_URL)
			.acceptHeader("application/json").contentTypeHeader("application/json")
			.header("Authorization", "Bearer " + TOKEN).shareConnections().disableCaching();

	private final ScenarioBuilder s01BrowseCourses = scenario("01 - Browse all courses")
			.exec(http("GET /api/course/all").get("/api/course/all").check(status().is(200))
					.check(jsonPath("$").exists()))
			.pause(Duration.ofMillis(200), Duration.ofMillis(600))
			.exec(http("GET /api/course/all?search").get("/api/course/all?search=Load")
					.check(status().is(200)));

	private final ScenarioBuilder s02CourseDetails = scenario("02 - Course details")
			.exec(http("GET /api/course/{id}").get("/api/course/" + COURSE_ID)
					.check(status().is(200)).check(jsonPath("$.id").exists()))
			.pause(Duration.ofMillis(200), Duration.ofMillis(500))
			.exec(http("GET /api/course/{id}/my-role").get("/api/course/" + COURSE_ID + "/my-role")
					.check(status().is(200)));

	private final ScenarioBuilder s03ModulesByCourse = scenario("03 - Modules of course")
			.exec(http("GET /api/module/courses/{id}").get("/api/module/courses/" + COURSE_ID)
					.check(status().is(200)).check(jsonPath("$").exists()))
			.pause(Duration.ofMillis(150), Duration.ofMillis(400)).exec(http("GET /api/module/{id}")
					.get("/api/module/" + MODULE_ID).check(status().in(200, 403)));

	private final ScenarioBuilder s04TasksByModule = scenario("04 - Tasks in module")
			.exec(http("GET /api/tasks?moduleId").get("/api/tasks?moduleId=" + MODULE_ID)
					.check(status().is(200)).check(jsonPath("$").exists()))
			.pause(Duration.ofMillis(150), Duration.ofMillis(400))
			.exec(http("GET /api/tasks/{id}").get("/api/tasks/" + TASK_ID).check(status().is(200))
					.check(jsonPath("$.id").exists()));

	private final ScenarioBuilder s05SubmitAnswers = scenario("05 - Submit answers").repeat(3)
			.on(exec(http("POST /api/tasks/{id}/submit").post("/api/tasks/" + TASK_ID + "/submit")
					.body(StringBody("{\"progressModuleId\":" + PROGRESS_ID + ",\"answer\":1}"))
					.check(status().in(200, 400)).check(jsonPath("$.correct").exists()))
					.pause(Duration.ofMillis(300), Duration.ofMillis(900)));

	private final ScenarioBuilder s06MyProgress = scenario("06 - My progress")
			.exec(http("GET /api/take/course/my").get("/api/take/course/my")
					.check(status().is(200)))
			.pause(Duration.ofMillis(200), Duration.ofMillis(500))
			.exec(http("GET /api/user/courses").get("/api/user/courses").check(status().is(200)));

	private final ScenarioBuilder s07DependenciesGraph = scenario("07 - Dependency graph")
			.exec(http("GET /api/dependencies/graph/takenCourse/{id}")
					.get("/api/dependencies/graph/takenCourse/" + TAKEN_ID)
					.check(status().in(200, 404)))
			.pause(Duration.ofMillis(150), Duration.ofMillis(400))
			.exec(http("GET /api/dependencies/graph/{id}")
					.get("/api/dependencies/graph/" + COURSE_ID).check(status().is(200)));

	private final ScenarioBuilder s08ProfileAndScores = scenario("08 - Profile and scores")
			.exec(http("GET /api/profile/me").get("/api/profile/me").check(status().is(200))
					.check(jsonPath("$.username").exists()))
			.pause(Duration.ofMillis(200), Duration.ofMillis(500))
			.exec(http("GET /api/scores/{userId}").get("/api/scores/" + USER_ID)
					.check(status().is(200)));

	private final ScenarioBuilder s09LessonsAndComments = scenario("09 - Lessons and comments")
			.exec(http("GET /api/lessons/module/{id}").get("/api/lessons/module/" + MODULE_ID)
					.check(status().is(200)))
			.pause(Duration.ofMillis(150), Duration.ofMillis(400))
			.exec(http("GET /api/comments/task/{id}").get("/api/comments/task/" + TASK_ID)
					.check(status().is(200)));

	private final ScenarioBuilder s10FullJourney = scenario("10 - Full student journey")
			.exec(http("J1: GET /api/course/all").get("/api/course/all").check(status().is(200)))
			.pause(Duration.ofMillis(200), Duration.ofMillis(400))
			.exec(http("J2: GET /api/course/{id}")
					.get("/api/course/" + COURSE_ID).check(status().is(200)))
			.pause(Duration.ofMillis(200), Duration.ofMillis(400))
			.exec(http("J3: GET /api/module/courses/{id}")
					.get("/api/module/courses/" + COURSE_ID).check(status().is(200)))
			.pause(Duration.ofMillis(200), Duration.ofMillis(400))
			.exec(http("J4: GET /api/module/{id}").get("/api/module/" + MODULE_ID)
					.check(status().in(200, 403)))
			.pause(Duration.ofMillis(200), Duration.ofMillis(400))
			.exec(http("J5: GET /api/tasks?moduleId")
					.get("/api/tasks?moduleId=" + MODULE_ID).check(status().is(200)))
			.pause(Duration.ofMillis(300), Duration.ofMillis(600))
			.exec(http("J6: POST /api/tasks/{id}/submit").post("/api/tasks/" + TASK_ID + "/submit")
					.body(StringBody("{\"progressModuleId\":" + PROGRESS_ID + ",\"answer\":1}"))
					.check(status().in(200, 400)))
			.pause(Duration.ofMillis(300), Duration.ofMillis(600))
			.exec(http("J7: GET /api/take/course/my").get("/api/take/course/my")
					.check(status().is(200)))
			.pause(Duration.ofMillis(200), Duration.ofMillis(400))
			.exec(http("J8: GET /api/profile/me").get("/api/profile/me").check(status().is(200)));

	{
		setUp(s01BrowseCourses.injectOpen(rampUsers(40).during(Duration.ofSeconds(10)),
				constantUsersPerSec(12).during(Duration.ofSeconds(40))),

				s02CourseDetails.injectOpen(rampUsers(25).during(Duration.ofSeconds(10)),
						constantUsersPerSec(8).during(Duration.ofSeconds(40))),

				s03ModulesByCourse.injectOpen(rampUsers(25).during(Duration.ofSeconds(10)),
						constantUsersPerSec(8).during(Duration.ofSeconds(40))),

				s04TasksByModule.injectOpen(rampUsers(25).during(Duration.ofSeconds(10)),
						constantUsersPerSec(8).during(Duration.ofSeconds(40))),

				s05SubmitAnswers.injectOpen(rampUsers(30).during(Duration.ofSeconds(10)),
						constantUsersPerSec(10).during(Duration.ofSeconds(40))),

				s06MyProgress.injectOpen(constantUsersPerSec(6).during(Duration.ofSeconds(50))),

				s07DependenciesGraph
						.injectOpen(constantUsersPerSec(4).during(Duration.ofSeconds(50))),

				s08ProfileAndScores
						.injectOpen(constantUsersPerSec(5).during(Duration.ofSeconds(50))),

				s09LessonsAndComments
						.injectOpen(constantUsersPerSec(4).during(Duration.ofSeconds(50))),

				s10FullJourney.injectOpen(rampUsers(20).during(Duration.ofSeconds(15)),
						constantUsersPerSec(5).during(Duration.ofSeconds(35))))
				.protocols(httpProtocol).assertions(global().responseTime().percentile(50).lt(500),
						global().responseTime().percentile(95).lt(1500),
						global().responseTime().percentile(99).lt(3000),
						global().successfulRequests().percent().gt(95.0),
						global().requestsPerSec().gte(50.0),
						forAll().failedRequests().count().lt(50L));
	}
}