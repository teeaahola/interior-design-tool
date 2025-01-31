
/** Create some exceptions to throw when the program encounters issues. */
class DesignExceptions(text: String) extends Exception(text)

/** Thrown when the program encounters a shape it does not know how to work with. */
case class NoSuchShapeException(text: String) extends DesignExceptions(text)

/** Thrown when the program encounters an element that does not exist. */
case class NoSuchElementsException(text: String) extends DesignExceptions(text)

/** If the code works properly, these exceptions should never be thrown. They are
 * implemented as a way to tell the user what the issue is should something go wrong. */