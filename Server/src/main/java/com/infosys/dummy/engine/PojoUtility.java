package com.infosys.dummy.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.dummy.exception.BadRequest400Exception;
import com.infosys.dummy.exception.NotFound404Exception;
import com.infosys.dummy.exception.ResponseException;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.Entity;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * By convention, most methods that may throw a ResponseException begin with the word confirm
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 2/11/18
 * <p>Time: 10:26 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"UnusedReturnValue", "HardCodedStringLiteral", "OverlyBroadThrowsClause"})
public enum PojoUtility {
  ;

  private static final Logger log = LoggerFactory.getLogger(PojoUtility.class);
  private static final ObjectMapper mapper = new ObjectMapper();

  /**
   * Returns the provided collection. If the collection is null, returns an unmodifiable empty List. This lets you
   * iterate over any collection without checking it for null: 
   * <p>
   * {@code for (String s: skipNull(maybeNullSetOfStrings)) {...}}
   * @param iterable The collection or other Iterable
   * @param <T> The type of the collection members
   * @return the supplied Iterable, or if it's null, an unmodifiable empty Iterable.
   */
  public static <T> Iterable<T> skipNull(@Nullable Iterable<T> iterable) {
    if (iterable == null) {
      return Collections.emptyList(); // immutable
    }
    return iterable;
  }

  /**
   * Converts the String Id value to an Integer, throwing an exception if it can't.
   *
   * @param id The id as a String
   * @return the id as a Integer value
   * @throws ResponseException BAD_REQUEST (400) if id is null or is not readable as an int value.
   */
  public static Integer confirmAndDecodeInteger(final String id) throws ResponseException {
    try {
      return Integer.valueOf(id); // throws NumberFormatException on null
    } catch (NumberFormatException e) {
      throw new BadRequest400Exception(id, e);
    }
  }

  /**
   * Converts the String Id value to a Long, throwing an exception if it can't.
   *
   * @param id The id as a String
   * @return the id as a Long value
   * @throws ResponseException BAD_REQUEST (400) if id is null or is not readable as a long value.
   */
  public static Long confirmAndDecodeLong(final String id) throws ResponseException {
    try {
      return Long.valueOf(id); // throws NumberFormatException on null
    } catch (NumberFormatException e) {
      throw new BadRequest400Exception(id, e);
    }
  }

  /**
   * Retrieves an entity by its ID. First tests the entity for existence, and throws a NotFound404Exception if it's not
   * in the table. This should only be used for Entities, because it throws a 404 (Not Found) on failure. For
   * non-entities, use
   *
   * @param repository The repository
   * @param id The id
   * @param <E> The entity type
   * @param <ID> The type of the entity's ID field
   * @return The entity with the provided id
   * @link https://www.javacodemonk.com/difference-between-getone-and-findbyid-in-spring-data-jpa-3a96c3ff
   * @throws NotFound404Exception if the entity with the specified id is not found
   */
  public static <E, ID> E findOrThrow404(JpaRepository<E, ID> repository, ID id) throws ResponseException {
    // We use findById() instead of getOne() because getOne returns a lazily loadable value even if it didn't find anything. That
    // delays the exception until the code tries to use the object. findById() returns an empty Optional if it didn't find anything,
    // which makes our code cleaner.
    return repository
        .findById(id)
        .orElseThrow(() -> new NotFound404Exception(String.format("Missing object with %s", id)));
  }

  /**
   * Use when a non-entity object should not be null. Throws a BadRequest400Exception if null. If testing for an
   * entity by its id, you should use findOrThrow404(T object, Object id), which return a NOT_FOUND (404).
   * @param object The non-entity object to test.
   * @param <T> The object type
   * @return object, only if it's not null
   * @throws ResponseException BAD_REQUEST (400) if object is null
   */
  public static <T> T confirmNotNull(@Nullable T object) throws ResponseException {
    if (object == null) {
      throw new BadRequest400Exception("Missing object");
    }
    assert !isEntityAssertion(object) : String.format(
            "This method is not for entity objects. Use findOrThrow404(): %s", getEntityClass(object));
    return object;
  }

  /**
   * Use when a non-entity object should not be null. Throws a BadRequest400Exception if null. If testing for an
   * entity by its id, you should use findOrThrow404(T object, Object id), which return a NOT_FOUND (404).
   * @param object The non-entity object to test.
   * @param label A String to identify the bad value, usually a property name, like ID.
   * @param <T> The object type
   * @return object, only if it's not null
   * @throws ResponseException BAD_REQUEST (400) if object is null
   */
  public static <T> T confirmNotNull(@Nullable T object, String label) throws ResponseException {
    if (object == null) {
      throw new BadRequest400Exception(String.format("Missing object: %s", label));
    }
    assert !isEntityAssertion(object) : String.format(
            "This method is not for entity objects. Use findOrThrow404(): %s", getEntityClass(object));
    return object;
  }

//   This used to work. Then it broke, and now it works again.
//   I'm wondering if this is a bad idea. Fortunately, it's only executed in assertions.
  private static boolean isEntityAssertion(Object object) {
    Class<?> entityClass = getEntityClass(object);
    return entityClass != null;
  }
  
  private static boolean isDeclaredEntityClass(Class<?> theClass) {
    return Arrays.stream(theClass.getDeclaredAnnotations())
        .anyMatch(a -> a.annotationType() == Entity.class);
  }
  
  private static @Nullable Class<?> getEntityClass(final Object object) {
    Class<?> objectClass = object.getClass();
    Class<?> superClass = objectClass;
    
    // If the object class is an interface, this loop will end with a null superclass.
    while ((superClass != Object.class) && (superClass != null)) {
      if (isDeclaredEntityClass(superClass)) {
        return superClass;
      }
      objectClass = superClass;
      superClass = objectClass.getSuperclass();
    }
    return null;
  }
  
  @SafeVarargs
  public static <T> Set<T> asSet(T... tArray) {
    return new HashSet<>(Arrays.asList(tArray));
  }

  /**
   * Converts the array or ordered elements into a Set of the type specified by the constructor. For
   * example, to construct a TreeSet from three elements, you could do this:
   * <pre>
   *     {@literal Set<String>} set = asSet(TreeSet::new, "Red", "White", "Blue");
   * </pre>
   * @param constructor The constructor or other function to convert a List to a Set
   * @param array the items of type T, or an array of type T
   * @param <T> The type of items in the Set
   * @return A Set of the type generated by {@code constructor}, containing the elements in {@code array}.
   */
  @SafeVarargs
  public static <T> Set<T> asSet(Function<List<T>, Set<T>> constructor, T... array) {
    return constructor.apply(Arrays.asList(array));
  }

  /**
   * Use when a value should be null. For example, if a field should not be initialized, such as the ID of an entity 
   * that is about to be created, or an end-time for an operation that has not yet ended.
   * @param object The object that should be null.
   * @throws ResponseException BAD_REQUEST (400) if the object is not null.
   */
  public static void confirmNull(@Nullable Object object) throws ResponseException {
    if (object != null) {
	    //noinspection StringConcatenation
	    throw new BadRequest400Exception("non-null value: " + object);
    }
  }

  /**
   * Use when a value should be null. For example, if a field should not be initialized, such as the ID of an entity
   * that is about to be created, or an end-time for an operation that has not yet ended.
   * @param object The object that should be null.
   * @param label A String to identify the bad value, usually a property name, like ID.
   * @throws ResponseException BAD_REQUEST (400) if the object is not null.
   */
  public static void confirmNull(@Nullable Object object, String label) throws ResponseException {
    if (object != null) {
      throw new BadRequest400Exception(String.format("Non null field %s = %s", label, object));
    }
  }

  /**
   * Confirms the two objects are equal. Uses Objects.equals().
   * @param expected The expected value
   * @param actual The actual value
   * @param <T> The type of each object
   * @throws ResponseException BAD_REQUEST (400) if the objects are not equal
   * @see Objects#equals(Object, Object) 
   */
  public static <T> void confirmEqual(T expected, T actual) throws ResponseException {
    if (!Objects.equals(actual, expected)) {
      throw new BadRequest400Exception(String.format("Expected %s  Found %s", expected, actual));
    }
  }

  /**
   * Confirms the two objects are equal. Uses Objects.equals().
   * @param message The message to use if the objects are not equal
   * @param expected The expected value
   * @param actual The actual value
   * @param <T> The type of each object
   * @throws ResponseException BAD_REQUEST (400) if the objects are not equal, using the specified message
   * @see Objects#equals(Object, Object)
   */
  public static <T> void confirmEqual(String message, T expected, T actual) throws ResponseException {
    if (!Objects.equals(actual, expected)) {
      throw new BadRequest400Exception(message);
    }
  }

  /**
   * Returns the String. Throws a ResponseException if the String is null or empty. 
   * The return value is usually not used, since this is just to test for valid data.
   * It returns the first parameter, so it may be used in function chaining.
   * @param s The String
   * @return s
   * @throws ResponseException BAD_REQUEST (400) if the String is null or empty
   */
  public static String confirmNotEmpty(@Nullable String s) throws ResponseException {
    if ((s == null) || s.isEmpty()) {
      throw new BadRequest400Exception("Null or empty value.");
    }
    return s;
  }

  /**
   * Returns the String. Throws a ResponseException if the String is null or empty.
   * The return value is usually not used, since this is just to test for valid data.
   * It returns the first parameter, so it may be used in function chaining.
   * @param s The String
   * @param label The label to describe the empty value in the exception message.
   * @return s
   * @throws ResponseException BAD_REQUEST (400) if the String is null or empty
   */
  public static String confirmNotEmpty(@Nullable String s, String label) throws ResponseException {
    if ((s == null) || s.isEmpty()) {
      throw new BadRequest400Exception(String.format("Null or empty value for : \"%s\"", label));
    }
    return s;
  }

  /**
   * Convert a Collection of DTOs into a collection of the corresponding entities.
   * This convenience method isn't really very convenient, but I keep it around to remind me of how to do this kind of conversion.
   * @param inputList The list of DTOs
   * @param <I> The Input DTO type
   * @param <O> The Output entity type
   * @return A list of entities of type O
   */
  public static <I, O> List<O> convertList(Collection<I> inputList, TypeReference<List<O>> typeReference) {
    return mapper.convertValue(inputList, typeReference);
  }

  /**
   * Returns the String, or an empty String if the String is null.
   * The return value is usually not used, since this is just to test for valid data.
   * @param s The String
   * @return The original String, or an empty String if the original was empty. Never returns null.
   */
  public static String emptyIfNull(@Nullable String s) {
    return (s == null) ? "" : s;
  }
}
