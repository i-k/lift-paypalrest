package liftpaypal
package lib

import msgs.{Outgoing, Incoming, PayPalToken, Payer}

import dispatch._
import com.ning.http.client.{
  Response, AsyncCompletionHandler, AsyncHandler, HttpResponseStatus
}
import net.liftweb.json._
import Serialization.{read, write}
import net.liftweb.common._
import net.liftweb.util.Helpers.tryo

object PayPalImplicits {
  implicit def reqToPayPalReq(req: Req): PayPalReq = new PayPalReq(req)
  implicit def futureToBoxEnrichedFuture[A](f: Future[A]): BoxEnrichedFuture[A] =
    new BoxEnrichedFuture[A](f)
  implicit val formats = msgs.Serialization.formats()
}

class PayPalReq(req: Req) {
  import PayPalImplicits.formats
  
  def addRequiredPayPalHeaders(t: PayPalToken) = 
    req.
      addHeader("Authorization", t.token_type + " " + t.access_token).
      addHeader("Content-Type", "application/json")
  
  def payPalPOST(t: PayPalToken): Req =
    addRequiredPayPalHeaders(t).POST
    
  // POST to PayPal with @param body formatted into json
  def payPalPOST[BodyType <: AnyRef](t: PayPalToken, body: BodyType): Req =
    addRequiredPayPalHeaders(t) << write[BodyType](body)
  
  def payPalGET(t: PayPalToken): Req = 
    addRequiredPayPalHeaders(t).GET
}

import scala.concurrent.{ExecutionContext,Await,ExecutionException}
import dispatch.EnrichedFuture

class BoxEnrichedFuture[A](underlying: Future[A]) {

  /** Project promised value into a Box containing the value or Failure with any
    * exception thrown retrieving it. Unwraps `cause` of any top-level
    * ExecutionException */
  def box: Future[Box[A]] = {
    implicit val ctx = BoxEnrichedFuture.currentThreadContext
    underlying.map { res => Full(res) }.recover {
      case exc: ExecutionException => Failure(exc.getMessage, Full(exc.getCause), Empty)
      case throwable => Failure(throwable.getMessage, Full(throwable), Empty)
    }
  }
}
// see https://github.com/dispatch/reboot/blob/master/core/src/main/scala/enrich.scala
object BoxEnrichedFuture {
  /** Execute on the current thread, for certain cpu-bound operations */
  private val currentThreadContext = new ExecutionContext {
    def execute(runnable: Runnable) {
      runnable.run()
    }
    def reportFailure(t: Throwable) {
      ExecutionContext.defaultReporter(t)
    }
  }
}
