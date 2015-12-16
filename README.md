Basic Java [Stockfighter](https://www.stockfighter.io/) client library.

# About

"StockFighter is a free Capture The Flag (CTF) programming challenge by Erin, 
Patrick, and Thomas at [Starfighter](http://www.starfighters.io/)."

(I've got nothing to do with those guys, I just play the game).

This is a (currently, very basic) Java wrapper around the [REST API](https://starfighter.readme.io/v1.0/docs).


# Example use

## Simple

The api has a bunch of GET/POST/DELETE commands to get the status of the API,
stock prices, and to trade stocks. Each new level gives you a venue code, an
account and a symbol. The `Trader` class wraps these.

    String api_key = .... // Register to get key from [Stockfighter](https://www.stockfighter.io/)
    Trader t = new Trader(api_key);

    // Check everything is working
    if (!t.heartbeat()) {
        log.error("API down, giving up");
        return;
    }

    // Buy a share
    Order o = new Order();
    o.setVenue("TESTEX");       // TEXTEX is the test exchange
    o.setAccount("EXB123456");  // ... and the test account
    o.setSymbol("FOOBAR");      // ... and the test symbol (stock)
    o.setPrice(2000); 
    o.setBuy(true);             // (or false to sell)
    o.setOrderType(OrderType.Limit);
    o.setQuantity(1);

    OrderStatus status = t.postOrder(o);

## Websockets

The api also has a couple of websockets for streaming information. One for
quotes and one for your executions.

     String api_key = .... // Register to get key from [Stockfighter](https://www.stockfighter.io/)

     QuoteTicker qt = new QuoteTicker(api_key);
     qt.addListener(new QuoteTicker.Listener() {
        void onQuote(Quote q) {
            log.debug("Bid {} ask {}", q.getBid(), q.getAsk());
        }
     });

     qt.connect("EXB123456", "TESTEX");

     // Wait forever.
     new Object().wait();
     

# Licence

The code is released under the [MIT licence](LICENCE.txt). I'd like to know
if you use it, but that's just to feed my ego.


