
function ApplicationModel(stompClient) {
  var self = this;

  self.username = ko.observable();
  self.portfolio = ko.observable(new PortfolioModel());
  self.trade = ko.observable(new TradeModel(stompClient));
  self.notifications = ko.observableArray();

  self.connect = function() {
    stompClient.connect('', '', function(frame) {

      console.log('Connected ' + frame);
      self.username(frame.headers['user-name']);

      stompClient.subscribe("/app/positions", function(message) {
        self.portfolio().loadPositions(JSON.parse(message.body));
      });
      stompClient.subscribe("/topic/price.stock.*", function(message) {
        self.portfolio().processQuote(JSON.parse(message.body));
      });
      stompClient.subscribe("/user/queue/position-updates", function(message) {
    	//portfolio = {"company":"Dell Inc.","ticker":"DELL","shares":10,"capital":94.3,"updateTime":1390801786844,"price":9.25}
        var portfolio = JSON.parse(message.body);
    	self.pushNotification(
    			"交易成功: 您现在持有'" + portfolio.company + "'的股票 " + portfolio.shares + " 份." +
    			"持仓均价: $" + (portfolio.capital/portfolio.shares).toFixed(2) + "."
    		);
        self.portfolio().updatePosition(portfolio);
      });
      stompClient.subscribe("/user/queue/funds-updates", function(message) {
          self.portfolio().updateFunds(JSON.parse(message.body));
      });
      stompClient.subscribe("/user/queue/errors", function(message) {
        self.pushNotification("Error " + message.body);
      });
    }, function(error) {
      console.log("STOMP protocol error " + error);
    });
  }

  self.pushNotification = function(text) {
    self.notifications.push({notification: text});
    if (self.notifications().length > 5) {
      self.notifications.shift();
    }
  }

  self.logout = function() {
    stompClient.disconnect();
    window.location.href = "../logout.html";
  }
}

function PortfolioModel() {
  var self = this;

  self.rows = ko.observableArray();
  self.funds = ko.observable(0);
  self.fundsValue = ko.computed(function() {
	    return "$" + self.funds().toFixed(2);
	  });

  self.totalShares = ko.computed(function() {
    var result = 0;
    for ( var i = 0; i < self.rows().length; i++) {
      result += self.rows()[i].shares();
    }
    return result;
  });

  self.totalValue = ko.computed(function() {
    var result = 0;
    for ( var i = 0; i < self.rows().length; i++) {
      result += self.rows()[i].value();
    }
    return result;
  });
  
  self.formattedTotalValue = ko.computed(function() {
	var result = 0;  
    for ( var i = 0; i < self.rows().length; i++) {
	  result += self.rows()[i].value();
	}
	return "$" + result.toFixed(2);
  });
  
  self.assets = ko.computed(function() {
	return self.totalValue() + self.funds();
  });
  
  self.formattedAssets = ko.computed(function() {
	return "$" + self.assets().toFixed(2);
  });
  
  self.totalProfit = ko.computed(function() {
	var result = 0;
	for ( var i = 0; i < self.rows().length; i++) {
	  result += self.rows()[i].profit() * 1;
	}
	return result;
  });
  self.formattedTotalProfit = ko.computed(function() {
	return "$" + self.totalProfit().toFixed(2);
  });
  
  var rowLookup = {};

  self.loadPositions = function(position) {
	positions = position.positions
    for ( var i = 0; i < position.positions.length; i++) {
      var row = new PortfolioRow(positions[i]);
      self.rows.push(row);
      rowLookup[row.ticker] = row;
    }
    self.funds(position.funds);
  };

  self.processQuote = function(quote) {
    if (rowLookup.hasOwnProperty(quote.ticker)) {
      rowLookup[quote.ticker].updatePrice(quote.price);
    }
  };

  self.updatePosition = function(position) {
	var pos = rowLookup[position.ticker];
	pos.shares(position.shares);
	pos.capital(position.capital);
	pos.average(pos.shares() == 0 ? 0 : (pos.capital() / pos.shares()).toFixed(2));
  };
  
  self.updateFunds = function(funds) {
	  self.funds(funds);
  };
};

function PortfolioRow(data) {
  var self = this;

  self.company = data.company;
  self.ticker = data.ticker;
  self.price = ko.observable(data.price);
  self.formattedPrice = ko.computed(function() { return "$" + self.price().toFixed(2); });
  self.change = ko.observable(0);
  self.arrow = ko.observable();
  self.shares = ko.observable(data.shares);
  self.value = ko.computed(function() { return (self.price() * self.shares()); });
  self.formattedValue = ko.computed(function() { return "$" + self.value().toFixed(2); });
  self.capital = ko.observable(data.capital);
  self.average = ko.observable(self.shares() == 0 ? 0 :(self.capital() / self.shares()).toFixed(2));
  self.formattedAverage = ko.computed(function() { return "$" + self.average()});
  self.profit = ko.observable((self.shares() * self.price() - self.capital()).toFixed(2));
  self.formattedProfit = ko.computed(function() { return "$" + self.profit()});

  self.updatePrice = function(newPrice) {
    var delta = (newPrice - self.price()).toFixed(2);
    self.arrow((delta < 0) ? '<i class="icon-arrow-down"></i>' : '<i class="icon-arrow-up"></i>');
    self.change((delta / self.price() * 100).toFixed(2));
    self.price(newPrice);
    self.profit((self.shares() * self.price() - self.capital()).toFixed(2));
  };
};

function TradeModel(stompClient) {
  var self = this;

  self.action = ko.observable();
  self.actionName = ko.observable();
  self.sharesToTrade = ko.observable(0);
  self.currentRow = ko.observable({});
  self.error = ko.observable('');
  self.suppressValidation = ko.observable(false);

  self.showBuy  = function(row) { self.showModal('Buy', row) }
  self.showSell = function(row) { self.showModal('Sell', row) }
  self.shortcutBuy  = function(row) { self.shortcutModal('Buy', row) }
  self.shortcutSell = function(row) { self.shortcutModal('Sell', row) }

  self.showModal = function(action, row) {
    self.action(action);
    self.actionName(action == "Buy" ? "买入" : "卖出");
    self.sharesToTrade(0);
    self.currentRow(row);
    self.error('');
    self.suppressValidation(false);
    $('#trade-dialog').modal();
  }
  self.shortcutModal = function(action, row) {
	    self.action(action);
	    self.actionName(action == "Buy" ? "买入" : "卖出");
	    self.sharesToTrade(10);
	    self.currentRow(row);
	    self.error('');
	    self.suppressValidation(false);
	    self.executeTrade();
  }

  $('#trade-dialog').on('shown', function () {
    var input = $('#trade-dialog input');
    input.focus();
    input.select();
  })
  
  var validateShares = function() {
      if (isNaN(self.sharesToTrade()) || (self.sharesToTrade() < 1)) {
        self.error('无效的份额');
        return false;
      }
      if ((self.action() === 'Sell') && (self.sharesToTrade() > self.currentRow().shares())) {
        self.error('没有足够的股票');
        return false;
      }
      return true;
  }

  self.executeTrade = function() {
    if (!self.suppressValidation() && !validateShares()) {
      return;
    }
    var trade = {
        "action" : self.action(),
        "ticker" : self.currentRow().ticker,
        "shares" : self.sharesToTrade()
      };
    console.log(trade);
    stompClient.send("/app/trade", {}, JSON.stringify(trade));
    $('#trade-dialog').modal('hide');
  }
}
