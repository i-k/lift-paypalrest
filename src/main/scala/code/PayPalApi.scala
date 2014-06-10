package liftpaypal

import msgs._

import scala.reflect.Manifest

import dispatch._, Defaults._
import net.liftweb.json._
import net.liftweb.common._
import com.ning.http.client.Response

trait PayPalBaseService {
  
  def http = new Http()
  
  lazy val payPalBase = PayPalRulesFactory.payPalBase.vend()
  
  import lib.PayPalImplicits.formats
  // extracts a PayPalToken from the json response in @param res
  def fromJson[T](res: Response)(implicit formats: Formats, mf: Manifest[T]): T = 
    res.getStatusCode() / 100 match {
      case 2 => as.lift.Json(res).extract[T]
      case 5 => throw PayPalStatusCode(res.getStatusCode, as.String(res)) //PayPal server error
      case 4 if(res.getStatusCode() == 400) => // validation error
        throw PayPalStatusCode(400, as.String(res))
      case _ => throw StatusCode(res.getStatusCode)
    }
}

case class PayPalStatusCode(code: Int, msg: String) extends Exception("Unexpected PayPal response %d: %s".format(code, msg))
