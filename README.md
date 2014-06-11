# Lift PayPal REST

**Lift PayPal REST** is a [Lift](http://github.com/lift) module that makes it easy to create Comet applications using [PayPal's REST API](https://developer.paypal.com/webapps/developer/docs/api/).
[Here is an example website](https://easyethiopia.com) built with it.

##Good stuff:

* More control of your checkout process with Authorizations, Payments, PayerInfos, etc.
* All monetary values are in CurrencyZone#Currency
* More control of the API call using futures. Use with Actors or directly with Await
* All PayPal messages are case classes

## Example Usage
  1. Create an Actor object that extends LiftPayPalServer.
  This Actor makes the actual API calls and deals with handling tokens, etc.
  2. Create a Comet for dealing with the purchasing logic.
  This Comet will send messages to the actor from the previous step
  and receives messages back by e.g. sending a reference to itself in the messages sent to the API server.
  E.g.
    "#buy [onclick]" #> {
      YourPayPalServer ! CreateAuthorization(callBackUrl, callBackCancelUrl, itemsInfoEtc, price, this)
      ...
    }
    ...
    override def lowPriority: PartialFunction[Any, Unit] = {
      case AuthorizationCreated(id, approvalUrl) => ...
    }
    
  3. Create an object that extends PayPalRestHelper and add your own serve. The user is redirected back from PayPal to 
  the URL that this object listens on, e.g. /paypal and the call will contain the payer id (or it is a cancellation)
  E.g.
    object YourPayPalRestServer extends PayPalRestHelper {
      serve {
        case List("paypal") Get req => withPayerId(req, (payerId: PayerId) => {
          msgComet(payerId)
          RedirectResponse("/your/url/here", req)
        })
      
        case List("paypal", "cancel") Get req =>
          msgComet(CancelPayment)
          RedirectResponse("/your/cancel/url/here", req)
      }
    
      def msgComet(msg: Any) = S.session.foreach(
        _.sendCometActorMessage("YourComet", Empty, msg)
      )
    }
    
  4. Add to boot from the previous step: LiftRules.dispatch.append(YourPayPalRestServer)

## Installing
  Download lift-paypalrest_2.10-0.0.1.jar into your project folder or [build it yourself](#building) (proper hosted builds coming soon)
  
  In build.sbt:
    unmanagedJars in Compile += file("lift-paypalrest_2.10-0.0.1.jar")
    libraryDependencies ++= {
      val liftVersion = "2.6-SNAPSHOT"
      val specsVersion = "2.4-SNAPSHOT"
      Seq(
        "net.liftweb" %% "lift-webkit" % liftVersion % "compile",
        "org.specs2" %% "specs2-core" % specsVersion % "test",
        "org.specs2" %% "specs2-junit" % specsVersion % "test",
        "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
        "net.databinder.dispatch" %% "dispatch-lift-json" % "0.11.0"
      )
    }
    
  Add your Test credential client id and secret from https://developer.paypal.com/webapps/developer/applications/myapps to
    /src/main/resources/props/default.props
    /src/test/resources/props/test.default.props
  
  Like so:
    paypal.endpoint=api.sandbox.paypal.com
    paypal.client.id=TEST_CLIENT_ID
    paypal.client.secret=TEST_SECRET
    paypal.callbackurl=http://localhost:8080/paypal
  
  Then add your Live credentials from the same page to
    /src/main/resources/props/production.default.props

  paypal.endpoint=api.paypal.com
  paypal.client.id=LIVE_CLIENT_ID
  paypal.client.secret=LIVE_SECRET
  paypal.callbackurl=https://INSERT_YOUR_DOMAIN_HERE_DONT_USE_THIS_HAHA.com/paypal
  
  If you want something else than /paypal as the suffix of your callbackurl,
  remember to match for it in your PayPalRestHelper's serve

## Building
  Requires sbt >= 0.13.5 and JRE >= 7
  
    git clone https://github.com/i-k/lift-paypal.git
    cd lift-paypal
    sbt package
    
### Adding and running tests
  Add your Test credentials (see Installing) to /src/test/resources/props/test.default.props to make calls to PayPal

## Coming soon
  * Premade case classes for the logic between Comets and the API-actor
  * More default values to PayPalRestHelper
  * Hosted builds
  * Go from this to something easier: ![How to draw an owl](http://i.imgur.com/RadSf.jpg)

## License

Open source software released under the **Apache 2.0 license**.
