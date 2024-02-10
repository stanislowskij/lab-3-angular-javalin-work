package umm3601.todo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import umm3601.Main;

/**
 * Tests the logic of the TodoController
 *
 * @throws IOException
 */
// The tests here include a ton of "magic numbers" (numeric constants).
// It wasn't clear to me that giving all of them names would actually
// help things. The fact that it wasn't obvious what to call some
// of them says a lot. Maybe what this ultimately means is that
// these tests can/should be restructured so the constants (there are
// also a lot of "magic strings" that Checkstyle doesn't actually
// flag as a problem) make more sense.
@SuppressWarnings({ "MagicNumber" })
public class TodoControllerSpec {

  // An instance of the `TodoController` that were testing, which
  // is prepared in `setUp()`, and then exercised in the various
  // tests below.
  private TodoController todoController;

  // An instance of our database "layer" that is prepared in `setUp()`
  // and then used in the tests below to "act" as if it were a real
  // database providing `Todo` objects.
  private static TodoDatabase db;

  // A "fake" version of Javalin's `Context` object that we can
  // use to test with.
  @Mock
  private Context ctx;

  // A captor allows us to make assertions on arguments to method
  // calls that are made "indirectly" by the code we are testing,
  // in this case `json()` calls in `UserController`. We'll use
  // this to make assertions about the data passed to `json()`.
  @Captor
  private ArgumentCaptor<Todo[]> todoArrayCaptor;

  @BeforeEach
  public void setUp() throws IOException {
    // Reset our mock context and argument captor
    // (declared above with Mockito annotations @Mock and @Captor)
    MockitoAnnotations.openMocks(this);
    // Construct our "database"
    db = new TodoDatabase(Main.TODO_DATA_FILE);
    // Construct an instance of our controller which
    // we'll then test.
    todoController = new TodoController(db);
  }

  /**
   * Verify that we can successfully build a `TodoController`
   * and call it's `addRoutes` method. This doesn't verify
   * much beyond that the code actually runs without throwing
   * an exception. We do, however, confirm that the `addRoutes`
   * causes `.get()` to be called at least twice.
   */
  @Test
  public void canBuildController() throws IOException {
    // Call the `UserController.buildUserController` method
    // to construct a controller instance "by hand".
    TodoController controller = TodoController.buildTodoController(Main.TODO_DATA_FILE);
    Javalin mockServer = Mockito.mock(Javalin.class);
    controller.addRoutes(mockServer);

    // Verify that calling `addRoutes()` above caused `get()` to be called
    // on the server at least twice. We use `any()` to say we don't care about
    // the arguments that were passed to `.get()`.
    verify(mockServer, Mockito.atLeast(2)).get(any(), any());
  }

  /**
   * Verify that attempting to build a `TodoController` with an
   * invalid `todoDataFile` throws an `IOException`.
   */
  @Test
  public void buildControllerFailsWithIllegalDbFile() {
    Assertions.assertThrows(IOException.class, () -> {
      TodoController.buildTodoController("this is not a legal file name");
    });
  }

  @Test
  public void canGetAllTodos() throws IOException {
    // Call the method on the mock context, which doesn't
    // include any filters, so we should get all the users
    // back.
    todoController.getTodos(ctx);

    // Confirm that `json` was called with all the users.
    // The `ArgumentCaptor<Todo[]> todoArrayCaptor` was initialized in the `@BeforeEach`
    // Here, we wait to see what happens when `ctx` calls the json method in the call
    // `todoController.getTodos(ctx)` and the `json()` method is passed a `Todo[]`
    // (That's when the `Todo[]` that was passed as input to the json method is captured)
    verify(ctx).json(todoArrayCaptor.capture());
    // Now that the `Todo[]` that was passed as input to the json method is captured,
    // we can make assertions about it. In particular, we'll assert that its length
    // is the same as the size of the "database". We could also confirm that the
    // particular users are the same/correct, but that can get complicated
    // since the order of the users in the "database" isn't specified. So we'll
    // just check that the counts are correct.
    assertEquals(db.size(), todoArrayCaptor.getValue().length);
  }

  /**
   * Confirm that we can get a todo by its owner.
   *
   * @throws IOException if there are problems reading from the "database" file.
   */
  @Test
  public void canGetTodoByOwner() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("owner", Arrays.asList(new String[] {"Blanche"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    // Confirm that all the todos passed to `json` have 'Blanche' as their owner.
    verify(ctx).json(todoArrayCaptor.capture());
    Todo[] todos = todoArrayCaptor.getValue();
    for (Todo todo : todos) {
      assertEquals("Blanche", todo.owner);
    }
  }

  /**
   * Confirm that we can get all the todos with status 'complete'.
   *
   * @throws IOException if there are problems reading from the "database" file.
   */
  @Test
  public void canGetTodosByStatusComplete() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("status", Arrays.asList(new String[] {"complete"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    // Confirm that all the todos passed to `json` have 'complete' as their status.
    verify(ctx).json(todoArrayCaptor.capture());
    Todo[] todos = todoArrayCaptor.getValue();
    for (Todo todo : todos) {
      assertEquals(true, todo.status);
    }
  }

  /**
   * Confirm that we can get all the todos with status 'incomplete'.
   *
   * @throws IOException if there are problems reading from the "database" file.
   */
  @Test
  public void canGetTodosByStatusIncomplete() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("status", Arrays.asList(new String[] {"incomplete"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    // Confirm that all the todos passed to `json` have 'incomplete' as their status.
    verify(ctx).json(todoArrayCaptor.capture());
    Todo[] todos = todoArrayCaptor.getValue();
    for (Todo todo : todos) {
      assertEquals(false, todo.status);
    }
  }

  /**
   * Confirm that an illegal status value (i.e., something other than
   * "complete" or "incomplete") results in a 400 Bad Request response.
   */
  @Test
  public void respondsAppropriatelyToIllegalStatus() {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("status", Arrays.asList(new String[] {"invalidStatus"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);
    // This should now throw a `BadRequestResponse` exception because
    // our request has an illegal status value.
    Assertions.assertThrows(BadRequestResponse.class, () -> {
      todoController.getTodos(ctx);
    });
  }

  /**
   * Confirm that we can get all the todos with category 'homework'.
   *
   * @throws IOException if there are problems reading from the "database" file.
   */
  @Test
  public void canGetTodosByCategory() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("category", Arrays.asList(new String[] {"homework"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    // Confirm that all the todos passed to `json` have 'homework' as their category.
    verify(ctx).json(todoArrayCaptor.capture());
    Todo[] todos = todoArrayCaptor.getValue();
    for (Todo todo : todos) {
      assertEquals("homework", todo.category);
    }
  }

  /**
   * Confirm that we can get all the todos with body containing 'tempor'.
   *
   * @throws IOException if there are problems reading from the "database" file.
   */
  @Test
  public void canGetTodosByBody() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("contains", Arrays.asList(new String[] {"tempor"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    // Confirm that all the todos passed to `json` have 'tempor' in their body.
    verify(ctx).json(todoArrayCaptor.capture());
    Todo[] todos = todoArrayCaptor.getValue();
    for (Todo todo : todos) {
      assertEquals(true, todo.body.toLowerCase().contains("tempor"));
    }
  }

  /**
   * Confirm that `getTodos` works when we have a `limit` query parameter.
   *
   * @throws IOException if there are problems reading from the JSON "database" file.
   */
  @Test
  public void canLimitTo20Todos() throws IOException {
    // Add a query param map to the context that maps "limit" to 20.
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("limit", Arrays.asList(new String[] {"20"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    // Call the `getTodos` method on the mock controller with the
    // added query param to limit the result to just the first
    // 20 todos.
    todoController.getTodos(ctx);

    // Confirm that the todos passed to `json` have length 20.
    verify(ctx).json(todoArrayCaptor.capture());
    assertEquals(20, todoArrayCaptor.getValue().length);
  }

  /**
   * Test that if the Todo sends a request with an illegal value in
   * the limit field (i.e., something that can't be parsed to a number)
   * we get a reasonable error code back.
   */
  @Test
  public void respondsAppropriatelyToIllegalLimit() {
    // We'll set the requested "limit" to be a string ("abc")
    // that can't be parsed to a number.
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("limit", Arrays.asList(new String[] {"abc"}));

    when(ctx.queryParamMap()).thenReturn(queryParams);
    // This should now throw a `BadRequestResponse` exception because
    // our request has an limit that can't be parsed to a number.
    Assertions.assertThrows(BadRequestResponse.class, () -> {
      todoController.getTodos(ctx);
    });
  }

  @Test
  public void canGetTodosSortedByOwner() throws IOException {

    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("orderBy", Arrays.asList(new String[] {"owner"}));

    when(ctx.queryParamMap()).thenReturn(queryParams);
    todoController.getTodos(ctx);

    // Confirm that all the todos passed to `json` are sorted by owner.
    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    for (int i = 0; i < argument.getValue().length - 1; ++i) {
      Assertions.assertTrue(argument.getValue()[i].owner.compareTo(argument.getValue()[i + 1].owner) <= 0);
    }
  }

  /**
   * Confirm that we can sort the todos by body using the `orderBy` query
   * parameter.
   *
   * @throws IOException if there are problems reading from the "database" file.
   */
  @Test
  public void canSortTodosByBody() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("orderBy", Arrays.asList(new String[] {"body"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    verify(ctx).json(todoArrayCaptor.capture());
    Todo[] todos = todoArrayCaptor.getValue();

    // Confirm that all the todos passed to `json` are ordered by body.
    for (int i = 0; i < todos.length - 1; i++) {
      assertTrue(todos[i].body.compareTo(todos[i + 1].body) <= 0);
    }
  }

  /**
   * Confirm that we can sort the todos by status using the `orderBy` query
   * parameter.
   *
   * @throws IOException if there are problems reading from the "database" file.
   */
  @Test
  public void canSortTodosByStatus() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("orderBy", Arrays.asList(new String[] {"status"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    verify(ctx).json(todoArrayCaptor.capture());
    Todo[] todos = todoArrayCaptor.getValue();

    // Confirm that all the todos passed to `json` are ordered by status.
    for (int i = 0; i < todos.length - 1; i++) {
      assertTrue(Boolean.compare(todos[i].status, todos[i + 1].status) <= 0);
    }
  }

  /**
   * Test that if the Todo sends a request with an illegal value in
   * the orderBy field (i.e., an non-applicable todo attribute)
   * we get a reasonable error code back.
   */
  @Test
  public void respondsAppropriateToIllegalOrderBy() {
    // We'll set the requested "age" to be a string ("abc")
    // that can't be parsed to a number.
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("orderBy", Arrays.asList(new String[] {"unknown"}));

    when(ctx.queryParamMap()).thenReturn(queryParams);
    // This should now throw a `BadRequestResponse` exception because
    // our request has an order that is not an applicable todo attribute.
    Assertions.assertThrows(BadRequestResponse.class, () -> {
      todoController.getTodos(ctx);
    });
  }

  /**
   * Confirm that we handle multiple query parameters correctly.
   * For example:
   *    `api/todos?owner=Blanche&status=complete&limit=12&orderBy=category`
   * should give us the first 12 todos with status 'complete' and owner 'Blanche'
   * ordered by category.
   *
   * @throws IOException if there are problems reading from the "database" file.
   */
  @Test
  public void canFilterByMultipleParameters() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("owner", Arrays.asList(new String[] {"Blanche"}));
    queryParams.put("status", Arrays.asList(new String[] {"complete"}));
    queryParams.put("limit", Arrays.asList(new String[] {"12"}));
    queryParams.put("orderBy", Arrays.asList(new String[] {"category"}));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    todoController.getTodos(ctx);

    verify(ctx).json(todoArrayCaptor.capture());
    Todo[] todos = todoArrayCaptor.getValue();

    // Confirm that all the todos passed to `json` have 'Blanche' as their owner.
    for (Todo todo : todos) {
      assertEquals("Blanche", todo.owner);
    }

    // Confirm that all the todos passed to `json` have 'complete' as their status.
    for (Todo todo : todos) {
      assertEquals(true, todo.status);
    }

    // Confirm that exactly 12 todos were returned.
    assertEquals(12, todos.length);

    // Confirm that all the todos passed to `json` are ordered by category.
    for (int i = 0; i < todos.length - 1; i++) {
      assertTrue(todos[i].category.compareTo(todos[i + 1].category) <= 0);
    }
  }

  @Test
  public void canGetTodoWithSpecifiedId() throws IOException {
    // A specific todo ID known to be in the "database".
    String id = "58895985c1849992336c219b"; // replace with a valid ID from your todos.json
    // Get the todo associated with that ID.
    Todo todo = db.getTodo(id);

    when(ctx.pathParam("id")).thenReturn(id);

    todoController.getTodo(ctx);

    verify(ctx).json(todo);
    verify(ctx).status(HttpStatus.OK);
  }

  /**
   * Confirm that we get a 404 Not Found response when
   * we request a todo ID that doesn't exist.
   *
   * @throws IOException if there are problems reading from the "database" file.
   */
  @Test
  public void respondsAppropriatelyToRequestForNonexistentId() throws IOException {
    when(ctx.pathParam("id")).thenReturn("invalidID");
    Throwable exception = Assertions.assertThrows(NotFoundResponse.class, () -> {
      todoController.getTodo(ctx);
    });
    assertEquals("No todo with id " + "invalidID" + " was found.", exception.getMessage());
  }
}
