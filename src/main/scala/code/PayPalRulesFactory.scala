package liftpaypal

import dispatch._, Defaults._
import net.liftweb.http.Factory
import net.liftweb.util.Props

object PayPalRulesFactory extends Factory {

  val payPalBase = new FactoryMaker[() => Req](() => createPayPalBase(
    Props.get("paypal.endpoint") openOrThrowException("The base endpoint URL for PayPal must be provided!")
  )){}
  
  val payPalCallbackUrl = new FactoryMaker[String](
    Props.get("paypal.callbackurl") openOrThrowException("A callback URL for PayPal must be provided!")
  ){}
  
  def createPayPalBase(path: String): Req = host(path).secure / "v1"
}
