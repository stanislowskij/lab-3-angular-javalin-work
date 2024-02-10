package umm3601.todo;

import java.io.IOException;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import umm3601.Controller;

/**
 * Controller that manages requests for info about todos.
 */
public class TodoController implements Controller {

  private TodoDatabase todoDatabase;

  /**
   * Construct a controller for todos.
   * <p>
   * This loads the "todoDatabase" of todo info from a JSON file and stores that
   * internally so that (subsets of) todos can be returned in response to
   * requests.
   *
   * @param todoDatabase the `TodoDatabase` containing todo data
   */
  public TodoController(TodoDatabase todoDatabase) {
    this.todoDatabase = todoDatabase;
  }

  /**s
   * Create a database using the json file, use it as data source for a new
   * UserController
   *
   * Constructing the controller might throw an IOException if there are problems
   * reading from the JSON "database" file. If that happens we'll print out an
   * error message exit the program.
   *
   * @throws IOException
   */
  public static TodoController buildTodoController(String todoDataFile) throws IOException {
    TodoDatabase todoDatabase = new TodoDatabase(todoDataFile);
    TodoController todoController = new TodoController(todoDatabase);

    return todoController;
  }

  /**
   * Get the single todo specified by the `id` parameter in the request.
   *
   * @param ctx a Javalin HTTP context
   */
  public void getTodo(Context ctx) {
    String id = ctx.pathParam("id");
    Todo todo = todoDatabase.getTodo(id);
    if (todo != null) {
      ctx.json(todo);
      ctx.status(HttpStatus.OK);
    } else {
      throw new NotFoundResponse("No todo with id " + id + " was found.");
    }
  }

  /**
   * Get a JSON response with a list of all the todos in the "database".
   *
   * @param ctx a Javalin HTTP context
   */
  public void getTodos(Context ctx) {
    Todo[] todos = todoDatabase.listTodos(ctx.queryParamMap());
    ctx.json(todos);
  }

  /**
   * Setup routes for the `todo` collection endpoints.
   *
   * These endpoints are:
   * - `GET /api/todos?status=complete&category=homework&owner=STRING`
   * - List todos, filtered using query parameters
   * - `owner`, `status`, `body`, and `category` are optional query parameters
   * - `GET /api/todos/:id`
   * - Get the specified todo
   *
   * @param server The Javalin server instance
   */
  @Override
  public void addRoutes(Javalin server) {
    // Get a single todo
    server.get("api/todos/{id}", this::getTodo);

    // Get a JSON response with a list of all the todos,
    // filtered using query parameters provided.
    server.get("api/todos", this::getTodos);
  }

}
