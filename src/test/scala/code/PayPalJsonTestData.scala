package liftpaypal

// https://devtools-paypal.com/guide/authorize_capture_lookup_refund

object JsonTestData {
  // "".replace(" ", "").replace("\n", "")
  object Outgoing {
    def authorize = """{"intent":"authorize","redirect_urls":{"return_url":"http://localhost","cancel_url":"http://localhost/cancel"},"payer":{"payment_method":"paypal"},"transactions":[{"amount":{"total":"7.47","currency":"USD","details":{"subtotal":"7.41","tax":"0.03","shipping":"0.03"}}}]}"""
    def createSale = """{"intent":"sale","redirect_urls":{"return_url":"http://localhost","cancel_url":"http://localhost/cancel"},"payer":{"payment_method":"paypal"},"transactions":[{"amount":{"total":"7.47","currency":"USD"},"description":"This is the payment transaction description."}]}"""
    def createSaleEur = """{"intent":"sale","redirect_urls":{"return_url":"http://localhost","cancel_url":"http://localhost/cancel"},"payer":{"payment_method":"paypal"},"transactions":[{"amount":{"total":"5,000.05","currency":"EUR"}}]}"""
  }
  
  object Incoming {
    def creditCardPaymentResource = """{
  "id": "PAY-5YK922393D847794YKER7MUI",
  "create_time": "2013-02-19T22:01:53Z",
  "update_time": "2013-02-19T22:01:55Z",
  "state": "approved",
  "intent": "sale",
  "payer": {
    "payment_method": "credit_card",
    "funding_instruments": [
      {
        "credit_card": {
          "type": "mastercard",
          "number": "xxxxxxxxxxxx5559",
          "expire_month": "2",
          "expire_year": "2018",
          "first_name": "Betsy",
          "last_name": "Buyer"
        }
      }
    ]
  },
  "transactions": [
    {
      "amount": {
        "total": "7.47",
        "currency": "USD",
        "details": {
          "subtotal": "7.47"
        }
      },
      "description": "This is the payment transaction description.",
      "related_resources": [
        {
          "sale": {
            "id": "36C38912MN9658832",
            "create_time": "2013-02-19T22:01:53Z",
            "update_time": "2013-02-19T22:01:55Z",
            "state": "completed",
            "amount": {
              "total": "7.47",
              "currency": "USD"
            },
            "parent_payment": "PAY-5YK922393D847794YKER7MUI",
            "links": [
              {
                "href": "https://api.sandbox.paypal.com/v1/payments/sale/36C38912MN9658832",
                "rel": "self",
                "method": "GET"
              },
              {
                "href": "https://api.sandbox.paypal.com/v1/payments/sale/36C38912MN9658832/refund",
                "rel": "refund",
                "method": "POST"
              },
              {
                "href": "https://api.sandbox.paypal.com/v1/payments/payment/PAY-5YK922393D847794YKER7MUI",
                "rel": "parent_payment",
                "method": "GET"
              }
            ]
          }
        }
      ]
    }
  ],
  "links": [{"href": "https://api.sandbox.paypal.com/v1/payments/payment/PAY-5YK922393D847794YKER7MUI","rel": "self","method": "GET"}]}"""
  
    def captureDetails = """{"id":"3BP99512TE161372D","create_time":"2014-06-06T09:22:41Z","update_time":"2014-06-06T09:22:43Z","amount":{"total":"2.00","currency":"USD"},"state":"completed","parent_payment":"PAY-4HS42401MJ7933646KOIYKBI","links":[{"href":"https://api.sandbox.paypal.com/v1/payments/capture/3BP99512TE161372D","rel":"self","method":"GET"},{"href":"https://api.sandbox.paypal.com/v1/payments/capture/3BP99512TE161372D/refund","rel":"refund","method":"POST"},{"href":"https://api.sandbox.paypal.com/v1/payments/payment/PAY-4HS42401MJ7933646KOIYKBI","rel":"parent_payment","method":"GET"}]}"""
  
    def authorizationDetails = """{"id":"8N977268U0664193L","create_time":"2014-06-06T09:08:21Z","update_time":"2014-06-06T09:08:24Z","state":"authorized","amount":{"total":"2.00","currency":"USD","details":{"subtotal":"2.00"}},"parent_payment":"PAY-4HS42401MJ7933646KOIYKBI","valid_until":"2014-07-05T09:08:21Z","links":[{"href":"https://api.sandbox.paypal.com/v1/payments/authorization/8N977268U0664193L","rel":"self","method":"GET"},{"href":"https://api.sandbox.paypal.com/v1/payments/authorization/8N977268U0664193L/capture","rel":"capture","method":"POST"},{"href":"https://api.sandbox.paypal.com/v1/payments/authorization/8N977268U0664193L/void","rel":"void","method":"POST"},{"href":"https://api.sandbox.paypal.com/v1/payments/payment/PAY-4HS42401MJ7933646KOIYKBI","rel":"parent_payment","method":"GET"}]}"""
  }
}
