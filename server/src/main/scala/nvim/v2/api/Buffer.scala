package nvim.v2.protcol

import akka.actor.{Actor, ActorRef, Props}
import com.rallyhealth.weepack.v1.Str
import akka.actor.ActorLogging
import nvim.v2.Request
import com.rallyhealth.weepack.v1.Int32

case class GetMark(name:String) {
}// => return array of intergers
class Buffer(code:Int) extends Actor with ActorLogging {
  // {
  //   "parameters": [
  //     [
  //       "Buffer",
  //       "buffer"
  //     ],
  //     [
  //       "String",
  //       "name"
  //     ]
  //   ],
  //   "method": true,
  //   "return_type": "ArrayOf(Integer, 2)",
  //   "name": "nvim_buf_get_mark",
  //   "since": 1
  // }
  def receive = {
    case gm:GetMark =>
      context.parent ! Request(0, 123, "nvim_buf_get_mark", List(Str(gm.name), Int32(code)))
  }
}
