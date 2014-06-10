package liftpaypal
package actor

import net.liftweb.util._
import Helpers._
import net.liftweb.common._
import net.liftweb.actor.LiftActor

import dispatch.StatusCode

trait PayPalServer extends LiftActor with TokenizedPayPalPaymentService with PayPalTokenService with Loggable {
  
  import scala.concurrent.ExecutionContext.Implicits.global
  
  protected var tokenFetchIsInProgress = false
  
  def messageHandler: PartialFunction[Any, Unit] =
    tokenFetchService
  
  def handlePayPalResponseToken[T](
    payPalResponse: Box[T],
    msgToSendSelfAfterTokenFetched: Any,
    doWithFullMsgFromResponse: Function1[T, Unit],
    doWithMsgFromOtherThanTokenFailure: Function1[String, Unit]
  ) = payPalResponse match {
    case Full(m) =>
      doWithFullMsgFromResponse(m)
    case Failure(msg, Full(StatusCode(403)), _) =>
      this ! FetchToken(msgToSendSelfAfterTokenFetched)
    case Failure(msg, exc, _) if(msg.startsWith("Token")) =>
      this ! FetchToken(msgToSendSelfAfterTokenFetched)
    case Empty =>
      this ! FetchToken(msgToSendSelfAfterTokenFetched)
    case Failure(msg, exc, _) =>
      doWithMsgFromOtherThanTokenFailure(msg)
    case _ =>
      doWithMsgFromOtherThanTokenFailure("Other failure when opening PayPal response")
  }
   // limits setting the token to one fetch at a time
  protected def tokenFetchService: PartialFunction[Any, Unit] = {
    case FetchToken(successMsg) =>
      token.filterNot(isExpired(_, tokenSetTime)).map(_ =>
        this ! successMsg
      ).getOrElse {
        if(tokenFetchIsInProgress)
          Schedule.schedule(() => this ! successMsg, 2 seconds)
        else {
          tokenFetchIsInProgress = true
          fetchToken.foreach(t => {
            t match {
              case Full(tok) =>
                token = t
                this ! successMsg
              case _ =>
                token = Empty
                Schedule.schedule(() => this ! FetchToken(successMsg), 2 seconds)
            }
            tokenFetchIsInProgress = false
          })
        }
      }
  }
  
  case class FetchToken(msgToSelfOnSuccess: Any)
}
