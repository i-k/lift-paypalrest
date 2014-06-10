package liftpaypal

import msgs._

import java.util.Calendar

import scala.concurrent.future
import dispatch._, Defaults._
import net.liftweb.common._
import net.liftweb.util.CurrencyZone

/*
 * PayPalPaymentService with a guard for auth tokens.
 * Holds the token but doesn't actually set it, for better control of when and how to set it.
 */
trait TokenizedPayPalPaymentService extends PayPalPaymentService {
  import lib.PayPalImplicits.formats
  
  private var _token: Box[PayPalToken] = None
  implicit def token: Box[PayPalToken] = _token
  var tokenSetTime: Box[Calendar] = None // set when token is set
  
  def token_=(t: Box[PayPalToken]) {
    _token = t
    tokenSetTime = Full(Calendar.getInstance)
  }
  
  def timeNow = Calendar.getInstance
  
  def isExpired(t: PayPalToken, tokenSetTime: Box[Calendar]): Boolean =
    tokenSetTime.map(tokenTime =>
      (tokenTime.getTimeInMillis + (t.expires_in * 1000L)) <= timeNow.getTimeInMillis
    ).getOrElse(true)
  
  def authorizePayment(
    returnUrl: String,
    cancelUrl: String,
    payer: Payer, 
    transaction: Transaction
  ): Future[Box[Incoming.PayPalAuthorizationCreated]] =
    withToken(authorizePayment(_, returnUrl, cancelUrl, payer, transaction))
  
  def createPayPalPayment(returnUrl: String, cancelUrl: String, transaction: Transaction): Future[Box[Incoming.PayPalPaymentCreated]] = 
    withToken(createPayPalPayment(_, returnUrl, cancelUrl, transaction))
    
  def executePayPalPaymentAuthorization(id: String, payerId: String): Future[Box[Incoming.PayPalAuthorizationExecuted]] =
    withToken(executePayPalPaymentAuthorization(_, id, payerId))
    
  def fetchPayment(id: String): Future[Box[Incoming.PaymentResource]] =
    withToken(fetchPayment(_, id))
    
  def fetchCapture(id: String): Future[Box[Incoming.AuthorizationCaptured]] =
    withToken(fetchCapture(_, id))
    
  def fetchAuthorization(id: String): Future[Box[Incoming.Authorization]] =
    withToken(fetchAuthorization(_, id))
    
  def captureAuthorization(
    id: String,
    total: CurrencyZone#Currency,
    isFinalCapture: Boolean
  ): Future[Box[Incoming.AuthorizationCaptured]] =
    withToken(captureAuthorization(_, id, total, isFinalCapture))
    
  def voidAuthorization(id: String): Future[Box[Incoming.Authorization]] = 
    withToken(voidAuthorization(_, id))
  
  def withToken[T](doThis: PayPalToken => Future[Box[T]]): Future[Box[T]] =
    token match {
      case Full(p: PayPalToken) =>
        if(isExpired(p, tokenSetTime))
          future {
            ParamFailure[String]("Token has expired", Empty, Empty, p.access_token)
          }
        else 
          doThis(p)
      case f => future { Empty }
    }
}
