Regular Expression
==================

CookCC follows closely to the `Flex
patterns <http://dinosaur.compilertools.net/flex/flex_7.html>`__, so
this page is contains a number of text copied/pasted from the referenced
article. There are some minor differences though since CookCC allows
both double quoted strings as well as single quoted strings (since
version 0.3.1).

Some examples can be found in test cases for
`NFA <https://github.com/coconut2015/cookcc/tree/master/tests/java/lexer/nfa>`__
and
`CCL <https://github.com/coconut2015/cookcc/tree/master/tests/java/lexer/ccl>`__.

+--------------------+-------------------------------------------------------+
| Format             | Description                                           |
+====================+=======================================================+
| ``x``              | matches the character ``x``                           |
+--------------------+-------------------------------------------------------+
| ``.``              | matches anything except ``\n`` character              |
+--------------------+-------------------------------------------------------+
| ``[xyz]``          | a character class which includes ``x``, ``y``, and    |
|                    | ``z``                                                 |
+--------------------+-------------------------------------------------------+
| ``[abj-oZ]``       | a "character class" with a range in it; matches an    |
|                    | ``a``, a ``b``, any letter from ``j`` through ``o``,  |
|                    | or a ``Z``                                            |
+--------------------+-------------------------------------------------------+
| ``[^A-Z]``         | a "negated character class", i.e., any character but  |
|                    | those in the class. In this case, any character       |
|                    | EXCEPT an uppercase letter.                           |
+--------------------+-------------------------------------------------------+
| ``[^A-Z\n]``       | any character EXCEPT an uppercase letter or a newline |
+--------------------+-------------------------------------------------------+
| ``r*``             | zero or more r's, where a is any regular expression   |
+--------------------+-------------------------------------------------------+
| ``r+``             | one or more r's                                       |
+--------------------+-------------------------------------------------------+
| ``r?``             | zero or one r's (that is, "an optional r")            |
+--------------------+-------------------------------------------------------+
| ``r{2,5}``         | anywhere from two to five r's                         |
+--------------------+-------------------------------------------------------+
| ``r{2,}``          | two or more r's                                       |
+--------------------+-------------------------------------------------------+
| ``r{4}``           | exactly 4 ``r``'s                                     |
+--------------------+-------------------------------------------------------+
| ``{name}``         | Replace with the ``<shortcut>`` pattern of the same   |
|                    | name.                                                 |
+--------------------+-------------------------------------------------------+
| ``"[xyz]"``        | the literal string: ``[xyz]``                         |
+--------------------+-------------------------------------------------------+
| ``'[xyz]'``        | the literal string: ``[xyz]``, since version 0.3.1.   |
+--------------------+-------------------------------------------------------+
| ``\x``             | Escape character, see the section below.              |
+--------------------+-------------------------------------------------------+
| ``\0``             | the character with 0 (ASCII code 0)                   |
+--------------------+-------------------------------------------------------+
| ``(r)``            | match an r                                            |
+--------------------+-------------------------------------------------------+
| ``rs``             | the concatenation of the regular expression ``r``     |
|                    | followed by the regular expression ``s``              |
+--------------------+-------------------------------------------------------+
| ``r|s``            | either an ``r`` or an ``s``.                          |
+--------------------+-------------------------------------------------------+
| ``r/s``            | ``r`` followed by ``s``, but only obtains the ``r``   |
|                    | part. At present, CookCC can only handle cases where  |
|                    | either ``r`` or ``s`` is fixed in length.             |
+--------------------+-------------------------------------------------------+
| ``^r``             | an ``r`` at beginning of line, or the start of scan.  |
|                    | It will increase the DFA size slightly and slightly   |
|                    | decrease the lexer performance.                       |
+--------------------+-------------------------------------------------------+
| ``r$``             | an ``r`` at the end of the line (``\n``), but not     |
|                    | including the ``\n`` character. Note that this        |
|                    | pattern does not work well on files in dos format     |
|                    | because it does not consider ``\r`` character.        |
+--------------------+-------------------------------------------------------+
| ``<<EOF>>``        | an end of file character. This character is           |
|                    | artificial and will not be actually seen in the       |
|                    | input. If this character is the entire pattern,       |
|                    | return value from yyText () is null.                  |
+--------------------+-------------------------------------------------------+

Escape Characters
-----------------

+--------------+----------------------------------------------------------------------+
| Code         | Description                                                          |
+==============+======================================================================+
| ``\b``       | backspace                                                            |
+--------------+----------------------------------------------------------------------+
| ``\e``       | escape ``\033``                                                      |
+--------------+----------------------------------------------------------------------+
| ``\f``       | form feed                                                            |
+--------------+----------------------------------------------------------------------+
| ``\n``       | line feed                                                            |
+--------------+----------------------------------------------------------------------+
| ``\r``       | carriage return                                                      |
+--------------+----------------------------------------------------------------------+
| ``\t``       | tab                                                                  |
+--------------+----------------------------------------------------------------------+
| ``\s``       | space ``' '``                                                        |
+--------------+----------------------------------------------------------------------+
| ``\\``       | back slash ()                                                        |
+--------------+----------------------------------------------------------------------+
| ``\"``       | double quote (")                                                     |
+--------------+----------------------------------------------------------------------+
| ``\'``       | single quote (')                                                     |
+--------------+----------------------------------------------------------------------+
| ``\123``     | the character with octal value 123 (maximum 3 digits)                |
+--------------+----------------------------------------------------------------------+
| ``\x2a``     | the character with hexadecimal value 2a (maximum 2 digits)           |
+--------------+----------------------------------------------------------------------+
| ``\u002a``   | the unicode character with hexadecimal value 2a (exactly 4 digits)   |
+--------------+----------------------------------------------------------------------+

Escape sequences with ``\`` proceeding any character that is not listed
above would treat treat that character as the literal character, rather
than special characters.

POSIX Character Classes
-----------------------

+----------------+-----------------------------------------------------------+
| Class          | Equivalent                                                |
+================+===========================================================+
| ``[:alnum:]``  | ``[a-zA-Z0-9]``                                           |
+----------------+-----------------------------------------------------------+
| ``[:alpha:]``  | ``[a-zA-Z]``                                              |
+----------------+-----------------------------------------------------------+
| ``[:blank:]``  | ``[ \t]``                                                 |
+----------------+-----------------------------------------------------------+
| ``[:cntrl:]``  | ``[\x00-\x1f\x7f]``                                       |
+----------------+-----------------------------------------------------------+
| ``[:digit:]``  | ``[0-9]``                                                 |
+----------------+-----------------------------------------------------------+
| ``[:graph:]``  | ``[[:alnum:][:punct:]]``                                  |
+----------------+-----------------------------------------------------------+
| ``[:lower:]``  | ``[a-z]``                                                 |
+----------------+-----------------------------------------------------------+
| ``[:print:]``  | ``[[:graph:] ]``                                          |
+----------------+-----------------------------------------------------------+
| ``[:punct:]``  | ``[!"#$%&'()*+,-./:;<=>?@[\\\]^_`{|}~]``                  |
+----------------+-----------------------------------------------------------+
| ``[:space:]``  | ``[ \t\n\x0b\f\r]``                                       |
+----------------+-----------------------------------------------------------+
| ``[:upper:]``  | ``[A-Z]``                                                 |
+----------------+-----------------------------------------------------------+
| ``[:xdigit:]`` | ``[a-fA-F0-9]``                                           |
+----------------+-----------------------------------------------------------+
| ``[:word:]``   | ``[a-zA-Z0-9_]``. Technically, it is not part of POSIX.   |
|                | Added in 0.4.                                             |
+----------------+-----------------------------------------------------------+

For example, the following character classes are all equivalent:

+-------------------+----------------------------+----------------------+-------------------+
| ``[[:alnum:]]``   | ``[[:alpha:][:digit:]]``   | ``[[:alpha:]0-9]``   | ``[a-zA-Z0-9]``   |
+===================+============================+======================+===================+
+-------------------+----------------------------+----------------------+-------------------+

Character Class Manipulations
-----------------------------

A character class can be manipulated in several ways:

+------------+----------------------------+-------------------------------------+
| Operation  | Description                | Examples                            |
+============+============================+=====================================+
| ``{+}``    | Merging two character      | ``[A-Z]{+}[a-z]`` is the same as    |
|            | classes.                   | ``[A-Za-z]``                        |
+------------+----------------------------+-------------------------------------+
| ``{-}``    | Remove all characters in   | ``[A-Za-z]{-}[a-z]`` is the same as |
|            | the second character class | ``[A-Z]``. ``[a-z]{-}[aeiou]``      |
|            | from the first character   | represents all lower case consonant |
|            | class                      | letters.                            |
+------------+----------------------------+-------------------------------------+
| ``^``      | Not operation              | ``[[:^alpha:]]`` is the same as     |
|            |                            | ``[^[:alpha:]]`` and                |
|            |                            | ``[\0-\xff]{-}[a-zA-Z]``.           |
|            |                            | ``[^ \t\r\n]`` represents all       |
|            |                            | non-space characters.               |
+------------+----------------------------+-------------------------------------+

Tips
----

-  Unless one is generating a full DFA table, it is usually a good idea
   not to generate new character classes. For example, ``(a|b)`` can be
   better written as ``[ab]`` which would reduce the DFA table size for
   ``ecs`` and ``compressed`` format quite a bit. This approach is
   particularly necessary when there are lots of DFA states.

   -  In 0.4, a special handling is added to handle ``(a|b)`` like
      expression so that it is treated as ``[ab]``.

-  Long strings of keyword matches such as ``"while"``, ``"continue"``
   can increase the DFA table size quite a bit. Consider using hash
   tables or `perfect
   hash <http://en.wikipedia.org/wiki/Perfect_hash_function>`__ instead
   if the tools are available in the target language.
-  On the other hand, specifying long patterns without causing backups
   can increase the lexer performances at the expense of more DFA
   states.
