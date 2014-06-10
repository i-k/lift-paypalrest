package liftpaypal
package rest

import msgs.Outgoing.PayerId

import net.liftweb.util._
import net.liftweb.http._
import Helpers.tryo
import net.liftweb.common._

import rest._

/*  Example usage:
 * 
  object YourPayPalRestServer extends PayPalRestHelper {
    serve {
      case List("paypal") Get req => withPayerId(req, (payerId: PayerId) => {
        msgComet(payerId)
        RedirectResponse("/your/url/here", req)
      })
    
      case List("paypal", "cancel") Get req =>
        msgComet(CancelPayment)
        RedirectResponse("/your/cancel/url/here", req)
    }
  
    def msgComet(msg: Any) = S.session.foreach(
      _.sendCometActorMessage("YourComet", Empty, msg)
    )
  }
  
  in boot:
  
  LiftRules.dispatch.append(YourPayPalRestServer)
*/
trait PayPalRestHelper extends RestHelper {

  // PayerId so can be easily sent to actors
  def withPayerId[T](req: Req, doThis: Function1[PayerId, T]): Box[T] =
    withPayerIdParam(req, (id: String) => doThis(PayerId(id)))
    
  def withPayerIdParam[T](req: Req, doThis: Function1[String, T]): Box[T] =
    req.param("PayerID").map(doThis)
}

case object CancelPayment
