package liftpaypal

import msgs._

import lib._

import java.util.Calendar
import java.util.concurrent.TimeoutException

import net.liftweb.util._
import Helpers.{tryo, randomString}
import net.liftweb.common._
import net.liftweb.util.{CurrencyZone, US, EU}
import Box.option2Box
import net.liftweb.json._

import scala.concurrent.{Await, Awaitable}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import org.specs2.mutable.Specification
import org.specs2.specification.AroundExample
import org.specs2.execute.{Result, Skipped}
import org.specs2.time.NoTimeConversions

object TokenizedPayPalPaymentServiceTestSpecs extends Specification with NoTimeConversions with RandomPayPal {
  
  getPropOrSkipAll("paypal.client.id")
  getPropOrSkipAll("paypal.client.secret")
  
  def await[T](f: Awaitable[T]) = Await.result[T](f, 5 seconds)

  "PayPalTokenService" should {
    val p = new PayPalTokenService {}
    
    "fetch a token" in {
      val f = p.fetchToken
      await(f) must beLike[Any] {
        case Full(t: PayPalToken) => ok
      }
    }
  }
  
  "PayPalPaymentService" should {
    
    val p = new TokenizedPayPalPaymentService {
      token = tryo(
        await((new PayPalTokenService {}).fetchToken)
      ).flatMap(t => t)
    }
    
    "authorize a payment" in {
      // 127 is upper limit for paypal description
      val descr = Some(randomString(127))
      descr.get.size must_== 127
      val f = p.authorizePayment(
        localRedirectUrls.return_url,
        localRedirectUrls.cancel_url,
        Outgoing.PayPalPayer,
        Transaction(Amount.fromStrings("12.14999999", "USD", Some("10.00", Some("1.10"), Some("1.05"))), descr)
      )
      await(f) must beLike[Any] {
        case Full(a: Incoming.PayPalAuthorizationCreated) =>
          a.state must_== "created"
          a.payer.payment_method must_== "paypal"
          a.links.find(_.rel == "approval_url") must not beEmpty
          val t = a.transactions.head
          t.description must_== descr
          val firstAuth = t.related_resources.head.authorization
          firstAuth.valid_until.getTime must beGreaterThan(Calendar.getInstance.getTimeInMillis)
          
        case Failure(msg, Full(t), _) =>
          ko
      }
    }

    "create a payment" in {
      val amount = "1,555.01"
      val f = p.createPayPalPayment(
        localRedirectUrls.return_url,
        localRedirectUrls.cancel_url,
        Transaction(Amount.fromStrings(amount, "USD", None), None)
      )
      await(f) must beLike[Any] {
        case Full(c: Incoming.PayPalPaymentCreated) =>
          c.payer.payment_method must_== "paypal"
      }
    }
  }
  
  def getPropOrSkipAll(name: String) = skipAllIf(
    Props.get(name).filterNot(_.isEmpty).isEmpty
  )
}
