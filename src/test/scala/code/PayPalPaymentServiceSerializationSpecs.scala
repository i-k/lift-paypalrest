package liftpaypal

import msgs._

import lib._

import java.util.Calendar
import java.text.ParseException

import net.liftweb.util._
import net.liftweb.common._
import net.liftweb.util.{CurrencyZone, US, EU}
import Box.option2Box
import net.liftweb.json._

import org.specs2.mutable.Specification
import org.specs2.specification.AroundExample
import org.specs2.execute.Result
import org.specs2.time.NoTimeConversions

trait RandomPayPal {
  
  def emptyToken = PayPalToken("", "", "", "", BigInt(0))
  
  def localRedirectUrls = RedirectUrls("http://localhost", "http://localhost/cancel")
}

object PayPalPaymentServiceSerializationSpecs extends Specification with RandomPayPal {
  
  "PayPalPaymentService json serialization" should {
    val p = new PayPalPaymentService {}
    import lib.PayPalImplicits._
    def postBodyAsString[MsgType <: AnyRef](msg: MsgType) =
      p.paymentEndpoint.payPalPOST(emptyToken, msg).toRequest.getStringData()
      
    "write authorize-json properly into body" in {
      val msg = Outgoing.Authorize(
        localRedirectUrls,
        Outgoing.PayPalPayer,
        List(Transaction(Amount.fromStrings("7.47", "USD", Some("7.41", Some("0.03"), Some("0.03"))), None))
      )
      
      postBodyAsString(msg) must_== JsonTestData.Outgoing.authorize
    }
    
    "write create payment -json properly into body" in {
      val msg = Outgoing.CreatePayment(
        localRedirectUrls,
        Outgoing.PayPalPayer,
        List(Transaction(Amount.fromStrings("7.47", "USD", None), Some("This is the payment transaction description.")))
      )
      
      postBodyAsString(msg) must_== JsonTestData.Outgoing.createSale
    }

    "write Euros into the format supported by PayPal (format used with USD's)" in {
      val msg = Outgoing.CreatePayment(
        localRedirectUrls,
        Outgoing.PayPalPayer,
        List(Transaction(Amount.fromStrings("5,000.05", "EUR", None), None))
      )
      
      postBodyAsString(msg) must_== JsonTestData.Outgoing.createSaleEur
    }
    
  }
  
  "PayPalPaymentService json deserialization" should {
    implicit val formats = msgs.Serialization.formats()
    
    "parse dates in ISO8601-format properly" in {
      formats.dateFormat.parse("2014-01-14T22:38:19Z") must throwA[ParseException].not
    }
    
    "parse payment resources" in {
      val json = parse(JsonTestData.Incoming.creditCardPaymentResource)
      
      json.extract[Incoming.PaymentResource] must beLike[Any] {
        case ccr: Incoming.CreditCardPaymentResource =>
          ccr.payer.funding_instruments.head must beLike[Any] {
            case FundingInstrument(card) =>
              card.first_name must_== Some("Betsy")
              card.last_name must_== Some("Buyer")
          }
      }
    }
    
    "parse different responses from PayPal" in {
      val capture = parse(JsonTestData.Incoming.captureDetails)
      capture.extract[Incoming.AuthorizationCaptured] must throwA[ParseException].not
      val authorization = parse(JsonTestData.Incoming.authorizationDetails)
      authorization.extract[Incoming.Authorization] must throwA[ParseException].not
      
    }
  }
}
