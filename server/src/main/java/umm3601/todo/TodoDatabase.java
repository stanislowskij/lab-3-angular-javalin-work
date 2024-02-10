package umm3601.todo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.javalin.http.BadRequestResponse;

/**
 * A fake "todoDatabase" of todo info
 * <p>
 * Since we don't want to complicate this lab with a real database, we're going
 * to instead just read a bunch of todo data from a specified JSON file, and
 * then provide various database-like methods that allow the `TodoController` to
 * "query" the "todoDatabase".
 */
public class TodoDatabase {

  private Todo[] allTodos;

  public TodoDatabase(String todoDataFile) throws IOException {
    // The `.getResourceAsStream` method searches for the given resource in
    // the classpath, and returns `null` if it isn't found. We want to throw
    // an IOException if the data file isn't found, so we need to check for
    // `null` ourselves, and throw an IOException if necessary.
    InputStream resourceAsStream = TodoDatabase.class.getResourceAsStream(todoDataFile);
    if (resourceAsStream == null) {
      throw new IOException("Could not find " + todoDataFile);
    }
    InputStreamReader reader = new InputStreamReader(resourceAsStream);
    // A Jackson JSON mapper knows how to parse JSON into sensible 'Todo'
    // objects.
    ObjectMapper objectMapper = new ObjectMapper();
    // Read our user data file into an array of `Todo` objects.
    allTodos = objectMapper.readValue(reader, Todo[].class);

    // Close the `reader` to free resources.
    reader.close();
  }

  public int size() {
    return allTodos.length;
  }

  /**
   * Get the single todo specified by the given ID. Return `null` if there is no
   * todo with that ID.
   *
   * @param id the ID of the desired todo
   * @return the todo with the given ID, or null if there is no todo with that ID
   */
  public Todo getTodo(String id) {
    return Arrays.stream(allTodos).filter(x -> x._id.equals(id)).findFirst().orElse(null);
  }

  /**
   * Get an array of all the todos satisfying the queries in the params.
   *
   * @param queryParams map of key-value pairs for the query
   * @return an array of all the todos matching the given criteria
   */
  public Todo[] listTodos(Map<String, List<String>> queryParams) {
    Todo[] filteredTodos = allTodos;

    // Filter status if defined
    if (queryParams.containsKey("status")) {
      String targetStatus = queryParams.get("status").get(0);
      filteredTodos = filterTodosByStatus(filteredTodos, targetStatus);
    }
    // Filter body if defined
    if (queryParams.containsKey("contains")) {
      String targetBody = queryParams.get("contains").get(0);
      filteredTodos = filterTodosByBody(filteredTodos, targetBody);
    }
    // Filter owner if defined
    if (queryParams.containsKey("owner")) {
      String targetOwner = queryParams.get("owner").get(0);
      filteredTodos = filterTodosByOwner(filteredTodos, targetOwner);
    }
    // Filter category if defined
    if (queryParams.containsKey("category")) {
      String targetCategory = queryParams.get("category").get(0);
      filteredTodos = filterTodosByCategory(filteredTodos, targetCategory);
    }
    // Sort todo with specific order if defined
    if (queryParams.containsKey("orderBy")) {
      String targetOrder = queryParams.get("orderBy").get(0);
      filteredTodos = sortTodos(filteredTodos, targetOrder);
    }
    // Filter todos within specific limit if defined
    if (queryParams.containsKey("limit")) {
      String limitParam = queryParams.get("limit").get(0);
      try {
        int targetLimit = Integer.parseInt(limitParam);
        filteredTodos = filterTodosByLimit(filteredTodos, targetLimit);
      } catch (NumberFormatException e) {
        throw new BadRequestResponse("Specified limit '" + limitParam + "' can't be parsed to an integer");
      }
    }

    return filteredTodos;
  }

  /**
   * Get an array of all the todos having the target status.
   *
   * @param todos         the list of todos to filter by status
   * @param targetStatus  the target status to look for
   * @return an array of all the todos from the given list that have the target
   *         status
   */
  public Todo[] filterTodosByStatus(Todo[] todos, String targetStatus) {
    switch (targetStatus.toLowerCase()) {
      case "complete":
        return Arrays.stream(todos).filter(x -> x.status).toArray(Todo[]::new);
      case "incomplete":
        return Arrays.stream(todos).filter(x -> !x.status).toArray(Todo[]::new);
      default:
        throw new BadRequestResponse("Specified status '" + targetStatus + "' is not a valid todo status");
    }
  }

  /**
   * Get an array of all the todos having the target body.
   *
   * @param todos       the list of todos to filter by body
   * @param targetBody  the target body to look for
   * @return an array of all the todos from the given list that have the target
   *         body
   */
  public Todo[] filterTodosByBody(Todo[] todos, String targetBody) {
    return Arrays
        .stream(todos)
        .filter(todo -> todo.body.toLowerCase().contains(targetBody.toLowerCase()))
        .toArray(Todo[]::new);
  }

  /**
   * Get an array of all the todos having the target owner.
   *
   * @param todos       the list of todos to filter by owner
   * @param targetOwner  the target owner to look for
   * @return an array of all the todos from the given list that have the target
   *         owner
   */
  public Todo[] filterTodosByOwner(Todo[] todos, String targetOwner) {
    return Arrays
        .stream(todos)
        .filter(todo -> todo.owner.equalsIgnoreCase(targetOwner.toLowerCase()))
        .toArray(Todo[]::new);
  }

  /**
   * Get an array of all the todos having the target category.
   *
   * @param todos           the list of todos to filter by category
   * @param targetCategory  the target category to look for
   * @return an array of all the todos from the given list that have the target
   *         category
   */
  public Todo[] filterTodosByCategory(Todo[] todos, String targetCategory) {
    return Arrays
        .stream(todos)
        .filter(todo -> todo.category.equalsIgnoreCase(targetCategory.toLowerCase()))
        .toArray(Todo[]::new);
  }

  /**
   * Get an array of all the todos sorted in the given order.
   *
   * @param todos        the list of todos to filter by category
   * @param targetOrder  the target order to sort
   * @return an array of all the todos from the given list sorted in the given order
   */
  public Todo[] sortTodos(Todo[] todos, String targetOrder) {
    switch (targetOrder) {
      case "owner":
        return Arrays.stream(todos).sorted((x, y) -> x.owner.compareTo(y.owner)).toArray(Todo[]::new);
      case "body":
        return Arrays.stream(todos).sorted((x, y) -> x.body.compareTo(y.body)).toArray(Todo[]::new);
      case "status":
        return Arrays.stream(todos).sorted((x, y) -> Boolean.compare(x.status, y.status)).toArray(Todo[]::new);
      case "category":
        return Arrays.stream(todos).sorted((x, y) -> x.category.compareTo(y.category)).toArray(Todo[]::new);
      default:
        throw new BadRequestResponse("Specified order '" + targetOrder + "' is not an applicable todo attribute");
    }
  }

  /**
   * Get an array of all the todos within specific limit.
   *
   * @param todos         the list of todos to filter by limit
   * @param targetLimit  the target limit of todo to return
   * @return an array of all the todos from the given list within the target limit
   */
  public Todo[] filterTodosByLimit(Todo[] todos, int targetLimit) {
    return Arrays.copyOfRange(Arrays.stream(todos).toArray(Todo[]::new), 0, targetLimit);
  }
}
