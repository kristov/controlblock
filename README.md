# Controlblock

Experiment of a Lisp interpreter in Java.

![funny lisp](https://github.com/kristov/controlblock/blob/master/lisplogo_warning.png?raw=true)

Some features:

## The "eval" function is not recursive

This is a very important feature that I wanted to have in this interpreter. The call to `evalStep()` will perform one evaluation step only. This means the lisp program stack and the Java execution stack are separate. I really wanted the lisp stack to be inspectable and manipulatable from within the running environment.

## The memory model is simple (dumb)

There are two "heaps" in the interpreter:

1) A heap for storing cons cells. This is a big array of 32 bit signed integers. Cons cells occupy two consecutive items in the array.
2) A heap for storing every other object (strings).

A cons cell is made up of two consecutive 32 bit signed integers in the cons heap. The first integer represents the "value". If the number is positive it is an index into another location in the heap (it's a list). If negative it is an index into the object heap (after making it positive). If the number is zero it is an empty list (AKA: the null list). The second 32 bit integer should always be either positive or zero. If positive it is the index of the next cell in a cons list:

    | int32 | int32 |
    |-------|-------|
    |  CAR  |  CDR  |

    | CAR | CDR | Meaning                                               |
    |-----|-----|-------------------------------------------------------|
    |  0  |  0  | Empty cell                                            |
    | > 0 |  -  | Value (car) points to the head of another cons list   |
    | < 0 |  -  | Value is an "atom" - a pointer into strings heap      |
    |  -  | > 0 | Pointer index to next cell in cons list               |

For example to access the cons cell pair at index 5 you do the following:

    int i = 5;
    int value = heap[i * 2];
    if (value == 0) {
        // the empty list, or null
    }
    else if (value > 0) {
        // a list - the value points to the head of another cons
    }
    else {
        // a value - an index into the strings object heap
    }

To check if there is another item linked to this cons:

    if (heap[(i * 2) + 1]) {
        // there is another cons cell after this one
    }

Iterating over a cons list:

    int i = 5;
    while (i > 0) {
        // heap[(i * 2)] is your data
        i = heap[(i * 2) + 1];
    }

An optimization I would like to make is to split a single 32 bit number in the cons heap into two 16 bit parts, meaning no need for multiplication. I don't really need 31 bits of cells or strings, 15 will do fine.

## There is no type system

All values are stored as strings. Values are converted into native types inside the builtin functions. This is not a performance language ;-)

## How it works (roughly)

These are two stacks involved in the `evalStep()` function: 1) an expression stack to store the upcoming expressions than need to be evaluated, and 2) a "value" stack which stores the results of previously evaluated expressions (expressions that have been reduced to an atom). For example:

    (+ 3 4)

The "3" and the "4" will end up getting pushed onto the value stack. When the "+" atom is evaluated it will pop the two arguments from the value stack. Now consider this expression:

    (+ 5 (+ 2 3))

This cries out for a recursive evaluation because obviously we need to evaluate the "+ 2 3" before we evaluate the outer expression. In this implementation the "+ 2 3" expression will be pushed onto the expression stack, and eventually its value will get pushed onto the value stack. By the time we get to evaluating the outer expression the value stack contains "5" and "5", so the outer expression will pop these values from the value stack.

When an expression is pushed onto the expression stack it is pushed as a pair. The first item in the pair is the expression, and the second is an "environment". The environment is a key-value list of symbols.

### Overview

The `evalStep()` function works approximately like so:

If the expression being evaluated is an atom first try to resolve the atom to something else using the symbol table. If it's still an atom then push it onto the value stack. If it was converted into an expression (or it was an expression all along) then try to handle a few hard-coded constructs (lambda, cond, quote). If it wasn't one of those then just push all the sub-expressions onto the expression stack.

In psuedo code:

    Pop the next pair from the expression stack
    Get the expression from the pair (first item)
    Get the environment from the pair (second item)
    If the expression is an atom (a string) {
        Try to convert the atom into an expression using the environment. This
        is how function names are expanded into their lambda bodies.
    }
    Check the expression again and if it's not an atom {
        Check if the expression is a lambda, and if so {
            Create a new environment and pop all the argument values from the
            value stack into the enviromnent, using the lamda parameter names
            as keys (AKA: binding).
            If the body of the lambda is an atom it is a built-in so dispatch
            it.
            Otherwise push the body of the lambda onto the expression stack
            with the new environment.
        } Otherwise {
            Process some other structures like quote and cond
        } Otherwise {
            Loop through the expression and push the sub-expressions onto the
            expression stack, with the current environment.
        }
    } Otherwise if it is an atom {
        Push it onto the value stack
    }

