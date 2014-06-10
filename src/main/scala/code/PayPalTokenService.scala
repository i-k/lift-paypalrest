package liftpaypal

import msgs.PayPalToken
import lib.PayPalImplicits.futureToBoxEnrichedFuture

import dispatch._, Defaults._
import net.liftweb.json._
import net.liftweb.util.Props
import net.liftweb.common._

/*
 * Fetches auth tokens.
 * Requires Props paypal.client.id and paypal.client.secret 
 */
trait PayPalTokenService extends PayPalBaseService {
  import lib.PayPalImplicits.formats
  
  lazy val clientId = Props.get("paypal.client.id") openOrThrowException("PayPal client_id must be provided!")
  lazy val clientSecret = Props.get("paypal.client.secret") openOrThrowException("PayPal secret must be provided!")
  
  lazy val tokenEndpoint = payPalBase / "oauth2" / "token"

  def fetchToken: Future[Box[PayPalToken]] =
    fetchToken(tokenEndpoint, clientId, clientSecret)
  
  //content-type for an access token request
  lazy val requiredParams = Map("grant_type" -> "client_credentials")
  
  //makes a POST with payPalParams into @param endpoint and tries to extract a PayPalToken out of it.
  def fetchToken(endpoint: Req, clientId: String, clientSecret: String): Future[Box[PayPalToken]] =
    http(
      (endpoint << requiredParams).
        as_!(clientId, clientSecret) // AuthScheme.BASIC
      OK(fromJson[PayPalToken])
    ).box
}
