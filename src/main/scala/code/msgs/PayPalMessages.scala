package liftpaypal
package msgs

import java.util.Date

import net.liftweb.json._
import net.liftweb.common.Loggable
import net.liftweb.util.{CurrencyZone, US, EU}

// add CreditCardAuthorizationCreated

object Outgoing {
  case object PayPalPayer extends Payer {
    val payment_method: String = "paypal"
  }
  trait PayPalMsg {
    def intent: String
    def payer: Payer
    def transactions: List[Transaction]
  }
  case class Authorize(
    redirect_urls: RedirectUrls,
    payer: Payer,
    transactions: List[Transaction]) extends PayPalMsg {
    val intent: String = "authorize"
  }

  case class CreatePayment(
    redirect_urls: RedirectUrls,
    payer: Payer,
    transactions: List[Transaction]) extends PayPalMsg {
    val intent: String = "sale"
  }
  
  case class Capture(amount: Amount, is_final_capture: Boolean)
  
  case class PayerId(payer_id: String) 
}

object Incoming {
  trait PayPalMsg {
    def id: String
    def create_time: Date
    def update_time: Date
  }

  case class PayPalAuthorizationCreated(
    id: String,
    create_time: Date,
    update_time: Date,
    state: String,
    intent: String,
    payer: PayPalPayer,
    transactions: List[AuthorizationTransaction],
    links: List[Link]
  ) extends PayPalMsg
  
  case class CreditCardAuthorizationCreated(
    id: String,
    create_time: Date,
    update_time: Date,
    state: String,
    intent: String,
    payer: CreditCardPayer,
    transactions: List[AuthorizationTransaction],
    links: List[Link]
  ) extends PayPalMsg
  
  case class PayPalAuthorizationExecuted(
    id: String,
    create_time: Date,
    update_time: Date,
    state: String,
    intent: String,
    payer: PayPalPayer,
    transactions: List[AuthorizationExecutedTransaction],
    links: List[Link]
  ) extends PayPalMsg
    
  // same as returned from GET https://api.paypal.com/v1/payments/capture/{capture_id}
  case class AuthorizationCaptured(
    id: String,
    create_time: Date,
    update_time: Date,
    amount: Amount,
    is_final_capture: Boolean,
    state: String,
    parent_payment: String,
    links: List[Link]
  ) extends PayPalMsg
  // same as responses from /reauthorize and /void 
  case class Authorization(
    id: String,
    create_time: Date,
    update_time: Date,
    state: String,
    amount: Amount,
    parent_payment: String,
    valid_until: Date,
    links: List[Link]
  ) extends PayPalMsg
    
  case class AuthorizationTransaction(
    amount: Amount,
    description: Option[String],
    related_resources: List[AuthorizationResource]
  ) extends BaseTransaction
  
  case class AuthorizationResource(authorization: AuthorizationResourceDetails)
  case class AuthorizationResourceDetails(
    create_time: Date,
    update_time: Date,
    parent_payment: String,
    valid_until: Date,
    links: List[Link]
  )
  
   case class AuthorizationExecutedTransaction(
    amount: Amount,
    description: Option[String],
    related_resources: List[AuthorizationExecutedResource]
  ) extends BaseTransaction
  case class AuthorizationExecutedResource(authorization: AuthorizationExecutedResourceDetails)
  case class AuthorizationExecutedResourceDetails(
    id: String, // <- not in AuthorizationResourceDetails
    create_time: Date,
    update_time: Date,
    amount: Amount, // <- not in AuthorizationResourceDetails
    parent_payment: String,
    valid_until: Date,
    links: List[Link]
  )
  
  
  case class Link(href: String, rel: String, method: String)
  
  // same as PayPalAuthorizationCreated
  case class PayPalPaymentCreated(
    id: String,
    create_time: Date,
    update_time: Date,
    state: String,
    intent: String,
    payer: PayPalPayer,
    transactions: List[Transaction],
    links: List[Link]) extends PayPalMsg

  case class CreditCardPaymentCreated(
    id: String,
    create_time: Date,
    update_time: Date,
    state: String,
    intent: String,
    payer: CreditCardPayer,
    transactions: List[Transaction],
    links: List[Link]) extends PayPalMsg
  
  //payer_info actually seems to be either PayerInfo or PayerInfo with only shipping_address (the other values being optional)
  case class PayPalPayer(payer_info: Option[PayerInfo]) extends Payer {
    val payment_method: String = "paypal"
  }
  case class PayerInfo(email: Option[String], first_name: Option[String], last_name: Option[String], payer_id: String, phone: Option[String], shipping_address: Option[ShippingAddress])
  case class ShippingAddress(recipient_name: String, `type`: String, line1: String, line2: Option[String], city: String, country_code: String, postal_code: String, state: String, phone: String)
  case class CreditCardPayer(funding_instruments: List[FundingInstrument]) extends Payer {
    val payment_method: String = "credit_card"
  }
  
  sealed trait PaymentResource extends PayPalMsg {}
  
  case class PayPalPaymentResource(
    id: String,
    create_time: Date,
    update_time: Date,
    state: String,
    intent: String,
    payer: PayPalPayer,
    transactions: List[SaleTransaction],
    redirect_urls: RedirectUrls,
    links: List[Link]
  ) extends PaymentResource
  
  case class CreditCardPaymentResource(
    id: String,
    create_time: Date,
    update_time: Date,
    state: String,
    intent: String,
    payer: CreditCardPayer,
    transactions: List[SaleTransaction],
    links: List[Link]
  ) extends PaymentResource
  
  case class SaleTransaction(
    amount: Amount,
    description: Option[String],
    related_resources: List[SaleResource]
  ) extends BaseTransaction
  
  case class SaleResource(sale: SaleResourceDetails)
  case class SaleResourceDetails(
    id: String,
    create_time: Date,
    update_time: Date,
    state: String,
    amount: Amount,
    parent_payment: String,
    valid_until: Option[Date],
    links: List[Link]
  )
}

sealed trait Payer {
  def payment_method: String
}

trait BaseTransaction {
  def amount: Amount
  def description: Option[String]
}
case class Transaction(amount: Amount, description: Option[String])
  extends BaseTransaction

