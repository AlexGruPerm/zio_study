package Exceptions

/**
 * Exception
*/
class ExcCantConnDb(message: String) extends Exception(message) {

  def this(message: String, cause: Throwable) {
    this(message)
    initCause(cause)
  }

}