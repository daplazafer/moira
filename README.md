# Moira Library

Moira is a library for managing and executing workflows reactively in Java applications. It uses Reactor to execute decision nodes asynchronously and provides a straightforward interface for handling complex workflows.

## Features

- **Asynchronous Execution**: Uses Reactor for non-blocking workflow execution.
- **Node Management**: Allows execution of nodes based on scenario and decisions.
- **Execution Logging**: Includes detailed logging of execution flow and decision results.
- **Flexible Configuration**: Compatible with projects of various frameworks.

## Requirements

- **Java**: 17 or higher
- **Maven** or **Gradle** for project build

## Example with SpringBoot

For a practical example of how to use this library, check out my [dop-moira-spring-example repository](https://github.com/daplazafer/dop-moira-spring-example).

## Installation

### Using Maven

First add jitpack.io repositories to your `pom.xml`:

```xml
<repositories>
    ...
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
    ...
</repositories>
```

Then add the following dependency to your `pom.xml`:

```xml
<dependencies>
    ...
    <dependency>
        <groupId>com.github.daplazafer</groupId>
        <artifactId>moira</artifactId>
        <version>0.3.0</version>
    </dependency>
    ...
</dependencies>
```

### Using Gradle

Add the following lines to your build.gradle:

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

```groovy
dependencies {
    implementation 'com.dpf.moira:moira:0.3.0'
}
```

## Usage

### Configuring Moira library

Configure properties in `moira.properties` in your resources directory. This is an example with the default values:

```properties
workflowFilesPath=workflows
hotReloadMode=false
```

### Configuring Nodes

Annotate your node classes with `@Decision` to define id and description and extend Node class. You can use whatever class you want to work as a scenario.

My advice of managing Node responses is the following:

```java
import com.dpf.example.moira.dop.node.IsCarRunningNode.Result;

import static com.dpf.example.dop.node.IsCarRunningNode.Result.RUNNING;
import static com.dpf.example.dop.node.IsCarRunningNode.Result.STOPPED;

@Decision(id = "isCarRunning", description = "Is the car running?")
public class IsCarRunningNode extends Node<Car, Result> {

    public enum Result {
        RUNNING, STOPPED
    }

    @Override
    public Result execute(Scenario<Car> scenario) {
        return scenario.get().getSpeed() > 0 ? RUNNING: STOPPED;
    }
}

```

### Configuring Workflows

Create a yml file and place it into the directory configured with `workflowFilesPath` variable. 

```yml
id: car
start: isCarRunning
nodes:
  - id: isCarRunning
    transitions:
      - result: RUNNING
        next: isCarOverMaximumSpeed
      - result: STOPPED
        next: end
  - id: isCarOverMaximumSpeed
    transitions:
      - result: YES
        next: brake
      - result: NO
        next: end
  - id: brake
    transitions:
      - result: DONE
        next: isCarRunning
  - id: end
```

### Running a Workflow

Create an instance of Moira and call the `decide` method:

```java
import com.dpf.moira.Moira;

import com.dpf.example.dop.scenario.Car;

public class Application {

    public static void main(String[] args) {
        
        Moira moira = new Moira(List.of(
                new IsCarRunningNode(), 
                new BrakeNode(), 
                new IsCarOverMaximumSpeedNode(), 
                new TerminalNode()));
        
        Car carScenario = new Car(120);

        moira.decideAsync("carWorkflow", carScenario);
    }
}
```

### Logging

Execution logs are recorded using SLF4J. Make sure to configure your logging system to view the details. To debug Moira executions:

```properties
logging.level.com.dpf.moira.Moira=DEBUG
```

### Building

To build the library use Maven:

#### Maven
```bash
mvn clean package
```

## Contributing

Contributions are welcome. Please follow these steps to contribute:

Fork the repository.
- Create a new branch `git checkout -b feature-branch`.
- Make your changes and commit `git commit -am 'Add new feature`.
- Push your changes `git push origin feature-branch`.
- Create a Pull Request on GitHub.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact

For any questions or comments, contact me [here](mailto:dpf220691@gmail.com).
