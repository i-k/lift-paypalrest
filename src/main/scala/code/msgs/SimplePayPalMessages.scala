package liftpaypal
package msgs

import java.util.Date

case class PayPalToken(
  scope: String,
  access_token: String,
  token_type: String,
  app_id: String,
  expires_in: BigInt
)

case class PayPalError(
  name: String,
  message: String,
  information_link: String,
  details: String // Option[String]
)

case class ShippingAddress(recipient_name: String, `type`: String, line1: String, line2: Option[String], city: String, country_code: String, postal_code: String, state: String, phone: String)

case class FundingInstrument(credit_card: CreditCard)

case class CreditCard(`type`: String, number: String, expire_month: String,
  expire_year: String, cvv2: Option[String], first_name: Option[String], last_name: Option[String],
  billing_address: Option[BillingAddress])

case class BillingAddress(line1: String, city: String, country_code: String,
  postal_code: String, state: String)

case class RedirectUrls(return_url: String, cancel_url: String)

import net.liftweb.util._
//currency (symbol): 3-character ISO 4217
case class Amount(total: CurrencyZone#Currency, currency: String, details: Option[AmountDetails])

case class AmountDetails(
  subtotal: CurrencyZone#Currency,
  tax: Option[CurrencyZone#Currency],
  shipping: Option[CurrencyZone#Currency]
)

object Amount {
  /* Converts @param total and numbers from @param details into Currency derived from @param symbol.
   * The number-strings must be in US-CurrencyZone's format (5,005.05)
  */
  def fromStrings(
    total: String,
    symbol: String,
    details: Option[(String, Option[String], Option[String])]
  ): Amount = {
    val USToWantedZone: Function1[String, CurrencyZone#Currency] = {
      val zone = vendCurrencyZone(symbol)
      if(zone == US) US(_)
      else (s: String) => zone(US(s).doubleValue)
    }
    
    Amount(USToWantedZone(total), symbol, details.map(d =>
      AmountDetails(USToWantedZone(d._1), d._2.map(USToWantedZone), d._3.map(USToWantedZone))
    ))
  }
  
  def fromTotal(total: CurrencyZone#Currency) =
    Amount(total, total.designation, None)
    
  def vendCurrencyZone(symbol: String): CurrencyZone = symbol match {
    case "USD" => US
    case _ => EU
  }
}
