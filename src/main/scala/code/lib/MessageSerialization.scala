package liftpaypal
package msgs

import java.util.Date

import net.liftweb.json._
import net.liftweb.common.Loggable
import net.liftweb.util.{CurrencyZone, US, EU}

object Serialization {
  
  def formats(hints: TypeHints): Formats = 
    fieldSerializers.foldLeft(
      iso8601DateFormattingDefaultFormats(hints)
    )(_ + _) ++ customSerializers
  
  def formats(): Formats = formats(NoTypeHints)
  
  def iso8601DateFormattingDefaultFormats(hints: TypeHints): Formats = new DefaultFormats {
    override val typeHints = hints
  }

  def fieldSerializers = List(
    FieldSerializer[Payer](),
    FieldSerializer[Outgoing.PayPalMsg](),
    FieldSerializer[Incoming.PayPalMsg]()
  )
  
  def customSerializers = List(
    new MonetarySerializer(),
    new PaymentResourceSerializer()
  )
  
  
  class MonetarySerializer extends CustomSerializer[Amount](format => (
    { 
      case JObject(List(
        JField("total", JString(total)),
        JField("currency", JString(cur))
      )) => Amount.fromStrings(total, cur, None)
      case JObject(List(
        JField("total", JString(total)),
        JField("currency", JString(cur)),
        JField("details", JObject(details))
      )) => details match {
        case List(JField("subtotal", JString(subtotal))) =>
          Amount.fromStrings(total, cur, Some(subtotal, None, None))
        case List(
          JField("subtotal", JString(subtotal)),
          JField("tax", JString(tax))
        ) => Amount.fromStrings(total, cur, Some(subtotal, Some(tax), None))
        case List(
          JField("subtotal", JString(subtotal)),
          JField("shipping", JString(s))
        ) => Amount.fromStrings(total, cur, Some(subtotal, None, Some(s)))
        case List(
          JField("subtotal", JString(subtotal)),
          JField("tax", JString(tax)),
          JField("shipping", JString(shipping))
        ) => Amount.fromStrings(total, cur, Some(subtotal, Some(tax), Some(shipping)))
      }
    }, {
      case Amount(total, currency, details) => JObject(
        List(
          JField("total", asUSDFormattedJString(total)),
          JField("currency", JString(currency))
        ) ::: details.map(d => JField("details", JObject(
          JField("subtotal", asUSDFormattedJString(d.subtotal)) ::
          d.tax.map(t => JField("tax", asUSDFormattedJString(t))).toList :::
          d.shipping.map(s => JField("shipping", asUSDFormattedJString(s))).toList
        ))).toList
      )
    }
  ))
  
  class PaymentResourceSerializer extends CustomSerializer[Incoming.PaymentResource](format => (
    {
      case json =>
        implicit val formats = format
        json \ "payer" \ "payment_method" match {
          case JString("paypal") => json.extract[Incoming.PayPalPaymentResource]
          case _ => json.extract[Incoming.CreditCardPaymentResource]
        }
    },
    Map()
  ))
  
  def formatAsUSD(c: CurrencyZone#Currency) =
    US(c.doubleValue).toString.trim // trim needed due to minor bug in US.toString
  
  // formats the given Currency in US-CurrencyZone's format, e.g. 5,005.05 so PayPal will accept it.
  def asUSDFormattedJString(c: CurrencyZone#Currency) =
    JString(formatAsUSD(c))
}
