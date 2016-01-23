package nvim.protocol

final class UnexpectedResponse(response: String) extends RuntimeException(s"Request can't handle `$response`.")
