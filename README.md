# JLox

## A basic programing language written in java.

### My Attempt in learning how interpreters work by following the guide of Robert Nystrom in his book [Crafting Interpreters](https://craftinginterpreters.com/).

## How it works

Firstly clone the repo

then run the following to build the jar file

```shell
 ./gradlew jar 
```

and finally run the application as follows

```shell
java -jar ./build/libs/jlox-0.0.1.jar
```

## Syntax

the syntax and grammar for JLox is defined in the book.

### variables

```js
var n = 5.5;
var str = "Hello";
var bool = true;

print
str + "World!";
```

### Functions

```
fun makeCounter()
{
    var i = 0;

    fun count()
    {
        i = i + 1;
        print
        i;
    }

    return count;
}

var counter = makeCounter();

counter(); // "1".
counter(); // "2".
```

### Classes

```
class Bacon {
    eat() {
        print "Crunch crunch crunch!";
    }
}

Bacon().eat(); //

class Doughnut {
    cook() {
        print "Fry until golden brown.";
    }
}

// Inheritance
class BostonCream < Doughnut {
}

BostonCream().cook();

class Person {
    sayName() {
        print this.name;
    }
}

var jane = Person();
jane.name = "Jane";

var method = jane.sayName;

method(); // "Jane"
```

## License

This project is open source and available under the [MIT License](LICENCE).