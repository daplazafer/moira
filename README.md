# Moira Library

Moira is a library for managing and executing decision trees reactively in Java applications. It uses Reactor to execute decision nodes asynchronously and provides a straightforward interface for handling complex decision trees.

## Features

- **Asynchronous Execution**: Uses Reactor for non-blocking decision tree execution.
- **Node Management**: Allows execution of nodes based on context and decisions.
- **Execution Logging**: Includes detailed logging of execution flow and decision results.
- **Flexible Configuration**: Compatible with projects using the Spring Framework.

## Requirements

- **Java**: 17 or higher
- **Spring Framework**: 6.x
- **Maven** or **Gradle** for project build

## Installation

### Using Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.github.daplazafer</groupId>
    <artifactId>moira</artifactId>
    <version>0.1.1</version>
</dependency>
```

### Using Gradle

Add the following line to your build.gradle:

```groovy
dependencies {
    implementation 'com.dpf.moira:moira:0.1.1'
}
```

## Usage

### Configuring Nodes

Annotate your node classes with @Decision to define the description and ID:

```java
import com.dpf.moira.Decision;

@Decision(description = "Decide the next step based on speed", id = "speedDecision")
public class SpeedNode extends Node<CarContext, SpeedResult> {
    @Override
    public SpeedResult decide(CarContext context) {
        return context.getSpeed() > 100 ? SpeedResult.HIGH_SPEED : SpeedResult.NORMAL_SPEED;
    }
}
```

### Running a Decision Tree

Create an instance of Moira and call the runAsync or run method:

```java
import com.dpf.moira.Moira;
import com.dpf.moira.test.CarContext;

public class Application {

    public static void main(String[] args) {
        Moira moira = new Moira(decisionTreeRegistry, nodeRegistry);
        CarContext context = new CarContext(120);

        moira.runAsync("carDecision", context);
    }
}
```

### Logging

Execution logs are recorded using SLF4J. Make sure to configure your logging system to view the details.

### Building

To build the library, use Maven or Gradle:

#### Maven
```bash
mvn clean package
```

#### Gradle
```bash
./gradlew build
```

## Publishing

To publish the library, you can use JitPack or Maven Central. Check the JitPack documentation for quick publishing or follow the Sonatype OSSRH process for Maven Central.

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

For any questions or comments, contact dpf220691@gmail.com.
