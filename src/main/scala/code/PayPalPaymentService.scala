package liftpaypal

import msgs._
import lib.PayPalReq
import lib.PayPalImplicits.{reqToPayPalReq, futureToBoxEnrichedFuture}

import dispatch._, Defaults._
import net.liftweb.common._
import net.liftweb.util.CurrencyZone

// transaction instead of transactions because PayPal supports only single payment transactions right now
trait PayPalPaymentService extends PayPalBaseService {
  import lib.PayPalImplicits.formats

  lazy val endpoint = payPalBase / "payments"
  lazy val paymentEndpoint = endpoint / "payment"
  // for manipulating existing authorizations
  lazy val authorizationEndpoint = endpoint / "authorization"
  // for looking up and refunding captures
  lazy val captureEndpoint = endpoint / "capture" 
  
  // calls POST with param @body turned into json and returns a future of box of json
  def payPalHttpPOST[FromT <: Incoming.PayPalMsg](
    endpoint: PayPalReq,
    token: PayPalToken,
    body: AnyRef
  )(implicit mf: Manifest[FromT]): Future[Box[FromT]] =
    http(endpoint.payPalPOST(token, body) > (r => fromJson[FromT](r)(formats, mf))).box
    
  def payPalHttpPOST[FromT <: Incoming.PayPalMsg](
    endpoint: PayPalReq,
    token: PayPalToken
  )(implicit mf: Manifest[FromT]): Future[Box[FromT]] =
    http(endpoint.payPalPOST(token) > (r => fromJson[FromT](r)(formats, mf))).box
    
  def payPalHttpGET[FromT <: Incoming.PayPalMsg](
    endpoint: PayPalReq,
    token: PayPalToken
  )(implicit mf: Manifest[FromT]): Future[Box[FromT]] =
    http(endpoint.payPalGET(token) > (r => fromJson[FromT](r)(formats, mf))).box
  
  def authorizePayment(
    token: PayPalToken,
    returnUrl: String, 
    cancelUrl: String,
    payer: Payer, transaction: Transaction
  ): Future[Box[Incoming.PayPalAuthorizationCreated]] =
    payPalHttpPOST[Incoming.PayPalAuthorizationCreated](
      paymentEndpoint,
      token,
      Outgoing.Authorize(RedirectUrls(returnUrl, cancelUrl), payer, List(transaction))
    )
      
  def createPayPalPayment(
    token: PayPalToken,
    returnUrl: String,
    cancelUrl: String,
    transaction: Transaction
  ): Future[Box[Incoming.PayPalPaymentCreated]] =
    payPalHttpPOST[Incoming.PayPalPaymentCreated](
      paymentEndpoint,
      token,
      Outgoing.CreatePayment(
        RedirectUrls(returnUrl, cancelUrl),
        Outgoing.PayPalPayer,
        List(transaction)
      )
    )
  
  def executePayPalPaymentAuthorization(token: PayPalToken, id: String, payerId: String): Future[Box[Incoming.PayPalAuthorizationExecuted]] =
    payPalHttpPOST[Incoming.PayPalAuthorizationExecuted](
      paymentEndpoint / id / "execute",
      token,
      Outgoing.PayerId(payerId)
    )
    
  //payments/payment/{id}
  def fetchPayment(token: PayPalToken, id: String): Future[Box[Incoming.PaymentResource]] =
    payPalHttpGET[Incoming.PaymentResource](paymentEndpoint / id, token)
    
  //payments/capture/{id}
  def fetchCapture(token: PayPalToken, id: String): Future[Box[Incoming.AuthorizationCaptured]] =
    payPalHttpGET[Incoming.AuthorizationCaptured](captureEndpoint / id, token)
    
  // payments/authorization/{id}
  def fetchAuthorization(token: PayPalToken, id: String): Future[Box[Incoming.Authorization]] =
    payPalHttpGET[Incoming.Authorization](authorizationEndpoint / id, token)

  //payments/authorization/{id}/capture
  // id is NOT a payment id (doesn't start with "PAY-")
  // you get the id from the response of the executed authorization: transactions[0].related_resources[0].id
  def captureAuthorization(
    token: PayPalToken,
    id: String,
    total: CurrencyZone#Currency,
    isFinalCapture: Boolean
  ): Future[Box[Incoming.AuthorizationCaptured]] =
    payPalHttpPOST[Incoming.AuthorizationCaptured](
      authorizationEndpoint / id / "capture",
      token,
      Outgoing.Capture(Amount.fromTotal(total), isFinalCapture)
    )
  //payments/authorization/{id}/void
  def voidAuthorization(
    token: PayPalToken,
    id: String
  ): Future[Box[Incoming.Authorization]] = 
    payPalHttpPOST(authorizationEndpoint / id / "void", token)
  
  //payments/capture/8F148933LY9388354/refund
    /*Transaction(
      Amount.fromCurrency(total)
      description
    )*/
}
