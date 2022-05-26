# Spring-Boot Error Message Issue

This is the demo project that inspired the question at
https://stackoverflow.com/questions/72355196/im-getting-inadequate-error-messages-from-spring-boot

As I describe in the issue, when I send invalid data that gets detected by Hibernate, I get a detailed error
message back describing the entity and field with an illegal value. Unfortunately, if the OpenAPI-generated 
DTOs have @NotNull annotations, Spring Boot rejects the input without even reaching my server code. That's
fine, but the error messages it sends say nothing about the problem with the input data.

## To Reproduce…

Here I will describe two messages you can send. One will demonstrate a good error message genrated by Hibernate.
The other will show the worthless error message.

### Build and launch the server from the root directory with these two commands:

    mvn clean install
    java -jar Server/target/Server-1.0-SNAPSHOT.jar

### Generate useful Hibernate error message:

    POST http://localhost:27777/demo/admin/menuItem/addOption/1
    BODY {"name":"Olives","id":44,"deltaPrice":"0.50"}

### Generate useless Spring error message:

    POST http://localhost:27777/demo/admin/menuItem/addOption/1
    BODY {"name":"Olives"}

The difference is that the useful Hibernate message was passed through the server code in this project. The useless
Spring error message was sent before the request ever hit my code. 

If anyone could explain how to get better error messages out of the Spring Boot server, I would love to hear it.

---

# Spring Boot Demos

## Building

The project is divided into two modules, Gen, and Server. You can 
do `mvn clean install` in the main directory to build all packages, or you can
build each module separately. Either way, this will produce an executable jar
file at `Server/target/Server-1.0-SNAPSHOT.jar`. If you build separately, you
should build them in two stages:

Stage 1: **Gen**

This generates the APIs and DTOs from the specification in the
`SpringBootDemo.openApi.yaml` file. All the code in this package
is generated.

Stage 2: **Server**

This is where all the application code sits.

## Build, Launch, and Test

In these instructions, I use Windows conventions. If you're on a Mac or Linux, convert 
slashes in file paths to forward slashes.

#### Build
In the main directory, type `mvn clean install`

#### Launch

In the main directory, type this commands

    java -jar Server\target\Server-1.0-SNAPSHOT.jar

This starts a server running at port 27777. I chose that port because, for mysterious reasons,
the default port of 8080 didn't work on my work laptop, which I suspect is a security feature
that I don't understand.

##### Note
The standard command is `mvn spring-boot:run`, but this doesn't work because the application
isn't in the root project. I didn't put it there because then certain tests won't run. I know
how to fix this, but I haven't done that yet.

## Running and Exercising the Server

The following assumes you may use Postman.

Get the initial state of the database:

    GET:  http://localhost:27777/demo/menuItem

This returns all menu items, which at this point is an empty array: `[]`

Now, add two menu items. {'name':'pizza','itemPrice':"14.00'}

    PUT  http://localhost:27777/demo/admin/menuItem/add
    BODY { "name": "14 inch pizza", "itemPrice": "14.00" }
    
    PUT  http://localhost:27777/demo/admin/menuItem/add
    BODY { "name": "16 inch pizza", "itemPrice": "16.00" }

This will return 1, which is the id of the created entity.

Now repeat the GET, and you'll get this back:

    [
        {
            "id": 1,
            "name": "14 inch pizza",
            "itemPrice": 14.00,
            "allowedOptions": []
        },
        {
            "id": 2,
            "name": "16 inch pizza",
            "itemPrice": 16.00,
            "allowedOptions": []
        }
    ]

I should mention that the application keeps this menu in a cache, and flushes the
cache whenever the menu items are changed. While I can't demonstrate this, I have
verified that it works and you can see the caching annotations in the repository
interfaces.

Now we can add some allowedOptions. There are two ways to do this. We can add some
directly to a MenuItem, or we can create them individually, and add them separately.
First, we'll add an item directly to the 14 inch pizza:

    POST http://localhost:27777/demo/admin/menuItem/addOption/1
    BODY {"name": "Olives", "deltaPrice":"0.50"}
This returns a 3, the id of the created item. Now repeat the get:

    [
        {
            "id": 1,
            "name": "14 inch pizza",
            "itemPrice": 14.00,
            "allowedOptions": [
                {
                    "name": "Olives",
                    "deltaPrice": 0.50,
                    "id": 3
                }
            ]
        },
        {
            "id": 2,
            "name": "16 inch pizza",
            "itemPrice": 16.00,
            "allowedOptions": []
        }
    ]

As you can see, the option we created was just added to the 14 inch pizza.

Now, we will create a menuItemOption separately and add it to the 16 inch pizza,
in two steps.

    PUT  http://localhost:27777/demo/admin/menuItem/addOption
    BODY {"name": "onions", "deltaPrice": "0.50"}
This will return the id of the newly created option, which will be 4. We use this
value in the next command:
    
    POST http://localhost:27777/demo/admin/menuItem/addOption/2/4

Now we get the newly-modified menuItem:

    GET  http://localhost:27777/demo/menuItem/2

It returns the new option in the menuItem with id of 2.

    {
        "id": 2,
        "name": "16 inch pizza",
        "itemPrice": 16.00,
        "allowedOptions": [
            {
                "name": "onions",
                "deltaPrice": 0.50,
                "id": 4
            }
        ]
    }

If we want, we can include the options when we create the menuItem:

    PUT  http://localhost:27777/demo/admin/menuItem/add 
    BODY
    {
        "name": "18 inch pizza",
        "itemPrice": 18.00,
        "allowedOptions": [
            {
                "name": "onions",
                "deltaPrice": 0.50
            },
            {
                "name": "olives",
                "deltaPrice": 0.50
            }
        ]
    }

When we do another getAll, here's the last one on the list:

    {
        "id": 6,
        "name": "18 inch pizza",
        "itemPrice": 18.00,
        "allowedOptions": [
            {
                "name": "onions",
                "deltaPrice": 0.50,
                "id": 7
            },
            {
                "name": "olives",
                "deltaPrice": 0.50,
                "id": 8
            }
        ]
    }

#### Cleanup

In your home directory, delete the main database:

    > del springBootDemoDatabase.*

This should delete `springBootDemoDatabase.mv.db` and `springBootDemoDatabase.trace.db`

# Notes:

This was developed and tested using Java 8.

## API Design:
### General
The APIs are implemented with a call to a serve method, which takes a lambda expression that
delivers the requested data. This is packed into a ResponseEntity object and returned. I did
this to separate the server-related classes from the implementation, so the implementation
code does not need to know it's running on a server, unless it has to throw an Exception.
I did this partly because past servers I've worked on have been very inconsistent in how
they log errors, return results, and return errors. The `serve()` method, and some
convenience methods that delgate to it, is in the `ResponseUtility` class, and other useful
utilities are in the PojoUtilities class 
(See the [**Service Implementations**](#Service Implementations) section below for more
details.) At some point, I'd like to write generators for OpenAPI to encourage this design
in all APIs.

### Developer Rules for error responses:
1. Return an error response only by throwing an annotated Exception. Exceptions are annotated
with the `@HttpStatus` annotation, which specifies the status to return. All of these
exceptions extend ResponseException. If there's no Exception for the error response you want
to send, add one. Be sure to extend `ResponseException` and annotate it with `@HttpStatus`.
2. Never catch a RuntimeException. If the code generates a RuntimeException, let it pass
through. It will generate a 500 error response (Internal Server Error), which tells us we
need to find the bug and fix it. If you need to say `catch (Exception e)`, first catch any
RuntimeExceptions and rethrow them.
3. Don't worry about logging Exceptions, unless you catch them and don't rethrow. The
UncaughtExceptionHandler will log all Exceptions it sees.
4. The ResponseException subclasses will only get thrown in response to a known exceptional
situation that's not due to a bug, so their stack traces don't appear in the log. Never throw
one in response to a bug.

## Implemented Technologies

1. RESTful services
1. JPA Crud operations will write to an underlying H2 database, using Spring Data.
1. Unit tests use an in-memory h2 database.

## APIs

(The most important part of this demo is described in the [**Service Implementations**](#Service Implementations) section, below.)

This uses maven to build. It has been tested using Maven v3.8.4 and openjdk 
version "17.0.3," with a source-language-level of 8.

### REST API Documentation

You can view the api documentation in swagger-ui by launching the server, then go to `http:localhost:27777`, which will redirect to
`http://localhost:27777/demo/swagger-ui.html`

You may change the default port value in application.properties

### Service Implementations

To ensure consistency in how the services are written, and to reduce the amount of boilerplate
code, all the services use a variant of the `ResponseUtility.serve()` method. This allows the
service to focus solely on the task of generating the service data, and not worry about
creating the ResponseEntity, generating an error response, or logging exceptions. In case of
an error, the service need only throw one of the subclasses of ResponseException,which all
include an HttpStatus value. The `PojoUtilities` class has several convenience methods to
simplify this, all of which throw a ResponseException. By convention, most of these methods
begin with the word "confirm" or "find." For example, if a service requests an Entity with a
specific ID, the service should call `PojoUtility.findOrThrow404(entity, id);` If the entity
doesn't exist, the `findOrThrow404()` method will throw a ResponseException with a NOT_FOUND
status, and include the id in the error message.

So a service method that needs to return an instance of `MenuItemDto` would look something
like this:

```
1   public class MenuItemApiController {
2
3   private final DataEngine dataEngine;
4
5   // ...  
6   @Override
7   public ResponseEntity<MenuItemDto> getMenuItem(final Integer id) {
8     return serveOK(() -> dataEngine.getMenuItemDto(id));
9   }
```

So, on line 8, we specify an OK status if the method returns successfully. We also
create the lambda expression that delegates the work to the `DataEngine` class:

```
11  public class DataEngine {
12
13    MenuItem getMenuItemFromId(int id) {
14      MenuItem menuItem = findOrThrow404(menuItemRepository, id);  // throws NotFound404Exception extends ResponseException
15      return menuItem;
16   });
17 }
```

On line 6, we test for null, using the `findOrThrow404()` method. If no `menuItem`
exists with the specified id, it will throw `ResponseException` with an `HttpStatus`
of `NOT_FOUND`. We don't need to catch it, because it's annotated with
`@ResponseStatus(HttpStatus.NOT_FOUND)`, so the server will use that status code in
its response. But the `serveOK()` method, called in the previous method, catches it
for logging purposes, then rethrows it.

The call to the `serve()` method takes care of five boilerplate details:

1. It adds the return value (an instance of MenuItemDto) to the `ResponseEntity` on
successful completion.
2. It sets the specified HttpStatus, which in this example is `HttpStatus.OK`.
3. It generates the proper error response, with an error status code taken from the
`ResponseException` thrown by the lambda expression. In this case, this is a
`NotFound404Exception` thrown by the`findOrThrow404()` method. The
`NotFound404Exception` method extends `ResponseException`, as do all the others.
4. It logs the error message and exception.
5. It catches any RuntimeExceptions and returns a response of Internal Server Error.

Also, by using ResponseExceptions to send failure information back to the `serve()`
method, it discourages the use of common Exception anti-patterns, like
catch/log/return-null. Instead, developres are encouraged to wrap a checked
exception in a ResponseException and rethrow it, and to ignore all
RuntimeExceptions, letting them propogate up to the `serve()` method, which can
then generate an INTERNAL SERVER ERROR response.

The `serve()` method has this signature:

`  public static <T> ResponseEntity<T> serve(HttpStatus successStatus, Supplier<T> method)`

On line 8, above, the lambda expression creates a `Supplier<T>`

The only boilerplate code in the example is the `@RequestMapping` annotation and the
method signature, both of which are generated by OpenApi.

### Sample `confirmXxx()` methods.

All of these may throw a `ResponseException`. I've adopted the convention that all
methods that may throw `ResponseException` start with the words *confirm* or *find*. 

* `<E, ID> E findOrThrow404(JpaRepository<E, ID> repository, ID id) throws ResponseException`
Confirms the returned entity with specified id is not null. This also retrieves and
returns the entity.
* `<T> T confirmNeverNull(T object) throws ResponseException` Used for values that
are not entities. This returns the parameter, so it may be used in a function chain.
* `void confirmNull(Object object) throws ResponseException` This is useful to
ensure a new resource doesn't already exist.
* `<T> void confirmEqual(T expected, T actual) throws ResponseException` Confirms
the object is equal to an expected value.
* `Long confirmAndDecodeLong(final String id) throws ResponseException` Decodes a
String into a Long value, throwing a ResponseException if it fails.
* `Integer confirmAndDecodeInteger(final String id) throws ResponseException` These
two parse the String into an Integer or Long. A better name might be
  just `decodeInteger()`, but it starts with `confirm` to keep with the convention.

People have asked why I didn't use the word *validate,* since it's pretty standard.
I decided not to use it to be clear that these methods are not a part of any
third-party validation framework.

I should also stress that these are just convenience methods. If any developers
have cases not handled by one of these, and can't write a simple convenience method
to do what they need, they are free to throw a ResponseException directly. Any
RuntimeExceptions need not be caught. They will get logged and an
INTERNAL_SERVER_ERROR response will be returned.

## Data Model

### Assumptions

* A Menu item consists of options. Each menu item has a price, as does each option.
(Option prices may be zero.) An order consists of a menu item and a list of options.

* An order may calculate a price based on the Menu Item's base cost and the options
chosen.

* When an order is opened, the time is recorded. (I have no idea if that's useful,
but it may help in searching.) At this point, the order may be either canceled or
completed. If it's canceled, it's removed from the database. If it's completed, it
is marked complete and kept in the database.

* Orders may be searched by ID.

* I'm not sure if my API is most useful for a UI developer. I prefer to ask the UI
developers what they need, then build the API around their needs. That said, I have
APIs to define menu items, and add options to them. I have APIs to create an order,
to add options to either an order or a MenuItem, and to search for completed or
open orders in a given date range.

### JPA Entities

#### 1. MenuItem

A MenuItem consists of a name, price, and list of MenuItemOptions (below). The list consists of all possible options for this menu item.
MenuItem has a One-to-Many relationship with MenuItemOption. It also includes a price.

#### 2. MenuItemOption

A MenuItemOption adds an option to aMenuItem (below). It also has a delta price, which is the amount the price changes if the guest chooses
this option.

#### 3. CustomerOrder

A Food order is an actual order. It has a final price, a boolean to record when it has been completed and delivered, and an order date and
completion date, and a list of MenuItemOptions. Unlike the MenuItem, the list of options is all the chosen options, rather than the
available options. Also, unlike MenuItem, the CustomerOrder has a Many-To-Many relationship with MenuItemOption.

## Testing

The testing application properties specify an in-memory database, so changes get
wiped out from test to test.

The Controller classes have public method which are called by the server, and package-level methods that are only for testing. All of these
package methods are named `xxxXxxxTestOnly` to discourage their use even if somebody puts a class in the same package.

## Code Generation

Generated using Swagger's OpenAPI Specification OAS 3.0, using the Spring Server generator, with the following options:

* interfaceOnly: True
* bigDecimalAsString: True
* dateLibrary: Java 8
* developer name: Miguel Muñoz
* title: Pizza Orders
* generatorName: spring
* library: spring-boot

### Code Generator Bugs (All are minor)

#### Spurious Optional

When Java 8 is set, it adds a NativeWebRequest member. It also creates default getter for that property and the ObjectMapper property. This default getter wraps the values in an `Optional`.
Both properties are final and autowired, so they can't possibly have null values, so the Optional wrapper returned by the getters is
unnecessary.

#### Spurious default methods

When Java 8 is set, it turns on the defaultInterfaces option, which I would rather be left off. This generates stubs as default methods for
each api method. There are two consequences. First, failure to implement a recently added interface doesn't prevent compilation. Second, the
stubs return a 510 Not implemented. I would rather they throw an Error. (It also takes too much code to return the 501)

#### Date option

When the date library is set to one of the three java 8 values, it turns on Java 8, which is fine, but this activates the two java 8 bugs above.
