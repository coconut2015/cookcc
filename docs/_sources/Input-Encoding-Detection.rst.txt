Input Encoding Detection
========================

Detection Methods
-----------------

Encoding detection is mostly a guess work.

-  `BOM <https://en.wikipedia.org/wiki/Byte_order_mark>`__ is obviously
   the most useful in detecting the input incoding stream.
-  `XML <http://www.w3.org/TR/REC-xml/#charencoding>`__ has encoding
   declaration.
-  `HTML <http://www.w3.org/TR/html5/syntax.html#encoding-sniffing-algorithm>`__
   has an encoding sniffing algorithm.

Java Libraries
--------------

-  `juniversalchardet <https://code.google.com/p/juniversalchardet/>`__
-  `jChardet <http://jchardet.sourceforge.net/>`__
-  `cpdetector <http://cpdetector.sourceforge.net/>`__
-  `ICU4J <http://userguide.icu-project.org/conversion/detection>`__
-  `Apache Tika <http://tika.apache.org/>`__ - uses a combination of
   above libraries.
